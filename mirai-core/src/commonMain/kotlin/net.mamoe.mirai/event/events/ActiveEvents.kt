/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ


/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 */
data class BotLoginSucceedEvent(override val bot: Bot) : BotActiveEvent()

/**
 * [Bot] 主动离线.
 */
data class BotOfflineEvent(override val bot: Bot) : BotActiveEvent()

// region 好友

/**
 * [Bot] 删除一个好友
 */
class BotRemoveFriendEvent(override val friend: QQ) : FriendEvent, BotActiveEvent()

// endregion

// region 群

/**
 * 机器人踢出某个群员
 */
class BotKickMemberEvent(override val member: Member) : GroupMemberEvent, BotActiveEvent()

/**
 * 机器人禁言某个群成员
 */
class BotMuteMemberEvent(override val member: Member) : GroupMemberEvent, BotActiveEvent()

/**
 * 机器人取消禁言某个群成员
 */
class BotUnmuteMemberEvent(override val member: Member) : GroupMemberEvent, BotActiveEvent()

// endregion