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
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * @see RelocationNotes
 */
fun Project.configureMppShadow() {
    val kotlin = kotlinMpp ?: return

    configure(kotlin.targets.filter {
        it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
                && it.attributes.getAttribute(MIRAI_PLATFORM_ATTRIBUTE) == null
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
            destinationDirectory.set(buildDir.resolve("libs"))
//            archiveClassifier.set("")
            archiveBaseName.set("${project.name}-${targetName.toLowerCase()}")

            dependsOn(compilations["main"].compileKotlinTask) // compileKotlinJvm

            // Run after all *Jar tasks from all projects, since Kotlin compiler may depend on the .jar file, concurrently modifying the jar will cause Kotlin compiler to fail.
//            allprojects
//                .asSequence()
//                .flatMap { it.tasks }
//                .filter { it.name.contains("compileKotlin") }
//                .forEach { jar ->
//                    mustRunAfter(jar)
//                }

            from(compilations["main"].output)

//            // change name to
//            doLast {
//                outputs.files.singleFile.renameTo(
//                    outputs.files.singleFile.parentFile.resolve(
//                        "${project.name}-${targetName.toLowerCase()}-${project.version}.jar"
//                    )
//                )
//            }
            // Filter only those should be relocated

            afterEvaluate {
                setRelocations()

                var fileFiltered = relocationFilters.isEmpty()
                from(project.configurations.getByName("${targetName}RuntimeClasspath")
                    .files
                    .filter { file ->
                        val matchingFilter = relocationFilters.find { filter ->
                            // file.absolutePath example: /Users/xxx/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk8/1.7.0-RC/7f9f07fc65e534c15a820f61d846b9ffdba8f162/kotlin-stdlib-jdk8-1.7.0-RC.jar
                            filter.matchesFile(file)
                        }

                        if (matchingFilter != null) {
                            fileFiltered = true
                            println("Including file: ${file.absolutePath}")
                        }

                        matchingFilter?.includeInRuntime == true
                    }
                )
                check(fileFiltered) { "[Shadow Relocation] Expected at least one file filtered for target $targetName. Filters: $relocationFilters" }
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
                        println("[Shadow Relocation] Filtering out $groupId:$artifactId from pom")
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

            setRelocations()

            from(project.configurations.findByName("jvmRuntimeClasspath"))

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

private fun ShadowJar.setRelocations() {
    project.relocationFilters.forEach { relocation ->
        if (relocation.packages.size == 1) {
            val srcPkg = relocation.packages.first()
            val dst = if (relocation.groupId.endsWith(srcPkg)) {
                relocation.groupId
            } else {
                "${relocation.groupId}.$srcPkg"
            }

            relocate(srcPkg, "$relocationRootPackage.$dst")
        } else {
            relocation.packages.forEach { aPackage ->
                relocate(aPackage, "$relocationRootPackage.${relocation.groupId}.$aPackage")
            }
        }
    }
}