/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import net.mamoe.mirai.event.events.FriendNickChangedEvent
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.internal.network.protocol.data.jce.ShareData
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27.SubMsgType0x27
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class FriendNickChangeTest : AbstractNoticeProcessorTest() {

    @Test
    fun `nick changed`() = runBlockingUnit {
        // FriendNickChangedEvent 内容异常 https://github.com/mamoe/mirai/issues/1356

        suspend fun runTest() = use {

            OnlinePushPack.SvcReqPushMsg(
                uin = 1230002,
                uMsgTime = 1633037660,
                vMsgInfos = mutableListOf(
                    MsgInfo(
                        lFromUin = 1230002,
                        shMsgType = 528,
                        shMsgSeq = 142,
                        strMsg = "",
                        uRealMsgTime = 160,
                        vMsg = MsgType0x210(
                            uSubMsgType = 39,
                            vProtobuf = SubMsgType0x27.SubMsgType0x27MsgBody(
                                msgModInfos = mutableListOf(
                                    SubMsgType0x27.ForwardBody(
                                        opType = 20,
                                        msgModProfile = SubMsgType0x27.ModProfile(
                                            uin = 1230001,
                                            msgProfileInfos = mutableListOf(
                                                SubMsgType0x27.ProfileInfo(
                                                    field = 20002,
                                                    value = "ABC",
                                                )
                                            ),
                                        ),
                                    )
                                ),
                            )
                                .toByteArray(SubMsgType0x27.SubMsgType0x27MsgBody.serializer()),
                        ).toByteArray(),
                        uAppShareID = 0,
                        vMsgCookies = "08 90 04 10 90 84 A0 81 80 80 80 80 02 18 00 20 E3 86 03".hexToBytes(),
                        lMsgUid = 14411,
                        lLastChangeTime = 1,
                        vCPicInfo = mutableListOf(),
                        stShareData = ShareData(
                        ),
                        lFromInstId = 0,
                        vRemarkOfSender = net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY,
                        strFromMobile = "",
                        strFromName = "",
                        vNickName = mutableListOf(),
                    )
                ),
                svrip = 1273521418,
            )

        }

        setBot(1230002).apply {
            addFriend(1230001, nick = "aaa")
        }

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<FriendNickChangedEvent>(this)
                assertEquals(1230001, friend.id)
                assertEquals("aaa", from)
                assertEquals("ABC", to)
            }
        }

    }
}