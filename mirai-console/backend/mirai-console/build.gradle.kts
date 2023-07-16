/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnusedImport")

import BinaryCompatibilityConfigurator.configureBinaryValidator
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    optInForAllSourceSets("kotlinx.serialization.ExperimentalSerializationApi")

    optInForTestSourceSets("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
    optInForTestSourceSets("net.mamoe.mirai.console.ConsoleExperimentalApi")
    optInForTestSourceSets("net.mamoe.mirai.console.ConsoleInternalApi")
}


// 搜索 mirai-console (包括 core) 直接使用并对外公开的类 (api)
configurations.create("consoleRuntimeClasspath").attributes {
    attribute(
        Usage.USAGE_ATTRIBUTE,
        project.objects.named(Usage::class.java, Usage.JAVA_API)
    )
    attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
}.also { consoleRuntimeClasspath ->
    consoleRuntimeClasspath.exclude(group = "io.ktor")
}

dependencies {
    compileAndTestRuntime(project(":mirai-core-api"))
    compileAndTestRuntime(project(":mirai-core-utils"))
    compileAndTestRuntime(`kotlin-stdlib-jdk8`)

    compileAndTestRuntime(`kotlinx-atomicfu`)
    compileAndTestRuntime(`kotlinx-coroutines-core`)
    compileAndTestRuntime(`kotlinx-serialization-core`)
    compileAndTestRuntime(`kotlinx-serialization-json`)
    compileAndTestRuntime(`kotlin-reflect`)

    implementation(project(":mirai-console-compiler-annotations"))

    smartImplementation(`yamlkt`)
    smartImplementation(`jetbrains-annotations`)
    smartImplementation(`caller-finder`)
    smartImplementation(`maven-resolver-api`)
    smartImplementation(`maven-resolver-provider`)
    smartImplementation(`maven-resolver-impl`)
    smartImplementation(`maven-resolver-connector-basic`)
    smartImplementation(`maven-resolver-transport-http`)
    smartImplementation(`slf4j-api`)
    smartImplementation(`kotlin-jvm-blocking-bridge`)
    smartImplementation(`kotlin-dynamic-delegation`)
    smartApi(`kotlinx-coroutines-jdk8`)

    testApi(project(":mirai-core"))
    testApi(`kotlin-stdlib-jdk8`)
    testApi(`kotlinx-coroutines-test`)

    "consoleRuntimeClasspath"(project)
    "consoleRuntimeClasspath"(project(":mirai-core-utils"))
    "consoleRuntimeClasspath"(project(":mirai-core-api"))
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

tasks.withType<Test> {
    this.jvmArgs("-Dmirai.console.skip-end-user-readme")
}

tasks.getByName("compileKotlin").dependsOn(
    DependencyDumper.registerDumpTaskKtSrc(
        project,
        "consoleRuntimeClasspath",
        project.file("src/internal/MiraiConsoleBuildDependencies.kt"),
        "net.mamoe.mirai.console.internal.MiraiConsoleBuildDependencies"
    )
)

val graphDump = DependencyDumper.registerDumpClassGraph(project, "consoleRuntimeClasspath", "allclasses.txt")
tasks.named<Copy>("processResources").configure {
    from(graphDump) {
        into("META-INF/mirai-console")
    }
}

configurePublishing("mirai-console")
configureBinaryValidator(null)