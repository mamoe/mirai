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
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.data.FriendInfo
import net.mamoe.mirai.data.FriendInfoImpl
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentNotEmpty
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal fun net.mamoe.mirai.internal.network.protocol.data.jce.FriendInfo.toMiraiFriendInfo(): FriendInfoImpl =
    FriendInfoImpl(
        friendUin,
        nick,
        remark
    )

@OptIn(ExperimentalContracts::class)
internal inline fun FriendInfo.checkIsInfoImpl(): FriendInfoImpl {
    contract {
        returns() implies (this@checkIsInfoImpl is FriendInfoImpl)
    }
    check(this is FriendInfoImpl) { "A FriendInfo instance is not instance of checkIsInfoImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

@OptIn(ExperimentalContracts::class)
internal inline fun Friend.checkIsFriendImpl(): FriendImpl {
    contract {
        returns() implies (this@checkIsFriendImpl is FriendImpl)
    }
    check(this is FriendImpl) { "A Friend instance is not instance of FriendImpl. Your instance: ${this::class.qualifiedName}" }
    return this
}

internal class FriendImpl(
    bot: QQAndroidBot,
    coroutineContext: CoroutineContext,
    internal val friendInfo: FriendInfo
) : Friend, AbstractUser(bot, coroutineContext, friendInfo) {
    @Suppress("unused") // bug
    val lastMessageSequence: AtomicInt = atomic(-1)

    override suspend fun delete() {
        check(bot.friends[this.id] != null) {
            "Friend ${this.id} had already been deleted"
        }
        bot.network.run {
            FriendList.DelFriend.invoke(bot.client, this@FriendImpl)
                .sendAndExpect<FriendList.DelFriend.Response>().also {
                    check(it.isSuccess) { "delete friend failed: ${it.resultCode}" }
                }
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun sendMessage(message: Message): MessageReceipt<Friend> {
        require(message.isContentNotEmpty()) { "message is empty" }
        return sendMessageImpl(
            message,
            friendReceiptConstructor = { MessageReceipt(it, this) },
            tReceiptConstructor = { MessageReceipt(it, this) }
        ).also {
            logMessageSent(message)
        }
    }

    override fun toString(): String = "Friend($id)"
}
