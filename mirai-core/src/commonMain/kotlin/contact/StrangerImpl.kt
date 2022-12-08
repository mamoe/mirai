/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:OptIn(LowLevelApi::class)
@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "DEPRECATION_ERROR",
    "NOTHING_TO_INLINE",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE"
)

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.asFriendOrNull
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.event.events.StrangerMessagePostSendEvent
import net.mamoe.mirai.event.events.StrangerMessagePreSendEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.StrangerMessageProtocolStrategy
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToStrangerImpl
import net.mamoe.mirai.internal.message.source.createMessageReceipt
import net.mamoe.mirai.internal.network.protocol.packet.list.StrangerList
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.cast
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext


@OptIn(ExperimentalContracts::class)
internal inline fun Stranger.impl(): StrangerImpl {
    contract { returns() implies (this@impl is StrangerImpl) }
    check(this is StrangerImpl) { "A Stranger instance is not instance of StrangerImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

internal class StrangerImpl(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val info: StrangerInfo,
) : Stranger, AbstractUser(bot, parentCoroutineContext, info) {
    override val nick: String by info::nick
    override val remark: String by info::remark
    override suspend fun delete() {
        check(bot.strangers[this.id] != null) {
            "Stranger ${this.id} had already been deleted"
        }
        bot.network.sendAndExpect(StrangerList.DelStranger(bot.client, this@StrangerImpl), 5000, 2).also {
            check(it.isSuccess) { "delete Stranger $id failed: ${it.result}" }
        }
    }

    private val messageProtocolStrategy: MessageProtocolStrategy<StrangerImpl> = StrangerMessageProtocolStrategy

    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        return asFriendOrNull()?.sendMessage(message)?.convert()
            ?: sendMessageImpl(
                message = message,
                messageProtocolStrategy = messageProtocolStrategy,
                preSendEventConstructor = ::StrangerMessagePreSendEvent,
                postSendEventConstructor = ::StrangerMessagePostSendEvent.cast()
            )
    }

    private fun MessageReceipt<User>.convert(): MessageReceipt<StrangerImpl> {
        return OnlineMessageSourceToStrangerImpl(source, this@StrangerImpl).createMessageReceipt(
            this@StrangerImpl,
            doLightRefine = false //we've already did
        )
    }

    override fun toString(): String = "Stranger($id)"
}
