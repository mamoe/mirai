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
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("java")
    //signing
    `maven-publish`
    id("com.jfrog.bintray")

    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin-api").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    compileOnly(kotlin("gradle-plugin").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    compileOnly(kotlin("stdlib"))

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api(`jetbrains-annotations`)
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}")
}

version = Versions.console
description = "Gradle plugin for Mirai Console"

kotlin {
    explicitApi()
}

pluginBundle {
    website = "https://github.com/mamoe/mirai-console"
    vcsUrl = "https://github.com/mamoe/mirai-console"
    tags = listOf("framework", "kotlin", "mirai")
}

gradlePlugin {
    plugins {
        create("miraiConsole") {
            id = "net.mamoe.mirai-console"
            displayName = "Mirai Console"
            description = project.description
            implementationClass = "net.mamoe.mirai.console.gradle.MiraiConsoleGradlePlugin"
        }
    }
}

kotlin.target.compilations.all {
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

tasks {
    val compileKotlin by getting {}

    val fillBuildConstants by registering {
        group = "mirai"
        doLast {
            (compileKotlin as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).source.filter { it.name == "VersionConstants.kt" }.single()
                .let { file ->
                    file.writeText(
                        file.readText()
                            .replace(
                                Regex("""const val CONSOLE_VERSION = ".*"""")
                            ) {
                                """const val CONSOLE_VERSION = "${Versions.console}""""
                            }
                            .replace(
                                Regex("""const val CORE_VERSION = ".*"""")
                            ) { """const val CORE_VERSION = "${Versions.core}"""" }
                    )
                }
        }
    }

    compileKotlin.dependsOn(fillBuildConstants)
}
