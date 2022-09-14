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
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.GuildMember
import net.mamoe.mirai.data.GuildMemberInfo
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext

internal class  GuildMemberImpl constructor(
    guild: GuildImpl,
    parentCoroutineContext: CoroutineContext,
    guildMemberInfo: GuildMemberInfo,
) : GuildMember {

    override val bot: Bot
        get() = TODO("Not yet implemented")

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        TODO("Not yet implemented")
    }

    override val id: Long
        get() = TODO("Not yet implemented")
    override val nameCard: String
        get() = TODO("Not yet implemented")
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")

}