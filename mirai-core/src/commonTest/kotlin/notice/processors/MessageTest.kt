/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.KEY_FROM_SYNC
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class MessageTest : AbstractNoticeProcessorTest() {

    @Test
    fun `group message test`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush.PbPushMsg(
                msg = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                    msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                        fromUin = 1230001,
                        toUin = 1230003,
                        msgType = 82,
                        msgSeq = 1629, // id
                        msgTime = 1630,
                        msgUid = 14411, // neither id nor internalId
                        groupInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.GroupInfo(
                            groupCode = 2230203,
                            groupType = 1,
                            groupInfoSeq = 626,
                            groupCard = "user1",
                            groupLevel = 1,
                            groupCardType = 2,
                            groupName = "testtest".toByteArray(), /* 74 65 73 74 74 65 73 74 */
                        ),
                        fromAppid = 1,
                        fromInstid = 1,
                        userActive = 1,
                    ),
                    contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                        pkgNum = 1,
                    ),
                    msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                        richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                            attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                                codePage = 0,
                                time = 1630,
                                random = -1469, // internal id
                                size = 12,
                                effect = 0,
                                charSet = 134,
                                pitchAndFamily = 34,
                                fontName = "微软雅黑",
                            ),
                            elems = mutableListOf(
                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                        str = "hello",
                                    ),
                                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                                        msgRptCnt = 1,
                                    ),
                                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                        glamourLevel = 3,
                                        pbReserve = "08 0A 20 CB 50 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 D1 02 A0 03 40 B0 03 00 C0 03 00 D0 03 00 E8 03 00 90 04 80 01 B8 04 02 C0 04 00 CA 04 00 F8 04 00 88 05 00".hexToBytes(),
                                    ),
                                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                                        nick = "user1",
                                        level = 1,
                                        groupMask = 3,
                                    ),
                                )
                            ),
                        ),
                    ),
                ),
                svrip = 2057,
                generalFlag = 1,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER, nick = "user1")
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<GroupMessageEvent>(event)
            assertEquals(1630, event.time)
            assertEquals(1230001, event.sender.id)
            assertEquals("user1", event.senderName)

            assertEquals("hello", event.message.content)

            event.message.run {
                assertEquals(2, size)
                get(0).run {
                    assertIs<OnlineMessageSource.Incoming.FromGroup>(this)
                    assertContentEquals(intArrayOf(1629), ids)
                    assertContentEquals(intArrayOf(-1469), internalIds)
                    assertEquals(1630, time)
                    assertEquals(1230001, fromId)
                    assertEquals(2230203, targetId)
                    assertEquals(event.message.filterNot { it is MessageSource }.toMessageChain(), originalMessage)
                }
                assertIs<PlainText>(get(1))
                assertEquals("hello", get(1).content)
            }
        }
    }


    @Test
    fun `friend message test`() = runBlockingUnit {
        suspend fun runTest() = use(KEY_FROM_SYNC to false) {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 1230001,
                    toUin = 1230003,
                    msgType = 166,
                    c2cCmd = 11,
                    msgSeq = 13985,
                    msgTime = 1630,
                    msgUid = 72057,
                    wseqInC2cMsghead = 25159,
                ),
                contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                    pkgNum = 1,
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                        attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                            codePage = 0,
                            time = 1630,
                            random = -5872,
                            size = 12,
                            effect = 0,
                            charSet = 134,
                            pitchAndFamily = 34,
                            fontName = "微软雅黑",
                        ),
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "123",
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00".hexToBytes(),
                                ),
                            )
                        ),
                    ),
                ),
            )
        }

        setBot(1230003)
            .addFriend(1230001, "user1")



        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<FriendMessageEvent>(event)
            assertEquals(1630, event.time)
            assertEquals(1230001, event.sender.id)
            assertEquals("user1", event.senderName)

            assertEquals("123", event.message.content)

            event.message.run {
                assertEquals(2, size)
                get(0).run {
                    assertIs<OnlineMessageSource.Incoming.FromFriend>(this)
                    assertContentEquals(intArrayOf(13985), ids)
                    assertContentEquals(intArrayOf(-5872), internalIds)
                    assertEquals(1630, time)
                    assertEquals(1230001, fromId)
                    assertEquals(1230003, targetId)
                    assertEquals(event.message.filterNot { it is MessageSource }.toMessageChain(), originalMessage)
                }
                assertIs<PlainText>(get(1))
                assertEquals("123", get(1).content)
            }
        }
    }

    @Test
    fun `group temp message test`() = runBlockingUnit {
        suspend fun runTest() = use(KEY_FROM_SYNC to false) {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 1230001,
                    toUin = 1230003,
                    msgType = 141,
                    c2cCmd = 11,
                    msgSeq = 11080,
                    msgTime = 1630,
                    msgUid = 720,
                    c2cTmpMsgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.C2CTmpMsgHead(
                        c2cType = 2,
                        groupUin = 2230203,
                        groupCode = 2230203,
                        sig = "38 59 CD 1E 22 9A 0A BA 28 59 46 BE FA 51 36 D0 F1 7A 5D 54 F5 04 05 7E 66 C7 36 4F 73 BF 45 96 00 39 7C 8F F5 43 57 74 B0 EB D9 5E 0F 1F 9B CF".hexToBytes(),
                    ),
                    wseqInC2cMsghead = 25160,
                ),
                contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                    pkgNum = 1,
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                        attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                            codePage = 0,
                            time = 1630,
                            random = 1854,
                            size = 12,
                            effect = 0,
                            charSet = 134,
                            pitchAndFamily = 34,
                            fontName = "微软雅黑",
                        ),
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "hello",
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                                    nick = "user1",
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00".hexToBytes(),
                                ),
                            )
                        ),
                    ),
                ),
            )
        }

        setBot(1230003).apply {
            addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER, nick = "user1")
            }
            addStranger(1230001, "user1", fromGroupId = 2230203)
        }


        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<GroupTempMessageEvent>(event)
            assertEquals(1630, event.time)
            assertEquals(1230001, event.sender.id)
            assertEquals("user1", event.senderName)

            assertEquals("hello", event.message.content)

            event.message.run {
                assertEquals(2, size)
                get(0).run {
                    assertIs<OnlineMessageSource.Incoming.FromTemp>(this)
                    assertContentEquals(intArrayOf(11080), ids)
                    assertContentEquals(intArrayOf(1854), internalIds)
                    assertEquals(1630, time)
                    assertEquals(1230001, fromId)
                    assertEquals(1230003, targetId)
                    assertEquals(event.message.filterNot { it is MessageSource }.toMessageChain(), originalMessage)
                }
                assertIs<PlainText>(get(1))
                assertEquals("hello", get(1).content)
            }
        }
    }

    // for #1410
    @Test
    fun `group temp message test for issue 1410`() = runBlockingUnit {
        suspend fun runTest() = use(KEY_FROM_SYNC to false) {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 1230001,
                    toUin = 1230003,
                    msgType = 141,
                    c2cCmd = 11,
                    msgSeq = 11080,
                    msgTime = 1630,
                    msgUid = 720,
                    c2cTmpMsgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.C2CTmpMsgHead(
                        c2cType = 2,
                        groupUin = 2055561833,
                        groupCode = 112561833,
                        sig = "38 59 CD 1E 22 9A 0A BA 28 59 46 BE FA 51 36 D0 F1 7A 5D 54 F5 04 05 7E 66 C7 36 4F 73 BF 45 96 00 39 7C 8F F5 43 57 74 B0 EB D9 5E 0F 1F 9B CF".hexToBytes(),
                    ),
                    wseqInC2cMsghead = 25160,
                ),
                contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                    pkgNum = 1,
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                        attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                            codePage = 0,
                            time = 1630,
                            random = 1854,
                            size = 12,
                            effect = 0,
                            charSet = 134,
                            pitchAndFamily = 34,
                            fontName = "微软雅黑",
                        ),
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "hello",
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                                    nick = "user1",
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                                ),
                            ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00".hexToBytes(),
                                ),
                            )
                        ),
                    ),
                ),
            )
        }

        setBot(1230003).apply {
            addGroup(112561833, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER, nick = "user1")
            }
            addStranger(1230001, "user1", fromGroupId = 2230203)
        }


        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<GroupTempMessageEvent>(event)
            assertEquals(1630, event.time)
            assertEquals(1230001, event.sender.id)
            assertEquals("user1", event.senderName)

            assertEquals("hello", event.message.content)

            event.message.run {
                assertEquals(2, size)
                get(0).run {
                    assertIs<OnlineMessageSource.Incoming.FromTemp>(this)
                    assertContentEquals(intArrayOf(11080), ids)
                    assertContentEquals(intArrayOf(1854), internalIds)
                    assertEquals(1630, time)
                    assertEquals(1230001, fromId)
                    assertEquals(1230003, targetId)
                    assertEquals(event.message.filterNot { it is MessageSource }.toMessageChain(), originalMessage)
                }
                assertIs<PlainText>(get(1))
                assertEquals("hello", get(1).content)
            }
        }
    }

    @Test
    fun `stranger message test`() { // TODO: 2021/8/27 cannot start a such conversation

    }
}