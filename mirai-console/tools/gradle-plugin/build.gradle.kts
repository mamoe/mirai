/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    groovy
    id("java")
    //signing
    `maven-publish`
}

val integTest = sourceSets.create("integTest")

/**
 * Because we use [compileOnly] for `kotlin-gradle-plugin`, it would be missing
 * in `plugin-under-test-metadata.properties`. Here we inject the jar into TestKit plugin
 * classpath via [PluginUnderTestMetadata] to avoid [NoClassDefFoundError].
 */
val kotlinVersionForIntegrationTest: Configuration by configurations.creating

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("stdlib"))

    implementation("com.google.code.gson:gson:2.8.6")

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api(`jetbrains-annotations`)

    // override vulnerable Log4J version
    // https://blog.gradle.org/log4j-vulnerability
    implementation(`log4j-api`)
    implementation(`log4j-core`)

    testApi(kotlin("test-junit5"))
    testApi(`junit-jupiter-api`)
    testApi(`junit-jupiter-params`)

    "integTestApi"(kotlin("test-junit5"))
    "integTestApi"(`junit-jupiter-api`)
    "integTestApi"(`junit-jupiter-params`)
    "integTestImplementation"(`junit-jupiter-engine`)
    "integTestImplementation"(gradleTestKit())

    kotlinVersionForIntegrationTest(kotlin("gradle-plugin", "1.5.21"))
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(kotlinVersionForIntegrationTest)
}

version = Versions.console
description = "Gradle plugin for Mirai Console"

kotlin {
    explicitApi()
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    testSourceSets(integTest)
    website.set("https://github.com/mamoe/mirai")
    vcsUrl.set("https://github.com/mamoe/mirai")
    plugins {
        create("miraiConsole") {
            id = "net.mamoe.mirai-console"
            displayName = "Mirai Console"
            description = project.description
            implementationClass = "net.mamoe.mirai.console.gradle.MiraiConsoleGradlePlugin"
            tags.set(listOf("framework", "kotlin", "mirai"))
        }
    }
}


val integrationTestTask = tasks.register<Test>("integTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = integTest.output.classesDirs
    classpath = integTest.runtimeClasspath
    mustRunAfter(tasks.test)
}
tasks.check {
    dependsOn(integrationTestTask)
}

tasks {
    val generateBuildConstants by registering {
        group = "mirai"
        doLast {
            projectDir.resolve("src/main/kotlin/VersionConstants.kt").apply { createNewFile() }
                .writeText(
                    projectDir.resolve("src/main/kotlin/VersionConstants.kt.template").readText()
                        .replace("$\$CONSOLE_VERSION$$", Versions.console)
                        .replace("$\$CORE_VERSION$$", Versions.core)
                )
        }
    }

    afterEvaluate {
        getByName("compileKotlin").dependsOn(generateBuildConstants)
    }
}

if (System.getenv("MIRAI_IS_SNAPSHOTS_PUBLISHING")?.toBoolean() == true) {
    configurePublishing("mirai-console-gradle", skipPublicationSetup = true)
}