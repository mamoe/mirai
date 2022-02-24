/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractTest {
    @JvmField
    @TempDir
    var tempDirField: File? = null

    val tempDir: File get() = tempDirField!!

    lateinit var buildFile: File
    lateinit var settingsFile: File
    lateinit var propertiesFile: File


    fun gradleRunner(): GradleRunner {
        println(PluginUnderTestMetadataReading.readImplementationClasspath())
        return GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withGradleVersion("7.2")
            .forwardOutput()
            .withEnvironment(System.getenv())
    }

    @BeforeEach
    fun setup() {
        println("Temp path is " + tempDir.absolutePath)

        settingsFile = File(tempDir, "settings.gradle")
        settingsFile.delete()
        settingsFile.writeText(
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
        """
        )

        File(tempDir, "gradle.properties").apply {
            delete()
            writeText("""
                org.gradle.daemon=false
                org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
            """.trimIndent())
        }

        buildFile = File(tempDir, "build.gradle")
        buildFile.delete()
        buildFile.writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "1.6.0"
                id("net.mamoe.mirai-console")
            }
            
            repositories {
                mavenCentral()
            }
        """
        )


//        buildFile = new File(tempDir, "build.gradle.kts")
//        buildFile.delete()
//        buildFile << """
//            plugins {
//                kotlin("jvm") version "1.4.30"
//                id("net.mamoe.mirai-console")
//            }
//            repositories {
//                mavenCentral()
//            }
//        """

    }

    @JvmField
    @RegisterExtension
    internal val after: AfterEachCallback = AfterEachCallback { context ->
        if (context.executionException.isPresent) {
            val inst = context.requiredTestInstance as AbstractTest
            println("====================== build.gradle ===========================")
            println(inst.tempDir.resolve("build.gradle").readText())
            println("==================== settings.gradle ==========================")
            println(inst.tempDir.resolve("settings.gradle").readText())
        }
    }
}