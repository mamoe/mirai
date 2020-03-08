@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaHappyAPI
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

/**
 * 发送消息后得到的回执. 可用于撤回.
 *
 * 此对象持有 [Contact] 的弱引用, [Bot] 离线后将会释放引用, 届时 [target] 将无法访问.
 *
 * @see Group.sendMessage 发送群消息, 返回回执（此对象）
 * @see QQ.sendMessage 发送群消息, 返回回执（此对象）
 *
 * @see MessageReceipt.sourceId 源 id
 * @see MessageReceipt.sourceSequenceId 源序列号
 * @see MessageReceipt.sourceTime 源时间
 */
@Suppress("FunctionName")
@OptIn(MiraiInternalAPI::class)
actual open class MessageReceipt<C : Contact> actual constructor(
    actual val source: MessageSource,
    target: C,
    private val botAsMember: Member?
) {
    init {
        require(target is Group || target is QQ) { "target must be either Group or QQ" }
    }

    /**
     * 发送目标, 为 [Group] 或 [QQ]
     */
    actual val target: C by target.unsafeWeakRef()

    /**
     * 是否为发送给群的消息的回执
     */
    actual val isToGroup: Boolean = botAsMember != null

    private val _isRecalled = atomic(false)

    /**
     * 撤回这条消息. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @see Bot.recall
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    actual suspend fun recall() {
        @Suppress("BooleanLiteralArgument")
        if (_isRecalled.compareAndSet(false, true)) {
            target.bot.recall(source)
        } else error("message is already or planned to be recalled")
    }

    /**
     * 在一段时间后撤回这条消息.. [recall] 或 [recallIn] 只能被调用一次.
     *
     * @param millis 延迟时间, 单位为毫秒
     * @throws IllegalStateException 当此消息已经被撤回或正计划撤回时
     */
    actual fun recallIn(millis: Long): Job {
        @Suppress("BooleanLiteralArgument")
        if (_isRecalled.compareAndSet(false, true)) {
            return when (val contact = target) {
                is QQ,
                is Group -> contact.bot.recallIn(source, millis)
                else -> error("Unknown contact type")
            }
        } else error("message is already or planned to be recalled")
    }

    /**
     * [确保 sequenceId可用][MessageSource.ensureSequenceIdAvailable] 然后引用这条消息.
     * @see MessageChain.quote 引用一条消息
     */
    actual open suspend fun quote(): QuoteReplyToSend {
        this.source.ensureSequenceIdAvailable()
        @OptIn(LowLevelAPI::class)
        return _unsafeQuote()
    }

    /**
     * 引用这条消息, 但不会 [确保 sequenceId可用][MessageSource.ensureSequenceIdAvailable].
     * 在 sequenceId 可用前就发送这条消息则会导致一个异常.
     * 当且仅当用于存储而不用于发送时使用这个方法.
     *
     * @see MessageChain.quote 引用一条消息
     */
    @LowLevelAPI
    @Suppress("FunctionName")
    actual fun _unsafeQuote(): QuoteReplyToSend {
        return this.source.quote(botAsMember as? QQ)
    }

    /**
     * 引用这条消息并回复.
     * @see MessageChain.quote 引用一条消息
     */
    @JvmName("quoteReplySuspend")
    @JvmSynthetic
    actual suspend fun quoteReply(message: MessageChain) {
        target.sendMessage(this.quote() + message)
    }


    @JavaHappyAPI
    @JvmName("quoteReply")
    fun __quoteReplyBlockingForJava__(message: Message) {
        runBlocking { quoteReply(message) }
    }

    @JavaHappyAPI
    @JvmName("recall")
    fun __recallBlockingForJava__() {
        runBlocking { recall() }
    }

    @JavaHappyAPI
    @JvmName("quote")
    fun __quoteBlockingForJava__() {
        runBlocking { quote() }
    }
}