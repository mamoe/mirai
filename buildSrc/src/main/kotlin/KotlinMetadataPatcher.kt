/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import shadow.relocationFilters
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

fun Project.configurePatchKotlinModuleMetadataTask(
    relocatedPublicationName: String,
    relocateDependencies: Task,
    originalPublicationName: String
) {
    // We will modify Kotlin metadata, so do generate metadata before relocation
    val generateMetadataTask =
        tasks.getByName("generateMetadataFileFor${originalPublicationName.titlecase()}Publication") as GenerateModuleMetadata

    publications.getByName(relocatedPublicationName) {
        this as MavenPublication
        this.artifact(generateMetadataTask.outputFile) {
            classifier = null
            extension = "module"
        }
    }

    generateMetadataTask.dependsOn(relocateDependencies)
    val patchMetadataTask =
        tasks.create("patchMetadataFileFor${relocatedPublicationName.capitalize()}RelocatedPublication") {
            group = "mirai"
            generateMetadataTask.finalizedBy(this)
            dependsOn(generateMetadataTask)
            dependsOn(relocateDependencies)

            // remove dependencies in Kotlin module metadata
            doLast {
                // mirai-core-jvm-2.13.0.module
                val file = generateMetadataTask.outputFile.asFile.get()
                val metadata = Gson().fromJson(
                    file.readText(),
                    JsonElement::class.java
                ).asJsonObject

                val metadataVersion = metadata["formatVersion"]?.asString
                check(metadataVersion == "1.1") {
                    "Unsupported Kotlin metadata version. version=$metadataVersion, file=${file.absolutePath}"
                }
                for (variant in metadata["variants"]!!.asJsonArray) {
                    patchKotlinMetadataVariant(variant, relocateDependencies.outputs.files.singleFile)
                }


                file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(metadata))
            }
        }

    // Set "publishKotlinMultiplatformPublicationTo*" and "publish${targetName.capitalize()}PublicationTo*" dependsOn patchMetadataTask
    if (project.kotlinMpp != null) {
        tasks.filter { it.name.startsWith("publishKotlinMultiplatformPublicationTo") }.let { publishTasks ->
            if (publishTasks.isEmpty()) {
                throw GradleException("[Shadow Relocation] Cannot find publishKotlinMultiplatformPublicationTo for project '${project.path}'.")
            }
            publishTasks.forEach { it.dependsOn(patchMetadataTask) }
        }

        tasks.filter { it.name.startsWith("publish${relocatedPublicationName.capitalize()}PublicationTo") }
            .let { publishTasks ->
                if (publishTasks.isEmpty()) {
                    throw GradleException("[Shadow Relocation] Cannot find publish${relocatedPublicationName.capitalize()}PublicationTo for project '${project.path}'.")
                }
                publishTasks.forEach { it.dependsOn(patchMetadataTask) }
            }
    }
}

private fun Project.patchKotlinMetadataVariant(variant: JsonElement, relocatedJar: File) {
    val dependencies = variant.asJsonObject["dependencies"]!!.asJsonArray
    dependencies.removeAll { dependency ->
        val dep = dependency.asJsonObject

        val groupId = dep["group"]!!.asString
        val artifactId = dep["module"]!!.asString
        relocationFilters.any { filter ->
            filter.matchesDependency(
                groupId = groupId,
                artifactId = artifactId
            )
        }.also {
            println("[Shadow Relocation] Filtering out $groupId:$artifactId from Kotlin module")
        }
    }


    /*
    "files": [
    {
      "name": "mirai-core-jvm-2.99.0-local.jar",
      "url": "mirai-core-jvm-2.99.0-local.jar",
      "size": 14742378,
      "sha512": "7ab4afc88384a58687467ba13c6aefeda20fa53fd7759dc2bc78b2d46a6285f94ba6ccae426d192e7745f773401b3cb42a853e5445dc23bdcb1b5295e78ff71c",
      "sha256": "772f593bfb85a80794693d4d9dfe2f77c222cfe9ca7e0d571abaa320e7aa82d3",
      "sha1": "cb7937269d29b574725d6f28668847fd672de7cf",
      "md5": "3fca635ba5e55b7dd56c552e4ca01f7e"
    }
  ]
     */

    val files = variant.asJsonObject["files"].asJsonArray
    val filesList = files.toList()
    files.removeAll { true }
    for (publishedFile0 in filesList) {
        val publishedFile = publishedFile0.asJsonObject

        val name = publishedFile["name"].asJsonPrimitive.asString
        if (name.endsWith(".jar")) {
            logPublishing { "Patching Kotlin Metadata: file $name" }
            for (algorithm in ALGORITHMS) {
                publishedFile.add(algorithm, JsonPrimitive(relocatedJar.digest(algorithm)))
            }
            publishedFile.add("size", JsonPrimitive(relocatedJar.length()))
        } else {
            error("Unexpected file '$name' while patching Kotlin metadata")
        }

        files.add(publishedFile)
    }
}

private val ALGORITHMS = listOf("md5", "sha1", "sha256", "sha512")

fun File.digest(algorithm: String): String {
    val arr = inputStream().buffered().use { it.digest(algorithm) }
    return arr.toUHexString("").lowercase()
}

fun InputStream.digest(algorithm: String): ByteArray {
    val digest = MessageDigest.getInstance(algorithm)
    digest.reset()
    use { input ->
        object : OutputStream() {
            override fun write(b: Int) {
                digest.update(b.toByte())
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                digest.update(b, off, len)
            }
        }.use { output ->
            input.copyTo(output)
        }
    }
    return digest.digest()
}

