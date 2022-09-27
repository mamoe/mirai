/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.GuildMember
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.GuildMemberInfoImpl
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.UserNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

internal class GuildMemberImpl constructor(
    bot: QQAndroidBot,
    guildId: Long,
    parentCoroutineContext: CoroutineContext,
    guildMemberInfo: GuildMemberInfoImpl,
) : GuildMember, AbstractGuildMember(bot, guildId, parentCoroutineContext, guildMemberInfo) {
    override val nick: String get() = info.nickname
    override val remark: String get() = info.nickname
    override val tinyId: Long
        get() = bot.tinyId

    override suspend fun sendMessage(message: Message): MessageReceipt<User> {
        TODO("Not yet implemented")
    }

    override fun nudge(): UserNudge {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        TODO("Not yet implemented")
    }

    override val nameCard: String
        get() = info.nickname
    override val avatarUrl: String
        get() = ""

}