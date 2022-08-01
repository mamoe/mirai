/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM root from Kotlin JVM`() {
        mainSrcDir.resolve("main.kt").writeText(
            """
            package test
            fun main () {
                println(net.mamoe.mirai.BotFactory)
            }
        """.trimIndent()
        )
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
        """.trimIndent()
        )
        runGradle("build")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM from Kotlin JVM`() {
        mainSrcDir.resolve("main.kt").writeText(
            """
            package test
            fun main () {
                println(net.mamoe.mirai.BotFactory)
            }
        """.trimIndent()
        )
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
        """.trimIndent()
        )
        runGradle("build")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve JVM and Native from common`() {
        commonMainSrcDir.resolve("main.kt").writeText(
            """
            package test
            fun main () {
                println(net.mamoe.mirai.BotFactory)
            }
        """.trimIndent()
        )
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
            |        val nativeMainSets = mutableListOf<KotlinSourceSet>()
            |        val nativeTestSets = mutableListOf<KotlinSourceSet>()
            |        when {
            |            Os.isFamily(Os.FAMILY_MAC) -> if (Os.isArch("aarch64")) macosArm64("native") else macosX64("native")
            |            Os.isFamily(Os.FAMILY_WINDOWS) -> mingwX64("native")
            |            else -> linuxX64("native")
            |        }
            |    }
            |    sourceSets {
            |        val commonMain by getting {
            |            dependencies {
            |               api("net.mamoe:mirai-core:$miraiLocalVersion")
            |            }
            |        }
            |    }
            |}
        """.trimMargin()
        )

        runGradle("build")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test resolve Native from common`() {
        nativeMainSrcDir.resolve("main.kt").writeText(
            """
            package test
            fun main () {
                println(net.mamoe.mirai.BotFactory)
            }
        """.trimIndent()
        )
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
            |        val nativeMainSets = mutableListOf<KotlinSourceSet>()
            |        val nativeTestSets = mutableListOf<KotlinSourceSet>()
            |        when {
            |            Os.isFamily(Os.FAMILY_MAC) -> if (Os.isArch("aarch64")) macosArm64("native") else macosX64("native")
            |            Os.isFamily(Os.FAMILY_WINDOWS) -> mingwX64("native")
            |            else -> linuxX64("native")
            |        }
            |    }
            |    sourceSets {
            |        val nativeMain by getting {
            |            dependencies {
            |               api("net.mamoe:mirai-core:$miraiLocalVersion")
            |            }
            |        }
            |    }
            |}
        """.trimMargin()
        )

        runGradle("build")
    }
}