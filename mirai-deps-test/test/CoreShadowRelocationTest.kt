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
import kotlin.test.assertTrue

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
        val fragment = buildTestCases {
            +relocated(`ktor-io`)
            -both(`ktor-client-core`)
            -both(`ktor-client-okhttp`)
            -both(`okhttp3-okhttp`)
            -both(okio)
        }
        applyCodeFragment(fragment)
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
    fun `test mirai-core-api with transitive mirai-core-utils`() {
        val fragment = buildTestCases {
            +relocated(`ktor-io`)
            -both(`ktor-client-core`)
            -both(`ktor-client-okhttp`)
            -both(`okhttp3-okhttp`)
            -both(okio)
        }
        applyCodeFragment(fragment)
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
    fun `test mirai-core with transitive mirai-core-api and mirai-core-utils`() {
        val fragment = buildTestCases {
            +relocated(`ktor-io`)
            +relocated(`ktor-client-core`)
            +relocated(`ktor-client-okhttp`)
            +relocated(`okhttp3-okhttp`)
            +relocated(okio)
        }
        applyCodeFragment(fragment)
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
    fun `test mirai-core without transitive mirai-core-api and mirai-core-utils`() {
        val fragment = buildTestCases {
            -both(`ktor-io`)
            +relocated(`ktor-client-core`)
            +relocated(`ktor-client-okhttp`)
            +relocated(`okhttp3-okhttp`)
            +relocated(okio)
        }
        applyCodeFragment(fragment)
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
    fun `test mirai-core-api without transitive mirai-core-utils`() {
        val fragment = buildTestCases {
            -both(`ktor-io`)
            -both(`ktor-client-core`)
            -both(`ktor-client-okhttp`)
            -both(`okhttp3-okhttp`)
            -both(okio)
        }
        applyCodeFragment(fragment)
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
    fun `test mirai-core-all`() {
        val fragment = buildTestCases {
            +relocated(`ktor-io`)
            +relocated(`ktor-client-core`)
            +relocated(`ktor-client-okhttp`)
            +relocated(`okhttp3-okhttp`)
            +relocated(okio)
        }
        applyCodeFragment(fragment)

        // mirai-core-all-2.99.0-deps-test-all.jar
        val miraiCoreAllJar =
            mavenLocalDir.resolve("net/mamoe/mirai-core-all/$miraiLocalVersion/mirai-core-all-$miraiLocalVersion-all.jar")
        val path = miraiCoreAllJar.absolutePath.replace("\\", "/") // overcome string escape in source files.
        assertTrue("'$path' does not exist") { miraiCoreAllJar.exists() }

        buildFile.appendText(
            """
            dependencies {
                implementation(fileTree("$path"))
            }
        """.trimIndent()
        )
        runGradle("check")
    }


    @Suppress("PropertyName")
    private class TestBuilder {
        private val result = StringBuilder(
            """
            package test
            import org.junit.jupiter.api.*
            class MyTest {
        """.trimIndent()
        ).append("\n").append("\n")

        class TestCase(
            val name: String,
            val qualifiedClassName: String,
        )

        val `ktor-io` = TestCase("ktor-io ByteBufferChannel", ByteBufferChannel)
        val `ktor-client-core` = TestCase("ktor-client-core HttpClient", HttpClient)
        val `ktor-client-okhttp` = TestCase("ktor-client-core OkHttp", KtorOkHttp)
        val `okhttp3-okhttp` = TestCase("okhttp3 OkHttp", OkHttp)
        val okio = TestCase("okio ByteString", OkIO)

        class Relocated(
            val testCase: TestCase
        )

        class Both(
            val testCase: TestCase
        )


        private fun appendHas(name: String, qualifiedClassName: String) {
            result.append(
                """
                      @Test
                      fun `has ${name}`() {
                        Class.forName("$qualifiedClassName")
                      }
                    """.trimIndent()
            ).append("\n")
        }

        private fun appendNotFound(name: String, qualifiedClassName: String) {
            result.append(
                """
                      @Test
                      fun `no relocated ${name}`() {
                        assertThrows<ClassNotFoundException> { Class.forName("$qualifiedClassName") }
                      }
                    """.trimIndent()
            ).append("\n")
        }


        /**
         * Asserts a class exists. Also asserts its relocated class does not exist.
         */
        operator fun TestCase.unaryPlus() {
            appendHas(name, qualifiedClassName)
            appendNotFound("relocated $name", Companion.relocated(qualifiedClassName))
        }

        /**
         * Asserts a class does not exist.
         */
        operator fun TestCase.unaryMinus() {
            appendNotFound(name, qualifiedClassName)
        }


        /**
         * Asserts a relocated class exists. Also asserts the original class does not exist.
         */
        operator fun Relocated.unaryPlus() {
            this.testCase.run {
                appendHas("relocated $name", Companion.relocated(qualifiedClassName))
                appendNotFound(name, qualifiedClassName)
            }
        }

        /**
         * Asserts a relocated class does not exist.
         */
        operator fun Relocated.unaryMinus() {
            this.testCase.run {
                appendNotFound("relocated $name", Companion.relocated(qualifiedClassName))
            }
        }

        /**
         * Asserts both the class and its relocated one do not exist.
         */
        operator fun Both.unaryMinus() {
            -this.testCase
            -relocated(this.testCase)
        }

        fun relocated(testCase: TestCase): Relocated = Relocated(testCase)
        fun both(testCase: TestCase) = Both(testCase)

        fun build(): String = result.append("\n}\n").toString()
    }

    private inline fun buildTestCases(action: TestBuilder.() -> Unit): String {
        return TestBuilder().apply(action).build()
    }


    private fun applyCodeFragment(fragment: String) {
        println("Applying code fragment: \n\n$fragment\n\n\n===========End of Fragment===========")
        testDir.resolve("test.kt").writeText(fragment)
    }
}