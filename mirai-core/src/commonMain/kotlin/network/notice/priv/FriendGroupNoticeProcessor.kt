/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.priv

import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.context
import net.mamoe.mirai.utils.error

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
                            msgModInfo.msgModFriendGroup,
                            logger
                        )
                        msgModInfo.msgModGroupName != null -> handleFriendGroupNameChanged(
                            msgModInfo.msgModGroupName,
                            logger
                        )
                        else -> markNotConsumed(msgModInfo)
                    }
                }
            }
        }
    }

    private fun NoticePipelineContext.handleFriendGroupNameChanged(
        modFriendGroup: Submsgtype0x27.SubMsgType0x27.ModGroupName,
        logger: MiraiLogger
    ) {
        bot.friendGroups[modFriendGroup.groupid]?.let {
            it.impl().name = modFriendGroup.groupname.toString()
        } ?: let {
            logger.error { "fail to find FriendGroup(id=${modFriendGroup.groupid}) in Bot(id=${bot.id})" }
            return
        }
    }

    private fun NoticePipelineContext.handleFriendGroupChanged(
        modFriendGroup: Submsgtype0x27.SubMsgType0x27.ModFriendGroup,
        logger: MiraiLogger
    ) {
        modFriendGroup.msgFrdGroup.forEach { body ->
            val friend = bot.getFriend(body.fuin) ?: let {
                logger.error { "fail to find Friend(id=${body.fuin}) in Bot(id=${bot.id})" }
                return
            }
            bot.friendGroups[friend.impl().info.friendGroupId]?.let {
                // don't care result
                it.impl().friends.delegate.remove(friend.impl())
            } ?: let {
                logger.error { "fail to find FriendGroup(id=${body.fuin}) in Bot(id=${bot.id})" }
                return
            }
            friend.info.friendGroupId = body.uint32NewGroupId.first()
            bot.friendGroups[friend.info.friendGroupId]?.let {
                // don't care result
                it.impl().friends.delegate.add(friend)
            } ?: let {
                logger.error { "fail to find FriendGroup(id=${body.fuin}) in Bot(id=${bot.id})" }
                return
            }
        }
    }
}