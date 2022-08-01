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

class CoreShadowRelocationTest : AbstractTest() {
    @Test
    @EnabledIf("isMiraiLocalAvailable", disabledReason = REASON_LOCAL_ARTIFACT_NOT_AVAILABLE)
    fun `test OkHttp filtered out`() {
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
                implementation("net.mamoe:mirai-core:$miraiLocalVersion")
            }
        """.trimIndent()
        )
        runGradle("check")
    }
}