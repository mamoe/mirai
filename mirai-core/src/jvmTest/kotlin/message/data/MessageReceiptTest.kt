/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.internal.AbstractTestWithMiraiImpl
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.contact.info.GroupInfoImpl
import net.mamoe.mirai.internal.message.ForwardMessageInternal
import net.mamoe.mirai.internal.message.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.currentTimeSeconds
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class MessageReceiptTest : AbstractTestWithMiraiImpl() {
    override suspend fun uploadMessageHighway(
        contact: AbstractContact,
        nodes: Collection<ForwardMessage.INode>,
        isLong: Boolean,
    ): String {
        return "id"
    }

    private val bot = MockBot()

    /**
     * This test is very ugly but we cannot do anything else.
     */ // We need #1304
    @Test
    fun `refine ForwardMessageInternal for MessageReceipt`() = runBlockingUnit {
        val group =
            GroupImpl(bot, bot.coroutineContext, 1, GroupInfoImpl(StTroopNum(1, 1, dwGroupOwnerUin = 2)), sequenceOf())

        val forward = buildForwardMessage(group) {
            2 says "ok"
        }
        val message = forward.toMessageChain()

        val handler = object : GroupSendMessageHandler(group) {
            override val messageSvcSendMessage: (client: QQAndroidClient, contact: GroupImpl, message: MessageChain, fragmented: Boolean, sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit) -> List<OutgoingPacket> =
                { _, contact, message, fragmented, sourceCallback ->

                    assertIs<ForwardMessageInternal>(message[ForwardMessageInternal])
                    assertSame(forward, message[ForwardMessageInternal]?.origin)

                    sourceCallback(CompletableDeferred(OnlineMessageSourceToGroupImpl(
                        group,
                        internalIds = intArrayOf(1),
                        sender = bot,
                        target = group,
                        time = currentTimeSeconds().toInt(),
                        originalMessage = message //,
                        //   sourceMessage = message
                    )))
                    listOf()
                }
        }
        val result = handler.sendMessage(message, message, SendMessageStep.FIRST)

        assertIs<ForwardMessage>(result.source.originalMessage[ForwardMessage])
        assertEquals(message, result.source.originalMessage)
        assertSame(forward, result.source.originalMessage[ForwardMessage])
    }
}