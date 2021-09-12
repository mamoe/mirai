/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.internal.message.outgoing

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.components.OutgoingMessagePipelineFactory
import net.mamoe.mirai.internal.network.components.OutgoingMessagePipelineFactoryImpl
import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.framework.replace
import net.mamoe.mirai.internal.network.message.MessagePipelineConfiguration
import net.mamoe.mirai.internal.network.message.MessagePipelineContext
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_FINAL_MESSAGE_CHAIN
import net.mamoe.mirai.internal.network.pipeline.replacePhase
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.utils.cast

internal abstract class AbstractOutgoingPipelineTest : AbstractMockNetworkHandlerTest(), GroupExtensions {
    init {
        components.replace(OutgoingMessagePipelineFactory) { origin ->
            object : OutgoingMessagePipelineFactoryImpl() {
                override fun createForGroup(group: GroupImpl): MessagePipelineConfiguration<GroupImpl> {
                    return (origin?.createForGroup(group) ?: super.createForGroup(group)).apply {
                        replacePhase("CreatePacketsFallback") {
                            it ?: listOf<OutgoingPacket>().also {
                                attributes[MessagePipelineContext.KEY_PACKET_TRACE] = "CreatePacketsFallback"
                            }
                        }
                        replacePhase("CreatePacketsForMusicShare") {
                            if (MusicShare !in attributes[KEY_FINAL_MESSAGE_CHAIN]) return@replacePhase it
                            it ?: listOf<OutgoingPacket>().also {
                                attributes[MessagePipelineContext.KEY_PACKET_TRACE] = "CreatePacketsForMusicShare"
                            }
                        }
                        replacePhase("CreatePacketsForFileMessage") {
                            if (FileMessage !in attributes[KEY_FINAL_MESSAGE_CHAIN]) return@replacePhase it
                            it ?: listOf<OutgoingPacket>().also {
                                attributes[MessagePipelineContext.KEY_PACKET_TRACE] = "CreatePacketsForFileMessage"
                            }
                        }
                    }
                }
            }
        }
    }

    fun <T : AbstractContact> MessagePipelineConfiguration<T>.replaceSendPacketPhase(block: suspend MessagePipelineContext<T>.(packets: List<OutgoingPacket>) -> MessageReceipt<T>) {
        replacePhase({ it.name == "SendPacketsAndCreateReceipt" }, "test monitor") { input ->
            input.cast<List<OutgoingPacket>>()
            block(input)
        }
    }
}