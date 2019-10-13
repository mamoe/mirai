@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ

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
sealed class Message {
    /**
     * 易读的 [String] 值
     * 如:
     * ```
     * [@123456789]
     * [face123]
     * ```
     */
    abstract val stringValue: String

    final override fun toString(): String = stringValue

    infix fun eq(other: Message): Boolean = this == other

    /**
     * 将 [stringValue] 与 [other] 比较
     */
    infix fun eq(other: String): Boolean = this.stringValue == other

    open operator fun contains(sub: String): Boolean = false

    /**
     * 把这个消息连接到另一个消息的头部. 类似于字符串相加
     */
    open fun concat(tail: Message): MessageChain =
            if (tail is MessageChain) MessageChain(this).also { tail.list.forEach { child -> it.concat(child) } }
            else MessageChain(this, tail)

    infix operator fun plus(another: Message): MessageChain = this.concat(another)
    infix operator fun plus(another: String): MessageChain = this.concat(another.toMessage())
    infix operator fun plus(another: Number): MessageChain = this.concat(another.toString().toMessage())
}

// ==================================== PlainText ====================================

data class PlainText(override val stringValue: String) : Message() {
    override operator fun contains(sub: String): Boolean = this.stringValue.contains(sub)
}

// ==================================== Image ====================================

/**
 * 图片消息.
 * 由接收消息时构建, 可直接发送
 *
 * @param imageId 类似 `{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg`. 群的是大写id, 好友的是小写id
 */
data class Image(val imageId: String) : Message() {
    override val stringValue: String = "[$imageId]"
}

// ==================================== At ====================================

/**
 * At 一个人
 */
data class At(val targetQQ: Long) : Message() {
    constructor(target: QQ) : this(target.number)

    override val stringValue: String = "[@$targetQQ]"
}

// ==================================== Face ====================================

/**
 * QQ 自带表情
 */
data class Face(val id: FaceID) : Message() {
    override val stringValue: String = "[face${id.id}]"
}

// ==================================== MessageChain ====================================

data class MessageChain(
        /**
         * Elements will not be instances of [MessageChain]
         */
        val list: MutableList<Message>
) : Message(), Iterable<Message> {
    constructor() : this(mutableListOf())
    constructor(vararg messages: Message) : this(mutableListOf(*messages))
    constructor(messages: Iterable<Message>) : this(messages.toMutableList())

    val size: Int = list.size

    override val stringValue: String get() = this.list.joinToString("") { it.stringValue }

    override fun iterator(): Iterator<Message> = this.list.iterator()

    /**
     * 获取第一个 [M] 类型的实例
     * @throws [NoSuchElementException] 如果找不到该类型的实例
     */
    inline fun <reified M : Message> first(): Message = this.list.first { M::class.isInstance(it) }

    /**
     * 获取第一个 [M] 类型的实例
     */
    inline fun <reified M : Message> firstOrNull(): Message? = this.list.firstOrNull { M::class.isInstance(it) }

    override operator fun contains(sub: String): Boolean = list.any { it.contains(sub) }

    override fun concat(tail: Message): MessageChain {
        if (tail is MessageChain) tail.list.forEach { child -> this.concat(child) }
        else this.list.add(tail)
        return this
    }
}