/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.Image.Key.FRIEND_IMAGE_ID_REGEX_1
import net.mamoe.mirai.message.data.Image.Key.FRIEND_IMAGE_ID_REGEX_2
import net.mamoe.mirai.message.data.Image.Key.GROUP_IMAGE_ID_REGEX
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.native.concurrent.SharedImmutable

// region image

/////////////////////////
//// IMPLEMENTATIONS ////
/////////////////////////

@Suppress("unused", "UNUSED_PARAMETER")
private fun Message.hasDuplicationOfConstrain(key: MessageKey<*>): Boolean {
    return true
    /*
     return when (this) {
         is SingleMessage -> (this as? ConstrainSingle)?.key == key
         is CombinedMessage -> return this.left.hasDuplicationOfConstrain(key) || this.tail.hasDuplicationOfConstrain(key)
         is MessageChainImplByCollection -> this.delegate.any { (it as? ConstrainSingle)?.key == key }
         else -> error("stub")
     }*/
}

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
            this.forEachContent { thisElement ->
                if (thisElement.isPlain()) return@forEachContent
                for (it in anotherIterator) {
                    if (it.isPlain() || it !is MessageContent) continue
                    if (thisElement != it) return false
                }
            }
            return true
        }
        else -> error("shouldn't be reached")
    }
}

@JvmSynthetic
internal fun Message.followedByImpl(tail: Message): MessageChain {
    return MessageChainImplBySequence(this.flatten() + tail.flatten())
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


@JvmSynthetic
internal fun Sequence<SingleMessage>.constrainSingleMessages(): List<SingleMessage> =
    constrainSingleMessagesImpl(this.asSequence())

/**
 * - [Sequence.toMutableList]
 * - Replace in-place with marker null
 */
@MiraiExperimentalApi
@JvmSynthetic
internal fun constrainSingleMessagesImpl(sequence: Sequence<SingleMessage>): List<SingleMessage> {
    val list: MutableList<SingleMessage?> = sequence.toMutableList()

    for (singleMessage in list.asReversed()) {
        if (singleMessage is ConstrainSingle) {
            val key = singleMessage.key.topmostKey
            val firstOccurrence = list.first { it != null && key.isInstance(it) } // may be singleMessage itself
            list.replaceAll {
                when {
                    it == null -> null
                    it === firstOccurrence -> singleMessage
                    key.isInstance(it) -> null // remove duplicates
                    else -> it
                }
            }
        }
    }

    return list.filterNotNull()
}

@JvmSynthetic
internal fun Iterable<SingleMessage>.constrainSingleMessages(): List<SingleMessage> =
    constrainSingleMessagesImpl(this.asSequence())


@JvmSynthetic
@Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR", "DEPRECATION")
internal fun <M : SingleMessage> MessageChain.getImpl(key: MessageKey<M>): M? {
    return this.asSequence().mapNotNull { key.safeCast.invoke(it) }.firstOrNull()
}

/**
 * 使用 [Collection] 作为委托的 [MessageChain]
 */
@Serializable
internal data class MessageChainImpl constructor(
    @JvmField
    internal val delegate: List<SingleMessage> // 必须 constrainSingleMessages, 且为 immutable
) : Message, MessageChain, List<SingleMessage> by delegate {
    override val size: Int get() = delegate.size
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()

    private val toStringTemp: String by lazy { this.delegate.joinToString("") { it.toString() } }
    override fun toString(): String = toStringTemp

    private val contentToStringTemp: String by lazy { this.delegate.joinToString("") { it.contentToString() } }
    override fun contentToString(): String = contentToStringTemp

    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean = other is MessageChainImpl && other.delegate == this.delegate
}

@Suppress("FunctionName") // source compatibility with 1.x
internal fun MessageChainImplBySequence(
    delegate: Sequence<SingleMessage> // 可以有重复 ConstrainSingle
): MessageChain = MessageChainImpl(delegate.constrainSingleMessages())

@Suppress("FunctionName")
internal fun SingleMessageChainImpl(
    delegate: SingleMessage
): MessageChain = MessageChainImpl(listOf(delegate))


//////////////////////
// region Image impl
//////////////////////


@SharedImmutable
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

@OptIn(ExperimentalStdlibApi::class)
internal fun calculateImageMd5ByImageId(imageId: String): ByteArray {
    @Suppress("DEPRECATION")
    return when {
        imageId matches FRIEND_IMAGE_ID_REGEX_2 -> imageId.imageIdToMd5(imageId.skipToSecondHyphen() + 1)
        imageId matches FRIEND_IMAGE_ID_REGEX_1 -> imageId.imageIdToMd5(1)
        imageId matches GROUP_IMAGE_ID_REGEX -> imageId.imageIdToMd5(1)

        else -> error(
            "illegal imageId: $imageId. $ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE"
        )
    }
}

internal val ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE: String =
    "ImageId must match Regex `${FRIEND_IMAGE_ID_REGEX_1.pattern}`, " +
            "`${FRIEND_IMAGE_ID_REGEX_2.pattern}` or " +
            "`${GROUP_IMAGE_ID_REGEX.pattern}`"

// endregion