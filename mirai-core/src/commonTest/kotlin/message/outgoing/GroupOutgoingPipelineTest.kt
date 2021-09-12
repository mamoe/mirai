/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.outgoing

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.message.OnlineMessageSourceToGroupImpl
import net.mamoe.mirai.internal.message.createMessageReceipt
import net.mamoe.mirai.internal.network.message.MessagePipelineContext
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_PACKET_TRACE
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_STATE_CONTROLLER
import net.mamoe.mirai.internal.network.message.SendMessageState
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@JvmBlockingBridge
internal class GroupOutgoingPipelineTest : AbstractOutgoingPipelineTest() {

    @Test
    suspend fun `group send normal`() {
        val group = bot.addGroup(1, 1).apply {
            addMember(1, permission = MemberPermission.OWNER)
        }
        group.sendMessagePipeline.replaceSendPacketPhase {
            assertEquals("CreatePacketsFallback", attributes[KEY_PACKET_TRACE])
            createReceipt(group)
        }
        group.sendMessage("Test message")
    }

    @Test
    suspend fun `group send states`() {
        val group = bot.addGroup(1, 1).apply {
            addMember(1, permission = MemberPermission.OWNER)
        }
        var called = 0
        group.sendMessagePipeline.replaceSendPacketPhase {
            assertEquals("CreatePacketsFallback", attributes[KEY_PACKET_TRACE])
            when (called) {
                0 -> {
                    called++
                    assertEquals(SendMessageState.ORIGIN, attributes[KEY_STATE_CONTROLLER].state)
                }
                1 -> {
                    called++
                    assertEquals(SendMessageState.LONG, attributes[KEY_STATE_CONTROLLER].state)
                }
                2 -> {
                    called++
                    assertEquals(SendMessageState.FRAGMENTED, attributes[KEY_STATE_CONTROLLER].state)
                }
            }
            error("fake failure")
        }
        assertFailsWith<IllegalStateException> { group.sendMessage("a".repeat(10_000)) }.run {
            assertEquals("fake failure", message)
        }
    }

    @Test
    suspend fun `group send file`() {
        val group = bot.addGroup(1, 1).apply {
            addMember(1, permission = MemberPermission.OWNER)
        }
        group.sendMessagePipeline.replaceSendPacketPhase {
            assertEquals("CreatePacketsForFileMessage", attributes[KEY_PACKET_TRACE])
            createReceipt(group)
        }
        group.sendMessage(FileMessage("id", 1, "name", 2))
    }

    @Test
    suspend fun `group send music`() {
        val group = bot.addGroup(1, 1).apply {
            addMember(1, permission = MemberPermission.OWNER)
        }
        group.sendMessagePipeline.replaceSendPacketPhase {
            assertEquals("CreatePacketsForMusicShare", attributes[KEY_PACKET_TRACE])
            createReceipt(group)
        }
        group.sendMessage(
            MusicShare(
                kind = MusicKind.NeteaseCloudMusic,
                title = "ファッション",
                summary = "rinahamu/Yunomi",
                brief = "",
                jumpUrl = "http://music.163.com/song/1338728297/?userid=324076307",
                pictureUrl = "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
                musicUrl = "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"
            )
        )
    }

    private fun MessagePipelineContext<GroupImpl>.createReceipt(
        group: GroupImpl
    ) = OnlineMessageSourceToGroupImpl(
        bot,
        intArrayOf(1),
        1,
        EmptyMessageChain,
        bot,
        group,
        intArrayOf(1)
    ).createMessageReceipt(group, true)

}