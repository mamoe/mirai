/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GuildInfo
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

internal expect class GuildImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    id: Long,
    guildInfo: GuildInfo,
    members: ContactList<GuildMemberImpl>,
    channelNodes: List<ChannelImpl>,
) : Guild, CommonGuildImpl {
    companion object;
}

@Suppress("PropertyName")
internal abstract class CommonGuildImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val id: Long,
    guildInfo: GuildInfo,
    override val channelNodes: List<ChannelImpl>,
    override val members: ContactList<GuildMemberImpl>,
) : Guild{
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val guildCode: Long
        get() = TODO("Not yet implemented")
    override val owner: GuildMember
        get() = TODO("Not yet implemented")
    override val botAsMember: GuildMember
        get() = TODO("Not yet implemented")

    override fun get(id: Long): GuildMember? {
        TODO("Not yet implemented")
    }

    override fun contains(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun quit(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: String): MessageReceipt<Contact> {
        return super.sendMessage(message)
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        TODO("Not yet implemented")
    }

    override val bot: Bot
        get() = TODO("Not yet implemented")
}