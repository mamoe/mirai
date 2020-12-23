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
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalImage
import kotlin.coroutines.CoroutineContext

internal class OtherClientImpl(
    bot: Bot,
    coroutineContext: CoroutineContext,
    override val kind: ClientKind
) : OtherClient, AbstractContact(bot, coroutineContext) {
    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(image: ExternalImage): Image {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "OtherClient(bot=${bot.id},kind=$kind)"
    }
}