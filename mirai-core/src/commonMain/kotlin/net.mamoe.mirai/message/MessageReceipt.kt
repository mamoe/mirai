/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.recallIn
import kotlin.jvm.JvmSynthetic

/**
 * 发送消息后得到的回执. 可用于撤回.
 *
 * 此对象持有 [Contact] 的弱引用, [Bot] 离线后将会释放引用, 届时 [target] 将无法访问.
 *
 * @param source 指代发送出去的消息
 * @param target 消息发送对象
 *
 * @see Group.sendMessage 发送群消息, 返回回执（此对象）
 * @see QQ.sendMessage 发送群消息, 返回回执（此对象）
 *
 * @see MessageReceipt.sourceId 源 id
 * @see MessageReceipt.sourceSequenceId 源序列号
 * @see MessageReceipt.sourceTime 源时间
 */
expect open class MessageReceipt<out C : Contact> @OptIn(ExperimentalMessageSource::class) constructor(
    source: MessageSource,
    target: C,
    botAsMember: Member?
) {
    /**
     * 指代发送出去的消息
     */
    @ExperimentalMessageSource
    val source: MessageSource

    /**
     * 发送目标, 为 [Group] 或 [QQ]
     */
    val target: C

    /**
     * 是否为发送给群的消息的回执
     */
    val isToGroup: Boolean

    /**
     * 撤回这条消息. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @see Bot.recall
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    suspend fun recall()

    /**
     * 在一段时间后撤回这条消息.. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @param millis 延迟时间, 单位为毫秒
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    fun recallIn(millis: Long): Job

    /**
     * [确保 sequenceId可用][MessageSource.ensureSequenceIdAvailable] 然后引用这条消息.
     * @see MessageChain.quote 引用一条消息
     */
    open suspend fun quote(): QuoteReplyToSend

    /**
     * 引用这条消息, 但不会 [确保 sequenceId可用][MessageSource.ensureSequenceIdAvailable].
     * 在 sequenceId 可用前就发送这条消息则会导致一个异常.
     * 当且仅当用于存储而不用于发送时使用这个方法.
     *
     * @see MessageChain.quote 引用一条消息
     */
    @LowLevelAPI
    @Suppress("FunctionName")
    fun _unsafeQuote(): QuoteReplyToSend

    /**
     * 引用这条消息并回复.
     * @see MessageChain.quote 引用一条消息
     */
    suspend fun quoteReply(message: MessageChain)
}

/**
 * 获取源消息 [MessageSource.id]
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
@ExperimentalMessageSource
inline val MessageReceipt<*>.sourceId: Long
    get() = this.source.id

/**
 * 获取源消息 [MessageSource.sequenceId]
 *
 * @see MessageSource.sequenceId
 */
@get:JvmSynthetic
@ExperimentalMessageSource
inline val MessageReceipt<*>.sourceSequenceId: Int
    get() = this.source.sequenceId

/**
 * 获取源消息 [MessageSource.time]
 *
 * @see MessageSource.time
 */
@get:JvmSynthetic
@ExperimentalMessageSource
inline val MessageReceipt<*>.sourceTime: Long
    get() = this.source.time

suspend inline fun MessageReceipt<*>.quoteReply(message: Message) {
    return this.quoteReply(message.asMessageChain())
}

suspend inline fun MessageReceipt<*>.quoteReply(message: String) {
    return this.quoteReply(message.toMessage().asMessageChain())
}

