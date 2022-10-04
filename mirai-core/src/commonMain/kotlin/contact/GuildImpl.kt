/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Guild
import net.mamoe.mirai.contact.GuildMember
import net.mamoe.mirai.data.GuildInfo
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun GuildImpl.Companion.checkIsInstance(instance: Guild) {
    contract { returns() implies (instance is GuildImpl) }
    check(instance is GuildImpl) { "guild is not an instanceof GuildImpl!! DO NOT interlace two or more protocol implementations!!" }
}

internal fun Guild.checkIsGuildImpl(): GuildImpl {
    contract { returns() implies (this@checkIsGuildImpl is GuildImpl) }
    GuildImpl.checkIsInstance(this)
    return this
}

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
) : Guild, AbstractContact(bot, parentCoroutineContext) {
    companion object;

    override val name: String by guildInfo::name

    val guildId: Long
        get() = id
    final override lateinit var owner: GuildMember
    final override lateinit var botAsMember: GuildMember
    internal val botAsMemberInitialized get() = ::botAsMember.isInitialized
    override fun get(tinyId: Long): GuildMember? {
        if (tinyId == bot.account.tinyId) return botAsMember
        return members.firstOrNull { it.id == tinyId }
    }

    override fun contains(tinyId: Long): Boolean {
        return bot.account.tinyId == tinyId || members.firstOrNull { it.id == tinyId } != null
    }

    override suspend fun quit(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Guild> {
        throw EventCancelledException("The Guild does not support sending messages, please channel them instead")
    }


    override suspend fun uploadImage(resource: ExternalResource): Image {
        throw EventCancelledException("The Guild does not support upload image, please channel them instead")
    }

}