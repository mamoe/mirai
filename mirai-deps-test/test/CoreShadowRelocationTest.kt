/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
        private const val BigInteger = "com.ionspin.kotlin.bignum.integer.BigInteger"

        fun relocated(string: String): String {
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
            -both(`kt-bignum`)
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
            -both(`kt-bignum`)
            +relocated(`ExternalResource-input`)
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
            +relocated(`ExternalResource-input`)
            +relocated(`kt-bignum`)
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
            +relocated(`kt-bignum`)
        }
        applyCodeFragment(fragment)
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core:$miraiLocalVersion") {
                    exclude("net.mamoe", "mirai-core-api-jvm")
                    exclude("net.mamoe", "mirai-core-utils-jvm")
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
            -`mirai-core-utils`
            -both(`ktor-io`)
            -both(`ktor-client-core`)
            -both(`ktor-client-okhttp`)
            -both(`okhttp3-okhttp`)
            -both(okio)
            -both(`kt-bignum`)
//            +relocated(`ExternalResource-input`) // Will fail with no class def found error because there is no runtime ktor-io
        }
        applyCodeFragment(fragment)
        buildFile.appendText(
            """
            dependencies {
                implementation("net.mamoe:mirai-core-api:$miraiLocalVersion") {
                    exclude("net.mamoe", "mirai-core-utils-jvm")
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
            +relocated(`ExternalResource-input`)
            +relocated(`kt-bignum`)
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
            import java.lang.reflect.Method
            import kotlin.reflect.jvm.kotlinFunction
            import kotlin.test.assertTrue
            import kotlin.test.assertFalse
            
            private val Method.signature: String
                get() = buildString {
                    append(kotlinFunction?.toString())
                    return@buildString
                    append(kotlinFunction?.visibility?.name?.lowercase())
                    append(kotlinFunction?.visibility?.name?.lowercase())
                    append(' ')
                    append(returnType.canonicalName)
                    append(' ')
                    append(name)
                    append('(')
                    for (parameter in parameters) {
                        append(parameter.type.canonicalName)
                        append(' ')
                        append(parameter.name)
                        append(", ")
                    }
                    if (parameterCount != 0) {
                        deleteAt(lastIndex)
                        deleteAt(lastIndex)
                    }
                    append(')')
                }

            class MyTest {
        """.trimIndent()
        ).append("\n").append("\n")

        class ClassTestCase(
            val name: String,
            val qualifiedClassName: String,
        )

        class FunctionTestCase(
            val name: String,
            val qualifiedClassName: String,
            val signature: String,
            val relocated: (FunctionTestCase.() -> FunctionTestCase)? = null,
        )

        val `mirai-core-utils` = ClassTestCase("mirai-core-utils Symbol", "net.mamoe.mirai.utils.Symbol")
        val `ktor-io` = ClassTestCase("ktor-io ByteBufferChannel", ByteBufferChannel)
        val `ktor-client-core` = ClassTestCase("ktor-client-core HttpClient", HttpClient)
        val `ktor-client-okhttp` = ClassTestCase("ktor-client-core OkHttp", KtorOkHttp)
        val `okhttp3-okhttp` = ClassTestCase("okhttp3 OkHttp", OkHttp)
        val okio = ClassTestCase("okio ByteString", OkIO)
        val `kt-bignum` = ClassTestCase("kt-bignum BigInteger", BigInteger)
        val `ExternalResource-input` =
            FunctionTestCase(
                "ExternalResource_input",
                "net.mamoe.mirai.utils.ExternalResource",
                "fun net.mamoe.mirai.utils.ExternalResource.input(): io.ktor.utils.io.core.Input"
            ) original@{
                FunctionTestCase(
                    "relocated ExternalResource_input",
                    "net.mamoe.mirai.utils.ExternalResource",
                    "fun net.mamoe.mirai.utils.ExternalResource.input(): ${relocated("io.ktor.utils.io.core.Input")}"
                ) {
                    this@original
                }
            }

        class RelocatedClassTestCase(
            val testCase: ClassTestCase
        )

        class BothClassTestCase(
            val testCase: ClassTestCase
        )


        private fun appendHasClass(name: String, qualifiedClassName: String) {
            result.append(
                """
                      @Test
                      fun `has ${name}`() {
                        Class.forName("$qualifiedClassName")
                      }
                    """.trimIndent()
            ).append("\n")
        }

        fun appendClassHasMethod(name: String, qualifiedClassName: String, methodSignature: String) {
            result.append(
                """
                      @Test
                      fun `has ${name}`() {
                        val signatures = Class.forName("$qualifiedClassName").declaredMethods.map { it.signature }
                        assertTrue("All signatures: " + signatures.joinToString("\n")) { signatures.any { it == "$methodSignature" } }
                      }
                    """.trimIndent()
            ).append("\n")
        }

        fun appendClassMethodNotFound(name: String, qualifiedClassName: String, methodSignature: String) {
            result.append(
                """
                      @Test
                      fun `has ${name}`() {
                        val signatures = Class.forName("$qualifiedClassName").declaredMethods.map { it.signature }
                        assertFalse("All signatures: " + signatures.joinToString("\n")) { signatures.any { it == "$methodSignature" } }
                      }
                    """.trimIndent()
            ).append("\n")
        }

        private fun appendNotFound(name: String, qualifiedClassName: String) {
            result.append(
                """
                      @Test
                      fun `no ${name}`() {
                        assertThrows<ClassNotFoundException> { Class.forName("$qualifiedClassName") }
                      }
                    """.trimIndent()
            ).append("\n")
        }


        /**
         * Asserts a class exists. Also asserts its relocated class does not exist.
         */
        operator fun FunctionTestCase.unaryPlus() {
            appendClassHasMethod(name, qualifiedClassName, signature)
            relocated?.invoke(this)?.let { inverted ->
                appendClassMethodNotFound(inverted.name, inverted.qualifiedClassName, inverted.signature)
            }
        }

        /**
         * Asserts a class exists. Also asserts its relocated class does not exist.
         */
        operator fun ClassTestCase.unaryPlus() {
            appendHasClass(name, qualifiedClassName)
            appendNotFound("relocated $name", Companion.relocated(qualifiedClassName))
        }

        /**
         * Asserts a class does not exist.
         */
        operator fun ClassTestCase.unaryMinus() {
            appendNotFound(name, qualifiedClassName)
        }


        /**
         * Asserts a relocated class exists. Also asserts the original class does not exist.
         */
        operator fun RelocatedClassTestCase.unaryPlus() {
            this.testCase.run {
                appendHasClass("relocated $name", Companion.relocated(qualifiedClassName))
                appendNotFound(name, qualifiedClassName)
            }
        }

        /**
         * Asserts a relocated class does not exist.
         */
        operator fun RelocatedClassTestCase.unaryMinus() {
            this.testCase.run {
                appendNotFound("relocated $name", Companion.relocated(qualifiedClassName))
            }
        }

        /**
         * Asserts both the class and its relocated one do not exist.
         */
        operator fun BothClassTestCase.unaryMinus() {
            -this.testCase
            -relocated(this.testCase)
        }

        fun relocated(testCase: ClassTestCase): RelocatedClassTestCase = RelocatedClassTestCase(testCase)
        fun relocated(testCase: FunctionTestCase): FunctionTestCase = testCase.relocated!!(testCase)
        fun both(testCase: ClassTestCase) = BothClassTestCase(testCase)

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