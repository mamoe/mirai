/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

/**
 * 发送消息后得到的回执. 可用于撤回.
 *
 * 此对象持有 [Contact] 的弱引用, [Bot] 离线后将会释放引用, 届时 [target] 将无法访问.
 *
 * @see Group.sendMessage 发送群消息, 返回回执（此对象）
 * @see QQ.sendMessage 发送群消息, 返回回执（此对象）
 */
open class MessageReceipt<C : Contact>(
    private val source: MessageSource,
    target: C
) {
    init {
        require(target is Group || target is QQ) { "target must be either Group or QQ" }
    }

    /**
     * 发送目标, 为 [Group] 或 [QQ]
     */
    val target: C by target.unsafeWeakRef()

    private val _isRecalled = atomic(false)

    /**
     * 撤回这条消息. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @see Group.recall
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    @UseExperimental(MiraiExperimentalAPI::class)
    suspend fun recall() {
        @Suppress("BooleanLiteralArgument")
        if (_isRecalled.compareAndSet(false, true)) {
            when (val contact = target) {
                is Group -> {
                    contact.bot.recall(source)
                }
                is QQ -> {
                    TODO()
                }
                else -> error("Unknown contact type")
            }
        } else error("message is already or planned to be recalled")
    }

    /**
     * 撤回这条消息. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @param millis 延迟时间, 单位为毫秒
     *
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    @UseExperimental(MiraiExperimentalAPI::class)
    fun recallIn(millis: Long): Job {
        @Suppress("BooleanLiteralArgument")
        if (_isRecalled.compareAndSet(false, true)) {
            when (val contact = target) {
                is Group -> {
                    return contact.bot.recallIn(source, millis)
                }
                is QQ -> {
                    TODO()
                }
                else -> error("Unknown contact type")
            }
        } else error("message is already or planned to be recalled")
    }

    /**
     * 引用这条消息. 仅群消息能被引用
     *
     * @see MessageChain.quote 引用一条消息
     *
     * @throws IllegalStateException 当此消息不是群消息时
     */
    @MiraiExperimentalAPI("unstable")
    open fun quote(): QuoteReplyToSend {
        val target = target
        check(target is Group) { "quote is only available for GroupMessage" }
        return this.source.quote(target.botAsMember)
    }

    /**
     * 引用这条消息并回复. 仅群消息能被引用
     *
     * @see MessageChain.quote 引用一条消息
     *
     * @throws IllegalStateException 当此消息不是群消息时
     */
    @MiraiExperimentalAPI("unstable")
    suspend fun quoteReply(message: MessageChain) {
        target.sendMessage(this.quote() + message)
    }
}

@MiraiExperimentalAPI("unstable")
suspend inline fun MessageReceipt<out Contact>.quoteReply(message: Message) {
    return this.quoteReply(message.toChain())
}

@MiraiExperimentalAPI("unstable")
suspend inline fun MessageReceipt<out Contact>.quoteReply(message: String) {
    return this.quoteReply(message.toMessage().toChain())
}

