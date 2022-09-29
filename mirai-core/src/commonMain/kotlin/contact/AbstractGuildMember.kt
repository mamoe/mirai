/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.data.GuildMemberInfo
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.GuildMemberInfoImpl
import kotlin.coroutines.CoroutineContext

internal sealed class AbstractGuildMember(
    bot: QQAndroidBot,
    guildId: Long,
    parentCoroutineContext: CoroutineContext,
    guildMemberInfoImpl: GuildMemberInfoImpl,
) : User, AbstractContact(bot, parentCoroutineContext) {
    final override val id: Long = guildMemberInfoImpl.tinyId
    abstract override val nick: String
    abstract override val remark: String

    open val guildMemberInfo: GuildMemberInfo = guildMemberInfoImpl
    private val guildIdByPrivate: Long = guildId
    open val guild: GuildImpl get() = bot.guilds.getOrFail(guildIdByPrivate)
}