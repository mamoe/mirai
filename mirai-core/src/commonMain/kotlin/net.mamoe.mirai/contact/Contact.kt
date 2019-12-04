@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.chain
import net.mamoe.mirai.message.singleChain
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.withSession
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * 联系人. 虽然叫做联系人, 但他的子类有 [QQ] 和 [群][Group].
 *
 * @author Him188moe
 */
interface Contact {
    /**
     * 这个联系人所属 [Bot]
     */
    val bot: Bot

    /**
     * 可以是 QQ 号码或者群号码 [GroupId].
     */
    val id: UInt

    /**
     * 向这个对象发送消息.
     *
     * 速度太快会被服务器屏蔽(无响应). 在测试中不延迟地发送 6 条消息就会被屏蔽之后的数据包 1 秒左右.
     */
    suspend fun sendMessage(message: MessageChain)


    //这两个方法应写为扩展函数, 但为方便 import 还是写在这里
    suspend fun sendMessage(plain: String) = sendMessage(plain.singleChain())

    suspend fun sendMessage(message: Message) = sendMessage(message.chain())
}

/**
 * 以 [BotSession] 作为接收器 (receiver) 并调用 [block], 返回 [block] 的返回值.
 * 这个方法将能帮助使用在 [BotSession] 中定义的一些扩展方法, 如 [BotSession.sendAndExpectAsync]
 */
@UseExperimental(ExperimentalContracts::class)
inline fun <R> Contact.withSession(block: BotSession.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return bot.withSession(block)
}

/**
 * 只读联系人列表
 */
class ContactList<C : Contact> @PublishedApi internal constructor(internal val mutable: MutableContactList<C>) : Map<UInt, C> {
    /**
     * ID 列表的字符串表示.
     * 如:
     * ```
     * [123456, 321654, 123654]
     * ```
     */
    val idContentString: String get() = this.keys.joinToString(prefix = "[", postfix = "]") { it.toLong().toString() }

    override fun toString(): String = mutable.toString()


    // TODO: 2019/12/2 应该使用属性代理, 但属性代理会导致 UInt 内联错误. 等待 kotlin 修复后替换

    override val size: Int get() = mutable.size
    override fun containsKey(key: UInt): Boolean = mutable.containsKey(key)
    override fun containsValue(value: C): Boolean = mutable.containsValue(value)
    override fun get(key: UInt): C? = mutable[key]
    override fun isEmpty(): Boolean = mutable.isEmpty()
    override val entries: MutableSet<MutableMap.MutableEntry<UInt, C>> get() = mutable.entries
    override val keys: MutableSet<UInt> get() = mutable.keys
    override val values: MutableCollection<C> get() = mutable.values
}

/**
 * 可修改联系人列表. 只会在内部使用.
 */
@PublishedApi
internal class MutableContactList<C : Contact> : MutableMap<UInt, C> {
    override fun toString(): String = asIterable().joinToString(separator = ", ", prefix = "ContactList(", postfix = ")") { it.value.toString() }


    // TODO: 2019/12/2 应该使用属性代理, 但属性代理会导致 UInt 内联错误. 等待 kotlin 修复后替换
    private val delegate = linkedMapOf<UInt, C>()

    override val size: Int get() = delegate.size
    override fun containsKey(key: UInt): Boolean = delegate.containsKey(key)
    override fun containsValue(value: C): Boolean = delegate.containsValue(value)
    override fun get(key: UInt): C? = delegate[key]
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override val entries: MutableSet<MutableMap.MutableEntry<UInt, C>> get() = delegate.entries
    override val keys: MutableSet<UInt> get() = delegate.keys
    override val values: MutableCollection<C> get() = delegate.values
    override fun clear() = delegate.clear()
    override fun put(key: UInt, value: C): C? = delegate.put(key, value)
    override fun putAll(from: Map<out UInt, C>) = delegate.putAll(from)
    override fun remove(key: UInt): C? = delegate.remove(key)
}