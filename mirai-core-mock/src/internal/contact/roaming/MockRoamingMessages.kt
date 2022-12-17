/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.roaming

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.contact.roaming.RoamingSupported
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.mock.internal.MockBotImpl
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.cast
import java.util.stream.Stream
import kotlin.streams.asStream

internal class MockRoamingMessages(
    internal val contact: RoamingSupported,
) : RoamingMessages {
    override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        return getMsg(timeStart, timeEnd, filter).asFlow()
    }

    private fun getMsg(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Sequence<MessageChain> {
        val msgDb = contact.bot.cast<MockBotImpl>().msgDatabase
        return msgDb.queryMessageInfosBy(
            contact.id,
            when (contact) {
                is Friend -> MessageSourceKind.FRIEND
                is Group -> MessageSourceKind.GROUP
                is Stranger -> MessageSourceKind.STRANGER
                else -> error(contact.javaClass.toString())
            },
            contact,
            timeStart,
            timeEnd,
            filter ?: RoamingMessageFilter.ANY
        ).map { it.buildSource(contact.bot.mock()) + it.message }
    }

    @JavaFriendlyAPI
    override suspend fun getMessagesStream(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Stream<MessageChain> {
        return getMsg(timeStart, timeEnd, filter).asStream()
    }
}
