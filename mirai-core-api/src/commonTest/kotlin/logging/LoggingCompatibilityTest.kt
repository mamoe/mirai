/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.logging

import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class LoggingCompatibilityTest : AbstractLoggingTest() {

    @Suppress("DEPRECATION_ERROR")
    @Test
    fun `legacy overrides are still working if no services are found`() {
        val messages = StringBuilder()

        MiraiLoggerFactoryImplementationBridge.wrapCurrent {
            object : MiraiLogger.Factory {
                override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                    return net.mamoe.mirai.utils.SimpleLogger("my logger") { message: String?, _: Throwable? ->
                        messages.append(message)
                    }

                }
            }
        }

        val created = MiraiLogger.Factory.create(this::class)
        assertIs<MiraiLogger>(created)
        created.info("test")

        assertEquals("test", messages.toString().trim())
    }
}