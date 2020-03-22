/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import kotlin.jvm.JvmSynthetic

/**
 * 可发送的或从服务器接收的消息.
 * 采用这样的消息模式是因为 QQ 的消息多元化, 一条消息中可包含 [纯文本][PlainText], [图片][Image] 等.
 *
 * [消息][Message] 分为
 * - [MessageMetadata] 消息元数据, 包括: [消息来源][MessageSource]
 * - [MessageContent] 单个消息, 包括: [纯文本][PlainText], [@群员][At], [@全体成员][AtAll] 等.
 * - [CombinedMessage] 通过 [plus] 连接的两个消息. 可通过 [asMessageChain] 转换为 [MessageChain]
 * - [MessageChain] 不可变消息链, 即 [List] 形式链接的多个 [Message] 实例.
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
 * @see Face 原生表情
 * @see At 一个群成员的引用
 * @see AtAll 全体成员的引用
 * @see QuoteReply 一条消息的引用
 *
 * @see MessageChain 消息链(即 `List<Message>`)
 * @see CombinedMessage 链接的两个消息
 * @see buildMessageChain 构造一个 [MessageChain]
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

    infix fun eq(other: Message): Boolean = this.toString() == other.toString()

    /**
     * 将 [toString] 与 [other] 比较
     */
    infix fun eq(other: String): Boolean = this.toString() == other

    operator fun contains(sub: String): Boolean = false

    /**
     * 把 `this` 连接到 [tail] 的头部. 类似于字符串相加.
     *
     * 例:
     * ```kotlin
     * val a = PlainText("Hello ")
     * val b = PlainText("world!")
     * val c: CombinedMessage = a + b
     * println(c) // "Hello world!"
     *
     * val d = PlainText("world!")
     * val e = c + d; // PlainText + CombinedMessage
     * println(c) // "Hello world!"
     * ```
     */
    @JvmSynthetic // in java they should use `plus` instead
    fun followedBy(tail: Message): CombinedMessage {
        return CombinedMessage(tail, this)
    }

    override fun toString(): String

    operator fun plus(another: Message): CombinedMessage = this.followedBy(another)

    operator fun plus(another: String): CombinedMessage = this.followedBy(another.toMessage())

    // `+ ""` will be resolved to `plus(String)` instead of `plus(CharSeq)`
    operator fun plus(another: CharSequence): CombinedMessage = this.followedBy(another.toString().toMessage())
}

@Suppress("UNCHECKED_CAST")
suspend inline fun <C : Contact> Message.sendTo(contact: C): MessageReceipt<C> {
    return contact.sendMessage(this) as MessageReceipt<C>
}

fun Message.repeat(count: Int): MessageChain {
    return buildMessageChain(count) {
        add(this@repeat)
    }
}

inline operator fun Message.times(count: Int): MessageChain = this.repeat(count)

interface SingleMessage : Message

/**
 * 消息元数据, 即不含内容的元素.
 * 包括: [MessageSource]
 */
interface MessageMetadata : SingleMessage {
    /*
    fun iterator(): Iterator<Message> {
        return object : Iterator<Message> {
            var visited: Boolean = false
            override fun hasNext(): Boolean = !visited
            override fun next(): Message {
                if (visited) throw NoSuchElementException()
                return this@MessageMetadata.also { visited = true }
            }
        }
    }*/
}

/**
 * 消息内容
 */
interface MessageContent : SingleMessage

/**
 * 将 [this] 发送给指定联系人
 */
@Suppress("UNCHECKED_CAST")
suspend inline fun <C : Contact> MessageChain.sendTo(contact: C): MessageReceipt<C> =
    contact.sendMessage(this) as MessageReceipt<C>