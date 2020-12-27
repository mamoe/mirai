/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Deferred
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 发送消息后得到的回执. 可用于撤回, 引用回复等.
 *
 * @param source 指代发送出去的消息
 * @param target 消息发送对象
 *
 * @see quote 引用这条消息. 即引用机器人自己发出去的消息
 * @see quoteReply 引用并回复这条消息.
 * @see recallMessage 撤回这条消息
 *
 * @see Group.sendMessage 发送群消息, 返回回执（此对象）
 * @see User.sendMessage 发送群消息, 返回回执（此对象）
 * @see Member.sendMessage 发送临时消息, 返回回执（此对象）
 *
 * @see MessageReceipt.sourceIds 源 ids
 * @see MessageReceipt.sourceTime 源时间
 */
public open class MessageReceipt<out C : Contact> @MiraiInternalApi constructor(
    /**
     * 指代发送出去的消息.
     */
    public val source: OnlineMessageSource.Outgoing,
    /**
     * 发送目标, 为 [Group] 或 [Friend] 或 [Member]
     */
    public val target: C,
) {
    /**
     * 是否为发送给群的消息的回执
     */
    public val isToGroup: Boolean get() = target is Group

    /**
     * 撤回这条消息.
     *
     * @see IMirai.recallMessage
     */
    @JvmBlockingBridge
    public suspend inline fun recall() {
        return Mirai.recallMessage(target.bot, source)
    }

    /**
     * 在一段时间后撤回这条消息.
     *
     * @see IMirai.recallMessage
     */
    @Suppress("DeferredIsResult")
    public fun recallIn(millis: Long): Deferred<Unit> = this.source.recallIn(millis)

    /**
     * 引用这条消息.
     * @see MessageSource.quote 引用一条消息
     */
    public fun quote(): QuoteReply = this.source.quote()

    /**
     * 引用这条消息并回复.
     * @see MessageSource.quote 引用一条消息
     */
    @JvmBlockingBridge
    public suspend inline fun quoteReply(message: Message): MessageReceipt<C> {
        @Suppress("UNCHECKED_CAST")
        return target.sendMessage(this.quote() + message) as MessageReceipt<C>
    }

    /**
     * 引用这条消息并回复.
     * @see MessageSource.quote 引用一条消息
     */
    @JvmBlockingBridge
    public suspend inline fun quoteReply(message: String): MessageReceipt<C> {
        return this.quoteReply(PlainText(message))
    }

    public companion object
}

/**
 * 获取相关 [Bot]
 */
public inline val MessageReceipt<*>.bot: Bot
    get() = target.bot

/**
 * 获取源消息 [MessageSource.ids]
 */
public inline val MessageReceipt<*>.sourceIds: IntArray
    get() = this.source.ids

/**
 * 获取源消息 [MessageSource.internalIds]
 */
public inline val MessageReceipt<*>.sourceInternalIds: IntArray
    get() = this.source.internalIds

/**
 * 获取源消息 [MessageSource.time]
 */
public inline val MessageReceipt<*>.sourceTime: Int
    get() = this.source.time

/**
 * 获取源消息 [MessageSource.originalMessage]
 */
public inline val MessageReceipt<*>.sourceMessage: MessageChain
    get() = this.source.originalMessage
