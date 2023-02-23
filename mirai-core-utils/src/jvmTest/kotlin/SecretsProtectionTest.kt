/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class SecretsProtectionTest {
    @Test
    fun chaosTest() = runBlocking<Unit> {
        repeat(500) {
            launch {
                val data = ByteArray((1..255).random()) { (0..255).random().toByte() }
                val buffer = SecretsProtection.escape(data) as ByteBuffer
                assertContentEquals(
                    data, buffer.duplicate().readBytes()
                )
                delay(100)
                assertContentEquals(
                    data, buffer.duplicate().readBytes()
                )
            }
        }
    }
}
