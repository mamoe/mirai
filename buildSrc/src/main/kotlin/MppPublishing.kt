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
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

fun logPublishing(message: String) {
    println("[Publishing] Configuring $message")
}

fun Project.configureMppPublishing() {
    configureRemoteRepos()

    // mirai does some magic on MPP targets
    afterEvaluate {
//        tasks.findByName("compileKotlinCommon")?.enabled = false
//        tasks.findByName("compileTestKotlinCommon")?.enabled = false

//        tasks.findByName("compileCommonMainKotlinMetadata")?.enabled = false
//        tasks.findByName("compileKotlinMetadata")?.enabled = false

        // TODO: 2021/1/30 如果添加 JVM 到 root module, 这个 task 会失败因 root module artifacts 有变化
        //  tasks.findByName("generateMetadataFileForKotlinMultiplatformPublication")?.enabled = false // FIXME: 2021/1/21
    }

    val stubJavadoc = tasks.register("javadocJar", Jar::class) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        archiveClassifier.set("javadoc")
    }

    afterEvaluate {
        publishing {
            logPublishing("Publications: ${publications.joinToString { it.name }}")

            val (nonJvmPublications, jvmPublications) = publications.filterIsInstance<MavenPublication>()
                .partition { publication -> tasks.findByName("relocate${publication.name.titlecase()}Dependencies") == null }

            for (publication in nonJvmPublications) {
                configureMultiplatformPublication(publication, stubJavadoc, publication.name)
            }
            for (publication in jvmPublications) {
//                publications.remove(publication)
//                val newPublication =
//                    publications.register(publication.name + "Shadowed", MavenPublication::class.java) {
//                        val target = kotlinTargets.orEmpty().single { it.targetName == publication.name }
//                        from(target.components.single())
//                        this.groupId = publication.groupId
//                        this.artifactId = publication.artifactId
//                        this.version = publication.version
//                        artifacts {
//                            publication.artifacts
//                                .filter { !(it.classifier.isNullOrEmpty() && it.extension == "jar") } // not .jar
//                                .forEach { artifact(it) } // copy Kotlin metadata artifacts
//                        }
//                        artifacts.removeAll { it.classifier.isNullOrEmpty() && it.extension == "jar" }
//                        // add relocated jar
//                        tasks.findByName("relocate${publication.name.titlecase()}Dependencies")?.let { relocation ->
//                            artifact(relocation) {
//                                classifier = ""
//                                extension = "jar"
//                            }
//                        }
//                    }
                configureMultiplatformPublication(publication, stubJavadoc, publication.name)
                publication.apply {
                    artifacts.filter { it.classifier.isNullOrEmpty() && it.extension == "jar" }.forEach {
                        it.builtBy(tasks.findByName("relocate${publication.name.titlecase()}Dependencies"))
                    }
                }
            }
            configGpgSign(this@configureMppPublishing)
        }
    }
}

private fun Project.configureMultiplatformPublication(
    publication: MavenPublication,
    stubJavadoc: TaskProvider<Jar>,
    moduleName: String,
) {
    // Maven Central always require javadoc.jar
    publication.artifact(stubJavadoc)
    publication.setupPom(project)

    logPublishing(publication.name + ": moduleName = $moduleName")
    when (moduleName) {
        "kotlinMultiplatform" -> {
            publication.artifactId = project.name

            // publishPlatformArtifactsInRootModule(publications.getByName("jvm") as MavenPublication)

            // TODO: 2021/1/30 现在添加 JVM 到 root module 会导致 Gradle 依赖无法解决
            // https://github.com/mamoe/mirai/issues/932
        }
        "metadata" -> { // TODO: 2021/1/21 seems no use. none `type` is "metadata"
            publication.artifactId = "${project.name}-metadata"
        }
        else -> {
            // "jvm", "native", "js", "common"
            publication.artifactId = "${project.name}-$moduleName"
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

    // TODO: 2021/1/30 root module 问题可能要在这里解决
    tasks.matching { it.name == "generatePomFileForKotlinMultiplatformPublication" }.configureEach {
        dependsOn(tasks["generatePomFileFor${platformPublication.name.capitalize()}Publication"])
    }
}

fun MavenArtifact.smartToString(): String {
    return "${file.path}, classifier=${classifier}, ext=${extension}"
}
