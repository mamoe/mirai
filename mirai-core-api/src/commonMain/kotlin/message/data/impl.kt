/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:OptIn(MiraiInternalApi::class)

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.Image.Key.IMAGE_ID_REGEX
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_1
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_2
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.acceptChildren
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.asImmutable
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.replaceAllKotlin
import kotlin.jvm.JvmField
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

// region image

/////////////////////////
//// IMPLEMENTATIONS ////
/////////////////////////

@JvmSynthetic
internal fun Message.contentEqualsStrictImpl(another: Message, ignoreCase: Boolean): Boolean {
    if (!this.contentToString().equals(another.contentToString(), ignoreCase = ignoreCase)) return false
    return when {
        this is SingleMessage && another is SingleMessage -> true
        this is SingleMessage && another is MessageChain -> another.all { it is MessageMetadata || it is PlainText }
        this is MessageChain && another is SingleMessage -> this.all { it is MessageMetadata || it is PlainText }
        this is MessageChain && another is MessageChain -> {
            val anotherIterator = another.iterator()

            /**
             * 逐个判断非 [PlainText] 的 [Message] 是否 [equals]
             */
            this.contentsSequence().forEach { thisElement ->
                if (thisElement is PlainText) return@forEach
                for (it in anotherIterator) {
                    if (it is PlainText || it !is MessageContent) continue
                    if (thisElement != it) return false
                }
            }
            return true
        }
        else -> error("shouldn't be reached")
    }
}


internal sealed class AbstractMessageChain : MessageChain {
    /**
     * 去重算法 v1 - 2.12:
     * 在连接时若只有 0-1 方包含 [ConstrainSingle], 则使用 [CombinedMessage] 优化性能. 否则使用旧版复杂去重算法构造 [LinearMessageChainImpl].
     */
    @MiraiInternalApi
    abstract val hasConstrainSingle: Boolean

    @OptIn(MiraiInternalApi::class)
    override fun hashCode(): Int {
        var result = 1
        acceptChildren(object : RecursiveMessageVisitor<Unit>() {
//            override fun visitMessageChain(messageChain: MessageChain, data: Unit) {
//                result = 31 * result + messageChain.hashCode()
//                // do not call children
//            }

            // ensure `messageChainOf(messageChainOf(AtAll))` and `messageChainOf(AtAll)` get same hash code.
            override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                result = 31 * result + message.hashCode()
                super.visitSingleMessage(message, data)
            }
        })

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (other !is MessageChain) return false
        return chainEquals(this, other)
    }

    private companion object {
        private fun chainEquals(a: MessageChain, b: MessageChain): Boolean {
            if (a.size != b.size) return false // Averagely faster even if we may end up counting size.

            val itr1 = a.iterator()
            val itr2 = b.iterator()
            for (singleMessage in itr1) {
                if (!itr2.hasNext()) return false
                val n = itr2.next()
                if (singleMessage != n) return false
            }
            return true
        }
    }
}

@OptIn(MiraiInternalApi::class)
internal val Message.hasConstrainSingle: Boolean
    get() {
        if (this is SingleMessage) return this is ConstrainSingle
        // now `this` is MessageChain
        return this.castOrNull<AbstractMessageChain>()?.hasConstrainSingle ?: true // for external type, assume they do
    }

/**
 * @see ConstrainSingleHelper.constrainSingleMessages
 */
internal data class ConstrainSingleData(
    val value: List<SingleMessage>,
    val hasConstrainSingle: Boolean,
)

internal object ConstrainSingleHelper {
    @JvmName("constrainSingleMessages_Sequence")
    internal fun constrainSingleMessages(sequence: Sequence<Message>): ConstrainSingleData =
        constrainSingleMessages(sequence.flatMap { it.toMessageChain() })

    internal fun constrainSingleMessages(sequence: Sequence<SingleMessage>): ConstrainSingleData =
        constrainSingleMessagesImpl(sequence)

    /**
     * - [Sequence.toMutableList]
     * - Replace in-place with marker null
     */
    private fun constrainSingleMessagesImpl(sequence: Sequence<SingleMessage>): ConstrainSingleData {
        val list: MutableList<SingleMessage?> = sequence.toMutableList()

        var hasConstrainSingle = false
        for (singleMessage in list.asReversed()) {
            if (singleMessage is ConstrainSingle) {
                hasConstrainSingle = true
                val key = singleMessage.key.topmostKey
                val firstOccurrence = list.first { it != null && key.isInstance(it) } // may be singleMessage itself
                list.replaceAllKotlin {
                    when {
                        it == null -> null
                        it === firstOccurrence -> singleMessage
                        key.isInstance(it) -> null // remove duplicates
                        else -> it
                    }
                }
            }
        }

        return ConstrainSingleData(list.filterNotNull(), hasConstrainSingle)
    }

}

/**
 * 要求 opt-in, 避免意外调用构造器.
 */
@RequiresOptIn
internal annotation class MessageChainConstructor


/**
 * 使用 [Collection] 作为委托的 [MessageChain]
 */
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(MessageChain.Serializer::class)
internal class LinearMessageChainImpl @MessageChainConstructor private constructor(
    /**
     * Must be guaranteed to be immutable
     */
    @JvmField
    internal val delegate: List<SingleMessage>,
    override val hasConstrainSingle: Boolean
) : Message, MessageChain, List<SingleMessage> by delegate, AbstractMessageChain(),
    DirectSizeAccess, DirectToStringAccess {
    override val size: Int get() = delegate.size
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()

    private val toStringTemp: String by lazy { this.delegate.joinToString("") { it.toString() } }
    override fun toString(): String = toStringTemp

    private val contentToStringTemp: String by lazy { this.delegate.joinToString("") { it.contentToString() } }
    override fun contentToString(): String = contentToStringTemp

    override fun <D> acceptChildren(visitor: MessageVisitor<D, *>, data: D) {
        for (singleMessage in delegate) {
            singleMessage.accept(visitor, data)
        }
    }

    companion object {
        fun combineCreate(message: Message, tail: Message): MessageChain {
            return create(
                ConstrainSingleHelper.constrainSingleMessages(
                    message.toMessageChain().asSequence() + tail.toMessageChain().asSequence()
                )
            )
            /*
            when {
                this is SingleMessage && tail is SingleMessage -> {
                    if (this is ConstrainSingle && tail is ConstrainSingle) {
                        if (this.key == tail.key)
                            return SingleMessageChainImpl(tail)
                    }
                    return CombinedMessage(this, tail)
                }

                this is SingleMessage -> { // tail is not
                    tail as MessageChain

                    if (this is ConstrainSingle) {
                        val key = this.key
                        if (tail.any { (it as? ConstrainSingle)?.key == key }) {
                            return tail
                        }
                    }
                    return CombinedMessage(this, tail)
                }

                tail is SingleMessage -> {
                    this as MessageChain

                    if (tail is ConstrainSingle && this.hasDuplicationOfConstrain(tail.key)) {
                        return MessageChainImplByCollection(constrainSingleMessagesImpl(this.asSequence() + tail))
                    }

                    return CombinedMessage(this, tail)
                }

                else -> { // both chain
                    this as MessageChain
                    tail as MessageChain

                    return MessageChainImplByCollection(
                        constrainSingleMessagesImpl(this.asSequence() + tail)
                    )
                }
            }*/
        }

        /**
         * @param delegate must be immutable
         */
        @OptIn(MessageChainConstructor::class)
        fun create(delegate: List<SingleMessage>, hasConstrainSingle: Boolean): MessageChain {
            return if (delegate.isEmpty()) {
                emptyMessageChain()
            } else {
                LinearMessageChainImpl(delegate.asImmutable(), hasConstrainSingle)
            }
        }

        fun create(data: ConstrainSingleData): MessageChain {
            return create(data.value, data.hasConstrainSingle)
        }
    }
}


//////////////////////
// region Image impl
//////////////////////


@get:JvmSynthetic
internal val EMPTY_BYTE_ARRAY = ByteArray(0)


@JvmSynthetic
@Suppress("NOTHING_TO_INLINE") // no stack waste
internal inline fun Char.hexDigitToByte(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'A'..'F' -> 10 + (this - 'A')
        in 'a'..'f' -> 10 + (this - 'a')
        else -> throw IllegalArgumentException("Illegal hex digit: $this")
    }
}

@JvmSynthetic
internal fun String.skipToSecondHyphen(): Int {
    var count = 0
    this.forEachIndexed { index, c ->
        if (c == '-' && ++count == 2) return index
    }
    error("Internal error: failed skipToSecondHyphen, cannot find two hyphens. Input=$this")
}

@JvmSynthetic
internal fun String.imageIdToMd5(offset: Int): ByteArray {
    val result = ByteArray(16)
    var cur = 0
    var hasCurrent = false
    var lastChar: Char = 0.toChar()
    for (index in offset..this.lastIndex) {
        val char = this[index]
        if (char == '-') continue
        if (hasCurrent) {
            result[cur++] = (lastChar.hexDigitToByte().shl(4) or char.hexDigitToByte()).toByte()
            if (cur == 16) return result
            hasCurrent = false
        } else {
            lastChar = char
            hasCurrent = true
        }
    }
    error("Internal error: failed imageIdToMd5, no enough chars. Input=$this, offset=$offset")
}

internal val ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE: String =
    "ImageId must match Regex `${IMAGE_RESOURCE_ID_REGEX_1.pattern}`, " +
            "`${IMAGE_RESOURCE_ID_REGEX_2.pattern}` or " +
            "`${IMAGE_ID_REGEX.pattern}`"

// endregion