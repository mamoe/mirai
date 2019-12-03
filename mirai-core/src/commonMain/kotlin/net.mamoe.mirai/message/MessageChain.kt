package net.mamoe.mirai.message

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.js.JsName
import kotlin.jvm.Volatile
import kotlin.reflect.KProperty

/**
 * 消息链. 即 MutableList<Message>.
 * 它的一般实现为 [MessageChainImpl], `null` 实现为 [NullMessageChain]
 *
 * 有关 [MessageChain] 的创建和连接:
 * - 当任意两个不是 [MessageChain] 的 [Message] 相连接后, 将会产生一个 [MessageChain].
 * - 若两个 [MessageChain] 连接, 后一个将会被合并到第一个内.
 * - 若一个 [MessageChain] 与一个其他 [Message] 连接, [Message] 将会被添加入 [MessageChain].
 * - 若一个 [Message] 与一个 [MessageChain] 连接, [Message] 将会被添加入 [MessageChain].
 */
interface MessageChain : Message, MutableList<Message> {
    // region Message override
    override val stringValue: String

    override operator fun contains(sub: String): Boolean
    override fun concat(tail: Message): MessageChain
    // endregion

    operator fun plusAssign(message: Message) {
        this.concat(message)
    }

    operator fun plusAssign(plain: String) {
        this.plusAssign(plain.toMessage())
    }

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [PlainText.Key]
     */
    operator fun <M : Message> get(key: Message.Key<M>): M = first(key)

}

/**
 * 提供一个类型的值. 若不存在则会抛出异常 [NoSuchElementException]
 */
inline operator fun <reified T : Message> MessageChain.getValue(thisRef: Any?, property: KProperty<*>): T = this.first<T>() as T

/**
 * 构造无初始元素的可修改的 [MessageChain]. 初始大小将会被设定为 8
 */
@JsName("emptyMessageChain")
@Suppress("FunctionName")
fun MessageChain(): MessageChain = EmptyMessageChain()

/**
 * 构造无初始元素的可修改的 [MessageChain]. 初始大小将会被设定为 [initialCapacity]
 */
@Suppress("FunctionName")
fun MessageChain(initialCapacity: Int): MessageChain =
    if (initialCapacity == 0) EmptyMessageChain()
    else MessageChainImpl(ArrayList(initialCapacity))

/**
 * 构造 [MessageChain]
 * 若仅提供一个参数, 请考虑使用 [Message.chain] 以优化性能
 */
@Suppress("FunctionName")
fun MessageChain(vararg messages: Message): MessageChain =
    if (messages.isEmpty()) EmptyMessageChain()
    else MessageChainImpl(messages.toMutableList())

/**
 * 构造 [MessageChain]
 */
@Suppress("FunctionName")
fun MessageChain(messages: Iterable<Message>): MessageChain = MessageChainImpl(messages.toMutableList())

/**
 * 构造单元素的不可修改的 [MessageChain]. 内部类实现为 [SingleMessageChain]
 *
 * 参数 [delegate] 不能为 [MessageChain] 的实例, 否则将会抛出异常.
 * 使用 [Message.chain] 将帮助提前处理这个问题.
 *
 * @param delegate 所构造的单元素 [MessageChain] 代表的 [Message]
 * @throws IllegalArgumentException 当 [delegate] 为 [MessageChain] 的实例时
 *
 * @see Message.chain receiver 模式
 */
@MiraiExperimentalAPI
@UseExperimental(ExperimentalContracts::class)
@Suppress("FunctionName")
fun SingleMessageChain(delegate: Message): MessageChain {
    contract {
        returns() implies (delegate !is MessageChain)
    }
    require(delegate !is MessageChain) { "delegate for SingleMessageChain should not be any instance of MessageChain" }
    return SingleMessageChainImpl(delegate)
}


/**
 * 得到包含 [this] 的 [MessageChain].
 * 若 [this] 为 [MessageChain] 将直接返回 this
 * 否则将调用 [MessageChain] 构造一个 [MessageChainImpl]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Message.chain(): MessageChain = if (this is MessageChain) this else MessageChain(this)

/**
 * 构造 [MessageChain]
 */
@Suppress("unused", "NOTHING_TO_INLINE")
inline fun List<Message>.messageChain(): MessageChain = MessageChain(this)


/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
inline fun <reified M : Message> MessageChain.firstOrNull(): Message? = this.firstOrNull { it is M }

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
inline fun <reified M : Message> MessageChain.first(): Message = this.first { it is M }

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
inline fun <reified M : Message> MessageChain.any(): Boolean = this.firstOrNull { it is M } !== null


/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.firstOrNull(key: Message.Key<M>): M? = when (key) {
    At -> first<At>()
    PlainText -> first<PlainText>()
    Image -> first<Image>()
    Face -> first<Face>()
    else -> null
} as M

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.first(key: Message.Key<M>): M = firstOrNull(key) ?: error("unknown key: $key")

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.any(key: Message.Key<M>): Boolean = firstOrNull(key) != null

/**
 * 空的 [Message].
 *
 * 它不包含任何元素, 但维护一个 'lazy' 的 [MessageChainImpl].
 *
 * 只有在必要的时候(如迭代([iterator]), 插入([add]), 连接([concat], [plus], [plusAssign]))才会创建这个对象代表的 list
 *
 * 它是一个正常的 [Message] 和 [MessageChain]. 可以做所有 [Message] 能做的事.
 */
class EmptyMessageChain : MessageChain {
    private val delegate: MessageChain by lazy {
        MessageChainImpl().also { initialized = true }
    }

    @Volatile
    private var initialized: Boolean = false

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Message> =
        if (initialized) delegate.subList(
            fromIndex,
            toIndex
        ) else throw IndexOutOfBoundsException("given args that from $fromIndex to $toIndex, but the list is empty")

    override val stringValue: String
        get() = if (initialized) delegate.stringValue else ""

    override fun toString(): String = stringValue

    override fun contains(sub: String): Boolean = if (initialized) delegate.contains(sub) else false
    override fun contains(element: Message): Boolean = if (initialized) delegate.contains(element) else false
    override fun concat(tail: Message): MessageChain = delegate.concat(tail)

    override val size: Int = if (initialized) delegate.size else 0
    override fun containsAll(elements: Collection<Message>): Boolean =
        if (initialized) delegate.containsAll(elements) else false

    override fun get(index: Int): Message =
        if (initialized) delegate[index] else throw IndexOutOfBoundsException(index.toString())

    override fun indexOf(element: Message): Int = if (initialized) delegate.indexOf(element) else -1
    override fun isEmpty(): Boolean = if (initialized) delegate.isEmpty() else true
    override fun iterator(): MutableIterator<Message> = delegate.iterator()

    override fun lastIndexOf(element: Message): Int = if (initialized) delegate.lastIndexOf(element) else -1
    override fun add(element: Message): Boolean = delegate.add(element)
    override fun add(index: Int, element: Message) = delegate.add(index, element)
    override fun addAll(index: Int, elements: Collection<Message>): Boolean = delegate.addAll(elements)
    override fun addAll(elements: Collection<Message>): Boolean = delegate.addAll(elements)
    override fun clear() {
        if (initialized) delegate.clear()
    }

    override fun listIterator(): MutableListIterator<Message> = delegate.listIterator()

    override fun listIterator(index: Int): MutableListIterator<Message> = delegate.listIterator()
    override fun remove(element: Message): Boolean = if (initialized) delegate.remove(element) else false
    override fun removeAll(elements: Collection<Message>): Boolean =
        if (initialized) delegate.removeAll(elements) else false

    override fun removeAt(index: Int): Message =
        if (initialized) delegate.removeAt(index) else throw IndexOutOfBoundsException(index.toString())

    override fun retainAll(elements: Collection<Message>): Boolean =
        if (initialized) delegate.retainAll(elements) else false

    override fun set(index: Int, element: Message): Message =
        if (initialized) delegate.set(index, element) else throw IndexOutOfBoundsException(index.toString())
}

/**
 * Null 的 [MessageChain].
 * 它不包含任何元素, 也没有创建任何 list.
 *
 * - 所有 get 方法均抛出 [IndexOutOfBoundsException]
 * - 所有 add 方法均抛出 [UnsupportedOperationException]
 * - 其他判断类方法均 false 或 -1
 */
object NullMessageChain : MessageChain {
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Message> = unsupported()

    override val stringValue: String
        get() = ""

    override fun toString(): String = stringValue

    override fun contains(sub: String): Boolean = false
    override fun contains(element: Message): Boolean = false
    override fun concat(tail: Message): MessageChain =
        MessageChainImpl(tail)

    override val size: Int = 0
    override fun containsAll(elements: Collection<Message>): Boolean = false
    override fun get(index: Int): Message = throw IndexOutOfBoundsException()
    override fun indexOf(element: Message): Int = -1
    override fun isEmpty(): Boolean = true
    override fun iterator(): MutableIterator<Message> =
        EmptyMutableIterator()

    override fun lastIndexOf(element: Message): Int = -1
    override fun add(element: Message): Boolean = unsupported()
    override fun add(index: Int, element: Message) = throw IndexOutOfBoundsException(index.toString())
    override fun addAll(index: Int, elements: Collection<Message>): Boolean =
        throw IndexOutOfBoundsException(index.toString())

    override fun addAll(elements: Collection<Message>): Boolean = unsupported()
    override fun clear() {}
    override fun listIterator(): MutableListIterator<Message> =
        EmptyMutableListIterator()

    override fun listIterator(index: Int): MutableListIterator<Message> =
        throw IndexOutOfBoundsException(index.toString())

    override fun remove(element: Message): Boolean = false
    override fun removeAll(elements: Collection<Message>): Boolean = false
    override fun removeAt(index: Int): Message = throw IndexOutOfBoundsException(index.toString())
    override fun retainAll(elements: Collection<Message>): Boolean = false
    override fun set(index: Int, element: Message): Message = throw IndexOutOfBoundsException(index.toString())
    private fun unsupported(): Nothing = throw UnsupportedOperationException()
}

/**
 * [MessageChain] 实现
 * 它是一个特殊的 [Message], 实现 [MutableList] 接口, 但将所有的接口调用都转到内部维护的另一个 [MutableList].
 */
internal inline class MessageChainImpl constructor(
    /**
     * Elements will not be instances of [MessageChain]
     */
    private val delegate: MutableList<Message>
) : Message, MutableList<Message>,
    MessageChain {
    //constructor() : this(ArrayList(8))
    constructor(initialCapacity: Int) : this(ArrayList(initialCapacity))

    constructor(vararg messages: Message) : this(messages.toMutableList())
    constructor(messages: Iterable<Message>) : this(messages.toMutableList())

    // region Message override
    override val stringValue: String get() = this.delegate.joinToString("") { it.stringValue }

    override fun toString(): String = stringValue

    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
    override fun concat(tail: Message): MessageChain {
        if (tail is MessageChain) tail.forEach { child -> this.concat(child) }
        else this.delegate.add(tail)
        return this
    }

    // endregion

    // region MutableList override
    override fun containsAll(elements: Collection<Message>): Boolean = delegate.containsAll(elements)

    override operator fun get(index: Int): Message = delegate[index]
    override fun indexOf(element: Message): Int = delegate.indexOf(element)
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun lastIndexOf(element: Message): Int = delegate.lastIndexOf(element)
    override fun add(element: Message): Boolean = delegate.add(element)
    override fun add(index: Int, element: Message) = delegate.add(index, element)
    override fun addAll(index: Int, elements: Collection<Message>): Boolean = delegate.addAll(index, elements)
    override fun addAll(elements: Collection<Message>): Boolean = delegate.addAll(elements)
    override fun clear() = delegate.clear()
    override fun listIterator(): MutableListIterator<Message> = delegate.listIterator()
    override fun listIterator(index: Int): MutableListIterator<Message> = delegate.listIterator(index)
    override fun remove(element: Message): Boolean = delegate.remove(element)
    override fun removeAll(elements: Collection<Message>): Boolean = delegate.removeAll(elements)
    override fun removeAt(index: Int): Message = delegate.removeAt(index)
    override fun retainAll(elements: Collection<Message>): Boolean = delegate.retainAll(elements)
    override fun set(index: Int, element: Message): Message = delegate.set(index, element)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Message> = delegate.subList(fromIndex, toIndex)
    override fun iterator(): MutableIterator<Message> = delegate.iterator()
    override operator fun contains(element: Message): Boolean = delegate.contains(element)
    override val size: Int get() = delegate.size
    // endregion
}

/**
 * 单个成员的不可修改的 [MessageChain].
 *
 * 在连接时将会把它当做一个普通 [Message] 看待, 但它不能被 [plusAssign]
 */
internal inline class SingleMessageChainImpl(
    private val delegate: Message
) : Message, MutableList<Message>,
    MessageChain {

    // region Message override
    override val stringValue: String get() = this.delegate.stringValue

    override operator fun contains(sub: String): Boolean = delegate.contains(sub)
    override fun concat(tail: Message): MessageChain =
        if (tail is MessageChain) tail.apply { concat(delegate) }
        else MessageChain(delegate, tail)

    override fun plusAssign(message: Message) =
        throw UnsupportedOperationException("SingleMessageChainImpl cannot be plusAssigned")

    override fun toString(): String = stringValue
    // endregion

    // region MutableList override
    override fun containsAll(elements: Collection<Message>): Boolean = elements.all { it === delegate }

    override operator fun get(index: Int): Message = if (index == 0) delegate else throw NoSuchElementException()
    override fun indexOf(element: Message): Int = if (delegate === element) 0 else -1
    override fun isEmpty(): Boolean = false
    override fun lastIndexOf(element: Message): Int = if (delegate === element) 0 else -1
    override fun add(element: Message): Boolean = throw UnsupportedOperationException()
    override fun add(index: Int, element: Message) = throw UnsupportedOperationException()
    override fun addAll(index: Int, elements: Collection<Message>): Boolean = throw UnsupportedOperationException()
    override fun addAll(elements: Collection<Message>): Boolean = throw UnsupportedOperationException()
    override fun clear() = throw UnsupportedOperationException()
    override fun listIterator(): MutableListIterator<Message> = object : MutableListIterator<Message> {
        private var hasNext = true
        override fun hasPrevious(): Boolean = !hasNext
        override fun nextIndex(): Int = if (hasNext) 0 else -1
        override fun previous(): Message =
            if (hasPrevious()) {
                hasNext = true
                delegate
            } else throw NoSuchElementException()

        override fun previousIndex(): Int = if (!hasNext) 0 else -1
        override fun add(element: Message) = throw UnsupportedOperationException()
        override fun hasNext(): Boolean = hasNext
        override fun next(): Message =
            if (hasNext) {
                hasNext = false
                delegate
            } else throw NoSuchElementException()

        override fun remove() = throw UnsupportedOperationException()
        override fun set(element: Message) = throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<Message> =
        if (index == 0) listIterator() else throw UnsupportedOperationException()

    override fun remove(element: Message): Boolean = throw UnsupportedOperationException()
    override fun removeAll(elements: Collection<Message>): Boolean = throw UnsupportedOperationException()
    override fun removeAt(index: Int): Message = throw UnsupportedOperationException()
    override fun retainAll(elements: Collection<Message>): Boolean = throw UnsupportedOperationException()
    override fun set(index: Int, element: Message): Message = throw UnsupportedOperationException()
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Message> {
        return if (fromIndex == 0) when (toIndex) {
            1 -> mutableListOf<Message>(this)
            0 -> mutableListOf()
            else -> throw UnsupportedOperationException()
        }
        else throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<Message> = object : MutableIterator<Message> {
        private var hasNext = true
        override fun hasNext(): Boolean = hasNext
        override fun next(): Message =
            if (hasNext) {
                hasNext = false
                delegate
            } else throw NoSuchElementException()

        override fun remove() = throw UnsupportedOperationException()
    }

    override operator fun contains(element: Message): Boolean = element === delegate
    override val size: Int get() = 1
    // endregion
}


@Suppress("FunctionName")
private fun <E> EmptyMutableIterator(): MutableIterator<E> = object : MutableIterator<E> {
    override fun hasNext(): Boolean = false
    override fun next(): E = throw NoSuchElementException()
    override fun remove() = throw NoSuchElementException()
}

@Suppress("FunctionName")
private fun <E> EmptyMutableListIterator(): MutableListIterator<E> = object : MutableListIterator<E> {
    override fun hasPrevious(): Boolean = false
    override fun nextIndex(): Int = -1
    override fun previous(): E = throw NoSuchElementException()
    override fun previousIndex(): Int = -1
    override fun add(element: E) = throw UnsupportedOperationException()
    override fun hasNext(): Boolean = false
    override fun next(): E = throw NoSuchElementException()
    override fun remove() = throw NoSuchElementException()
    override fun set(element: E) = throw UnsupportedOperationException()
}

