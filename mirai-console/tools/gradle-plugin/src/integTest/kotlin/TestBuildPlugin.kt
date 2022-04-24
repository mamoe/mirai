/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DuplicatedCode", "FunctionName")

package net.mamoe.mirai.console.gradle

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestBuildPlugin : AbstractTest() {

    @Test
    @DisplayName("project as normal dependency")
    fun buildWithMultiProjectsAsNormalDependency() {
        settingsFile.appendText(
            """
            include("nested")
        """.trimIndent()
        )
        tempDir.resolve("nested").also { it.mkdirs() }.resolve("build.gradle").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
                id("net.mamoe.mirai-console")
            }
            dependencies {
                api "com.zaxxer:SparseBitSet:1.2"
            }
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )

        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                implementation project(":nested") 
                asNormalDep project(":nested") 
            }
        """.trimIndent()
        )

        gradleRunner()
            .withArguments(":buildPlugin", "--stacktrace", "--info")
            .build()


        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains(":nested") }
        }
    }

    @Test
    @DisplayName("no api extends if using implementation")
    fun buildWithMultiProjectsWithoutApi() {
        settingsFile.appendText(
            """
            include("nested")
        """.trimIndent()
        )
        tempDir.resolve("nested").also { it.mkdirs() }.resolve("build.gradle").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
                id("net.mamoe.mirai-console")
            }
            dependencies {
                api "com.zaxxer:SparseBitSet:1.2"
            }
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )

        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                implementation project(":nested") 
            }
        """.trimIndent()
        )

        gradleRunner()
            .withArguments(":buildPlugin", "--stacktrace", "--info")
            .build()


        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }
        }
    }

    @Test
    @DisplayName("build with multi projects")
    fun buildWithMultiProjects() {
        settingsFile.appendText(
            """
            include("nested")
        """.trimIndent()
        )
        tempDir.resolve("nested").also { it.mkdirs() }.resolve("build.gradle").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
                id("net.mamoe.mirai-console")
            }
            dependencies {
                api "com.zaxxer:SparseBitSet:1.2"
                implementation "com.google.code.gson:gson:2.8.9"
                api "org.slf4j:slf4j-simple:1.7.32"
            }
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )

        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                api project(":nested") 
                shadowLink "org.slf4j:slf4j-simple"
            }
        """.trimIndent()
        )

        gradleRunner()
            .withArguments(":buildPlugin", "dependencies", "--stacktrace", "--info")
            .build()
        checkOutput()
    }

    @Test
    fun `can build plugin`() {
        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                api "com.zaxxer:SparseBitSet:1.2"
                implementation "com.google.code.gson:gson:2.8.9"
                api "org.slf4j:slf4j-simple:1.7.32"
                shadowLink "org.slf4j:slf4j-simple"
            }
        """.trimIndent()
        )
        gradleRunner()
            .withArguments("buildPlugin", "dependencies", "--stacktrace", "--info")
            .build()
        checkOutput()
    }

    @Test
    fun `can build with bom dependencies`() {
        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                implementation platform("com.fasterxml.jackson:jackson-bom:2.12.4")
            }
        """.trimIndent()
        )
        gradleRunner()
            .withArguments("buildPlugin", "dependencies", "--stacktrace", "--info")
            .build()

        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpPrivate.contains("com.fasterxml.jackson:jackson-bom") }
        }
    }

    private fun findJar(): File = tempDir.resolve("build/mirai").listFiles()!!.first { it.name.endsWith(".mirai.jar") }

    private fun checkOutput() {
        val jar = findJar()
        ZipFile(jar).use { zipFile ->

            assertNotNull(zipFile.getEntry("org/slf4j/impl/SimpleLogger.class"))

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertTrue { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertFalse { dpShared.contains("com.google.code.gson:gson") }
            assertFalse { dpShared.contains("org.slf4j:slf4j-simple") }

            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.google.code.gson:gson:2.8.9") }
            assertFalse { dpPrivate.contains("org.slf4j:slf4j-simple") }
        }

    }

}