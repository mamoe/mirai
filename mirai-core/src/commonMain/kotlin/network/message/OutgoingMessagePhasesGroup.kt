/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.message

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.event.nextEventOrNull
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.message.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_SENDING_AS_FRAGMENTED
import net.mamoe.mirai.internal.network.notice.group.GroupMessageProcessor
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.currentTimeSeconds

internal object OutgoingMessagePhasesGroup : OutgoingMessagePhasesCommon(), OutgoingMessagePhases<GroupImpl> {
    val specialMessageSourceStrategy: SpecialMessageSourceStrategy<GroupImpl> =
        object : SpecialMessageSourceStrategy<GroupImpl> {
            override suspend fun constructSourceForSpecialMessage(
                context: MessagePipelineContext<GroupImpl>,
                finalMessage: MessageChain,
                fromAppId: Int
            ): OnlineMessageSource.Outgoing = context.run {
                val receipt: GroupMessageProcessor.SendGroupMessageReceipt =
                    nextEventOrNull(3000) { it.fromAppId == fromAppId }
                        ?: GroupMessageProcessor.SendGroupMessageReceipt.EMPTY

                return OnlineMessageSourceToGroupImpl(
                    contact,
                    internalIds = intArrayOf(receipt.messageRandom),
                    providedSequenceIds = intArrayOf(receipt.sequenceId),
                    sender = bot,
                    target = contact,
                    time = currentTimeSeconds().toInt(),
                    originalMessage = finalMessage
                )
            }
        }

    @Suppress("FunctionName")
    @PhaseMarker
    fun CreatePacketsNormal() = object : CreatePacketsFallback<GroupImpl>() {
        override suspend fun MessagePipelineContext<GroupImpl>.createPacketsImpl(chain: MessageChain): List<OutgoingPacket>? {
            return MessageSvcPbSendMsg.createToGroupImpl(
                bot.client,
                contact,
                chain,
                fragmented = attributes[KEY_SENDING_AS_FRAGMENTED]
            ) {
                attributes[MessagePipelineContext.KEY_MESSAGE_SOURCE_RESULT] = CompletableDeferred(it)
            }
        }
    }
}