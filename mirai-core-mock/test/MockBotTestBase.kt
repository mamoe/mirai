/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.database.queryMessageInfo
import net.mamoe.mirai.mock.internal.MockBotImpl
import net.mamoe.mirai.mock.utils.MockActionsScope
import net.mamoe.mirai.mock.utils.broadcastMockEvents
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.test.fail

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal open class MockBotTestBase : TestBase() {
    internal val bot = MockBotFactory.newMockBotBuilder()
        .id((100000000L..321111111L).random())
        .nick("Kafusumi")
        .create()

    @AfterEach
    internal fun `$$bot dispose`() {
        bot.close()
    }

    internal suspend fun runAndReceiveEventBroadcast(
        action: suspend MockActionsScope.() -> Unit
    ): List<Event> {

        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }

        val result = mutableListOf<Event>()
        val listener = GlobalEventChannel.subscribeAlways<Event> {
            result.add(this)
        }

        broadcastMockEvents {
            action()
        }

        (bot as MockBotImpl).joinEventBroadcast()

        listener.cancel()
        return result
    }


    internal fun assertMessageNotAvailable(source: MessageSource) {
        if (bot.msgDatabase.queryMessageInfo(source.ids, source.internalIds) != null) {
            fail("Require message $source no longer available.")
        }
    }

    internal fun assertMessageAvailable(source: MessageSource) {
        if (bot.msgDatabase.queryMessageInfo(source.ids, source.internalIds) == null) {
            fail("Require message $source available.")
        }
    }

}