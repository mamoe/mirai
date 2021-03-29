/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")

    id("org.jetbrains.intellij") version Versions.intellijGradlePlugin
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
}

version = Versions.console
description = "IntelliJ plugin for Mirai Console"

// JVM fails to compile
kotlin.target.compilations.forEach { kotlinCompilation ->
    kotlinCompilation.kotlinOptions.freeCompilerArgs += "-Xuse-ir"
} // don't use `useIr()`, compatibility with mirai-console dedicated builds

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = Versions.intellij
    isDownloadSources = true
    updateSinceUntilBuild = false

    sandboxDirectory = projectDir.resolve("run/idea-sandbox").absolutePath

    setPlugins(
        "org.jetbrains.kotlin:${Versions.kotlinIntellijPlugin}", // @eap
        "java",
        "gradle",
        "maven"
    )
}

project.extra.set("javaTarget", JavaVersion.VERSION_11) // see build.gradle.kts:213 in root project

tasks.getByName("publishPlugin", org.jetbrains.intellij.tasks.PublishTask::class) {
    val pluginKey = project.findProperty("jetbrains.hub.key")?.toString()
    if (pluginKey != null) {
        logger.info("Found jetbrains.hub.key")
        setToken(pluginKey)
    } else {
        logger.info("jetbrains.hub.key not found")
    }
}

fun File.resolveMkdir(relative: String): File {
    return this.resolve(relative).apply { mkdirs() }
}

tasks.withType<org.jetbrains.intellij.tasks.PatchPluginXmlTask> {
    sinceBuild("201.*")
    untilBuild("215.*")
    pluginDescription(
        """
        Plugin development support for <a href='https://github.com/mamoe/mirai-console'>Mirai Console</a>
        
        <h3>Features</h3>
        <ul>
            <li>Inspections for plugin properties, for example, checking PluginDescription.</li>
            <li>Inspections for illegal calls.</li>
            <li>Intentions for resolving serialization problems.</li>
        </ul>
    """.trimIndent()
    )
    changeNotes(
        """
        See <a href="https://github.com/mamoe/mirai-console/releases">https://github.com/mamoe/mirai-console/releases</a>
    """.trimIndent()
    )
}

dependencies {
    api(`jetbrains-annotations`)
    api(`kotlinx-coroutines-jdk8`)
    api(`kotlinx-coroutines-swing`)

    api(project(":mirai-console-compiler-common"))

    compileOnly(`kotlin-stdlib-jdk8`)
    compileOnly("com.jetbrains:ideaIC:${Versions.intellij}")
    // compileOnly(`kotlin-compiler`)

    compileOnly(files("libs/ide-common.jar"))
    compileOnly(fileTree("build/idea-sandbox/plugins/Kotlin/lib").filter {
        !it.name.contains("stdlib")
    })
    compileOnly(`kotlin-reflect`)
}
