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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * @see RelocationNotes
 */
fun Project.configureMppShadow() {
    val kotlin = kotlinMpp ?: return

    configure(kotlin.targets.filter {
        it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
                && (it.attributes.getAttribute(MIRAI_PLATFORM_INTERMEDIATE) != true)
    }) {
        configureRelocationForMppTarget(project)

        registerRegularShadowTask(this, mapTaskNameForMultipleTargets = true)
    }
}

/**
 * 配置 `publish` 和 `shadow` 相关依赖. 对于在本次构建的请求的任务及其直接或间接依赖, 以以下顺序执行:
 *
 * 1. 执行全部 `jar` 任务
 * 2. 执行全部 `relocate` 任务
 * 3. 执行全部 `publish` 任务
 *
 * 这是必要的因为 relocate 任务会覆盖 jar 任务的输出, 而在多模块并行编译时, Kotlin 编译器会依赖 jar 任务的输出. 如果在编译同时修改 JAR 文件, 就会导致 `ZipException`.
 *
 * 这也会让 publish 集中执行, Maven Central 不容易出问题.
 */
fun Project.configureShadowDependenciesForPublishing() {
    check(this.rootProject === this) {
        "configureShadowDependenciesForPublishing can only be used on root project."
    }

    gradle.projectsEvaluated {
        // Tasks requested to run in this build
        val allTasks = rootProject.allprojects.asSequence().flatMap { it.tasks }

        val publishTasks = allTasks.filter { it.name.contains("publish", ignoreCase = true) }
        val relocateTasks = allTasks.filter { it.name.contains("relocate", ignoreCase = true) }
        val jarTasks = allTasks.filter { it.name.contains("jar", ignoreCase = true) }
        val compileKotlinTasks = allTasks.filter { it.name.contains("compileKotlin", ignoreCase = true) }
        val compileTestKotlinTasks = allTasks.filter { it.name.contains("compileTestKotlin", ignoreCase = true) }

        relocateTasks.dependsOn(compileKotlinTasks.toList())
        relocateTasks.dependsOn(compileTestKotlinTasks.toList())
        relocateTasks.dependsOn(jarTasks.toList())
        publishTasks.dependsOn(relocateTasks.toList())
    }
}

val TaskExecutionGraph.hierarchicalTasks: Sequence<Task>
    get() = sequence {
        suspend fun SequenceScope<Task>.addTask(task: Task) {
            yield(task)
            for (dependency in getDependencies(task)) {
                addTask(dependency)
            }
        }

        for (task in allTasks) {
            addTask(task)
        }
    }

/**
 * Relocate some dependencies for `.jar`
 * @see RelocationNotes
 */
private fun KotlinTarget.configureRelocationForMppTarget(project: Project) = project.run {
    val configuration = project.configurations.findByName(SHADOW_RELOCATION_CONFIGURATION_NAME)

    // e.g. relocateJvmDependencies
    // do not change task name. see `configureShadowDependenciesForPublishing`
    val relocateDependencies = tasks.create("relocate${targetName.titlecase()}Dependencies", ShadowJar::class) {
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

    // We will modify Kotlin metadata, so do generate metadata before relocation
    val generateMetadataTask =
        tasks.getByName("generateMetadataFileFor${targetName.capitalize()}Publication") as GenerateModuleMetadata

    val patchMetadataTask = tasks.create("patchMetadataFileFor${targetName.capitalize()}RelocatedPublication") {
        dependsOn(generateMetadataTask)
        dependsOn(relocateDependencies)

        // remove dependencies in Kotlin module metadata
        doLast {
            // mirai-core-jvm-2.13.0.module
            val file = generateMetadataTask.outputFile.asFile.get()
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

    // Set "publishKotlinMultiplatformPublicationTo*" and "publish${targetName.capitalize()}PublicationTo*" dependsOn patchMetadataTask
    if (project.kotlinMpp != null) {
        tasks.filter { it.name.startsWith("publishKotlinMultiplatformPublicationTo") }.let { publishTasks ->
            if (publishTasks.isEmpty()) {
                throw GradleException("[Shadow Relocation] Cannot find publishKotlinMultiplatformPublicationTo for project '${project.path}'.")
            }
            publishTasks.forEach { it.dependsOn(patchMetadataTask) }
        }

        tasks.filter { it.name.startsWith("publish${targetName.capitalize()}PublicationTo") }.let { publishTasks ->
            if (publishTasks.isEmpty()) {
                throw GradleException("[Shadow Relocation] Cannot find publish${targetName.capitalize()}PublicationTo for project '${project.path}'.")
            }
            publishTasks.forEach { it.dependsOn(patchMetadataTask) }
        }
    }

    afterEvaluate {
        // Remove relocated dependencies in Maven pom
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
    }
}

private fun Sequence<Task>.dependsOn(
    task: Task,
) {
    return forEach { it.dependsOn(task) }
}

private fun Sequence<Task>.dependsOn(
    tasks: Iterable<Task>,
) {
    return forEach { it.dependsOn(tasks) }
}

/**
 * 添加 `implementation` 和 `shadow`
 */
fun DependencyHandlerScope.shadowImplementation(dependencyNotation: Any) {
    "implementation"(dependencyNotation)
    "shadow"(dependencyNotation)
}

fun Project.registerRegularShadowTaskForJvmProject(
    configurations: List<Configuration> = listOfNotNull(
        project.configurations.findByName("runtimeClasspath"),
        project.configurations.findByName("${kotlinJvm!!.target.name}RuntimeClasspath"),
        project.configurations.findByName("runtime")
    )
): ShadowJar {
    return project.registerRegularShadowTask(kotlinJvm!!.target, mapTaskNameForMultipleTargets = false, configurations)
}

fun Project.registerRegularShadowTask(
    target: KotlinTarget,
    mapTaskNameForMultipleTargets: Boolean,
    configurations: List<Configuration> = listOfNotNull(
        project.configurations.findByName("runtimeClasspath"),
        project.configurations.findByName("${target.targetName}RuntimeClasspath"),
        project.configurations.findByName("runtime")
    ),
): ShadowJar {
    return tasks.create(
        if (mapTaskNameForMultipleTargets) "shadow${target.targetName.capitalize()}Jar" else "shadowJar",
        ShadowJar::class
    ) {
        group = "mirai"
        archiveClassifier.set("all")

        (tasks.findByName("jar") as? Jar)?.let {
            manifest.inheritFrom(it.manifest)
        }

        val compilation = target.compilations["main"]
        dependsOn(compilation.compileKotlinTask)
        from(compilation.output)

//        components.findByName("java")?.let { from(it) }
        project.sourceSets.findByName("main")?.output?.let { from(it) } // for JVM projects
        this.configurations = configurations

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
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    }
}

fun Project.configureRelocatedShadowJarForJvmProject(kotlin: KotlinJvmProjectExtension): ShadowJar {
    return registerRegularShadowTask(kotlin.target, mapTaskNameForMultipleTargets = false)
}

const val RELOCATION_ROOT_PACKAGE = "net.mamoe.mirai.internal.deps"