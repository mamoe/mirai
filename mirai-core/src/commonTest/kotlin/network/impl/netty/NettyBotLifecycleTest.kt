/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.supervisorJob
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NettyBotLifecycleTest : AbstractNettyNHTest() {

    @Test
    fun `send logout on exit`() = runBlockingUnit {
        assertState(INITIALIZED)
        bot.login()
        assertState(OK)
        bot.close() // send logout blocking
        delay(1000)
        assertState(CLOSED)
        assertTrue { nhEvents.any { it is NHEvent.Logout } }
    }

    @Test
    fun `can override context`() = runBlockingUnit {
        bot = MockBot {
            conf {
                parentCoroutineContext = CoroutineName("Overrode")
            }
            networkHandlerProvider { createHandler() }
        }
        assertEquals("Overrode", bot.coroutineContext[CoroutineName]!!.name)
    }

    @Test
    fun `job attached`() = runBlockingUnit {
        val parentJob = SupervisorJob()
        bot = MockBot {
            conf {
                parentCoroutineContext = parentJob
            }
            networkHandlerProvider { createHandler() }
        }
        assertEquals(1, parentJob.children.count())
        assertEquals(bot.supervisorJob, parentJob.children.first())
    }

    @Test
    fun `network scope closed on bot close`() = runBlockingUnit {
        assertTrue { network.isActive }
        bot.close()
        assertFalse { network.isActive }
    }
}