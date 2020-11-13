@file:Suppress("UnusedImport")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

version = Versions.console
description = "Mirai Console Backend"

kotlin {
    explicitApiWarning()
}

dependencies {
    compileAndTestRuntime(`mirai-core`)
    compileAndTestRuntime(`kotlin-stdlib`)
    compileAndTestRuntime(`kotlin-stdlib-jdk8`)

    compileAndTestRuntime(`kotlinx-atomicfu`)
    compileAndTestRuntime(`kotlinx-coroutines-core`)
    compileAndTestRuntime(`kotlinx-serialization-core`)
    compileAndTestRuntime(`kotlinx-serialization-json`)
    compileAndTestRuntime(`kotlin-reflect`)

    smartImplementation(yamlkt)
    smartImplementation(`jetbrains-annotations`)
    smartImplementation(`caller-finder`)
    smartApi(`kotlinx-coroutines-jdk8`)

    testApi(`mirai-core-qqandroid`)
    testApi(`kotlin-stdlib-jdk8`)
}

tasks {
    val compileKotlin by getting {}

    register("fillBuildConstants") {
        group = "mirai"
        doLast {
            (compileKotlin as KotlinCompile).source.filter { it.name == "MiraiConsoleBuildConstants.kt" }.single()
                .let { file ->
                    file.writeText(
                        file.readText()
                            .replace(
                                Regex("""val buildDate: Instant = Instant.ofEpochSecond\(.*\)""")
                            ) {
                                """val buildDate: Instant = Instant.ofEpochSecond(${
                                    Instant.now().epochSecond
                                })"""
                            }
                            .replace(
                                Regex("""const val versionConst:\s+String\s+=\s+".*"""")
                            ) { """const val versionConst: String = "${project.version}"""" }
                    )
                }
        }
    }
}

setupPublishing("mirai-console")