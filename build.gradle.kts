/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import shadow.configureMppShadow
import java.time.LocalDateTime

buildscript {
    repositories {
        if (System.getProperty("use.maven.local") == "true") {
            mavenLocal()
        }

        mavenCentral()
        gradlePluginPortal()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.atomicFU}")
        classpath("org.jetbrains.dokka:dokka-base:${Versions.dokka}")
    }
}

plugins {
    kotlin("jvm") apply false // version Versions.kotlinCompiler
    kotlin("plugin.serialization") version Versions.kotlinCompiler apply false
    id("com.google.osdetector")
    id("org.jetbrains.dokka") version Versions.dokka
    id("me.him188.kotlin-jvm-blocking-bridge") version Versions.blockingBridge
    id("me.him188.kotlin-dynamic-delegation") version Versions.dynamicDelegation apply false
    id("me.him188.maven-central-publish") version Versions.mavenCentralPublish apply false
    id("com.gradle.plugin-publish") version "1.1.0" apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version Versions.binaryValidator apply false
    id("com.android.library") apply false
    id("de.mannodermaus.android-junit5") version "1.8.2.1" apply false
}

osDetector = osdetector
BuildSrcRootProjectHolder.value = rootProject
BuildSrcRootProjectHolder.lastUpdateTime = System.currentTimeMillis()

analyzes.CompiledCodeVerify.run { registerAllVerifyTasks() }

allprojects {
    group = "net.mamoe"
    version = Versions.project

    repositories {
        if (System.getProperty("use.maven.local") == "true") {
            mavenLocal()
        }

        mavenCentral()
        gradlePluginPortal()
        google()
    }

    preConfigureJvmTarget()
    afterEvaluate {
        configureJvmTarget()
        configureMppShadow()
        configureEncoding()
        configureKotlinTestSettings()
        configureKotlinOptIns()

        if (isKotlinJvmProject) {
            configureFlattenSourceSets()
        }
        configureJarManifest()
        substituteDependenciesUsingExpectedVersion()
    }
}

subprojects {
    afterEvaluate {
        if (project.path == ":mirai-core-api") configureDokka()
        if (project.path == ":mirai-console") configureDokka()
    }
}
rootProject.configureDokka()

tasks.register("cleanExceptIntellij") {
    group = "build"
    allprojects.forEach { proj ->
        if (proj.name != "mirai-console-intellij") {

            // Type mismatch
            // proj.tasks.findByName("clean")?.let(::dependsOn)

            proj.tasks.findByName("clean")?.let { dependsOn(it) }
        }
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

fun Project.configureDokka() {
    val isRoot = this@configureDokka == rootProject
    if (!isRoot) {
        apply(plugin = "org.jetbrains.dokka")
    }

    tasks.withType<org.jetbrains.dokka.gradle.AbstractDokkaTask>().configureEach {
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            this.footerMessage = """Copyright 2019-${
                LocalDateTime.now().year
            } <a href="https://github.com/mamoe">Mamoe Technologies</a> and contributors.
            Source code:
            <a href="https://github.com/mamoe/mirai">GitHub</a>
            """.trimIndent()

            this.customAssets = listOf(
                rootProject.projectDir.resolve("mirai-dokka/frontend/ext.js"),
            )
        }
    }

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            perPackageOption {
                matchingRegex.set("net\\.mamoe\\.mirai\\.*")
                skipDeprecated.set(true)
            }

            for (suppressedPackage in arrayOf(
                """net.mamoe.mirai.internal""",
                """net.mamoe.mirai.internal.message""",
                """net.mamoe.mirai.internal.network""",
                """net.mamoe.mirai.console.internal""",
                """net.mamoe.mirai.console.compiler.common"""
            )) {
                perPackageOption {
                    matchingRegex.set(suppressedPackage.replace(".", "\\."))
                    suppress.set(true)
                }
            }
        }
    }

    if (isRoot) {
        tasks.named<org.jetbrains.dokka.gradle.AbstractDokkaTask>("dokkaHtmlMultiModule").configure {
            outputDirectory.set(
                rootProject.projectDir.resolve("mirai-dokka/pages/snapshot")
            )
        }
    }
}
