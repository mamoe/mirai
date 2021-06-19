/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE",
    "NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE",
    "INAPPLICABLE_JVM_NAME"
)
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlinx.serialization.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.code.MiraiCode.serializeToMiraiCode
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import kotlin.internal.LowPriorityInOverloadResolution

/**
 * 可发送的或从服务器接收的消息.
 *
 * [Message] 派生为 [SingleMessage] 和 [MessageChain].
 *
 * [SingleMessage] 分为:
 *   - [MessageMetadata] 消息元数据, 即消息的属性. 包括: [消息来源][MessageSource], [引用回复][QuoteReply] 等.
 *   - [MessageContent] 含内容的消息, 包括: [纯文本][PlainText], [@群员][At], [@全体成员][AtAll] 等.
 *
 * [MessageChain] 是链表形式链接的多个 [SingleMessage] 实例, 类似 [List].
 *
 * ## [Message] 是不可变的
 *
 * 所有类型的 [Message] 都是不可变的 (immutable), 它们被构造后其所有属性的值就已经固定了. 因此在多线程环境使用是安全的.
 * 因此 [contentToString], [serializeToJsonString], [MiraiCode.serializeToMiraiCode] 的返回都是不变的.
 *
 * [MessageChain] 的 [contentToString] 会被缓存, 只会在第一次调用时计算.
 *
 * ## 获得 [Message]
 *
 * 查看 [Message] 子类. 或在 GitHub 查看 [表格](https://github.com/mamoe/mirai/blob/dev/docs/Messages.md#%E6%B6%88%E6%81%AF%E5%85%83%E7%B4%A0).
 *
 * ## 使用 [Message]
 *
 * ### 转换为 [MessageChain]
 *
 * [MessageChain] 为多个 [SingleMessage] 的集合. [Message] 可能表示 single 也可能表示 chain. 可以通过 [toMessageChain] 将 [Message] 转换为 [MessageChain] 统一处理.
 *
 * ### 连接两个或多个 [Message]
 *
 * 在 Kotlin, 使用操作符 [Message.plus]:
 * ```
 * val text = PlainText("Hello ") + PlainText("world") + "!"
 * friend.sendMessage(text) // "Hello world!"
 * ```
 *
 * 在 Java, 使用 [plus]:
 * ```
 * MessageChain text = new PlainText("Hello ")
 *     .plus(new PlainText("world"))
 *     .plus("!");
 * friend.sendMessage(text); // "Hello world!"
 * ```
 *
 * 注: 若需要拼接较多 [Message], 推荐使用 [MessageChainBuilder] 加快拼接效率
 *
 * ### 使用 [MessageChainBuilder] 来构建消息
 *
 * 查看 [MessageChainBuilder].
 *
 * ### 发送消息
 *
 * - [Contact.sendMessage] 接收 [Message] 参数
 * - [Message.sendTo] 是发送的扩展
 *
 * ### 处理消息
 *
 * 除了直接访问 [Message] 子类的对象外, 有时候可能需要将 [Message] 作为字符串处理,
 * 通常可以使用 [contentToString] 方法或 [content] 扩展得到与官方客户端显示格式相同的内容字符串.
 *
 * #### 文字处理示例
 *
 * 本示例实现处理以 `#` 开头的消息:
 *
 * Kotlin:
 * ```
 * val msg = event.message
 * val content = msg.content.trim()
 * if (content.startsWith("#")) {
 *     val name = content.substringAfter("#", "")
 *     when(name) {
 *         "mute" -> event.sender.mute(60000) // 发 #mute 就把自己禁言 1 分钟
 *     }
 * }
 * ```
 *
 * Java:
 * ```
 * MessageChain msg = event.message;
 * String content = msg.contentToString();
 * if (!content.equals("#") && content.startsWith("#")) {
 *     String name = content.substring(content.indexOf('#') + 1); // `#` 之后的内容
 *     switch(name) {
 *         case "mute": event.sender.mute(60000) // 发 #mute 就把自己禁言 1 分钟
 *     }
 * }
 * ```
 *
 * 若使用 Java 对象的 [toString], 会得到包含更多信息. 因此 [toString] 结果可能会随着 mirai 更新变化. [toString] 不适合用来处理消息. 只适合用来调试输出.
 *
 * [Message] 还提供了 [Mirai 码][MiraiCode] 和 [JSON][MessageChain.serializeToJsonString] 序列化方式. 可在 [MessageChain] 文档详细了解它们.
 *
 * ### 发送消息
 * - 通过 [Contact] 中的成员函数: [Contact.sendMessage]
 * - 通过 [Message] 的扩展函数: [Message.sendTo]
 *
 * @see MessageChain 消息链(即 `List<Message>`)
 * @see buildMessageChain 构造一个 [MessageChain]
 *
 * @see Contact.sendMessage 发送消息
 *
 * @suppress **注意:** [Message] 类型大多有隐藏的协议实现, 不能被第三方应用继承.
 */
public interface Message {

    /**
     * 得到包含 mirai 消息元素代码的, 易读的字符串. 如 `At(member) + "test"` 将转为 `"[mirai:at:qqId]test"`.
     *
     * 在使用消息相关 DSL 和扩展时, 一些内容比较的实现均使用的是 [contentToString] 而不是 [toString].
     *
     * 各个消息类型的转换示例:
     * - [PlainText] : `"Hello"`
     * - [Image] : `"[mirai:image:{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai]"`
     * - [PokeMessage] : `"[mirai:poke:1,-1]"`
     * - [MessageChain] : 无间隔地连接所有元素 (`joinToString("")`)
     * - ...
     *
     * @see contentToString 转为最接近官方格式的字符串
     */
    public override fun toString(): String

    /**
     * 转为最接近官方格式的字符串. 如 `At(member) + "test"` 将转为 `"@群名片 test"`.
     *
     * 在使用消息相关 DSL 和扩展时, 一些内容比较的实现均使用 [contentToString] 而不是 [toString].
     *
     * 由于消息元素都是不可变的, [contentToString] 的返回也是不变的.
     * [MessageChain] 的 [contentToString] 会被缓存, 只会在第一次调用时计算.
     *
     * 各个消息类型的转换示例:
     * - [PlainText] : `"Hello"`
     * - [Image] : `"[图片]"`
     * - [PokeMessage] : `"[戳一戳]"`
     * - [MessageChain] : 无间隔地连接所有元素 (`joinToString("", transformer=Message::contentToString)`)
     * - ...
     *
     * @see toString 得到包含 mirai 消息元素代码的, 易读的字符串
     * @see contentEquals
     * @see Message.content Kotlin 扩展
     */
    public fun contentToString(): String


    /**
     * 判断内容是否与 [another] 相等即 `this` 与 [another] 的 [contentToString] 相等.
     * [strict] 为 `true` 时, 还会额外判断每个消息元素的类型, 顺序和属性. 如 [Image] 会判断 [Image.imageId]
     *
     * **有关 [strict]:** 每个 [Image] 的 [contentToString] 都是 `"[图片]"`,
     * 在 [strict] 为 `false` 时 [contentEquals] 会得到 `true`,
     * 而为 `true` 时由于 [Image.imageId] 会被比较, 两张不同的图片的 [contentEquals] 会是 `false`.
     *
     * @param ignoreCase 为 `true` 时忽略大小写
     */
    public fun contentEquals(another: Message, ignoreCase: Boolean = false, strict: Boolean = false): Boolean {
        return if (strict) this.contentEqualsStrictImpl(another, ignoreCase)
        else this.contentToString().equals(another.contentToString(), ignoreCase = ignoreCase)
    }

    /**
     * 判断内容是否与 [another] 相等即 `this` 与 [another] 的 [contentToString] 相等.
     *
     * 单个消息的顺序和内容不会被检查, 即只要比较两个 [Image], 总是会得到 `true`, 因为 [Image] 的 [contentToString] 都是 `"[图片]"`.
     *
     *
     * 相当于
     * ```
     * this.contentToString().equals(another.contentToString(), ignoreCase = ignoreCase)
     * ```
     *
     * @param ignoreCase 为 `true` 时忽略大小写
     */
    @LowPriorityInOverloadResolution
    public fun contentEquals(another: Message, ignoreCase: Boolean = false): Boolean =
        contentEquals(another, ignoreCase, false)


    /**
     * 判断内容是否与 [another] 相等.
     *
     *
     * 相当于
     * ```
     * this.contentToString().equals(another, ignoreCase = ignoreCase)
     * ```
     *
     *
     * 若本函数返回 `true`, 则表明:
     * - `this` 与 [another] 的 [contentToString] 相等
     */
    public fun contentEquals(another: String, ignoreCase: Boolean = false): Boolean {
        return this.contentToString().equals(another, ignoreCase = ignoreCase)
    }

    /**
     * 将 `this` 和 [tail] 连接.
     *
     * 连接后可以保证 [ConstrainSingle] 的元素单独存在.
     *
     * 例:
     * ```
     * val a = PlainText("Hello ")
     * val b = PlainText("world!")
     * val c: MessageChain = a + b
     * println(c) // "Hello world!"
     * ```
     *
     * 在 Java 使用 [plus]
     *
     * @see plus `+` 操作符重载
     */
    @JvmSynthetic // in java they should use `plus` instead
    public fun followedBy(tail: Message): MessageChain = followedByImpl(tail)

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    public operator fun plus(another: MessageChain): MessageChain = this + another as Message

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    public operator fun plus(another: Message): MessageChain = this.followedBy(another)

    /** 将 [another] 连接到这个消息的尾部. */
    public operator fun plus(another: SingleMessage): MessageChain = this.followedBy(another)

    /** 将 [another] 作为 [PlainText] 连接到这个消息的尾部. */
    public operator fun plus(another: String): MessageChain = this.followedBy(PlainText(another))

    /** 将 [another] 作为 [PlainText] 连接到这个消息的尾部. */
    public operator fun plus(another: CharSequence): MessageChain =
        this.followedBy(PlainText(another.toString()))

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    public operator fun plus(another: Iterable<Message>): MessageChain =
        another.fold(this, Message::plus).toMessageChain()

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    public operator fun plus(another: Array<out Message>): MessageChain =
        another.fold(this, Message::plus).toMessageChain()

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    @JvmName("plusIterableString")
    public operator fun plus(another: Iterable<String>): MessageChain =
        another.fold(this, Message::plus).toMessageChain()

    /** 将 [another] 按顺序连接到这个消息的尾部. */
    public operator fun plus(another: Sequence<Message>): MessageChain =
        another.fold(this, Message::plus).toMessageChain()

    public companion object
}

/** 将 [another] 按顺序连接到这个消息的尾部. */
@JvmSynthetic
public suspend inline operator fun Message.plus(another: Flow<Message>): MessageChain =
    another.fold(this) { acc, it -> acc + it }.toMessageChain()


/**
 * [Message.contentToString] 的捷径
 */
@get:JvmSynthetic
public inline val Message.content: String
    get() = contentToString()

/**
 * 将 [this] 发送给指定联系人
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
public suspend inline fun <C : Contact> Message.sendTo(contact: C): MessageReceipt<C> =
    contact.sendMessage(this) as MessageReceipt<C>


/**
 * 当消息内容为空时返回 `true`.
 * @see String.isEmpty
 */
public fun Message.isContentEmpty(): Boolean {
    return when (this) {
        is MessageChain -> this.all { it.isContentEmpty() }
        else -> this.content.isEmpty()
    }
}

/**
 * 当消息内容为空白时返回 `true`.
 * @see String.isBlank
 */
public fun Message.isContentBlank(): Boolean {
    return when (this) {
        is MessageChain -> this.all { it.isContentBlank() }
        else -> this.content.isBlank()
    }
}

/**
 * 将此消息元素按顺序重复 [count] 次.
 */
// inline: for future removal
public inline fun Message.repeat(count: Int): MessageChain {
    if (this is ConstrainSingle) {
        // fast-path
        return this.toMessageChain()
    }
    return buildMessageChain(count) {
        repeat(count) {
            add(this@repeat)
        }
    }
}

/**
 * 将此消息元素按顺序重复 [count] 次.
 */
@JvmSynthetic
public inline operator fun Message.times(count: Int): MessageChain = this.repeat(count)
