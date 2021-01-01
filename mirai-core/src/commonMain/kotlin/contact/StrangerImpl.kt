/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
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

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.data.FriendInfoImpl
import net.mamoe.mirai.data.StrangerInfo
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentNotEmpty
import network.protocol.packet.list.StrangerList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext


internal class StrangerInfoImpl(
    override val uin: Long, override val nick: String, override val fromGroup: Long = 0,
    override val remark: String = ""
) : StrangerInfo

@OptIn(ExperimentalContracts::class)
internal inline fun StrangerInfo.checkIsInfoImpl(): FriendInfoImpl {
    contract {
        returns() implies (this@checkIsInfoImpl is StrangerInfoImpl)
    }
    check(this is FriendInfoImpl) { "A StrangerInfo instance is not instance of StrangerInfoImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

@OptIn(ExperimentalContracts::class)
internal inline fun Stranger.checkIsImpl(): StrangerImpl {
    contract {
        returns() implies (this@checkIsImpl is StrangerImpl)
    }
    check(this is StrangerImpl) { "A Stranger instance is not instance of StrangerImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

internal class StrangerImpl(
    bot: QQAndroidBot,
    coroutineContext: CoroutineContext,
    internal val strangerInfo: StrangerInfo
) : Stranger, AbstractUser(bot, coroutineContext, strangerInfo) {
    @Suppress("unused") // bug
    val lastMessageSequence: AtomicInt = atomic(-1)
    override suspend fun delete() {
        check(bot.strangers[this.id] != null) {
            "Stranger ${this.id} had already been deleted"
        }
        bot.network.run {
            StrangerList.DelStranger(bot.client, this@StrangerImpl)
                .sendAndExpect<StrangerList.DelStranger.Response>().also {
                    check(it.isSuccess) { "delete Stranger failed: ${it.result}" }
                }
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> {
        require(message.isContentNotEmpty()) { "message is empty" }
        return sendMessageImpl(
            message,
            strangerReceiptConstructor = { MessageReceipt(it, this) },
            tReceiptConstructor = { MessageReceipt(it, this) }
        ).also {
            logMessageSent(message)
        }
    }

    override fun toString(): String = "Stranger($id)"
}
