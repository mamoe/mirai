/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import kotlin.test.Test

class KotlinTransitiveDependenciesIntegrationTest {
    @Test
    fun `user can override Kotlin plugin version`(@TempDir dir: File) {
        // We're packaging the plugin with kotlin transitive dependencies > 1.4.30
        // This is testing that users are free to use different versions
        val userSpecifiedKotlinPluginVersion = "1.5.21"
        dir.resolve("settings.gradle").writeText("")
        dir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'net.mamoe.mirai-console'
                id 'org.jetbrains.kotlin.jvm' version '$userSpecifiedKotlinPluginVersion'
            }
        """.trimIndent()
        )

        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        GradleRunner.create()
            .withProjectDir(dir)
            .withGradleVersion("7.2")
            .withPluginClasspath()
            .forwardStdOutput(PrintWriter(stdout))
            .forwardStdError(PrintWriter(stderr))
            .withArguments(listOf("dependencies"))
            .build()

        System.out.println(stdout)
        System.err.println(stderr)

        Assertions.assertTrue(
            stdout.toString()
                .contains("org.jetbrains.kotlin:kotlin-compiler-embeddable:${userSpecifiedKotlinPluginVersion}")
        )
    }
}