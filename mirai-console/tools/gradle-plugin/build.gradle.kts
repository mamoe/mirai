/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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


    testApi(kotlin("test-junit5"))
    testApi("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testApi("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")

    "integTestApi"(kotlin("test-junit5"))
    "integTestApi"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    "integTestApi"("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
    "integTestImplementation"("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
//    "integTestImplementation"("org.spockframework:spock-core:1.3-groovy-2.5")
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

pluginBundle {
    website = "https://github.com/mamoe/mirai"
    vcsUrl = "https://github.com/mamoe/mirai"
    tags = listOf("framework", "kotlin", "mirai")
}

gradlePlugin {
    testSourceSets(integTest)
    plugins {
        create("miraiConsole") {
            id = "net.mamoe.mirai-console"
            displayName = "Mirai Console"
            description = project.description
            implementationClass = "net.mamoe.mirai.console.gradle.MiraiConsoleGradlePlugin"
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
    configurePublishing("mirai-console-gradle")
}