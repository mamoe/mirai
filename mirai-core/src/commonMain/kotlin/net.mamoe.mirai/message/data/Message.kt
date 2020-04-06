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
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 可发送的或从服务器接收的消息.
 * 采用这样的消息模式是因为 QQ 的消息多元化, 一条消息中可包含 [纯文本][PlainText], [图片][Image] 等.
 *
 * [消息][Message] 分为
 * - [SingleMessage]:
 *   - [MessageMetadata] 消息元数据, 包括: [消息来源][MessageSource], [引用回复][QuoteReply].
 *   - [MessageContent] 含内容的消息, 包括: [纯文本][PlainText], [@群员][At], [@全体成员][AtAll] 等.
 * - [MessageChain]: 不可变消息链, 链表形式链接的多个 [SingleMessage] 实例.
 *
 * #### 在 Kotlin 使用 [Message]:
 * 这与使用 [String] 的使用非常类似.
 *
 * 比较 [SingleMessage] 与 [String]:
 *  `if(message.contentToString() == "你好") qq.sendMessage(event)`
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
 * @see RichMessage 富文本消息, 如 [Xml][XmlMessage], [小程序][LightApp], [Json][JsonMessage]
 * @see HummerMessage 一些特殊的消息, 如 [闪照][FlashImage], [戳一戳][PokeMessage]
 *
 * @see MessageChain 消息链(即 `List<Message>`)
 * @see buildMessageChain 构造一个 [MessageChain]
 *
 * @see Contact.sendMessage 发送消息
 */
@OptIn(MiraiInternalAPI::class)
interface Message {
    /**
     * 类型 Key.
     * 除 [MessageChain] 外, 每个 [Message] 类型都拥有一个`伴生对象`(companion object) 来持有一个 Key
     * 在 [MessageChain.get] 时将会使用到这个 Key 进行判断类型.
     *
     * @param M 指代持有这个 Key 的消息类型
     */
    interface Key<out M : Message> {
        /**
         * 此 [Key] 指代的 [Message] 类型名. 一般为 `class.simpleName`, 如 "QuoteReply", "PlainText"
         */
        @SinceMirai("0.34.0")
        val typeName: String
    }

    /**
     * 把 `this` 连接到 [tail] 的头部. 类似于字符串相加.
     *
     * 连接后可以保证 [ConstrainSingle] 的元素单独存在.
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
    @SinceMirai("0.34.0")
    @Suppress("DEPRECATION_ERROR")
    @OptIn(MiraiInternalAPI::class)
    @JvmSynthetic // in java they should use `plus` instead
    fun followedBy(tail: Message): MessageChain = followedByImpl(tail)

    /**
     * 得到包含 mirai 消息元素代码的, 易读的字符串. 如 `At(member) + "test"` 将转为 `"[mirai:at:qqId]test"`
     *
     * 在使用消息相关 DSL 和扩展时, 一些内容比较的实现均使用的是 [contentToString] 而不是 [toString]
     *
     * 各个 [SingleMessage] 的转换示例:
     * [PlainText]: "Hello"
     * [GroupImage]: "[mirai:image:{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png]"
     * [FriendImage]: "[mirai:image:/f8f1ab55-bf8e-4236-b55e-955848d7069f]"
     * [PokeMessage]: "[mirai:poke:1,-1]"
     * [MessageChain]: 直接无间隔地连接所有元素 (`joinToString("")`)
     *
     * @see contentToString
     */
    override fun toString(): String

    /**
     * 转为最接近官方格式的字符串. 如 `At(member) + "test"` 将转为 `"@群名片 test"`.
     *
     * 对于 [NullMessageChain], 这个函数返回空字符串 "";
     * 对于其他 [MessageChain], 这个函数返回值同 [toString].
     *
     * 在使用消息相关 DSL 和扩展时, 一些内容比较的实现均使用的是 [contentToString] 而不是 [toString]
     */
    @SinceMirai("0.34.0")
    fun contentToString(): String

    operator fun plus(another: Message): MessageChain = this.followedBy(another)

    // don't remove! avoid resolution ambiguity between `CharSequence` and `Message`
    operator fun plus(another: SingleMessage): MessageChain = this.followedBy(another)

    operator fun plus(another: String): MessageChain = this.followedBy(another.toMessage())

    // `+ ""` will be resolved to `plus(String)` instead of `plus(CharSeq)`
    operator fun plus(another: CharSequence): MessageChain = this.followedBy(another.toString().toMessage())

    //////////////////////////////////////
    // FOR BINARY COMPATIBILITY UNTIL 1.0.0
    //////////////////////////////////////

    @PlannedRemoval("1.0.0")
    @JvmSynthetic
    @Deprecated(
        "有歧义, 自行使用 contentToString() 比较",
        ReplaceWith("this.contentToString() == other"),
        DeprecationLevel.ERROR
    )
    infix fun eq(other: Message): Boolean = this.contentToString() == other.contentToString()

    /**
     * 将 [contentToString] 与 [other] 比较
     */
    @PlannedRemoval("1.0.0")
    @JvmSynthetic
    @Deprecated(
        "有歧义, 自行使用 contentToString() 比较",
        ReplaceWith("this.contentToString() == other"),
        DeprecationLevel.ERROR
    )
    infix fun eq(other: String): Boolean = this.contentToString() == other

    @PlannedRemoval("1.0.0")
    @JvmSynthetic
    @Deprecated(
        "有歧义, 自行使用 contentToString() 比较",
        ReplaceWith("this.contentToString() == other"),
        DeprecationLevel.ERROR
    )
    operator fun contains(sub: String): Boolean = false

    @PlannedRemoval("1.0.0")
    @Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_FUNCTION_RETURN_TYPE")
    @JvmName("followedBy")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    fun followedBy1(tail: Message): CombinedMessage = this.followedByInternalForBinaryCompatibility(tail)

    @PlannedRemoval("1.0.0")
    @Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_FUNCTION_RETURN_TYPE")
    @JvmName("plus")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    fun plus1(another: Message): CombinedMessage = this.followedByInternalForBinaryCompatibility(another)

    @PlannedRemoval("1.0.0")
    @Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_FUNCTION_RETURN_TYPE")
    @JvmName("plus")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    fun plus1(another: SingleMessage): CombinedMessage = this.followedByInternalForBinaryCompatibility(another)

    @PlannedRemoval("1.0.0")
    @Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_FUNCTION_RETURN_TYPE")
    @JvmName("plus")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    fun plus1(another: String): CombinedMessage = this.followedByInternalForBinaryCompatibility(another.toMessage())

    @PlannedRemoval("1.0.0")
    @Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_FUNCTION_RETURN_TYPE")
    @JvmName("plus")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    fun plus1(another: CharSequence): CombinedMessage =
        this.followedByInternalForBinaryCompatibility(another.toString().toMessage())
}


@JvmSynthetic
@Suppress("UNCHECKED_CAST")
suspend inline fun <C : Contact> Message.sendTo(contact: C): MessageReceipt<C> {
    return contact.sendMessage(this) as MessageReceipt<C>
}

// inline: for future removal
inline fun Message.repeat(count: Int): MessageChain {
    if (this is ConstrainSingle<*>) {
        // fast-path
        return SingleMessageChainImpl(this)
    }
    return buildMessageChain(count) {
        add(this@repeat)
    }
}

@JvmSynthetic
inline operator fun Message.times(count: Int): MessageChain = this.repeat(count)

@Suppress("OverridingDeprecatedMember")
interface SingleMessage : Message, CharSequence, Comparable<String> {
    /**
     * 即 `sub in this.contentToString()`
     */
    /* final */ override operator fun contains(sub: String): Boolean = sub in this.contentToString()

    /**
     * 比较两个消息的 [contentToString]
     */
    /* final */ override infix fun eq(other: Message): Boolean = this.contentToString() == other.contentToString()

    /**
     * 将 [contentToString] 与 [other] 比较
     */
    /* final */ override infix fun eq(other: String): Boolean = this.contentToString() == other
}

/**
 * 消息元数据, 即不含内容的元素.
 * 包括: [MessageSource]
 */
interface MessageMetadata : SingleMessage {
    override val length: Int get() = 0
    override fun get(index: Int): Char = ""[index] // produce uniform exception
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = "".subSequence(startIndex, endIndex)
    override fun compareTo(other: String): Int = "".compareTo(other)
}

/**
 * 约束一个 [MessageChain] 中只存在这一种类型的元素. 新元素将会替换旧元素, 保持原顺序.
 *
 * **MiraiExperimentalAPI**: 此 API 可能在将来版本修改
 */
@SinceMirai("0.34.0")
@MiraiExperimentalAPI
interface ConstrainSingle<M : Message> : MessageMetadata {
    val key: Message.Key<M>
}

/**
 * 消息内容
 */
interface MessageContent : SingleMessage

/**
 * 将 [this] 发送给指定联系人
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
suspend inline fun <C : Contact> MessageChain.sendTo(contact: C): MessageReceipt<C> =
    contact.sendMessage(this) as MessageReceipt<C>


/////////////////////
/// IMPLEMENTATIONS
//////////////////////


@OptIn(MiraiInternalAPI::class)
private fun Message.hasDuplicationOfConstrain(key: Message.Key<*>): Boolean {
    return when (this) {
        is SingleMessage -> (this as? ConstrainSingle<*>)?.key == key
        is CombinedMessage -> return this.left.hasDuplicationOfConstrain(key) || this.tail.hasDuplicationOfConstrain(key)
        is SingleMessageChainImpl -> (this.delegate as? ConstrainSingle<*>)?.key == key
        is MessageChainImplByCollection -> this.delegate.any { (it as? ConstrainSingle<*>)?.key == key }
        is MessageChainImplBySequence -> this.any { (it as? ConstrainSingle<*>)?.key == key }
        else -> error("stub")
    }
}

@OptIn(MiraiInternalAPI::class)
@JvmSynthetic
@Suppress("DEPRECATION_ERROR")
internal fun Message.followedByInternalForBinaryCompatibility(tail: Message): CombinedMessage {
    return CombinedMessage(EmptyMessageChain, this.followedBy(tail))
}

private fun Message.followedByImpl(tail: Message): MessageChain {
    when {
        this is SingleMessage && tail is SingleMessage -> {
            if (this is ConstrainSingle<*> && tail is ConstrainSingle<*>) {
                if (this.key == tail.key)
                    return SingleMessageChainImpl(tail)
            }
            return CombinedMessage(this, tail)
        }

        this is SingleMessage -> { // tail is not
            tail as MessageChain

            if (this is ConstrainSingle<*>) {
                val key = this.key
                if (tail.any { (it as? ConstrainSingle<*>)?.key == key }) {
                    return tail
                }
            }
            return CombinedMessage(this, tail)
        }

        tail is SingleMessage -> {
            this as MessageChain

            if (tail is ConstrainSingle<*> && this.hasDuplicationOfConstrain(tail.key)) {
                val iterator = this.iterator()
                var tailUsed = false
                return MessageChainImplByCollection(
                    constrainSingleMessagesImpl {
                        if (iterator.hasNext()) {
                            iterator.next()
                        } else if (!tailUsed) {
                            tailUsed = true
                            tail
                        } else null
                    }
                )
            }

            return CombinedMessage(this, tail)
        }

        else -> { // both chain
            this as MessageChain
            tail as MessageChain

            var iterator = this.iterator()
            var tailUsed = false
            return MessageChainImplByCollection(
                constrainSingleMessagesImpl {
                    if (iterator.hasNext()) {
                        iterator.next()
                    } else if (!tailUsed) {
                        tailUsed = true
                        iterator = tail.iterator()
                        iterator.next()
                    } else null
                }
            )
        }
    }
}