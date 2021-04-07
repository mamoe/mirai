/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
import org.gradle.api.JavaVersion

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
    }
}

rootProject.name = "mirai-console"

val disableOldFrontEnds = true

fun includeProject(projectPath: String, path: String? = null) {
    include(projectPath)
    if (path != null) project(projectPath).projectDir = file(path)
}

includeProject(":mirai-console-compiler-annotations", "tools/compiler-annotations")
includeProject(":mirai-console", "backend/mirai-console")
includeProject(":mirai-console.codegen", "backend/codegen")
includeProject(":mirai-console-terminal", "frontend/mirai-console-terminal")
includeProject(":mirai-console-compiler-common", "tools/compiler-common")
includeProject(":mirai-console-intellij", "tools/intellij-plugin")
includeProject(":mirai-console-gradle", "tools/gradle-plugin")

@Suppress("ConstantConditionIf")
if (!disableOldFrontEnds) {
    includeProject(":mirai-console-terminal", "frontend/mirai-console-terminal")

    println("JDK version: ${JavaVersion.current()}")

    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
        includeProject(":mirai-console-graphical", "frontend/mirai-console-graphical")
    } else {
        println("当前使用的 JDK 版本为 ${System.getProperty("java.version")},  请使用 JDK 9 以上版本引入模块 `:mirai-console-graphical`\n")
    }
}

enableFeaturePreview("GRADLE_METADATA")