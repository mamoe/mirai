/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.gradle

import kotlin.Pair
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

abstract class AbstractTest {
    @TempDir
    public File tempDir
    File buildFile
    File settingsFile
    File propertiesFile

    private static Pair<String, Integer> getProxy() {
        if (System.getenv("user.name") == "Him188") new Pair<String, Integer>("127.0.0.1", 7890)
        else null
    }

    def gradleRunner() {
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


        propertiesFile = new File(tempDir, "gradle.properties")
        propertiesFile.delete()
        def proxy = getProxy()
        if (proxy != null) propertiesFile << """
            |systemProp.http.proxyHost=${proxy.first}
            |systemProp.http.proxyPort=${proxy.second}
            |systemProp.https.proxyHost=${proxy.first}
            |systemProp.https.proxyPort=${proxy.second}
        """.stripMargin()


//        buildFile = new File(tempDir, "build.gradle")
//        buildFile.delete()
//        buildFile << """
//            plugins {
//                id 'org.jetbrains.kotlin.jvm' version '1.4.32'
//                id 'net.mamoe.mirai-console'
//            }
//            repositories {
//                mavenCentral()
//            }
//        """


        buildFile = new File(tempDir, "build.gradle.kts")
        buildFile.delete()
        buildFile << """
            plugins {
                kotlin("jvm") version "1.4.30"
                id("net.mamoe.mirai-console")
            }
            repositories {
                mavenCentral()
            }
        """
    }

    @AfterEach
    void cleanup() {
        tempDir.deleteDir()
    }
}
