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
import net.mamoe.mirai.internal.AbstractTestWithMiraiImpl
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.message.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.message.MessagePipelineContext
import net.mamoe.mirai.internal.network.message.OutgoingMessagePhasesCommon
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.replaceAllKotlin
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class MessageReceiptTest : AbstractTestWithMiraiImpl(), GroupExtensions {
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
        val group = bot.addGroup(1L, 123L)
        val forward = buildForwardMessage(group) {
            123 says "ok"
        }
        val message = forward.toMessageChain()

        group.sendMessagePipeline.mutableNodes.replaceAllKotlin { node ->
            if (node.name == "CreatePacketsFallback") {
                object : OutgoingMessagePhasesCommon.CreatePacketsFallback<GroupImpl>() {
                    override suspend fun MessagePipelineContext<GroupImpl>.createPacketsImpl(chain: MessageChain): List<OutgoingPacket> {
                        return listOf<OutgoingPacket>().also {
                            attributes[MessagePipelineContext.KEY_MESSAGE_SOURCE_RESULT] = CompletableDeferred(
                                OnlineMessageSourceToGroupImpl(
                                    group,
                                    internalIds = intArrayOf(1),
                                    sender = bot,
                                    target = group,
                                    time = currentTimeSeconds().toInt(),
                                    originalMessage = message //,
                                    //   sourceMessage = message
                                )
                            )
                        }
                    }
                }
            } else node
        }

        val result = group.sendMessage(message)

        assertIs<ForwardMessage>(result.source.originalMessage[ForwardMessage])
        assertEquals(message, result.source.originalMessage)
        assertSame(forward, result.source.originalMessage[ForwardMessage])
    }
}