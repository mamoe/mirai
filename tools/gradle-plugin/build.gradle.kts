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
    groovy
    id("java")
    //signing
    `maven-publish`

    id("com.github.johnrengelman.shadow")
}

val integTest = sourceSets.create("integTest")

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    api(kotlin("gradle-plugin-api").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    api(kotlin("gradle-plugin").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    compileOnly(kotlin("stdlib"))

    implementation("com.google.code.gson:gson:2.8.6")

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api(`jetbrains-annotations`)
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")


    testApi(kotlin("test-junit5"))
    testApi("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testApi("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")

    "integTestApi"(kotlin("test-junit5"))
    "integTestApi"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    "integTestApi"("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
    "integTestImplementation"("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
//    "integTestImplementation"("org.spockframework:spock-core:1.3-groovy-2.5")
    "integTestImplementation"(gradleTestKit())
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

kotlin.target.compilations.all {
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
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
    val compileKotlin by getting {}

    val fillBuildConstants by registering {
        group = "mirai"
        doLast {
            projectDir.resolve("src").walk().filter { it.name == "VersionConstants.kt" }.single()
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
