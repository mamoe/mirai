/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

fun logPublishing(message: String) {
    println("[Publishing] Configuring $message")
}

fun Project.configureMppPublishing() {
    configureBintray()

    // mirai does some magic on MPP targets
    afterEvaluate {
        tasks.findByName("compileKotlinCommon")?.enabled = false
        tasks.findByName("compileTestKotlinCommon")?.enabled = false

        tasks.findByName("compileCommonMainKotlinMetadata")?.enabled = false
        tasks.findByName("compileKotlinMetadata")?.enabled = false

        tasks.findByName("generateMetadataFileForKotlinMultiplatformPublication")?.enabled = false // FIXME: 2021/1/21 
    }

    tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
        doFirst {
            publications
                .filterIsInstance<MavenPublication>()
                .forEach { publication ->
                    val moduleFile = buildDir.resolve("publications/${publication.name}/module.json")
                    if (moduleFile.exists()) {
                        publication.artifact(object :
                            org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact(moduleFile) {
                            override fun getDefaultExtension() = "module"
                        })
                    }
                }
        }
    }

    val stubJavadoc = tasks.register("javadocJar", Jar::class) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        archiveClassifier.set("javadoc")
    }

    afterEvaluate {
        publishing {
            logPublishing("Publications: ${publications.joinToString { it.name }}")

            publications.filterIsInstance<MavenPublication>().forEach { publication ->
                if (publication.name != "kotlinMultiplatform") {
                    publication.artifact(stubJavadoc)
                }

                logPublishing(publication.name)
                when (val type = publication.name) {
                    "kotlinMultiplatform" -> {
                        publication.artifactId = project.name
                        publishPlatformArtifactsInRootModule(publications.getByName("jvm") as MavenPublication)
                    }
                    "metadata" -> { // TODO: 2021/1/21 seems no use. none `type` is "metadata"
                        publication.artifactId = "${project.name}-metadata"
                    }
                    "common" -> {
                    }
                    else -> {
                        // "jvm", "native", "js"
                        publication.artifactId = "${project.name}-$type"
                    }
                }
            }
        }
    }
}

val publishPlatformArtifactsInRootModule: Project.(MavenPublication) -> Unit = { platformPublication ->
    lateinit var platformPomBuilder: XmlProvider
    platformPublication.pom.withXml { platformPomBuilder = this }

    publications.getByName("kotlinMultiplatform").let { it as MavenPublication }.run {
        this.artifacts.removeIf {
            it.classifier == null && it.extension == "jar"
            // mirai-core\build\libs\mirai-core-2.0.0.jar, classifier=null, ext=jar
        }

        logPublishing("Existing artifacts in kotlinMultiplatform: " +
                this.artifacts.joinToString("\n", prefix = "\n") { it.smartToString() }
        )

        platformPublication.artifacts.forEach {
            logPublishing("Adding artifact to kotlinMultiplatform: ${it.smartToString()}")
            artifact(it)
        }


        // replace pom
        pom.withXml {
            val pomStringBuilder = asString()
            pomStringBuilder.setLength(0)
            platformPomBuilder.toString().lines().forEach { line ->
                if (!line.contains("<!--")) { // Remove the Gradle module metadata marker as it will be added anew
                    pomStringBuilder.append(line.replace(platformPublication.artifactId, artifactId))
                    pomStringBuilder.append("\n")
                }
            }
        }
    }

    tasks.matching { it.name == "generatePomFileForKotlinMultiplatformPublication" }.configureEach {
        dependsOn(tasks["generatePomFileFor${platformPublication.name.capitalize()}Publication"])
    }
}

private fun MavenArtifact.smartToString(): String {
    return "${file.path}, classifier=${classifier}, ext=${extension}"
}
