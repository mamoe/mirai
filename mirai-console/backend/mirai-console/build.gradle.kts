/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

import BinaryCompatibilityConfigurator.configureBinaryValidator
import BinaryCompatibilityConfigurator.configureBinaryValidators
import java.time.*
import java.time.format.*

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("me.him188.kotlin-dynamic-delegation")
}

version = Versions.console
description = "Mirai Console Backend"

kotlin {
    explicitApiWarning()
}

configurations.register("consoleRuntimeClasspath")

dependencies {
    compileAndTestRuntime(project(":mirai-core-api"))
    compileAndTestRuntime(project(":mirai-core-utils"))
    compileAndTestRuntime(`kotlin-stdlib-jdk8`)

    compileAndTestRuntime(`kotlinx-atomicfu-jvm`)
    compileAndTestRuntime(`kotlinx-coroutines-core-jvm`)
    compileAndTestRuntime(`kotlinx-serialization-core-jvm`)
    compileAndTestRuntime(`kotlinx-serialization-json-jvm`)
    compileAndTestRuntime(`kotlin-reflect`)

    implementation(project(":mirai-console-compiler-annotations"))

    smartImplementation(`yamlkt-jvm`)
    smartImplementation(`jetbrains-annotations`)
    smartImplementation(`caller-finder`)
    smartImplementation(`maven-resolver-api`)
    smartImplementation(`maven-resolver-provider`)
    smartImplementation(`maven-resolver-impl`)
    smartImplementation(`maven-resolver-connector-basic`)
    smartImplementation(`maven-resolver-transport-http`)
    smartApi(`kotlinx-coroutines-jdk8`)

    testApi(project(":mirai-core"))
    testApi(`kotlin-stdlib-jdk8`)

    "consoleRuntimeClasspath"(project)
    "consoleRuntimeClasspath"(project(":mirai-core"))
}

tasks {
    val task = register("generateBuildConstants") {
        group = "mirai"
        doLast {
            val now = Instant.now()
            project.file("src/internal/MiraiConsoleBuildConstants.kt").writeText(
                project.file("src/internal/MiraiConsoleBuildConstants.kt.template").readText()
                    .replace("GENERATION_DATE", now.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .replace("BUILD_DATE", now.epochSecond.toString())
                    .replace("VERSION_CONSTANT", project.version.toString())
            )
        }
    }

    afterEvaluate {
        getByName("compileKotlin").dependsOn(task)
    }
}

tasks.getByName("compileKotlin").dependsOn(
    DependencyDumper.registerDumpTaskKtSrc(
        project,
        "consoleRuntimeClasspath",
        project.file("src/internal/MiraiConsoleBuildDependencies.kt"),
        "net.mamoe.mirai.console.internal.MiraiConsoleBuildDependencies"
    )
)

configurePublishing("mirai-console")
configureBinaryValidator(null)