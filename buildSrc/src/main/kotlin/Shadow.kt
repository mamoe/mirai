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
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
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
    }

    // regular shadow file, with suffix `-all`
    configureRegularShadowJar(kotlin)
}

/**
 * Relocate some dependencies for `.jar`
 * @see RelocationNotes
 */
private fun KotlinTarget.configureRelocationForTarget(project: Project) = project.run {
    val relocateDependencies =
        // e.g. relocateJvmDependencies
        tasks.create("relocate${targetName.titlecase()}Dependencies", ShadowJar::class) {
            group = "mirai"
            description = "Relocate dependencies to internal package"
            destinationDirectory.set(buildDir.resolve("libs")) // build/libs
            archiveBaseName.set("${project.name}-${targetName.toLowerCase()}") // e.g. "mirai-core-api-jvm"
            dependsOn(compilations["main"].compileKotlinTask) // e.g. compileKotlinJvm
            from(compilations["main"].output) // Add compilation result of mirai sourcecode, not including dependencies
            afterEvaluate {
                // Relocate dependencies and include it in result jar
                setRelocations("${targetName}RuntimeClasspath")
            }
        }

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
            check(it > 0) { "[Shadow Relocation] Expected at least one task matched for target $targetName." }
        }

    allTasks
        .filter { it.name.startsWith("compileKotlin") }
        .onEach { relocateDependencies.dependsOn(it) }
        .count().let {
            check(it > 0) { "[Shadow Relocation] Expected at least one task matched for target $targetName." }
        }

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

private fun Project.configureRegularShadowJar(kotlin: KotlinMultiplatformExtension) {
    if (project.configurations.findByName("jvmRuntimeClasspath") != null) {
        val shadowJvmJar by tasks.creating(ShadowJar::class) sd@{
            group = "mirai"
            archiveClassifier.set("-all")

            val compilations =
                kotlin.targets.filter { it.platformType == KotlinPlatformType.jvm }
                    .map { it.compilations["main"] }

            compilations.forEach {
                dependsOn(it.compileKotlinTask)
                from(it.output)
            }

            // Include relocated dependencies
            afterEvaluate {
                setRelocations("jvmRuntimeClasspath")

                // Include dependencies except relocated ones
                from(
                    project.configurations.findByName("jvmRuntimeClasspath")
                        ?.filesExcludeOriginalDependenciesForRelocation(project)
                )
            }

            this.exclude { file ->
                file.name.endsWith(".sf", ignoreCase = true)
            }

            /*
        this.manifest {
            this.attributes(
                "Manifest-Version" to 1,
                "Implementation-Vendor" to "Mamoe Technologies",
                "Implementation-Title" to this.name.toString(),
                "Implementation-Version" to this.version.toString()
            )
        }*/
        }
    }
}

private const val relocationRootPackage = "net.mamoe.mirai.internal.deps"

// For example, exclude io.ktor.*
private fun Configuration.filesExcludeOriginalDependenciesForRelocation(project: Project): List<File> {
    val relocationFilters = project.relocationFilters
    return this.files.filter { file ->
        val matchingFilter = relocationFilters.find { filter ->
            // file.absolutePath example: /Users/xxx/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk8/1.7.0-RC/7f9f07fc65e534c15a820f61d846b9ffdba8f162/kotlin-stdlib-jdk8-1.7.0-RC.jar
            filter.matchesFile(file)
        }
        matchingFilter == null
    }

//    var configuration = this.copyRecursive() // Original one cannot be changed after resolution
//    val relocationFilters = project.relocationFilters
//    relocationFilters.forEach { relocation ->
//        configuration = configuration.exclude(relocation.groupId, relocation.artifactId)
//    }
//    return configuration
}

private fun ShadowJar.setRelocations(runtimeClasspathConfiguration: String) {
    val shadowJar = this
    val relocationFilters = project.relocationFilters
    relocationFilters.forEach { relocation ->
        relocation.packages.forEach { aPackage ->
            relocate(aPackage, "$relocationRootPackage.$aPackage")
        }
    }


    var fileFiltered = relocationFilters.isEmpty()
    val files = project.configurations.getByName(runtimeClasspathConfiguration).files
    from(
        files.filter { file ->
            val matchingFilter = relocationFilters.find { filter ->
                // file.absolutePath example: /Users/xxx/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk8/1.7.0-RC/7f9f07fc65e534c15a820f61d846b9ffdba8f162/kotlin-stdlib-jdk8-1.7.0-RC.jar
                filter.matchesFile(file)
            }

            if (matchingFilter != null) {
                fileFiltered = true
            }

            if (matchingFilter?.includeInRuntime == true) {
                println("[Shadow Relocation] Including file in runtime (${project.path}:${shadowJar.name}/$runtimeClasspathConfiguration): ${file.name}")
                true
            } else {
                false
            }
        }
    )
    check(fileFiltered) {
        "[Shadow Relocation] Expected at least one file filtered for configuration '$runtimeClasspathConfiguration' for project '${project.path}'. \n" +
                "Filters: $relocationFilters\n" +
                "Files: ${files.joinToString("\n")}"
    }
}