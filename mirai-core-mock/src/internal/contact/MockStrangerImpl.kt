/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockStranger
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcFromStranger
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcToStranger
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.lateinitMutableProperty
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

internal class MockStrangerImpl(
    parentCoroutineContext: CoroutineContext,
    bot: MockBot,
    id: Long,

    remark: String,
    nick: String
) : AbstractMockContact(parentCoroutineContext, bot, id), MockStranger {

    override val mockApi: MockStranger.MockApi = object : MockStranger.MockApi {
        override val contact: MockStranger get() = this@MockStrangerImpl
        override var nick: String = nick
        override var remark: String = remark
    }
    override var avatarUrl: String by lateinitMutableProperty { runBlocking { MockImage.random(bot).getUrl(bot) } }

    override val nick: String
        get() = mockApi.nick
    override val remark: String
        get() = mockApi.remark

    override fun newMessagePreSend(message: Message): MessagePreSendEvent {
        return StrangerMessagePreSendEvent(this, message)
    }

    override suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>) {
        StrangerMessagePostSendEvent(this, message, null, receipt.cast()).broadcast()
    }

    override fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing {
        return newMsgSrc(false, message) { ids, internalIds, time ->
            OnlineMsgSrcToStranger(ids, internalIds, time, message, bot, bot, this)
        }
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        return super<AbstractMockContact>.sendMessage(message).cast()
    }

    override suspend fun delete() {
        if (bot.strangers.delegate.remove(this)) {
            StrangerRelationChangeEvent.Deleted(this).broadcast()
            cancel(CancellationException("Stranger deleted"))
        }
    }

    override suspend fun says(message: MessageChain): MessageChain {
        val src = newMsgSrc(true, message) { ids, internalIds, time ->
            OnlineMsgSrcFromStranger(ids, internalIds, time, message, bot, this)
        }
        val msg = src withMessage message
        StrangerMessageEvent(this, msg, src.time).broadcast()
        return msg
    }
}