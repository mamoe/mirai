/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl

///////////////////////////////////////////////////////////////////////////
// Extension interfaces ---- should convert to context receivers in the future.
///////////////////////////////////////////////////////////////////////////

internal interface BotAware : PrivateContactSupport {
    override val bot: QQAndroidBot
}

internal interface GroupAware : GroupMemberSupport, BotAware {
    override val group: GroupImpl
    override val bot: QQAndroidBot get() = group.bot
}

internal interface PrivateContactSupport {
    val bot: QQAndroidBot

    fun Long.findFriend() = bot.friends[this]
    fun Long.findStranger() = bot.strangers[this]
    fun Long.findFriendOrStranger() = findFriend() ?: findStranger()
    fun String.findFriend() = this.toLongOrNull()?.findFriend()
    fun String.findStranger() = this.toLongOrNull()?.findStranger()
    fun String.findFriendOrStranger() = this.toLongOrNull()?.findFriendOrStranger()
}

internal interface GroupMemberSupport {
    val group: GroupImpl

    fun Long.findMember() = group[this]
    fun String.findMember() = this.toLongOrNull()?.findMember()
}