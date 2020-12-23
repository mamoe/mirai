/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.User
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractUser(
    bot: Bot,
    coroutineContext: CoroutineContext,
    friendInfo: net.mamoe.mirai.data.FriendInfo,
) : User, AbstractContact(bot, coroutineContext) {
    final override val id: Long = friendInfo.uin
    final override val nick: String = friendInfo.nick
    final override val remark: String = friendInfo.remark
}