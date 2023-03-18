/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.BotMuteEvent
import net.mamoe.mirai.event.events.BotUnmuteEvent
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.events.MemberUnmuteEvent
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.internal.network.protocol.data.jce.ShareData
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


internal class MuteTest : AbstractNoticeProcessorTest() {
    @Test
    fun `bot mute`() = runBlockingUnit {
        suspend fun MuteTest.runTest() = use {
            OnlinePushPack.SvcReqPushMsg(
                uin = 1230001,
                uMsgTime = 1629868940,
                vMsgInfos = mutableListOf(
                    MsgInfo(
                        lFromUin = 2230203,
                        shMsgType = 732,
                        shMsgSeq = 8352,
                        strMsg = "",
                        uRealMsgTime = 16298,
                        vMsg = "00 22 07 BB 0C 01 00 12 C4 B2 61 25 D3 8D 00 01 00 12 C4 B1 00 00 02 58".hexToBytes(),
                        uAppShareID = 0,
                        vMsgCookies = "08 DC 05 10 DC 85 E0 80 80 80 80 80 02 18 03 20 DE 86 03".hexToBytes(),
                        lMsgUid = 1441151,
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
                svrip = -1467,
                vSyncCookie = net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY,
                vUinPairMsg = mutableListOf(),
                mPreviews = mutableMapOf(
                ),
            )


        }
        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<BotMuteEvent>(event)
            assertEquals(600, event.durationSeconds)
            assertEquals(1230002, event.operator.id)
        }
    }

    @Test
    fun `bot unmute`() = runBlockingUnit {
        suspend fun MuteTest.runTest() = use {
            OnlinePushPack.SvcReqPushMsg(
                uin = 1230001,
                uMsgTime = 1629869459,
                vMsgInfos = mutableListOf(
                    MsgInfo(
                        lFromUin = 2230203,
                        shMsgType = 732,
                        shMsgSeq = -26716,
                        strMsg = "",
                        uRealMsgTime = 1629,
                        vMsg = "00 22 07 BB 0C 01 00 12 C4 B2 61 25 D5 93 00 01 00 12 C4 B1 00 00 00 00".hexToBytes(),
                        uAppShareID = 0,
                        vMsgCookies = "08 DC 05 10 DC 85 E0 80 80 80 80 80 02 18 03 20 DE 86 03".hexToBytes(),
                        lMsgUid = 1441151,
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
                svrip = 1554,
                vSyncCookie = net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY,
                vUinPairMsg = mutableListOf(),
                mPreviews = mutableMapOf(
                ),
            )
        }

        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
                addMember(1230003, "user3", MemberPermission.MEMBER)
                botAsMember.apply {
                    _muteTimestamp = currentTimeSeconds().toInt() + 600
                }
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<BotUnmuteEvent>(event)
            assertEquals(1230002, event.operator.id)
        }


        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
                addMember(1230003, "user3", MemberPermission.MEMBER)
                botAsMember.apply {
                    _muteTimestamp = 0
                }
            }

        runTest().run {
            assertEquals(0, size)
        }
    }

    @Test
    fun `member mute`() = runBlockingUnit {
        suspend fun MuteTest.runTest() = use {
            OnlinePushPack.SvcReqPushMsg(
                uin = 1230001,
                uMsgTime = 1629870209,
                vMsgInfos = mutableListOf(
                    MsgInfo(
                        lFromUin = 2230203,
                        shMsgType = 732,
                        shMsgSeq = 8159,
                        strMsg = "",
                        uRealMsgTime = 16298,
                        vMsg = "00 22 07 BB 0C 01 00 12 C4 B2 61 25 D8 81 00 01 00 12 C4 B3 00 00 02 58".hexToBytes(),
                        uAppShareID = 0,
                        vMsgCookies = "08 DC 05 10 DC 85 E0 80 80 80 80 80 02 18 03 20 DE 86 03".hexToBytes(),
                        lMsgUid = 1441151,
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
                svrip = -176,
                vSyncCookie = net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY,
                vUinPairMsg = mutableListOf(),
                mPreviews = mutableMapOf(
                ),
            )
        }

        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
                addMember(1230003, "user3", MemberPermission.MEMBER)
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberMuteEvent>(event)
            assertEquals(600, event.durationSeconds)
            assertEquals(1230002, event.operator?.id)
        }
    }


    @Test
    fun `member unmute`() = runBlockingUnit {
        suspend fun MuteTest.runTest() = use {
            OnlinePushPack.SvcReqPushMsg(
                uin = 1230001,
                uMsgTime = 16298,
                vMsgInfos = mutableListOf(
                    MsgInfo(
                        lFromUin = 2230203,
                        shMsgType = 732,
                        shMsgSeq = 16929,
                        strMsg = "",
                        uRealMsgTime = 16298,
                        vMsg = "00 22 07 BB 0C 01 00 12 C4 B2 61 25 D7 02 00 01 00 12 C4 B3 00 00 00 00".hexToBytes(),
                        uAppShareID = 0,
                        vMsgCookies = "08 DC 05 10 DC 85 E0 80 80 80 80 80 02 18 03 20 DE 86 03".hexToBytes(),
                        lMsgUid = 1441151,
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
                svrip = 20406,
                vSyncCookie = net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY,
                vUinPairMsg = mutableListOf(),
                mPreviews = mutableMapOf(
                ),
            )
        }

        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
                addMember(1230003, "user3", MemberPermission.MEMBER).apply {
                    _muteTimestamp = currentTimeSeconds().toInt() + 600
                }
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberUnmuteEvent>(event)
            assertEquals(1230002, event.operator?.id)
        }


        setBot(1230001)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
                addMember(1230003, "user3", MemberPermission.MEMBER).apply {
                    _muteTimestamp = 0
                }
            }

        runTest().run {
            assertEquals(0, size)
        }
    }

}