/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package shadow

import MIRAI_PLATFORM_INTERMEDIATE
import capitalize
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlinJvm
import kotlinMpp
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import sourceSets

/**
 * @see RelocationNotes
 */
fun Project.configureMppShadow() {
    val kotlin = kotlinMpp ?: return

    configure(kotlin.targets.filter {
        it.platformType == KotlinPlatformType.jvm
                && (it.attributes.getAttribute(MIRAI_PLATFORM_INTERMEDIATE) != true)
    }) {
        configureRelocationForMppTarget(project)

        registerRegularShadowTask(this, mapTaskNameForMultipleTargets = true)
    }
}

/**
 * Relocate some dependencies for `.jar`
 * @see RelocationNotes
 */
private fun KotlinTarget.configureRelocationForMppTarget(project: Project) = project.run {
    val configuration = project.configurations.findByName(RelocationConfig.SHADOW_RELOCATION_CONFIGURATION_NAME)

    // e.g. relocateJvmDependencies
    // do not change task name. see `configureShadowDependenciesForPublishing`
    val relocateDependenciesName = RelocationConfig.taskNameForRelocateDependencies(targetName)
    tasks.create(relocateDependenciesName, ShadowJar::class) {
        group = "mirai"
        description = "Relocate dependencies to internal package"
        destinationDirectory.set(buildDir.resolve("libs")) // build/libs
        archiveBaseName.set("${project.name}-${targetName.lowercase()}-relocated") // e.g. "mirai-core-api-jvm"

        dependsOn(compilations["main"].compileTaskProvider) // e.g. compileKotlinJvm

        from(compilations["main"].output) // Add the compilation result of mirai sourcecode, not including dependencies
        configuration?.let {
            from(it) // Include runtime dependencies
        }

        // Relocate packages
        afterEvaluate {
            val relocationFilters = project.relocationFilters
            relocationFilters.forEach { relocation ->
                relocation.packages.forEach { aPackage ->
                    relocate(aPackage, "${RelocationConfig.RELOCATION_ROOT_PACKAGE}.$aPackage")
                }
            }
        }
    }
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
        dependsOn(compilation.compileTaskProvider)
        from(compilation.output)

//        components.findByName("java")?.let { from(it) }
        project.sourceSets.findByName("main")?.output?.let { from(it) } // for JVM projects
        this.configurations = configurations

        // Relocate packages
        afterEvaluate {
            val relocationFilters = project.relocationFilters
            relocationFilters.forEach { relocation ->
                relocation.packages.forEach { aPackage ->
                    relocate(aPackage, "${RelocationConfig.RELOCATION_ROOT_PACKAGE}.$aPackage")
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

