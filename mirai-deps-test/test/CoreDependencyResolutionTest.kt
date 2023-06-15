/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.deps.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf

class CoreDependencyResolutionTest : AbstractTest() {
    private val testCode = """
                package test
                @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPERIMENTAL_API_USAGE")
                fun main () {
                    println(net.mamoe.mirai.BotFactory)
                    println(net.mamoe.mirai.Mirai)
                    println(net.mamoe.mirai.internal.testHttpClient())
                }
            """.trimIndent()

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM root from Kotlin JVM`() {
        mainSrcDir.resolve("main.kt").writeText(testCode)
        buildFile.writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "$kotlinVersion"
            }
            repositories {
                mavenCentral()
                mavenLocal()
            }
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion")
            }
            kotlin.sourceSets.all {
                languageSettings.optIn("net.mamoe.mirai.utils.TestOnly")
            }
        """.trimIndent()
        )
        runGradle("build")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM from Kotlin JVM`() {
        mainSrcDir.resolve("main.kt").writeText(testCode)
        buildFile.writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "$kotlinVersion"
            }
            repositories {
                mavenCentral()
                mavenLocal()
            }
            dependencies {
                implementation("net.mamoe:mirai-core-jvm:$miraiLocalVersion")
            }
            kotlin.sourceSets.all {
                languageSettings.optIn("net.mamoe.mirai.utils.TestOnly")
            }
        """.trimIndent()
        )
        runGradle("build")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM and Native from common`() {
        commonMainSrcDir.resolve("main.kt").writeText(testCode)
        buildFile.writeText(
            """
            |import org.apache.tools.ant.taskdefs.condition.Os
            |import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
            |
            |plugins {
            |    id("org.jetbrains.kotlin.multiplatform") version "$kotlinVersion"
            |}
            |repositories {
            |    mavenCentral()
            |    mavenLocal()
            |}
            |kotlin {
            |    targets {
            |        jvm()
            |    }
            |    sourceSets {
            |        val commonMain by getting {
            |            dependencies {
            |               api("net.mamoe:mirai-core:$miraiLocalVersion")
            |            }
            |        }
            |    }
            |}
            |kotlin.sourceSets.all {
            |    languageSettings.optIn("net.mamoe.mirai.utils.TestOnly")
            |}
        """.trimMargin()
        )

        runGradle("build")
    }
}