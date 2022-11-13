/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File

/**
 * @see RelocationNotes
 */
fun Project.configureMppShadow() {
    val kotlin = kotlinMpp ?: return

    configure(kotlin.targets.filter {
        it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
                && (it.attributes.getAttribute(MIRAI_PLATFORM_INTERMEDIATE) != true)
    }) {
        configureRelocationForTarget(project)

        registerRegularShadowTask(this, mapTaskNameForMultipleTargets = true)
    }
}

/**
 * Relocate some dependencies for `.jar`
 * @see RelocationNotes
 */
private fun KotlinTarget.configureRelocationForTarget(project: Project) = project.run {
    val configuration = project.configurations.findByName(SHADOW_RELOCATION_CONFIGURATION_NAME)
    val relocateDependencies =
        // e.g. relocateJvmDependencies
        tasks.create("relocate${targetName.titlecase()}Dependencies", ShadowJar::class) {
            group = "mirai"
            description = "Relocate dependencies to internal package"
            destinationDirectory.set(buildDir.resolve("libs")) // build/libs
            archiveBaseName.set("${project.name}-${targetName.toLowerCase()}") // e.g. "mirai-core-api-jvm"

            dependsOn(compilations["main"].compileKotlinTask) // e.g. compileKotlinJvm

            from(compilations["main"].output) // Add compilation result of mirai sourcecode, not including dependencies
            configuration?.let {
                from(it) // Include runtime dependencies
            }

            // Relocate packages
            afterEvaluate {
                val relocationFilters = project.relocationFilters
                relocationFilters.forEach { relocation ->
                    relocation.packages.forEach { aPackage ->
                        relocate(aPackage, "$RELOCATION_ROOT_PACKAGE.$aPackage")
                    }
                }
            }
        }


    // set publishing tasks depends on shadow
    val allTasks = rootProject.allprojects.asSequence().flatMap { it.tasks }
    allTasks
        .filter {
            it.name.startsWith("publish${targetName.titlecase()}PublicationTo")
        }
        .onEach { it.dependsOn(relocateDependencies) }
        .count().let {
            check(it > 0) { "[Shadow Relocation] Expected at least one publication matched for target $targetName." }
        }


    // Ensure all compilation has finished, otherwise Kotlin compiler will complain.
    allTasks
        .filter { it.name.endsWith("Jar") }
        .onEach { relocateDependencies.dependsOn(it) }
        .count().let {
            check(it > 0) { "[Shadow Relocation] Expected at least one Jar task matched for target $targetName." }
        }

    allTasks
        .filter { it.name.startsWith("compileKotlin") }
        .onEach { relocateDependencies.dependsOn(it) }
        .count().let {
            check(it > 0) { "[Shadow Relocation] Expected at least one compileKotlin task matched for target $targetName." }
        }

    // We will modify metadata, so do generate metadata before relocation
    val metadataTask =
        tasks.getByName("generateMetadataFileFor${targetName.capitalize()}Publication") as GenerateModuleMetadata
    relocateDependencies.dependsOn(metadataTask)

    afterEvaluate {
        // remove dependencies in Maven pom
        mavenPublication {
            pom.withXml {
                val node = this.asNode().getSingleChild("dependencies")
                val dependencies = node.childrenNodes()
                logger.trace("[Shadow Relocation] deps: $dependencies")
                dependencies.forEach { dep ->
                    val groupId = dep.getSingleChild("groupId").value().toString()
                    val artifactId = dep.getSingleChild("artifactId").value().toString()
                    logger.trace("[Shadow Relocation] Checking $groupId:$artifactId")

                    if (
                        relocationFilters.any { filter ->
                            filter.matchesDependency(groupId = groupId, artifactId = artifactId)
                        }
                    ) {
                        logger.info("[Shadow Relocation] Filtering out '$groupId:$artifactId' from pom for project '${project.path}'")
                        check(node.remove(dep)) { "Failed to remove dependency node" }
                    }
                }
            }
        }

        // remove dependencies in Kotlin module metadata
        relocateDependencies.doLast {
            // mirai-core-jvm-2.13.0.module
            val file = metadataTask.outputFile.asFile.get()
            val metadata = Gson().fromJson(
                file.readText(),
                com.google.gson.JsonElement::class.java
            ).asJsonObject

            val metadataVersion = metadata["formatVersion"]?.asString
            check(metadataVersion == "1.1") {
                "Unsupported Kotlin metadata version. version=$metadataVersion, file=${file.absolutePath}"
            }
            for (variant in metadata["variants"]!!.asJsonArray) {
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
            }


            file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(metadata))
        }
    }
}

private fun Project.registerRegularShadowTask(target: KotlinTarget, mapTaskNameForMultipleTargets: Boolean): ShadowJar {
    return tasks.create(
        if (mapTaskNameForMultipleTargets) "shadow${target.targetName}Jar" else "shadowJar",
        ShadowJar::class
    ) {
        group = "mirai"
        archiveClassifier.set("all")

        val compilation = target.compilations["main"]
        dependsOn(compilation.compileKotlinTask)
        from(compilation.output)

//        components.findByName("java")?.let { from(it) }
        project.sourceSets.findByName("main")?.output?.let { from(it) } // for JVM projects
        configurations =
            listOfNotNull(
                project.configurations.findByName("runtimeClasspath"),
                project.configurations.findByName("${target.targetName}RuntimeClasspath"),
                project.configurations.findByName("runtime")
            )

        // Relocate packages
        afterEvaluate {
            val relocationFilters = project.relocationFilters
            relocationFilters.forEach { relocation ->
                relocation.packages.forEach { aPackage ->
                    relocate(aPackage, "$RELOCATION_ROOT_PACKAGE.$aPackage")
                }
            }
        }

        exclude { file ->
            file.name.endsWith(".sf", ignoreCase = true)
        }
    }
}

fun Project.configureRelocatedShadowJarForJvmProject(kotlin: KotlinJvmProjectExtension): ShadowJar {
    return registerRegularShadowTask(kotlin.target, mapTaskNameForMultipleTargets = false)
}

const val RELOCATION_ROOT_PACKAGE = "net.mamoe.mirai.internal.deps"