/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import io.ktor.utils.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceFromGroupImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.PbMessageSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RecallTest : AbstractNoticeProcessorTest() {

    fun source(b: Bot, senderid: Long, groupid: Long, permision: MemberPermission): OnlineMessageSource =
        OnlineMessageSourceFromGroupImpl(
            b,
            listOf(
                MsgComm.Msg(
                    msgHead = MsgComm.MsgHead(
                        fromUin = senderid,
                        toUin = b.id,
                        msgType = 82,
                        msgSeq = 1628,
                        msgTime = 1629,
                        msgUid = 1441,
                        groupInfo = MsgComm.GroupInfo(
                            groupCode = groupid,
                            groupType = 1,
                            groupInfoSeq = 624,
                            groupCard = "user3",
                            groupLevel = 1,
                            groupCardType = 2,
                            groupName = "testtest".toByteArray(), /* 74 65 73 74 74 65 73 74 */
                        ),
                        fromAppid = 1,
                        fromInstid = 1,
                        userActive = 1,
                    ),
                    contentHead = MsgComm.ContentHead(
                        pkgNum = 1,
                    ),
                    msgBody = ImMsgBody.MsgBody(
                        richText = ImMsgBody.RichText(
                            attr = ImMsgBody.Attr(
                                codePage = 0,
                                time = 162,
                                random = -313,
                                size = 9,
                                effect = 0,
                                charSet = 134,
                                pitchAndFamily = 0,
                                fontName = "微软雅黑",
                            ),
                            elems = mutableListOf(
                                ImMsgBody.Elem(
                                    text = ImMsgBody.Text(
                                        str = "123123123",
                                    ),
                                ), ImMsgBody.Elem(
                                    elemFlags2 = ImMsgBody.ElemFlags2(
                                        msgRptCnt = 1,
                                    ),
                                ), ImMsgBody.Elem(
                                    generalFlags = ImMsgBody.GeneralFlags(
                                        pbReserve = "08 01 20 CB 50 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 00 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 10 02 90 04 80 01 B8 04 00 C0 04 00 CA 04 00 F8 04 00 88 05 00".hexToBytes(),
                                    ),
                                ), ImMsgBody.Elem(
                                    extraInfo = ImMsgBody.ExtraInfo(
                                        nick = "user3",
                                        level = permision.level,
                                        groupMask = 1,
                                    ),
                                )
                            ),
                        ),
                    ),
                )
            )
        )

    override fun setBot(id: Long): QQAndroidBot {
        return super.setBot(id).also { bot ->
            runBlockingUnit { bot.login() }
            network.addPacketReplier {
                assertEquals("PbMessageSvc.PbMsgWithDraw", it.commandName)
                reply(PbMessageSvc.PbMsgWithDraw.Response.Success)
            }
        }
    }

    @Test
    fun `recall member message without permission`() = runBlockingUnit {
        val bot = setBot(2)
        val group = bot.addGroup(5, 3, MemberPermission.MEMBER).apply {
            // owner
            addMember(3, permission = MemberPermission.OWNER)
            // sender
            addMember(1, permission = MemberPermission.MEMBER)
        }
        assertFailsWith<PermissionDeniedException> {
            Mirai.recallMessage(bot, source(bot, 1, group.id, group.botPermission))
        }
    }

    @Test
    fun `recall member message`() = runBlockingUnit {
        val bot = setBot(2)
        val group = bot.addGroup(5, 3, MemberPermission.ADMINISTRATOR).apply {
            // owner
            addMember(3, permission = MemberPermission.OWNER)
            // sender
            addMember(1, permission = MemberPermission.MEMBER)
        }
        Mirai.recallMessage(bot, source(bot, 1, group.id, group.botPermission))
    }

    @Test
    fun `recall administrator message`() = runBlockingUnit {
        val bot = setBot(2)
        val group = bot.addGroup(5, 3, MemberPermission.ADMINISTRATOR).apply {
            // owner
            addMember(3, permission = MemberPermission.OWNER)
            // sender
            addMember(1, permission = MemberPermission.ADMINISTRATOR)
        }
        assertFailsWith<PermissionDeniedException> {
            Mirai.recallMessage(bot, source(bot, 1, group.id, group.botPermission))
        }
    }

    @Test
    fun `recall administrator message as owner`() = runBlockingUnit {
        val bot = setBot(2)
        val group = bot.addGroup(5, 2, MemberPermission.OWNER).apply {
            // sender
            addMember(1, permission = MemberPermission.ADMINISTRATOR)
        }
        Mirai.recallMessage(bot, source(bot, 1, group.id, group.botPermission))
    }

    @Test
    fun `recall owner message`() = runBlockingUnit {
        val bot = setBot(2)
        val group = bot.addGroup(5, 1, MemberPermission.ADMINISTRATOR).apply {
            // sender
            addMember(1, permission = MemberPermission.OWNER)
        }
        assertFailsWith<PermissionDeniedException> {
            Mirai.recallMessage(bot, source(bot, 1, group.id, group.botPermission))
        }
    }
}
