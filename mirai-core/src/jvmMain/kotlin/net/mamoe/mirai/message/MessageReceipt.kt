@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.data.ExperimentalMessageSource
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.QuoteReply
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
actual open class MessageReceipt<out C : Contact> @OptIn(ExperimentalMessageSource::class)
actual constructor(
    actual val source: OnlineMessageSource.Outgoing,
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

    @JavaFriendlyAPI
    @JvmName("quoteReply")
    fun __quoteReplyBlockingForJava__(message: Message): MessageReceipt<C> {
        return runBlocking { return@runBlocking quoteReply(message) }
    }

    @JavaFriendlyAPI
    @JvmName("quoteReply")
    fun __quoteReplyBlockingForJava__(message: String): MessageReceipt<C> {
        return runBlocking { quoteReply(message) }
    }

    @JavaFriendlyAPI
    @JvmName("recall")
    fun __recallBlockingForJava__() {
        return runBlocking { recall() }
    }

    @JavaFriendlyAPI
    @JvmName("recall")
    fun __recallInBlockingForJava__(timeMillis: Long): Job {
        return recallIn(timeMillis = timeMillis)
    }

    @JavaFriendlyAPI
    @JvmName("quote")
    fun __quoteBlockingForJava__(): QuoteReply {
        return this.quote()
    }
}