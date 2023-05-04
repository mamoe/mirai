/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.gradle

import com.google.gson.Gson
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.util.*

//private val Project.selfAndParentProjects: Sequence<Project>
//    get() = generateSequence(this) { it.parent }
//
//private fun Project.findPropertySmart(propName: String): String? {
//    return findProperty(propName)?.toString()
//        ?: System.getProperty(propName)
//        ?: selfAndParentProjects.map { it.projectDir.resolve(propName) }.find { it.exists() }?.readText()
//        ?: System.getenv(propName)
//}
//
//private class PropertyNotFoundException(message: String) : RuntimeException(message)
//
//private fun Project.findPropertySmartOrFail(propName: String): String {
//    return findPropertySmart(propName)
//        ?: throw PropertyNotFoundException("[Mirai Console] Cannot find property for publication: '$propName'. Please check your 'mirai' configuration.")
//}

internal fun Project.configurePublishing() {
    if (!miraiExtension.publishingEnabled) return
    val isSingleTarget = kotlinJvmOrAndroidTargets.size == 1

    kotlinJvmOrAndroidTargets.forEach {
        registerPublishPluginTasks(it, isSingleTarget)
        registerMavenPublications(it, isSingleTarget)
    }
}

// effectively public
internal data class PluginMetadata(
    val metadataVersion: Int,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val description: String?,
    val dependencies: List<String>
)

internal fun String.wrapNameWithPlatform(target: KotlinTarget, isSingleTarget: Boolean): String {
    return if (isSingleTarget) this else "$this${
        target.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }"
}

private fun Project.registerPublishPluginTasks(target: KotlinTarget, isSingleTarget: Boolean) {
    tasks.register("generatePluginMetadata".wrapNameWithPlatform(target, isSingleTarget)).get().apply {
        group = "mirai"

        val metadataFile =
            project.buildDir.resolve("mirai")
                .resolve(if (isSingleTarget) "mirai-plugin.metadata" else "mirai-plugin-${target.name}.metadata")
        outputs.file(metadataFile)



        doLast {
            val mirai = miraiExtension

            val output = outputs.files.singleFile
            output.parentFile.mkdir()

            fun getConfigurationsToInclude(): List<Configuration> {
                val compilation = target.compilations["main"]
                return compilation.relatedConfigurationNames.map { configurations[it] }
            }

            val dependencies = getConfigurationsToInclude().flatMap { it.allDependencies }.map {
                "${it.group}:${it.name}:${it.version}"
            }.distinct()

            val json = Gson().toJson(
                PluginMetadata(
                    metadataVersion = 1,
                    groupId = mirai.publishing.groupId ?: project.group.toString(),
                    artifactId = mirai.publishing.artifactId ?: project.name,
                    version = mirai.publishing.version ?: project.version.toString(),
                    description = mirai.publishing.description ?: project.description,
                    dependencies = dependencies
                )
            )

            logger.info("Generated mirai plugin metadata json: $json")

            output.writeText(json)
        }

        Unit
    }

//    val bintrayUpload = tasks.getByName(BintrayUploadTask.getTASK_NAME()).dependsOn(
//        "buildPlugin".wrapNameWithPlatform(target, isSingleTarget),
//        generateMetadataTask,
//        // "shadowJar",
//        tasks.filterIsInstance<BuildMiraiPluginTask>().single { it.target == target }
//    )
//    tasks.register("publishPlugin".wrapNameWithPlatform(target, isSingleTarget)).get().apply {
//        group = "mirai"
//        dependsOn(bintrayUpload)
//    }
}

private fun Project.registerMavenPublications(target: KotlinTarget, isSingleTarget: Boolean) {
    val mirai = miraiExtension

    @Suppress("DEPRECATION")
    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    publishing {
        /*
        repositories {
            maven {
                // change to point to your repo, e.g. http://my.org/repo
                url = uri("$buildDir/repo")
            }
        }*/
        publications.register(
            "mavenJava".wrapNameWithPlatform(target, isSingleTarget),
            MavenPublication::class.java
        ) { publication ->
            with(publication) {
                from(components["java"])

                this.groupId = mirai.publishing.groupId ?: project.group.toString()
                this.artifactId = mirai.publishing.artifactId ?: project.name
                this.version = mirai.publishing.version ?: project.version.toString()

                pom.withXml { xml ->
                    val root = xml.asNode()
                    root.appendNode("description", project.description)
                    root.appendNode("name", project.name)
                    // root.appendNode("url", vcs)
                    root.children().last()

                    mirai.publishing.mavenPomConfigs.forEach { it.invoke(xml) }
                }

                artifact(sourcesJar.get())
                artifact(tasks.filterIsInstance<BuildMiraiPluginTask>().single { it.target == target })
                artifact(
                    mapOf(
                        "source" to tasks.getByName(
                            "generatePluginMetadata".wrapNameWithPlatform(
                                target,
                                isSingleTarget
                            )
                        ).outputs.files.singleFile,
                        "extension" to "mirai.metadata"
                    )
                )

                mirai.publishing.mavenPublicationConfigs.forEach { it.invoke(this) }
            }
        }
    }
}


@PublishedApi
internal val Project.sourceSets: org.gradle.api.tasks.SourceSetContainer
    get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
@PublishedApi
internal fun Project.publishing(configure: org.gradle.api.publish.PublishingExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)
