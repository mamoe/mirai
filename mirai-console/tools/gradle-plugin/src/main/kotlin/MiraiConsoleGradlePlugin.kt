/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiConsoleGradlePluginKt")

package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

public class MiraiConsoleGradlePlugin : Plugin<Project> {
    public companion object {
        internal const val MIRAI_SHADOW_CONF_NAME: String = "shadowLink"
        internal const val MIRAI_AS_NORMAL_DEP_CONF_NAME: String = "asNormalDep"
        internal const val MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME: String = "testConsoleRuntime"

        public const val FILE_SUFFIX: String = "mirai.jar"
    }

    private fun KotlinSourceSet.configureSourceSet(project: Project, target: KotlinTarget) {
        try {
            languageSettings.optIn("kotlin.RequiresOptIn")
        } catch (e: NoSuchMethodError) {
            // User is using < 1.6
            target.compilations.forEach { compilation ->
                compilation.kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
        dependencies { configureDependencies(project, this@configureSourceSet, target) }
    }

    private fun Project.configureTarget(target: KotlinTarget) {
        val miraiExtension = project.miraiExtension

        for (compilation in target.compilations) with(compilation) {
            kotlinOptions {
                if (this !is KotlinJvmOptions) return@kotlinOptions
                jvmTarget = miraiExtension.jvmTarget.toString()
                if (!miraiExtension.dontConfigureKotlinJvmDefault) freeCompilerArgs =
                    freeCompilerArgs + "-Xjvm-default=all"
            }
        }
        when (target.platformType) {
            KotlinPlatformType.jvm,
            KotlinPlatformType.androidJvm,
            KotlinPlatformType.common
            -> {
                target.compilations.flatMap { it.allKotlinSourceSets }.forEach { sourceSet ->
                    sourceSet.configureSourceSet(project, target)
                }
            }
            else -> {
            }
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun KotlinDependencyHandler.configureDependencies(
        project: Project,
        sourceSet: KotlinSourceSet,
        target: KotlinTarget
    ) {
        val miraiExtension = project.miraiExtension

        val isJvm =
            target.platformType == KotlinPlatformType.jvm || target.platformType == KotlinPlatformType.androidJvm

        if (!miraiExtension.noCoreApi) compileOnly("net.mamoe:mirai-core-api:${miraiExtension.coreVersion}")
        if (!miraiExtension.noConsole && isJvm) compileOnly("net.mamoe:mirai-console:${miraiExtension.consoleVersion}")

        if (sourceSet.name.endsWith("test", ignoreCase = true)) {
            if (!miraiExtension.noCoreApi) api("net.mamoe:mirai-core-api:${miraiExtension.coreVersion}")
            if (!miraiExtension.noConsole && isJvm) api("net.mamoe:mirai-console:${miraiExtension.consoleVersion}")
            if (!miraiExtension.noTestCore) api("net.mamoe:mirai-core:${miraiExtension.coreVersion}")
            if (isJvm) {
                when (miraiExtension.useTestConsoleFrontEnd) {
                    MiraiConsoleFrontEndKind.TERMINAL -> api("net.mamoe:mirai-console-terminal:${miraiExtension.consoleVersion}")
                    null -> {
                    }
                }
            }
        }
    }

    private fun Project.configureCompileTarget() {
        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            val miraiExtension = miraiExtension
            sourceCompatibility = miraiExtension.jvmTarget
            targetCompatibility = miraiExtension.jvmTarget
        }

        tasks.withType(JavaCompile::class.java) {
            it.options.encoding = "UTF8"
        }
    }

    private fun Project.registerGradleTasks() {
        val miraiExtension = this.miraiExtension

        // Array<String>: [jar name, jar extension]
        val buildPluginTasks = mutableListOf<Pair<Task, Array<String>>>()
        tasks.findByName("shadowJar")?.enabled = false

        fun registerBuildPluginTask(target: KotlinTarget, isSingleTarget: Boolean) {
            tasks.create(
                "buildPlugin".wrapNameWithPlatform(target, isSingleTarget),
                BuildMiraiPluginV2::class.java
            ).also { buildPluginV2 ->
                buildPluginV2.group = "mirai"
                buildPluginV2.registerMetadataTask(
                    tasks,
                    "miraiPrepareMetadata".wrapNameWithPlatform(target, isSingleTarget)
                )
                buildPluginV2.init(target)
                buildPluginV2.destinationDirectory.value(
                    project.layout.projectDirectory.dir(project.buildDir.name).dir("mirai")
                )
                buildPluginTasks.add(buildPluginV2 to arrayOf(project.name + "-dev", BuildMiraiPluginV2.FILE_SUFFIX))
            }
            tasks.create(
                "buildPluginLegacy".wrapNameWithPlatform(target, isSingleTarget),
                BuildMiraiPluginTask::class.java,
                target
            ).apply shadow@{
                group = "mirai"

                archiveExtension.set(FILE_SUFFIX)

                val compilations = target.compilations.filter { it.name == MAIN_COMPILATION_NAME }

                compilations.forEach {
                    @Suppress("DEPRECATION") // We need to support older Kotlin versions
                    dependsOn(it.compileKotlinTask)
                    from(it.output.allOutputs)
                }

                from(project.configurations.getByName("runtimeClasspath").copyRecursive { dependency ->
                    for (excludedDependency in IGNORED_DEPENDENCIES_IN_SHADOW + miraiExtension.excludedDependencies) {
                        if (excludedDependency.group.equals(dependency.group, ignoreCase = true)
                            && excludedDependency.name.equals(dependency.name, ignoreCase = true)
                        ) return@copyRecursive false
                    }
                    true
                })

                exclude { file ->
                    file.name.endsWith(".sf", ignoreCase = true)
                }

                destinationDirectory.value(project.layout.projectDirectory.dir(project.buildDir.name).dir("mirai"))

                miraiExtension.shadowConfigurations.forEach { it.invoke(this@shadow) }
            }
        }

        val targets = kotlinTargets
        val isSingleTarget = targets.size == 1
        targets.forEach { target ->
            registerBuildPluginTask(target, isSingleTarget)
        }

        if (miraiExtension.consoleTestRuntime) {
            dependencies.add(
                MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME,
                "net.mamoe:mirai-core-api:${miraiExtension.coreVersion}"
            )
            dependencies.add(
                MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME,
                "net.mamoe:mirai-core:${miraiExtension.coreVersion}"
            )
            dependencies.add(
                MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME,
                "net.mamoe:mirai-console:${miraiExtension.consoleVersion}"
            )
            val frontendDep = when (miraiExtension.useTestConsoleFrontEnd) {
                MiraiConsoleFrontEndKind.TERMINAL -> "net.mamoe:mirai-console-terminal:${miraiExtension.consoleVersion}"
                null -> null
            }
            if (frontendDep != null) {
                dependencies.add(MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME, frontendDep)
            }
            tasks.register("runConsole", JavaExec::class.java) { runConsole ->
                runConsole.group = "mirai"
                runConsole.classpath += configurations.getByName(MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME)
                runConsole.mainClass.set(
                    when (miraiExtension.useTestConsoleFrontEnd) {
                        MiraiConsoleFrontEndKind.TERMINAL -> "net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader"
                        null -> "ERROR_mirai_console_frontend_not_found"
                    }
                )
                // default runs on $projectDir/debug-sandbox
                runConsole.workingDir = project.projectDir.resolve("debug-sandbox")
                runConsole.doFirst {
                    // working dir can be changed by user
                    val plugins = runConsole.workingDir.resolve("plugins")
                    plugins.mkdirs()
                    buildPluginTasks.forEach { (buildPluginTask, jarNameMetadata) ->
                        val (jarName, jarSuffix) = jarNameMetadata
                        buildPluginTask.outputs.files.files.forEachIndexed { index, outFile ->
                            val name = jarName + if (index == 0) {
                                ""
                            } else {
                                "-$index"
                            } + '.' + jarSuffix
                            outFile.copyTo(plugins.resolve(name), overwrite = true)
                        }
                    }
                }
                runConsole.standardInput = System.`in`
                runConsole.jvmArgs("-Dmirai.console.skip-end-user-readme")

                buildPluginTasks.forEach { runConsole.dependsOn(it.first) }
                miraiExtension.consoleTestRuntimeConf.forEach { it.invoke(runConsole) }
            }
        }
    }

    private fun Project.setupConfigurations() {
        configurations.create(MIRAI_SHADOW_CONF_NAME).isCanBeResolved = false
        configurations.create(MIRAI_AS_NORMAL_DEP_CONF_NAME).isCanBeResolved = false
        configurations.create(MIRAI_DIRECT_RUN_CONSOLE_CONF_NAME).let { runConsoleConf ->
            runConsoleConf.attributes { ac ->
                fun attribute(key: String, value: String) {
                    ac.attribute(Attribute.of(key, String::class.java), value)
                }
                attribute("org.jetbrains.kotlin.platform.type", "jvm")

                ac.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }
        }
    }

    override fun apply(target: Project): Unit = with(target) {
        extensions.create("mirai", MiraiConsoleExtension::class.java)

        plugins.apply(JavaPlugin::class.java)
        plugins.apply("org.gradle.maven-publish")
        // plugins.apply("org.gradle.maven")
        plugins.apply(ShadowPlugin::class.java)

        project.setupConfigurations()

        afterEvaluate {
            configureCompileTarget()
            kotlinTargets.forEach { configureTarget(it) }
            registerGradleTasks()
            configurePublishing()
        }
    }
}

internal val Project.miraiExtension: MiraiConsoleExtension
    get() = extensions.findByType(MiraiConsoleExtension::class.java)
        ?: error("Cannot find MiraiConsoleExtension in project ${this.name}")

internal val Project.kotlinTargets: Collection<KotlinTarget>
    get() {
        val kotlinExtension = extensions.findByType(KotlinProjectExtension::class.java)
            ?: error("Kotlin plugin not applied. Please read https://www.kotlincn.net/docs/reference/using-gradle.html")

        return when (kotlinExtension) {
            is KotlinMultiplatformExtension -> kotlinExtension.targets
            is KotlinSingleTargetExtension<*> -> listOf(kotlinExtension.target)
            else -> error("[MiraiConsole] Internal error: kotlinExtension is neither KotlinMultiplatformExtension nor KotlinSingleTargetExtension")
        }
    }

internal val Project.kotlinJvmOrAndroidTargets: Collection<KotlinTarget>
    get() = kotlinTargets.filter { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }
