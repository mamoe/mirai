/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.utils.SimpleLogger
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TestAbstractLoggerController {
    private class TestController(
        override val isLoggerControlStateSupported: Boolean,
    ) : AbstractLoggerController() {
        var callcount = 0


        override fun getPriority(identity: String?): LogPriority {
            return LogPriority.ALL
        }

        override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean {
            if (priority == SimpleLogger.LogPriority.INFO) {
                callcount++
            }
            return super.shouldLog(identity, priority)
        }
    }

    @Test
    fun `test logger control state caching`() {
        val controller = TestController(true)
        assertEquals(0, controller.callcount)
        val state = controller.getLoggerControlState("Test")
        assertEquals(0, controller.callcount) // lazy load

        state.shouldLog(SimpleLogger.LogPriority.DEBUG)
        assertEquals(1, controller.callcount)

        state.shouldLog(SimpleLogger.LogPriority.INFO)
        assertEquals(1, controller.callcount)

        state.shouldLog(SimpleLogger.LogPriority.DEBUG)
        state.shouldLog(SimpleLogger.LogPriority.INFO)
        state.shouldLog(SimpleLogger.LogPriority.INFO)
        assertEquals(1, controller.callcount)
    }

    @Test
    fun `test keep binary compatibility`() {
        val controller = TestController(false)
        assertEquals(0, controller.callcount)
        val state = controller.getLoggerControlState("Test")
        assertEquals(0, controller.callcount)

        repeat(50) { count ->
            state.shouldLog(SimpleLogger.LogPriority.INFO)
            assertEquals(count + 1, controller.callcount)
        }
    }
}