@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    kotlin("kapt")
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
    compileOnly(kotlin("gradle-plugin-api").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    compileOnly(kotlin("gradle-plugin").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    compileOnly(kotlin("stdlib"))

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api(`jetbrains-annotations`)
}

version = Versions.console
description = "Gradle plugin for Mirai Console"

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

setupPublishing("mirai-console-gradle")