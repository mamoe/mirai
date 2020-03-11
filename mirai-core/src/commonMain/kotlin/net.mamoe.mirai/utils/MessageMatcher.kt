/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

/**
 * [SingleMessage] 的序列，支持获取和替换等操作，用于 [MessageMatcher] 匹配消息。
 *
 * @param reversed 是否从结尾反向匹配
 */
class MessageSequence(private var messages: Array<SingleMessage>, reversed: Boolean = false) {
    /**
     * 若 [reversed] 为 false，则当前序列范围为"[0, index]"；
     * 否则，当前序列范围为"[index, messages.size-1]"。
     */
    private var index: Int = if (reversed) messages.size - 1 else 0

    var reversed: Boolean = reversed
        private set

    fun get(): SingleMessage {
        if (isEmpty()) throw IndexOutOfBoundsException()
        return messages[if (reversed) index-- else index++]
    }

    fun get(predict: (SingleMessage) -> Boolean): SingleMessage? {
        while (isNotEmpty()) get().run { if (predict(this)) return this }
        return null
    }

    fun put(msg: SingleMessage) {
        if (msg is PlainText) {
            if (msg.stringValue.isEmpty()) return
            if (isNotEmpty()) {
                val first = first()
                if (first is PlainText) {
                    messages[index] =
                        PlainText(if (reversed) (first.stringValue + msg.stringValue) else (msg.stringValue + first.stringValue))
                    return
                }
            }
        }
        messages[if (reversed) ++index else --index] = msg
    }

    fun first(): SingleMessage? = if (isEmpty()) null else messages[index]
    fun last(): SingleMessage? = if (isEmpty()) null else messages[if (reversed) 0 else messages.size - 1]

    val size: Int
        get() = if (reversed) index + 1 else messages.size - index

    /**
     * 匹配并消耗 [matcher]。即若匹配成功，被匹配到的内容会从 this 中删去。
     *
     * @return 匹配是否成功
     */
    fun consume(matcher: MessageMatcher, result: MutableMap<String, Any>? = null): Boolean {
        if (reversed) {
            val elements = matcher.elements
            for (i in elements.size - 1 downTo 0) if (!elements[i].consume(this, result)) return false
        } else for (i in matcher.elements) if (!i.consume(this, result)) return false
        return true
    }

    /**
     * 判断自己能否与 [matchers] 中的任意一个匹配。匹配成功的结果将被加入到 [result] 中（如果 [result] 不为 null 的话）。
     * 该方法不会对 this 对象造成改动。
     *
     * @see consume
     */
    fun match(
        matchers: Iterable<MessageMatcher>,
        result: MutableMap<String, Any>? = null
    ): Boolean {
        val extra = if (result == null) null else mutableMapOf<String, Any>()
        for (i in matchers) {
            if (match(i, extra)) {
                result?.putAll(extra!!)
                return true
            }
            extra?.clear()
        }
        return false
    }

    /**
     * 判断自己能否与 [matcher] 匹配。匹配成功的结果将被加入到 [result] 中（如果 [result] 不为 null 的话）。
     * 该方法不会对 this 对象造成改动。
     *
     * @see consume
     */
    fun match(
        matcher: MessageMatcher,
        result: MutableMap<String, Any>? = null
    ): Boolean = clone().run { consume(matcher, result) && isEmpty() }

    /**
     * 将自己赋值为另一个 [MessageSequence]。**只是浅层拷贝，并不做clone**
     */
    fun set(other: MessageSequence) {
        if (messages.size != other.messages.size) messages = other.messages.copyOf()
        else other.messages.copyInto(messages)
        reversed = other.reversed
        index = other.index
    }

    fun clone(): MessageSequence = MessageSequence(
        if (reversed) messages.copyOfRange(0, index + 1) else messages.copyOfRange(index, messages.size),
        reversed
    )

    fun isEmpty(): Boolean = if (reversed) index == -1 else index == messages.size
    fun isNotEmpty(): Boolean = !isEmpty()
    override fun toString(): String =
        messages.asList().subList(if (reversed) 0 else index, if (reversed) index + 1 else messages.size)
            .joinToString("") { it.toString() }
}

fun MessageSequence.firstNotEmptyPlainText(): PlainText? {
    val message = get { if (it is PlainText) it.stringValue.isNotEmpty() else true } ?: return null
    if (message is PlainText) return message
    else {
        put(message)
        return null
    }
}

fun MessageSequence(messages: Iterable<SingleMessage>, reversed: Boolean = false): MessageSequence =
    MessageSequence(messages.toList().toTypedArray(), reversed)

/**
 * 消息匹配类，通过多个 [Element] 来匹配 [MessageSequence]，支持正向和反向匹配。
 *
 * @see SingleMessage
 * @see MessageMatcher.Element
 **/
class MessageMatcher(val elements: List<Element<*>>) {
    constructor(vararg elements: Element<*>) : this(elements.toList())

    abstract class Element<T : Any>(argumentName: String? = null) {
        var argumentName: String? = argumentName
            internal set
        var matchedMessage: Message? = null
            protected set

        /**
         * 从指定的 [messages] 消耗所需要的消息知道匹配成功，并加入新的元素（如果可能）。
         * 如：消息为 ["你好啊！"]，匹配元素为"你好"。则匹配元素在消耗"你好啊！"这个消息后会加入一个"啊！"的消息，以备接下来的匹配。
         *
         * @return 是否匹配成功
         */
        abstract fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean

        /**
         * 用于子类设置解析结果
         */
        protected fun setResult(resultMap: MutableMap<String, Any>?, result: T) {
            val argumentName = argumentName
            if (resultMap != null && argumentName != null) resultMap[argumentName] = result
        }

        /**
         * 用于子类设置解析结果（lazy）
         */
        protected fun setResult(resultMap: MutableMap<String, Any>?, result: Lazy<T>) {
            val argumentName = argumentName
            if (resultMap != null && argumentName != null) resultMap[argumentName] = result.value
        }

        @Suppress("unused")
        protected inline fun <reified T : SingleMessage> Element<T>.getByType(messages: MessageSequence): T? {
            val message = messages.first() ?: return null
            if (message !is T) {
                messages.put(message)
                return null
            }
            return message
        }

        @Suppress("unused")
        protected inline fun <reified T : SingleMessage> Element<T>.consumeByType(
            messages: MessageSequence,
            resultMap: MutableMap<String, Any>?
        ): Boolean {
            val message = getByType(messages) ?: return false
            matchedMessage = message
            setResult(resultMap, message)
            return true
        }

        @Suppress("unused")
        protected inline fun <reified T : SingleMessage> Element<T>.consumeByPredictAndType(
            predict: (T) -> Boolean,
            messages: MessageSequence,
            resultMap: MutableMap<String, Any>?
        ): Boolean {
            val message = getByType(messages) ?: return false
            return if (predict(message)) {
                matchedMessage = message
                setResult(resultMap, message)
                true
            } else false
        }
    }
}

fun <R> MessageMatcher(lambda: MessageMatcherBuilder.() -> R): MessageMatcher =
    MessageMatcher(MessageMatcherBuilder().apply { lambda() }.messages)

fun Iterable<SingleMessage>.matchFromBegin(
    matcher: MessageMatcher,
    result: MutableMap<String, Any>? = null
): Boolean =
    MessageSequence(this, false).consume(matcher, result)

fun Iterable<SingleMessage>.matchFromEnd(
    matcher: MessageMatcher,
    result: MutableMap<String, Any>? = null
): Boolean =
    MessageSequence(this, true).consume(matcher, result)

fun Iterable<SingleMessage>.match(
    matchers: Iterable<MessageMatcher>,
    result: MutableMap<String, Any>? = null
): Boolean = MessageSequence(this).match(matchers, result)

fun Iterable<SingleMessage>.match(
    matcher: MessageMatcher,
    result: MutableMap<String, Any>? = null
): Boolean = MessageSequence(this).match(matcher, result)

private fun String.remove(len: Int, reversed: Boolean): String =
    if (reversed) substring(0, length - len) else substring(len)

/**
 * 对 [MessageMatcher.Element] 的一个封装，用于匹配“对 [delegate] 的匹配次数在 [timeRange] 之内”的消息
 * 如：`[WrappedMessageMatcherElement(CharMessageMatcherElement { it=='C' }, 2..3)]` 可以匹配"CC"和"CCC"
 * 该匹配元素的 [matchedMessage] 为多次匹配的 [matchedMessage] 顺序相加之和
 */
class WrappedMessageMatcherElement<T : Any>(val delegate: MessageMatcher.Element<T>, val timeRange: IntRange = 1..1) :
    MessageMatcher.Element<T>(delegate.argumentName) {
    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean {
        var message: Message? = null
        val original = messages.clone()
        val reversed = messages.reversed
        for (i in 1..timeRange.first)
            if (delegate.consume(messages, resultMap)) {
                val newMessage = delegate.matchedMessage ?: continue
                message = if (message == null) newMessage
                else (if (reversed) (newMessage + message) else (message + newMessage))
            } else {
                messages.set(original)
                return false
            }
        for (i in timeRange.first + 1..timeRange.last)
            if (delegate.consume(messages, resultMap)) {
                val newMessage = delegate.matchedMessage ?: continue
                message = if (message == null) newMessage
                else (if (reversed) (newMessage + message) else (message + newMessage))
            } else return true
        return true
    }
}

infix fun <T : Any> MessageMatcher.Element<T>.withRange(timeRange: IntRange): WrappedMessageMatcherElement<T> =
    WrappedMessageMatcherElement(this, timeRange)


/**
 * 用于根据 [predict] 来匹配一个字符
 */
open class CharMessageMatcherElement(argumentName: String? = null, private val predict: (Char) -> Boolean) :
    MessageMatcher.Element<Char>(argumentName) {
    constructor(vararg chars: Char, argumentName: String? = null) : this(argumentName, { it in chars })

    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean {
        val message = messages.firstNotEmptyPlainText() ?: return false
        val ret: Boolean
        val ch: Char
        if (messages.reversed) {
            ch = message.stringValue.last()
            ret = predict(ch)
        } else {
            ch = message.stringValue.first()
            ret = predict(ch)
        }
        if (ret) {
            matchedMessage = PlainText(ch.toString())
            setResult(resultMap, ch)
            messages.put(PlainText(message.stringValue.remove(1, messages.reversed)))
        } else messages.put(message)
        return ret
    }
}

/**
 * 用于匹配一个或多个字符串。
 * 注：这里并不是顺序匹配，而是只要出现了任意一个在 [strings] 中的字符串就算匹配成功
 */
open class StringMessageMatcherElement(private val strings: Set<String>, argumentName: String? = null) :
    MessageMatcher.Element<String>(argumentName) {
    constructor(vararg strings: String, argumentName: String? = null) : this(strings.toSet(), argumentName)

    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean {
        val message = messages.firstNotEmptyPlainText() ?: return false
        var ret = false
        var matched = ""
        val stringValue = message.stringValue
        if (messages.reversed) {
            for (str in strings) {
                if (stringValue.endsWith(str)) {
                    ret = true
                    matched = str
                    break
                }
            }
        } else {
            for (str in strings) {
                if (stringValue.startsWith(str)) {
                    ret = true
                    matched = str
                    break
                }
            }
        }
        if (ret) {
            matchedMessage = PlainText(matched)
            setResult(resultMap, matched)
            messages.put(PlainText(stringValue.remove(matched.length, messages.reversed)))
        }
        return ret
    }
}

/**
 * 用于匹配一个类型为 [T] 的消息
 */
inline fun <reified T : SingleMessage> TypedMessageMatcherElement(
    argumentName: String? = null
): MessageMatcher.Element<T> = object : MessageMatcher.Element<T>(argumentName) {
    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean =
        consumeByType(messages, resultMap)
}

/**
 * 用于匹配一个类型为 [T] 且满足 [predict] 的消息
 */
inline fun <reified T : SingleMessage> TypedMessageMatcherElementWithPredict(
    crossinline predict: (T) -> Boolean = { true },
    argumentName: String? = null
): MessageMatcher.Element<T> = object : MessageMatcher.Element<T>(argumentName) {
    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean =
        consumeByPredictAndType(predict, messages, resultMap)
}

/**
 * 匹配对一个人的引用，支持"你"、"我"、QQ号和At
 */
open class PersonMessageMatcherElement(argumentName: String? = null) :
    MessageMatcher.Element<PersonMessageMatcherElement.PersonReference>(argumentName) {
    sealed class PersonReference(val display: String) {
        class Specific(private val uin: Long, display: String) : PersonReference(display) {
            override fun getUIN(packet: MessagePacket<*, *>?): Long = uin
        }

        class Pronoun(private val me: Boolean) : PersonReference(if (me) "你" else "我") {
            override fun getUIN(packet: MessagePacket<*, *>?): Long =
                if (packet == null) throw IllegalStateException()
                else (if (me) packet.sender.id else packet.bot.uin)
        }

        abstract fun getUIN(packet: MessagePacket<*, *>?): Long
    }

    private fun Char.isDigit(): Boolean = this in '0'..'9'

    override fun consume(messages: MessageSequence, resultMap: MutableMap<String, Any>?): Boolean {
        val message = messages.first() ?: return false
        when (message) {
            is At -> {
                matchedMessage = message
                setResult(resultMap, lazy { PersonReference.Specific(message.target, message.display) })
                return true
            }
            is PlainText -> {
                val reversed = messages.reversed
                val stringValue = (messages.firstNotEmptyPlainText() ?: return false).stringValue
                val ch = if (reversed) stringValue.last() else stringValue.first()
                if (ch == '你' || ch == '我') {
                    matchedMessage = PlainText(ch.toString())
                    setResult(resultMap, lazy { PersonReference.Pronoun(ch == '我') })
                    messages.put(PlainText(stringValue.remove(1, reversed)))
                } else if (ch.isDigit()) {
                    val uinString =
                        if (reversed) stringValue.takeLastWhile { it.isDigit() }
                        else stringValue.takeWhile { it.isDigit() }
                    try {
                        val uin = uinString.toLong()
                        matchedMessage = PlainText(uinString)
                        setResult(resultMap, lazy { PersonReference.Specific(uin, uinString) })
                        messages.put(PlainText(stringValue.remove(uinString.length, reversed)))
                        return true
                    } catch (e: NumberFormatException) {
                        return false
                    }
                }
                return true
            }
            else -> return false
        }
    }
}

infix fun <R : Any> MessageMatcher.Element<R>.names(name: String): MessageMatcher.Element<R> =
    also { argumentName = name }

/**
 * 匹配一段空白字符（也可以没有）
 */
val BlankMessageMatcherElement = CharMessageMatcherElement { it.isWhitespace() } withRange 0..Int.MAX_VALUE

class MessageMatcherBuilder {
    val messages = mutableListOf<MessageMatcher.Element<*>>()

    fun add(element: MessageMatcher.Element<*>) = messages.add(element)
    fun Char.toElement() = CharMessageMatcherElement { it == this }
    fun Set<Char>.toElement() = CharMessageMatcherElement { it in this }
    fun String.toElement() = StringMessageMatcherElement(this)
    fun Set<String>.toElement() = StringMessageMatcherElement(this)
}