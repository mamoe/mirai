/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.FriendMessageSyncEvent
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.KEY_FROM_SYNC
import net.mamoe.mirai.message.data.content
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@JvmBlockingBridge
internal class MessageSyncTest : AbstractNoticeProcessorTest() {

    @Test
    suspend fun `can receive group sync from macOS client`() {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush.PbPushMsg(
                msg = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                    msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                        fromUin = 1230002,
                        toUin = 1230002,
                        msgType = 82,
                        msgSeq = 1772,
                        msgTime = 1640029614,
                        msgUid = 144115188088832082,
                        groupInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.GroupInfo(
                            groupCode = 2230203,
                            groupType = 1,
                            groupInfoSeq = 657,
                            groupCard = "user2",
                            groupLevel = 1,
                            groupCardType = 2,
                            groupName = "testtest".toByteArray(), /* 74 65 73 74 74 65 73 74 */
                        ),
                        fromAppid = 1001,
                        fromInstid = 537067835,
                        userActive = 1,
                    ),
                    msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                        richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                            attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                                codePage = 0,
                                time = 1640029614,
                                random = 25984994,
                                size = 9,
                                effect = 0,
                                charSet = 134,
                                pitchAndFamily = 0,
                                fontName = "Helvetica",
                            ),
                            elems = mutableListOf(
                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                        str = "s",
                                    ),
                                ),
                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                                        msgRptCnt = 1,
                                    ),
                                ),
                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                        pbReserve = "".hexToBytes(),
                                    ),
                                ),
                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                                        nick = "user2",
                                        level = 1,
                                        groupMask = 1,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                svrip = 1579509002,
                generalFlag = 1,
            )
        }

        setBot(1230002).apply {
            addGroup(2230203L, 1230001).apply {
                addMember(1230001, permission = MemberPermission.OWNER)
                addMember(1230002, permission = MemberPermission.MEMBER)
            }
        }

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<GroupMessageSyncEvent>(this)
                assertEquals(2230203, group.id)
                assertEquals(1230002, sender.id)
                assertEquals("s", message.content)
            }
        }

    }


    @Test
    suspend fun `can receive friend sync from macOS client`() {
        suspend fun runTest() = use {
            attributes[KEY_FROM_SYNC] = true

            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 1230002,
                    toUin = 1230001,
                    msgType = 166,
                    c2cCmd = 11,
                    msgSeq = 13887,
                    msgTime = 1640030199,
                    msgUid = 72057594845425959,
                    fromAppid = 1001,
                    fromInstid = 537067835,
                    userActive = 1,
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                        attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                            codePage = 0,
                            time = 1640030199,
                            random = 807498023,
                            size = 9,
                            effect = 0,
                            charSet = 134,
                            pitchAndFamily = 0,
                            fontName = "Helvetica",
                        ),
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "hi",
                                ),
                            ),
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            ),
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00".hexToBytes(),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        setBot(1230002).apply {
            addFriend(1230001)
        }

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<FriendMessageSyncEvent>(this)
                assertEquals(1230001, friend.id)
                assertEquals("hi", message.content)
            }
        }

    }
}