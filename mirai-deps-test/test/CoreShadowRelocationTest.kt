/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.deps.test

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.condition.EnabledIf
import java.util.stream.Stream
import kotlin.streams.asStream

class CoreShadowRelocationTest : AbstractTest() {
    @TestFactory
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test OkHttp filtered out`(): Stream<DynamicTest> = DynamicTest.stream(
        sequenceOf("mirai-core-utils", "mirai-core-api", "mirai-core").asStream(),
        { it }
    ) { name ->
        setup() // It is automatically done for every Test method but we use TestFactory here
        testDir.resolve("test.kt").writeText(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `test base dependency`() {
                assertThrows<ClassNotFoundException> {
                  Class.forName("io.ktor.client.engine.okhttp.OkHttp")
                }
              }
              @Test
              fun `test transitive dependency`() {
                assertThrows<ClassNotFoundException> {
                  Class.forName("okhttp3.OkHttpClient")
                }
              }
            }
        """.trimIndent()
        )
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:$name:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    // https://github.com/mamoe/mirai/issues/2291
    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `no duplicated class when dependency shared across modules`() {
        testDir.resolve("test.kt").writeText(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `test base dependency`() {
                assertThrows<ClassNotFoundException> {
                  Class.forName("net.mamoe.mirai.internal.deps.io.ktor.utils.io.ByteBufferChannel") // should only present in mirai-core-utils
                  Class.forName("net.mamoe.mirai.internal.deps.okhttp3.OkHttpClient")
                }
              }
            }
        """.trimIndent()
        )
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion") {
                    exclude("net.mamoe", "mirai-core-api")
                    exclude("net.mamoe", "mirai-core-utils")
                }
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `relocated ktor presents in mirai-core-utils`() {
        testDir.resolve("test.kt").writeText(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `test base dependency`() {
                Class.forName("net.mamoe.mirai.internal.deps.io.ktor.utils.io.ByteBufferChannel")
              }
            }
        """.trimIndent()
        )
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core-utils:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `relocated ktor presents transitively in mirai-core`() {
        testDir.resolve("test.kt").writeText(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `test base dependency`() {
                Class.forName("net.mamoe.mirai.internal.deps.io.ktor.utils.io.ByteBufferChannel")
                Class.forName("net.mamoe.mirai.internal.deps.okhttp3.OkHttpClient")
              }
            }
        """.trimIndent()
        )
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `relocated ktor presents in mirai-core-all`() {
        testDir.resolve("test.kt").writeText(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `test base dependency`() {
                Class.forName("net.mamoe.mirai.internal.deps.io.ktor.utils.io.ByteBufferChannel")
                Class.forName("net.mamoe.mirai.internal.deps.okhttp3.OkHttpClient")
              }
            }
        """.trimIndent()
        )
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }
}