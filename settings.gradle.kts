/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

pluginManagement {
    repositories {
        if (System.getProperty("use.maven.local") == "true") { // you can enable by adding `systemProp.use.maven.local=true` in 'gradle.properties'.
            mavenLocal()
        }
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "mirai"

/**
 * Projects included so far
 */
val allProjects = mutableListOf<ProjectDescriptor>()

fun includeProject(projectPath: String, dir: String? = null) {
    include(projectPath)
    if (dir != null) project(projectPath).projectDir = file(dir)
    allProjects.add(project(projectPath))
}

fun includeConsoleProject(projectPath: String, dir: String? = null) =
    includeProject(projectPath, "mirai-console/$dir")


includeProject(":mirai-core-utils")
includeProject(":mirai-core-api")
includeProject(":mirai-core")

includeProject(":mirai-core-all")
includeProject(":mirai-bom")
includeProject(":mirai-dokka")
includeProject(":mirai-deps-test")

includeProject(":mirai-logging-log4j2", "logging/mirai-logging-log4j2")
includeProject(":mirai-logging-slf4j", "logging/mirai-logging-slf4j")
includeProject(":mirai-logging-slf4j-simple", "logging/mirai-logging-slf4j-simple")
includeProject(":mirai-logging-slf4j-logback", "logging/mirai-logging-slf4j-logback")


includeConsoleProject(":mirai-console-compiler-annotations", "tools/compiler-annotations")
includeConsoleProject(":mirai-console", "backend/mirai-console")
includeConsoleProject(":mirai-console.codegen", "backend/codegen")
includeConsoleProject(":mirai-console-terminal", "frontend/mirai-console-terminal")

includeConsoleIntegrationTestProjects()

includeConsoleProject(":mirai-console-compiler-common", "tools/compiler-common")
includeConsoleProject(":mirai-console-intellij", "tools/intellij-plugin")
includeConsoleProject(":mirai-console-gradle", "tools/gradle-plugin")

//includeConsoleFrontendGraphical()

includeProject(":ci-release-helper")

includeBinaryCompatibilityValidatorProjects()

/**
 * Configures a project `:validator:path-to-project:target-name` for binary compatibility validation.
 *
 * To enable validation for a project,
 * create a subdirectory with name of the target under "compatibility-validation",
 * then sync **twice**. See `:mirai-core-api` for an example.
 *
 * **Note**: This function depends on [allProjects], and should be used at the end.
 */
fun includeBinaryCompatibilityValidatorProjects() {
    val result = mutableListOf<ProjectDescriptor>()
    for (project in allProjects) {
        val validationDir = project.projectDir.resolve("compatibility-validation")
        if (!validationDir.exists()) continue
        validationDir.listFiles().orEmpty<File>().forEach { dir ->
            if (dir.resolve("build.gradle.kts").isFile) {
                val path = ":validator" + project.path + ":${dir.name}"
                include(path)
                project(path).projectDir = dir
//            project(path).name = "${project.name}-validator-${dir.name}"
                result.add(project(path))
            }
        }
    }
}

fun includeConsoleLegacyFrontendProjects() {
    println("JDK version: ${JavaVersion.current()}")

    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
        includeConsoleProject(":mirai-console-graphical", "frontend/mirai-console-graphical")
    } else {
        println("当前使用的 JDK 版本为 ${System.getProperty("java.version")},  请使用 JDK 9 以上版本引入模块 `:mirai-console-graphical`\n")
    }
}

fun includeConsoleIntegrationTestProjects() {
    includeConsoleProject(":mirai-console.integration-test", "backend/integration-test")

    val consoleIntegrationTestSubPluginBuildGradleKtsTemplate by lazy {
        rootProject.projectDir
            .resolve("mirai-console/backend/integration-test/testers")
            .resolve("tester.template.gradle.kts")
            .readText()
    }

    @Suppress("SimpleRedundantLet")
    fun includeConsoleITPlugin(prefix: String, path: File) {
        path.resolve("build.gradle.kts").takeIf { !it.isFile }?.let { initScript ->
            initScript.writeText(consoleIntegrationTestSubPluginBuildGradleKtsTemplate)
        }

        val projectPath = "$prefix${path.name}"
        include(projectPath)
        project(projectPath).projectDir = path
        path.listFiles()?.asSequence().orEmpty()
            .filter { it.isDirectory }
            .filter { it.resolve(".nested-module.txt").exists() }
            .forEach { includeConsoleITPlugin("${projectPath}:", it) }
    }

    rootProject.projectDir
        .resolve("mirai-console/backend/integration-test/testers")
        .listFiles()?.asSequence().orEmpty()
        .filter { it.isDirectory }
        .forEach { includeConsoleITPlugin(":mirai-console.integration-test:", it) }
}