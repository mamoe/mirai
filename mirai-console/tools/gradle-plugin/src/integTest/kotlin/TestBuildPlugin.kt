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
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestBuildPlugin : AbstractTest() {
    private fun File.wt(text: String) {
        parentFile?.mkdirs()
        writeText(text)
    }

    @Test
    @DisplayName("project as normal dependency")
    fun buildWithMultiProjectsAsNormalDependency() {
        settingsFile.appendText(
            """
            include("nested1")
            include("nested0")
        """.trimIndent()
        )
        tempDir.resolve("nested1/build.gradle").wt(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
            }
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )
        tempDir.resolve("nested0/build.gradle").wt(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
                id("net.mamoe.mirai-console")
            }
            dependencies {
                api project(":nested1") 
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
                implementation project(":nested0") 
                asNormalDep project(":nested0")
            }
        """.trimIndent()
        )
        tempDir.resolve("nested0/src/main/kotlin/test.kt").wt(
            """
            package nested
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("nested1/src/main/kotlin/test.kt").wt(
            """
            package nested1
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("src/main/kotlin/test.kt").wt(
            """
            package thetop
            public class TestClass
        """.trimIndent()
        )

        runGradle(":buildPlugin", "--stacktrace", "--info")


        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains(":nested0") }
            assertTrue { dpPrivate.contains(":nested1") }
            assertNotNull(zipFile.getEntry("thetop/TestClass.class"))
            assertNull(zipFile.getEntry("nested/TestClass.class"))
            assertNull(zipFile.getEntry("nested1/TestClass.class"))
        }
    }

    @Test
    @DisplayName("project as normal dependency 2")
    fun buildWithMultiProjectsAsNormalDependency2() {
        settingsFile.appendText(
            """
            include("nested1")
            include("nested0")
        """.trimIndent()
        )
        tempDir.resolve("nested1/build.gradle").wt(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
            }
            repositories {
                mavenCentral()
            }
        """.trimIndent()
        )
        tempDir.resolve("nested0/build.gradle").wt(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm")
                id("net.mamoe.mirai-console")
            }
            dependencies {
                api project(":nested1") 
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
                implementation project(":nested0") 
                asNormalDep project(":nested1")
            }
        """.trimIndent()
        )
        tempDir.resolve("nested0/src/main/kotlin/test.kt").wt(
            """
            package nested
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("nested1/src/main/kotlin/test.kt").wt(
            """
            package nested1
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("src/main/kotlin/test.kt").wt(
            """
            package thetop
            public class TestClass
        """.trimIndent()
        )

        runGradle(":buildPlugin", "--stacktrace", "--info")


        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }
            assertFalse { dpPrivate.contains(":nested0") }
            assertTrue { dpPrivate.contains(":nested1") }
            assertNotNull(zipFile.getEntry("thetop/TestClass.class"))
            assertNotNull(zipFile.getEntry("nested/TestClass.class"))
            assertNull(zipFile.getEntry("nested1/TestClass.class"))
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
        tempDir.resolve("nested/build.gradle").wt(
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

        tempDir.resolve("nested/src/main/kotlin/test.kt").wt(
            """
            package nested
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("src/main/kotlin/test.kt").wt(
            """
            package thetop
            public class TestClass
        """.trimIndent()
        )

        runGradle(":buildPlugin", "--stacktrace", "--info")


        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }
            val dpShared = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-shared.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpShared.contains("com.zaxxer:SparseBitSet:1.2") }
            assertTrue { dpPrivate.contains("com.zaxxer:SparseBitSet:1.2") }

            assertNotNull(zipFile.getEntry("thetop/TestClass.class"))
            assertNotNull(zipFile.getEntry("nested/TestClass.class"))
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
        tempDir.resolve("nested/build.gradle").wt(
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

        tempDir.resolve("nested/src/main/kotlin/test.kt").wt(
            """
            package nested
            public class TestClass
        """.trimIndent()
        )
        tempDir.resolve("src/main/kotlin/test.kt").wt(
            """
            package thetop
            public class TestClass
        """.trimIndent()
        )

        runGradle(":buildPlugin", "dependencies", "--stacktrace", "--info")
        checkOutput()

        ZipFile(findJar()).use { zipFile ->
            assertNotNull(zipFile.getEntry("thetop/TestClass.class"))
            assertNotNull(zipFile.getEntry("nested/TestClass.class"))
        }
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
        runGradle("buildPlugin", "dependencies", "--stacktrace", "--info")
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
        runGradle("buildPlugin", "dependencies", "--stacktrace", "--info")

        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpPrivate.contains("com.fasterxml.jackson:jackson-bom") }
        }
    }

    @Test
    fun `can build with bom dependencies 2`() {
        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                implementation "com.fasterxml.jackson.core:jackson-annotations:2.12.4"
            }
        """.trimIndent()
        )
        runGradle("buildPlugin", "dependencies", "--stacktrace", "--info")

        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpPrivate.contains("com.fasterxml.jackson:jackson-bom") }
            assertTrue { dpPrivate.contains("com.fasterxml.jackson.core:jackson-annotations") }
        }
    }

    @Test
    @DisplayName("Ktor 2.x available")
    fun `ktor 2_x`() {
        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                implementation "io.ktor:ktor-client-core:2.0.0"
            }
        """.trimIndent()
        )
        runGradle("buildPlugin", "dependencies", "--stacktrace", "--info")

        ZipFile(findJar()).use { zipFile ->

            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }

            assertTrue { dpPrivate.contains("io.ktor:ktor-client-core:2.0.0") }
            assertTrue { dpPrivate.contains("io.ktor:ktor-client-core-jvm:2.0.0") }
        }
    }

    @Test
    @DisplayName("can shadow special libraries that another used")
    fun issue2070() {
        tempDir.resolve("build.gradle").appendText(
            """
            dependencies {
                 implementation("cn.hutool:hutool-extra:5.8.2")
                 shadowLink("cn.hutool:hutool-core")
            }
        """.trimIndent()
        )
        runGradle("buildPlugin", "dependencies", "--stacktrace", "--info")
        ZipFile(findJar()).use { zipFile ->
            assertNotNull(zipFile.getEntry("cn/hutool/core/annotation/Alias.class"))


            val dpPrivate = zipFile.getInputStream(
                zipFile.getEntry("META-INF/mirai-console-plugin/dependencies-private.txt")
            ).use { it.readBytes().decodeToString() }

            assertFalse { dpPrivate.contains("hutool-core") }
        }

    }

    private fun findJar(): File = tempDir.resolve("build/mirai").listFiles()!!.first { it.name.endsWith(".mirai2.jar") }

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
            assertFalse { dpPrivate.contains("io.ktor") }
        }

    }

}