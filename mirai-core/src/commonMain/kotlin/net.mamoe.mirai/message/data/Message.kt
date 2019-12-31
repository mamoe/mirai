@file:Suppress("MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.sendMessage

/**
 * 可发送的或从服务器接收的消息.
 * 采用这样的消息模式是因为 QQ 的消息多元化, 一条消息中可包含 [纯文本][PlainText], [图片][Image] 等.
 *
 * **在 Kotlin 使用 [Message]**
 * 这与使用 [String] 的使用非常类似.
 *
 * 比较 [Message] 与 [String] (使用 infix [Message.eq]):
 *  `if(event eq "你好") qq.sendMessage(event)`
 *
 * 连接 [Message] 与 [Message], [String], (使用 operator [Message.plus]):
 *  ```kotlin
 *      text = PlainText("Hello ")
 *      qq.sendMessage(text + "world")
 *  ```
 *
 * `Message1 + Message2 + Message3`, 类似 [String] 的连接:
 *
 *   +----------+   plus  +----------+   plus  +----------+
 *   | Message1 | <------ | Message2 | <------ | Message3 |
 *   +----------+         +----------+         +----------+
 *
 *
 * 但注意: 不能 `String + Message`. 只能 `Message + String`
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
     * 类型 Key.
     * 除 [MessageChain] 外, 每个 [Message] 类型都拥有一个`伴生对象`(companion object) 来持有一个 Key
     * 在 [MessageChain.get] 时将会使用到这个 Key 进行判断类型.
     *
     * @param M 指代持有这个 Key 的消息类型
     */
    interface Key<M : Message>

    infix fun eq(other: Message): Boolean = this == other

    /**
     * 将 [stringValue] 与 [other] 比较
     */
    infix fun eq(other: String): Boolean = this.toString() == other

    operator fun contains(sub: String): Boolean = false

    /**
     * 把 [this] 连接到 [tail] 的头部. 类似于字符串相加.
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
    fun followedBy(tail: Message): MessageChain {
        require(tail !is SingleOnly) { "SingleOnly Message cannot follow another message" }
        require(this !is SingleOnly) { "SingleOnly Message cannot be followed" }
        return if (tail is MessageChain) tail.followedBy(this)/*MessageChainImpl(this).also { tail.forEach { child -> it.concat(child) } }*/
        else MessageChainImpl(this, tail)
    }

    operator fun plus(another: Message): MessageChain = this.followedBy(another)
    operator fun plus(another: String): MessageChain = this.followedBy(another.toString().toMessage())
    // `+ ""` will be resolved to `plus(String)` instead of `plus(CharSeq)`
    operator fun plus(another: CharSequence): MessageChain = this.followedBy(another.toString().toMessage())

    // do remove these primitive types, they can reduce boxing
    operator fun plus(another: Int): MessageChain = this.followedBy(another.toString().toMessage())

    operator fun plus(another: Double): MessageChain = this.followedBy(another.toString().toMessage())
    operator fun plus(another: Long): MessageChain = this.followedBy(another.toString().toMessage())
    operator fun plus(another: Short): MessageChain = this.followedBy(another.toString().toMessage())
    operator fun plus(another: Byte): MessageChain = this.followedBy(another.toString().toMessage())
    operator fun plus(another: Float): MessageChain = this.followedBy(another.toString().toMessage())

    operator fun plus(another: Number): MessageChain = this.followedBy(another.toString().toMessage())
}

/**
 * 表示这个 [Message] 仅能单个存在, 无法被连接.
 */
interface SingleOnly : Message

/**
 * 将 [this] 发送给指定联系人
 */
suspend inline fun Message.sendTo(contact: Contact) = contact.sendMessage(this)