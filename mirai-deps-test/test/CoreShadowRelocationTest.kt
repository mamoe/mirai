/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.deps.test

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf

/**
 * 为每个模块测试 relocated 的依赖是否存在于运行时
 */
class CoreShadowRelocationTest : AbstractTest() {
    companion object {
        private const val ByteBufferChannel = "io.ktor.utils.io.ByteBufferChannel"
        private const val HttpClient = "io.ktor.client.HttpClient"
        private const val KtorOkHttp = "io.ktor.client.engine.okhttp.OkHttp"
        private const val OkHttp = "okhttp3.OkHttp"
        private const val OkIO = "okio.ByteString"

        private fun relocated(string: String): String {
            return "net.mamoe.mirai.internal.deps.$string"
        }
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test mirai-core-utils`() {
        @Language("kt")
        val fragment = """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `has relocated ktor-io`() {
                Class.forName("${relocated(ByteBufferChannel)}")
              }
              @Test
              fun `no relocated ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(HttpClient)}") }
              }
              @Test
              fun `no ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("$HttpClient") }
              }
              @Test
              fun `no relocated okhttp`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(KtorOkHttp)}") }
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(OkHttp)}") }
              }
            }
        """.trimIndent()
        testDir.resolve("test.kt").writeText(fragment)
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
    fun `test mirai-core-api`() {
        @Language("kt")
        val fragment = """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `has relocated ktor-io`() {
                Class.forName("${relocated(ByteBufferChannel)}")
              }
              @Test
              fun `no relocated ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(HttpClient)}") }
              }
              @Test
              fun `no ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("$HttpClient") }
              }
              @Test
              fun `no relocated okhttp`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(KtorOkHttp)}") }
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(OkHttp)}") }
              }
            }
        """.trimIndent()
        testDir.resolve("test.kt").writeText(fragment)
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core-api:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test mirai-core`() {
        @Language("kt")
        val fragment = """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `has relocated ktor-io`() {
                Class.forName("${relocated(ByteBufferChannel)}")
              }
              @Test
              fun `has relocated ktor`() {
                Class.forName("${relocated(HttpClient)}")
              }
              @Test
              fun `no ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("$HttpClient") }
              }
              @Test
              fun `has relocated ktor okhttp`() {
                Class.forName("${relocated(KtorOkHttp)}")
              }
              @Test
              fun `has relocated okhttp`() {
                Class.forName("${relocated(OkHttp)}")
              }
              @Test
              fun `has relocated okio`() {
                Class.forName("${relocated(OkIO)}")
              }
            }
        """.trimIndent()
        testDir.resolve("test.kt").writeText(fragment)
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }

    // ktor-io is shadowed into runtime in mirai-core-utils. So without mirai-core-utils,
    // we should expect no relocated ktor-io found, otherwise there will be duplicated classes on Android.
    // https://github.com/mamoe/mirai/issues/2291
    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `no ktor-io in mirai-core jar`() {
        @Language("kt")
        val fragment = """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `no relocated ktor-io`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(ByteBufferChannel)}") }
              }
              @Test
              fun `has relocated ktor`() {
                Class.forName("${relocated(HttpClient)}")
              }
              @Test
              fun `no ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("$HttpClient") }
              }
              @Test
              fun `has relocated ktor okhttp`() {
                Class.forName("${relocated(KtorOkHttp)}")
              }
              @Test
              fun `has relocated okhttp`() {
                Class.forName("${relocated(OkHttp)}")
              }
              @Test
              fun `has relocated okio`() {
                Class.forName("${relocated(OkIO)}")
              }
            }
        """.trimIndent()
        testDir.resolve("test.kt").writeText(fragment)
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
    fun `no ktor-io in mirai-core-api jar`() {
        @Language("kt")
        val fragment = """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
              @Test
              fun `no relocated ktor-io`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(ByteBufferChannel)}") }
              }
              @Test
              fun `no relocated ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(HttpClient)}") }
              }
              @Test
              fun `no ktor`() {
                assertThrows<ClassNotFoundException> { Class.forName("$HttpClient") }
              }
              @Test
              fun `no relocated ktor okhttp`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(KtorOkHttp)}") }
              }
              @Test
              fun `no relocated okhttp`() {
                assertThrows<ClassNotFoundException> { Class.forName("${relocated(OkHttp)}") }
              }
              @Test
              fun `no relocated okio`() {
                Class.forName("${relocated(OkIO)}")
              }
            }
        """.trimIndent()
        testDir.resolve("test.kt").writeText(fragment)
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core-api:$miraiLocalVersion") {
                    exclude("net.mamoe", "mirai-core-utils")
                }
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