/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.internal.contact.broadcastMessagePreSendEvent
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockContact
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

internal abstract class AbstractMockContact(
    parentCoroutineContext: CoroutineContext,
    override val bot: MockBot,
    override val id: Long
) : MockContact {

    override val coroutineContext: CoroutineContext = parentCoroutineContext.childScopeContext()

    /**
     * @return isCancelled
     */
    protected abstract fun newMessagePreSend(message: Message): MessagePreSendEvent
    protected abstract suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>)

    protected abstract fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        val msg = broadcastMessagePreSendEvent(message, false) { _, _ -> newMessagePreSend(message) }

        val source = newMessageSource(msg)
        val response = source.withMessage(msg)

        bot.logger.verbose("$this <- $msg".replaceMagicCodes())


        @Suppress("DEPRECATION_ERROR")
        return MessageReceipt(source, this).also {
            postMessagePreSend(response, it)
        }
    }


    override suspend fun uploadImage(resource: ExternalResource): Image {
        return bot.uploadMockImage(resource)
    }

    override suspend fun uploadShortVideo(
        thumbnail: ExternalResource,
        video: ExternalResource,
        fileName: String?
    ): ShortVideo {
        TODO("mock upload short video")
    }

    override fun toString(): String {
        return "$id"
    }
}

internal suspend inline fun <T : ExternalResource, R> T.inResource(action: () -> R): R {
    return useAutoClose {
        runBIO {
            inputStream().dropContent(close = true)
        }
        action()
    }
}
