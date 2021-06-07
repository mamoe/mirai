/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.gradle


import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

abstract class AbstractTest {
    @TempDir
    public File tempDir
    File buildFile
    File settingsFile
    File propertiesFile

    GradleRunner gradleRunner() {
        println(PluginUnderTestMetadataReading.readImplementationClasspath())
        GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .forwardOutput()
                .withEnvironment(System.getenv())
    }

    @BeforeEach
    void setup() {
        println('Temp path is ' + tempDir.absolutePath)

        settingsFile = new File(tempDir, "settings.gradle")
        settingsFile.delete()
        settingsFile << """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
        """

        buildFile = new File(tempDir, "build.gradle")
        buildFile.delete()
        buildFile << """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "1.5.10"
                id("net.mamoe.mirai-console")
            }
            
            repositories {
                mavenCentral()
            }
        """


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

    @AfterEach
    void cleanup() {
        tempDir.deleteDir()
    }
}
