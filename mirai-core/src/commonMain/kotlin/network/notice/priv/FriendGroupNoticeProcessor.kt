/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.priv

import net.mamoe.mirai.internal.contact.friendgroup.FriendGroupImpl
import net.mamoe.mirai.internal.contact.friendgroup.impl
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.info.FriendGroupInfo
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.context
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.warning

internal class FriendGroupNoticeProcessor(
    private val logger: MiraiLogger,
) : MixedNoticeProcessor(), NewContactSupport {

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) = data.context {
        when (data.uSubMsgType) {
            0x27L -> {
                val body = vProtobuf.loadAs(Submsgtype0x27.SubMsgType0x27.SubMsgType0x27MsgBody.serializer())
                for (msgModInfo in body.msgModInfos) {
                    markAsConsumed(msgModInfo)
                    when {
                        msgModInfo.msgModFriendGroup != null -> handleFriendGroupChanged(
                            msgModInfo.msgModFriendGroup, logger
                        )
                        msgModInfo.msgModGroupName != null -> handleFriendGroupNameChanged(
                            msgModInfo.msgModGroupName, logger
                        )
                        msgModInfo.msgDelGroup != null -> handleDelGroup(msgModInfo.msgDelGroup, logger)
                        msgModInfo.msgAddGroup != null -> handleAddGroup(msgModInfo.msgAddGroup)
                        else -> markNotConsumed(msgModInfo)
                    }
                }
            }
        }
    }

    private fun NoticePipelineContext.handleAddGroup(
        addGroup: Submsgtype0x27.SubMsgType0x27.AddGroup
    ) {
        bot.friendGroups.friendGroups.add(
            FriendGroupImpl(
                bot,
                FriendGroupInfo(addGroup.groupid, addGroup.groupname.decodeToString())
            )
        )
    }

    private fun NoticePipelineContext.handleDelGroup(
        delGroup: Submsgtype0x27.SubMsgType0x27.DelGroup, logger: MiraiLogger
    ) {
        bot.friendGroups[delGroup.groupid]?.let { friendGroup ->
            friendGroup.friends.forEach {
                it.impl().info.friendGroupId = 0
            }
            bot.friendGroups.friendGroups.remove(friendGroup)
        } ?: let {
            logger.warning { "Detected friendGroup(id=${delGroup.groupid}) was removed but it isn't available in bot's friendGroups list" }
            return
        }
    }

    private fun NoticePipelineContext.handleFriendGroupNameChanged(
        modFriendGroup: Submsgtype0x27.SubMsgType0x27.ModGroupName, logger: MiraiLogger
    ) {
        bot.friendGroups[modFriendGroup.groupid]?.let {
            it.impl().name = modFriendGroup.groupname.decodeToString()
        } ?: let {
            logger.warning { "Detected friendGroup(id=${modFriendGroup.groupid}) was renamed but it cannot be found in bot's friendGroups list" }
            return
        }
    }

    private fun NoticePipelineContext.handleFriendGroupChanged(
        modFriendGroup: Submsgtype0x27.SubMsgType0x27.ModFriendGroup,
        logger: MiraiLogger
    ) {
        modFriendGroup.msgFrdGroup.forEach { body ->
            val friend = bot.getFriend(body.fuin) ?: let {
                logger.error { "Detected friend(id=${body.fuin}) was moved to friendGroup(id=${body.uint32NewGroupId}) but friend not found in bot's friends list" }
                return
            }
            if (friend.impl().info.friendGroupId == body.uint32NewGroupId.first()) return@forEach
            friend.info.friendGroupId = body.uint32NewGroupId.first()
        }
    }
}