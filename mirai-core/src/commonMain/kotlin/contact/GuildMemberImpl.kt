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
import net.mamoe.mirai.data.GuildMemberInfo
import net.mamoe.mirai.event.events.DirectMessagePostSendEvent
import net.mamoe.mirai.event.events.DirectMessagePreSendEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.info.GuildMemberInfoImpl
import net.mamoe.mirai.internal.message.protocol.outgoing.DirectMessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.UserNudge
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.cast
import kotlin.coroutines.CoroutineContext

internal class GuildMemberImpl constructor(
    bot: QQAndroidBot,
    guildId: Long,
    parentCoroutineContext: CoroutineContext,
    override val guildMemberInfo: GuildMemberInfoImpl,
) : GuildMember, AbstractGuildMember(bot, guildId, parentCoroutineContext, guildMemberInfo) {
    override val nick: String get() = guildMemberInfo.nickname
    override val remark: String get() = guildMemberInfo.nickname

    private val messageProtocolStrategy: MessageProtocolStrategy<GuildMemberImpl> =
        DirectMessageProtocolStrategy(this.cast())
    val info: GuildMemberInfo get() = guildMemberInfo
    override val selfTinyId: Long
        get() = bot.selfTinyId

    override suspend fun sendMessage(message: Message): MessageReceipt<GuildMember> {
        return sendMessageImpl(
            message,
            messageProtocolStrategy.cast(),
            ::DirectMessagePreSendEvent,
            ::DirectMessagePostSendEvent.cast()
        )
    }

    override fun nudge(): UserNudge {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "GuildMember(id=${info.tinyId})"
    }

    override val nameCard: String
        get() = info.nickname
}