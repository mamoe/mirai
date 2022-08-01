/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.coroutines.Deferred
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.LightMessageRefiner.dropMiraiInternalFlags
import net.mamoe.mirai.internal.message.flags.ForceAsFragmentedMessage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.castUp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GeneralMessageSenderProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(TextProtocol(), GeneralMessageSenderProtocol())

    @Test
    fun `can convert messages failed to send to fragmented`() {
        val message = messageChainOf(PlainText("test"), PlainText("test"))

        runWithFacade {
            components[MessageProtocolStrategy] = object : TestMessageProtocolStrategy() {
                var count = 0
                override suspend fun sendPacket(bot: AbstractBot, packet: OutgoingPacket): Packet {
                    println("MessageProtocolStrategy.sendPacket called: $count")
                    if (count++ <= 1) { // fail the first and second attempt
                        return MessageSvcPbSendMsg.Response.MessageTooLarge
                    }
                    return super.sendPacket(bot, packet)
                }

                override suspend fun createPacketsForGeneralMessage(
                    client: QQAndroidClient,
                    contact: AbstractContact,
                    message: MessageChain,
                    originalMessage: MessageChain,
                    fragmented: Boolean,
                    sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
                ): List<OutgoingPacket> {
                    if (count == 2) {
                        assertTrue { fragmented }
                    } else {
                        assertFalse { fragmented }
                    }
                    return super.createPacketsForGeneralMessage(
                        client,
                        contact,
                        message,
                        originalMessage,
                        fragmented,
                        sourceCallback
                    )
                }
            }
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)
                assertMessageEquals(message.dropMiraiInternalFlags(), context.currentMessageChain)
            }
        }
    }

    @Test
    fun `can convert messages to fragmented`() {
        val message = messageChainOf(PlainText("test"), PlainText("test"), ForceAsFragmentedMessage)

        runWithFacade {
            components[MessageProtocolStrategy] = object : TestMessageProtocolStrategy() {
                override suspend fun createPacketsForGeneralMessage(
                    client: QQAndroidClient,
                    contact: AbstractContact,
                    message: MessageChain,
                    originalMessage: MessageChain,
                    fragmented: Boolean,
                    sourceCallback: (Deferred<OnlineMessageSource.Outgoing>) -> Unit
                ): List<OutgoingPacket> {
                    assertTrue { fragmented }
                    return super.createPacketsForGeneralMessage(
                        client,
                        contact,
                        message,
                        originalMessage,
                        fragmented,
                        sourceCallback
                    )
                }
            }
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)
                assertMessageEquals(message, context.currentMessageChain)
            }
        }
    }

}