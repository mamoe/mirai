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

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmSynthetic

/**
 * 发送消息后得到的回执. 可用于撤回, 引用回复等.
 *
 * @param source 指代发送出去的消息
 * @param target 消息发送对象
 *
 * @see quote 引用这条消息. 即引用机器人自己发出去的消息
 * @see quoteReply 引用并回复这条消息.
 * @see recall 撤回这条消息
 *
 * @see Group.sendMessage 发送群消息, 返回回执（此对象）
 * @see User.sendMessage 发送群消息, 返回回执（此对象）
 * @see Member.sendMessage 发送临时消息, 返回回执（此对象）
 *
 * @see MessageReceipt.sourceId 源 id
 * @see MessageReceipt.sourceTime 源时间
 */
public open class MessageReceipt<out C : Contact> @MiraiExperimentalApi("The constructor is subject to change.") constructor(
    /**
     * 指代发送出去的消息.
     */
    public val source: OnlineMessageSource.Outgoing,
    /**
     * 发送目标, 为 [Group] 或 [Friend] 或 [Member]
     */
    public val target: C,

    /**
     * @see Group.botAsMember
     */
    @MiraiExperimentalApi("This is subject to change.")
    public val botAsMember: Member?
) {
    /**
     * 是否为发送给群的消息的回执
     */
    public val isToGroup: Boolean get() = target is Group
}

/**
 * 撤回这条消息. [recall] 或 [recallIn] 只能被调用一次.
 *
 * @see Mirai.recall
 * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
 */
public suspend inline fun MessageReceipt<*>.recall() {
    return Mirai.recall(target.bot, source)
}

/**
 * 引用这条消息.
 * @see MessageChain.quote 引用一条消息
 */
@JvmSynthetic
public inline fun MessageReceipt<*>.quote(): QuoteReply = this.source.quote()

/**
 * 引用这条消息并回复.
 * @see MessageChain.quote 引用一条消息
 */
@JvmSynthetic
public suspend inline fun <C : Contact> MessageReceipt<C>.quoteReply(message: Message): MessageReceipt<C> {
    @Suppress("UNCHECKED_CAST")
    return target.sendMessage(this.quote() + message) as MessageReceipt<C>
}

/**
 * 引用这条消息并回复.
 * @see MessageChain.quote 引用一条消息
 */
@JvmSynthetic
public suspend inline fun <C : Contact> MessageReceipt<C>.quoteReply(message: String): MessageReceipt<C> {
    return this.quoteReply(PlainText(message))
}


/**
 * 获取源消息 [MessageSource.id]
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
public inline val MessageReceipt<*>.sourceId: Int
    get() = this.source.id


/**
 * 获取源消息 [MessageSource.internalId]
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
public inline val MessageReceipt<*>.sourceInternalId: Int
    get() = this.source.internalId

/**
 * 获取源消息 [MessageSource.time]
 *
 * @see MessageSource.time
 */
@get:JvmSynthetic
public inline val MessageReceipt<*>.sourceTime: Int
    get() = this.source.time

