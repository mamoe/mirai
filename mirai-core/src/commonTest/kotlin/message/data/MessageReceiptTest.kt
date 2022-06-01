/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.impl.GeneralMessageSenderProtocol
import net.mamoe.mirai.internal.message.protocol.outgoing.*
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.message.source.createMessageReceipt
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.internal.pipeline.replaceProcessor
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.Clock
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.test.*

internal class MessageReceiptTest : AbstractTest(), GroupExtensions {
    private val bot = MockBot()

    @Test
    fun `refine ForwardMessageInternal for MessageReceipt's original MessageChain`() = runBlockingUnit {
        val group = bot.addGroup(123, 2)

        val forward = buildForwardMessage(group) {
            2 says "ok"
        }
        val message = forward.toMessageChain()

        val facade = MessageProtocolFacade.INSTANCE.copy()

        assertTrue {
            facade.outgoingPipeline.replaceProcessor(
                { it is GeneralMessageSenderProtocol.GeneralMessageSender },
                OutgoingMessageProcessorAdapter(object : OutgoingMessageSender {
                    override suspend fun OutgoingMessagePipelineContext.process() {
                        assertIs<ForwardMessageInternal>(currentMessageChain[ForwardMessageInternal])
                        assertSame(forward, currentMessageChain[ForwardMessageInternal]?.origin)

                        val source = OnlineMessageSourceToGroupImpl(
                            group,
                            internalIds = intArrayOf(1),
                            sender = bot,
                            target = group,
                            time = currentTimeSeconds().toInt(),
                            originalMessage = currentMessageChain //,
                            //   sourceMessage = message
                        )

                        collect(source.createMessageReceipt(group, true))
                    }
                })
            )
        }

        val result = facade.preprocessAndSendOutgoing(group, message, ConcurrentComponentStorage {
            set(MessageProtocolStrategy, object : GroupMessageProtocolStrategy(group) {

            })
            set(HighwayUploader, object : HighwayUploader {
                override suspend fun uploadMessages(
                    contact: AbstractContact,
                    components: ComponentStorage,
                    nodes: Collection<ForwardMessage.INode>,
                    isLong: Boolean,
                    senderName: String
                ): String {
                    return "id"
                }
            })
            set(ClockHolder, object : ClockHolder() {
                override val local: Clock = object : Clock {
                    override fun currentTimeMillis(): Long {
                        return 160023456
                    }
                }
            })
        })

        assertIs<ForwardMessage>(result.source.originalMessage[ForwardMessage])
        assertEquals(message, result.source.originalMessage)
        assertSame(forward, result.source.originalMessage[ForwardMessage])
    }
}