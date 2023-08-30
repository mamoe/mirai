/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.buildTypeSafeMap
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class NewFriendRequestTest : AbstractNoticeProcessorTest() {
    @Test
    fun `new friend request from search test`() = runBlockingUnit {

        setBot(114514)

        use(attributes = buildTypeSafeMap { set(NewContact.SYSTEM_MSG_TYPE, 0) }) {
            mockTime += 1000
            Structmsg.StructMsg(
                version = 1,
                msgType = 2,
                msgSeq = mockTime * 1000,
                msgTime = mockTime,
                reqUin = 123456,
                unreadFlag = 0,
                msg = Structmsg.SystemMsg(
                    subType = 1,
                    msgTitle = "好友申请",
                    msgDescribe = "请求加为好友",
                    msgAdditional = "我是颠佬",
                    msgSource = "QQ群",
                    msgDecided = "",
                    srcId = 3004,
                    subSrcId = 2,
                    groupCode = 1,
                    actionUin = 0,
                    groupMsgType = 0,
                    groupInviterRole = 0,
                    friendInfo = Structmsg.FriendInfo(
                        msgJointFriend = "1 个共同好友",
                        msgBlacklist = "设为黑名单后你将拒绝对方，并不再接收此人请求。"
                    ),
                    groupInfo = null,
                    actorUin = 0,
                    msgActorDescribe = "",
                    msgAdditionalList = "",
                    relation = 0,
                    reqsubtype = 0,
                    cloneUin = 0,
                    discussUin = 0,
                    eimGroupId = 0,
                    msgInviteExtinfo = null,
                    msgPayGroupExtinfo = null,
                    sourceFlag = 1,
                    gameNick = byteArrayOf(),
                    gameMsg = byteArrayOf(),
                    groupFlagext3 = 0,
                    groupOwnerUin = 0,
                    doubtFlag = 0,
                    warningTips = byteArrayOf(),
                    nameMore = byteArrayOf(),
                    reqUinFaceid = 21762,
                    reqUinNick = "颠佬",
                    groupName = "",
                    actionUinNick = "",
                    msgQna = "",
                    msgDetail = "",
                    groupExtFlag = 0,
                    actorUinNick = "",
                    picUrl = "",
                    cloneUinNick = "",
                    reqUinBusinessCard = "",
                    eimGroupIdName = "",
                    reqUinPreRemark = "",
                    actionUinQqNick = "",
                    actionUinRemark = "",
                    reqUinGender = 255,
                    reqUinAge = 0,
                    c2cInviteJoinGroupFlag = 0,
                    cardSwitch = 0,
                    actions = listOf(
                        Structmsg.SystemMsgAction(
                            name = "同意",
                            result = "已同意",
                            action = 1,
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 2,
                                groupCode = 0,
                                sig = byteArrayOf(),
                                msg = "",
                                groupId = 0,
                                remark = "",
                                blacklist = false,
                                addFrdSNInfo = null
                            ),
                            detailName = "同意"
                        ),
                        Structmsg.SystemMsgAction(
                            name = "拒绝",
                            result = "已拒绝",
                            action = 1,
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 3,
                                groupCode = 0,
                                sig = byteArrayOf(),
                                msg = "",
                                groupId = 0,
                                remark = "",
                                blacklist = false,
                                addFrdSNInfo = null
                            ),
                            detailName = "拒绝"
                        ),
                    )
                )
            )

        }.run {
            assertEquals(1, size, toString())
            val event = single()
            assertIs<NewFriendRequestEvent>(event)

            assertEquals(123456, event.fromId)
            assertEquals("颠佬", event.fromNick)
        }
    }@Test
    fun `new friend request test`() = runBlockingUnit {

        setBot(114514)

        use(attributes = buildTypeSafeMap { set(NewContact.SYSTEM_MSG_TYPE, 0) }) {
            mockTime += 1000
            Structmsg.StructMsg(
                version = 1,
                msgType = 1,
                msgSeq = mockTime * 1000,
                msgTime = mockTime,
                reqUin = 654321,
                unreadFlag = 0,
                msg = Structmsg.SystemMsg(
                    subType = 1,
                    msgTitle = "好友申请",
                    msgDescribe = "请求加为好友1",
                    msgAdditional = "",
                    msgSource = "QQ群-%group_name%",
                    msgDecided = "",
                    srcId = 6,
                    subSrcId = 2,
                    groupCode = 111111,
                    actionUin = 0,
                    groupMsgType = 0,
                    groupInviterRole = 0,
                    friendInfo = Structmsg.FriendInfo(
                        msgJointFriend = "2 个共同好友",
                        msgBlacklist = "设为黑名单后你将拒绝对方，并不再接收此人请求。"
                    ),
                    groupInfo = Structmsg.GroupInfo(
                        groupAuthType = 0,
                        displayAction = 0,
                        msgAlert = "",
                        msgDetailAlert = "",
                        msgOtherAdminDone = "",
                        appPrivilegeFlag = 67633344
                    ),
                    actorUin = 0,
                    msgActorDescribe = "",
                    msgAdditionalList = "",
                    relation = 0,
                    reqsubtype = 0,
                    cloneUin = 0,
                    discussUin = 0,
                    eimGroupId = 0,
                    msgInviteExtinfo = null,
                    msgPayGroupExtinfo = null,
                    sourceFlag = 1,
                    gameNick = byteArrayOf(),
                    gameMsg = byteArrayOf(),
                    groupFlagext3 = 0,
                    groupOwnerUin = 0,
                    doubtFlag = 0,
                    warningTips = byteArrayOf(),
                    nameMore = byteArrayOf(),
                    reqUinFaceid = 0,
                    reqUinNick = "颠佬2",
                    groupName = "%group_name%",
                    actionUinNick = "",
                    msgQna = "",
                    msgDetail = "",
                    groupExtFlag = 1076036672,
                    actorUinNick = "",
                    picUrl = "",
                    cloneUinNick = "",
                    reqUinBusinessCard = "",
                    eimGroupIdName = "",
                    reqUinPreRemark = "",
                    actionUinQqNick = "",
                    actionUinRemark = "",
                    reqUinGender = 0,
                    reqUinAge = 0,
                    c2cInviteJoinGroupFlag = 0,
                    cardSwitch = 0,
                    actions = listOf(
                        Structmsg.SystemMsgAction(
                            name = "同意",
                            result = "已同意",
                            action = 1,
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 2,
                                groupCode = 0,
                                sig = byteArrayOf(),
                                msg = "",
                                groupId = 0,
                                remark = "",
                                blacklist = false,
                                addFrdSNInfo = null
                            ),
                            detailName = "同意"
                        ),
                        Structmsg.SystemMsgAction(
                            name = "拒绝",
                            result = "已拒绝",
                            action = 1,
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 3,
                                groupCode = 0,
                                sig = byteArrayOf(),
                                msg = "",
                                groupId = 0,
                                remark = "",
                                blacklist = false,
                                addFrdSNInfo = null
                            ),
                            detailName = "拒绝"
                        ),
                    )
                )
            )

        }.run {
            assertEquals(1, size, toString())
            val event = single()
            assertIs<NewFriendRequestEvent>(event)

            assertEquals(654321, event.fromId)
            assertEquals("颠佬2", event.fromNick)
            assertEquals(111111, event.fromGroupId)
        }
    }
}