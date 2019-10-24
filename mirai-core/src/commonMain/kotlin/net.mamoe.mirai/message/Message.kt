@file:Suppress("MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.protocol.tim.packet.FriendImageIdRequestPacket
import net.mamoe.mirai.utils.ExternalImage

/**
 * 可发送的或从服务器接收的消息.
 * 采用这样的消息模式是因为 QQ 的消息多元化, 一条消息中可包含 [纯文本][PlainText], [图片][Image] 等.
 *
 * #### 在 Kotlin 使用 [Message]
 *  这与使用 [String] 的使用非常类似.
 *
 *  比较 [Message] 与 [String] (使用 infix [Message.eq]):
 *  `if(event eq "你好") qq.sendMessage(event)`
 *
 *  连接 [Message] 与 [Message], [String], (使用 operator [Message.plus]):
 *  ```
 *      event = PlainText("Hello ")
 *      qq.sendMessage(event + "world")
 *  ```
 *
 *  但注意: 不能 `String + Message`. 只能 `Message + String`
 *
 * @see PlainText 纯文本
 * @see Image 图片
 * @see Face 表情
 * @see MessageChain 消息链(即 `List<Message>`)
 *
 * @see Contact.sendMessage 发送消息
 */
interface Message {
    /**
     * 易读的 [String] 值
     * 如:
     * ```
     * [@123456789]
     * [face123]
     * ```
     */
    val stringValue: String

    infix fun eq(other: Message): Boolean = this == other

    /**
     * 将 [stringValue] 与 [other] 比较
     */
    infix fun eq(other: String): Boolean = this.stringValue == other

    operator fun contains(sub: String): Boolean = false

    /**
     * 把这个消息连接到另一个消息的头部. 类似于字符串相加.
     *
     * 例:
     * ```kotlin
     * val a = PlainText("Hello ")
     * val b = PlainText("world!")
     * val c:MessageChain = a + b
     * println(c)// "Hello world!"
     * ```
     *
     * ```kotlin
     * val d = PlainText("world!")
     * val e = c + d;//PlainText + MessageChain
     * println(c)// "Hello world!"
     * ```
     */
    fun concat(tail: Message): MessageChain =
        if (tail is MessageChain) MessageChain(this).also { tail.forEach { child -> it.concat(child) } }
        else MessageChain(this, tail)

    infix operator fun plus(another: Message): MessageChain = this.concat(another)
    infix operator fun plus(another: String): MessageChain = this.concat(another.toMessage())
    infix operator fun plus(another: Number): MessageChain = this.concat(another.toString().toMessage())
}

// ==================================== PlainText ====================================

inline class PlainText(override val stringValue: String) : Message {
    override operator fun contains(sub: String): Boolean = this.stringValue.contains(sub)
}

// ==================================== Image ====================================

/**
 * 图片消息. 在发送时将会区分群图片和好友图片发送.
 * 由接收消息时构建, 可直接发送
 *
 * @param id 这个图片的 [ImageId]
 * @param filename 文件名. 这将决定图片的显示
 *
 * @see
 */
class Image(val id: ImageId, val filename: String = "") : Message {
    override val stringValue: String = "[${id.value}]"
}

/**
 * 图片的标识符. 由图片的数据产生.
 * 对于群, [value] 类似于 `{F61593B5-5B98-1798-3F47-2A91D32ED2FC}.jpg`, 由图片文件 MD5 直接产生.
 * 对于好友, [value] 类似于 `/01ee6426-5ff1-4cf0-8278-e8634d2909ef`, 由服务器返回.
 *
 * @see ExternalImage.groupImageId 群图片的 [ImageId] 获取
 * @see FriendImageIdRequestPacket.Response.imageId 好友图片的 [ImageId] 获取
 */
inline class ImageId(val value: String)

// ==================================== At ====================================

/**
 * At 一个人
 */
inline class At(val targetQQ: UInt) : Message {
    constructor(target: QQ) : this(target.id)

    override val stringValue: String get() = "[@$targetQQ]"
}

// ==================================== Face ====================================

/**
 * QQ 自带表情
 */
inline class Face(val id: FaceID) : Message {
    override val stringValue: String get() = "[face${id.id}]"
}

// ==================================== MessageChain ====================================
/**
 * 消息链. 即 MutableList<Message>.
 * 它是一个特殊的 [Message], 实现 [MutableList] 接口, 但将所有的接口调用都转到内部维护的另一个 [MutableList], [delegate]
 *
 * 有关 [MessageChain] 的创建和连接:
 * - 当任意两个不是 [MessageChain] 的 [Message] 相连接后, 将会产生一个 [MessageChain].
 * - 若两个 [MessageChain] 连接, 后一个将会被添加到第一个内.
 * - 若一个 [MessageChain] 与一个其他 [Message] 连接, [Message] 将会被添加入 [MessageChain].
 * - 若一个 [Message] 与一个 [MessageChain] 连接, 将会创建一个新的 [MessageChain], 并顺序添加连接时的参数.
 */
data class MessageChain constructor(//todo 优化: 不构造 list. 而是在每个 Message 内写 head 和 tail 来连接.
    /**
     * Elements will not be instances of [MessageChain]
     */
    private val delegate: MutableList<Message>
) : Message, MutableList<Message> {
    constructor() : this(mutableListOf())
    constructor(vararg messages: Message) : this(messages.toMutableList())
    constructor(messages: Iterable<Message>) : this(messages.toMutableList())

    // region Message override
    override val stringValue: String get() = this.delegate.joinToString("") { it.stringValue }

    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
    override fun concat(tail: Message): MessageChain {
        if (tail is MessageChain) tail.delegate.forEach { child -> this.concat(child) }
        else this.delegate.add(tail)
        return this
    }
    // endregion

    /**
     * 获取第一个 [M] 类型的实例
     * @throws [NoSuchElementException] 如果找不到该类型的实例
     */
    inline fun <reified M : Message> first(): Message = this.first { M::class.isInstance(it) }

    /**
     * 获取第一个 [M] 类型的实例
     */
    inline fun <reified M : Message> firstOrNull(): Message? = this.firstOrNull { M::class.isInstance(it) }


    operator fun plusAssign(message: Message) {
        this.concat(message)
    }

    operator fun plusAssign(plain: String) {
        this.concat(plain.toMessage())
    }


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
    override val size: Int = delegate.size
    // endregion
}