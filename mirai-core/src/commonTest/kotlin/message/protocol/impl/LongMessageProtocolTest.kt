/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.message.LightMessageRefiner.dropMiraiInternalFlags
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.flags.ForceAsLongMessage
import net.mamoe.mirai.internal.message.flags.IgnoreLengthCheck
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.repeat
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.castUp
import net.mamoe.mirai.utils.getRandomString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LongMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> =
        arrayOf(
            TextProtocol(),
            ImageProtocol(),
            LongMessageProtocol(),
            GeneralMessageSenderProtocol(),
        )


    init {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun precondition() {
        assertEquals(getRandomString(5000, Random(1)), getRandomString(5000, Random(1)))
        assertMessageEquals(
            "test".toPlainText() + getRandomString(5000, Random(1)) +
                    Image("{40A7C56B-45C9-23AE-0CFA-23F095B71035}.jpg").repeat(200),
            "test".toPlainText() + getRandomString(5000, Random(1)) +
                    Image("{40A7C56B-45C9-23AE-0CFA-23F095B71035}.jpg").repeat(200)
        )
    }

    @Test
    fun `can convert messages to LongMessageInternal`() {
        var message = "test".toPlainText() + getRandomString(5000, Random(1)) +
                Image("{40A7C56B-45C9-23AE-0CFA-23F095B71035}.jpg").repeat(200)

        message += IgnoreLengthCheck
        message += ForceAsLongMessage

        runWithFacade {
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)

                assertMessageEquals(
                    LongMessageInternal(
                        """
                            <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                            <msg serviceID="35" templateID="1" action="viewMultiMsg"
                                 brief="testqGnJ1R..."
                                 m_resid="(size=1)6C6FD4AEC362AA8E54058A27B422FA42"
                                 m_fileName="160023" sourceMsgId="0" url=""
                                 flag="3" adverSign="0" multiMsgFlag="1">
                                <item layout="1">
                                    <title>testqGnJ1R...</title>
                                    <hr hidden="false" style="0"/>
                                    <summary>点击查看完整消息</summary>
                                </item>
                                <source name="聊天记录" icon="" action="" appid="-1"/>
                            </msg>
                """.trimIndent(), "(size=1)6C6FD4AEC362AA8E54058A27B422FA42"
                    ) + IgnoreLengthCheck + ForceAsLongMessage, context.currentMessageChain
                )
            }
        }
    }

    @Test
    fun `can convert messages failed to send at FIRST step to LongMessageInternal`() {
        val message = "test".toPlainText().toMessageChain()

        runWithFacade {
            components[MessageProtocolStrategy] = object : TestMessageProtocolStrategy() {
                var count = 0
                override suspend fun sendPacket(bot: AbstractBot, packet: OutgoingPacket): Packet {
                    println("MessageProtocolStrategy.sendPacket called: $count")
                    if (count++ == 0) { // fail the first attempt
                        return MessageSvcPbSendMsg.Response.MessageTooLarge
                    }
                    return super.sendPacket(bot, packet)
                }
            }
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)

                assertMessageEquals(
                    LongMessageInternal(
                        """
                            <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                            <msg serviceID="35" templateID="1" action="viewMultiMsg"
                                 brief="test"
                                 m_resid="(size=1)8698F15C27DA63648CEF9A93EA76B084"
                                 m_fileName="160023" sourceMsgId="0" url=""
                                 flag="3" adverSign="0" multiMsgFlag="1">
                                <item layout="1">
                                    <title>test</title>
                                    <hr hidden="false" style="0"/>
                                    <summary>点击查看完整消息</summary>
                                </item>
                                <source name="聊天记录" icon="" action="" appid="-1"/>
                            </msg>
                        """.trimIndent(), "(size=1)8698F15C27DA63648CEF9A93EA76B084"
                    ), context.currentMessageChain
                )
            }
        }
    }

    // should add tests for refining received LongMessage to normal messages (with a MessageOrigin)
}