/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.encode

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.toLongUnsigned
import kotlin.jvm.JvmStatic
import kotlin.math.log10


/**
 * An object that stores these length properties.
 * @see MessageLengthVerifier
 */
internal interface MessageLengthTokens {
    val uiChars: Long
    val uiImages: Long
    val uiForwardNodes: Long
//    val protocolTotal: Long

    companion object {
        val comparator: Comparator<MessageLengthTokens> =
            compareBy<MessageLengthTokens> { it.uiChars }
                .then(compareBy { it.uiImages })
                .then(compareBy { it.uiForwardNodes })
//                .then(compareBy { it.protocolTotal })
    }
}

/**
 * A [MessageVisitor] that calculates and verifies length of a message.
 *
 * Can be applied to any [Message] by calling[Message.accept] passing this visitor.
 *
 * Applying to [ForwardMessage] verifies the [ForwardMessage] itself and its nodes recursively.
 *
 * Use properties from [MessageLengthTokens] to retrieve calculation results.
 * @since 2.14
 */
internal interface MessageLengthVerifier : MessageVisitor<Unit, Unit>, MessageLengthTokens {
    val nestedVerifiers: List<MessageLengthVerifier>

    fun isLengthValid(): Boolean
}

/**
 * Gets an [MessageLengthVerifier] with specified configuration [lengthTokens] and [context].
 */
internal fun MessageLengthVerifier(
    context: Contact?,
    lengthTokens: MessageLengthLimits,
    failfast: Boolean,
): MessageLengthVerifier {
    return MessageLengthVerifierImpl(context, lengthTokens, failfast)
}

/**
 * Specifies length limits for [MessageLengthVerifier]
 * @sample net.mamoe.mirai.internal.message.protocol.encode.MessageLengthVerifierTest
 */
internal class MessageLengthLimits(
    override val uiChars: Long = 5000,// 5000 chars
    override val uiImages: Long = 50,
    override val uiForwardNodes: Long = 200, // 200 nodes for each forward message

//    override val protocolTotal: Long = 1 * 1000 * 1000, // 1 MB
) : MessageLengthTokens {
    companion object {
        @JvmStatic
        val DEFAULT = MessageLengthLimits()
    }
}

///////////////////////////////////////////////////////////////////////////
// IMPLEMENTATION
///////////////////////////////////////////////////////////////////////////

private inline operator fun MessageLengthTokens.compareTo(other: MessageLengthTokens): Int =
    MessageLengthTokens.comparator.compare(this, other)

internal class MessageLengthVerifierImpl constructor(
    private val context: Contact?,
    private val limits: MessageLengthLimits,
    private val failfast: Boolean,
) : RecursiveMessageVisitor<Unit>(), MessageLengthVerifier {
    override val nestedVerifiers: MutableList<MessageLengthVerifierImpl> = mutableListOf()
    private var hasInvalidNested: Boolean = false

    /**
     * 展示在 UI 的字符长度.
     * @see MessageLengthLimits.uiChars
     */
    override var uiChars: Long = 0
        private set

    override var uiImages: Long = 0
        private set

    override var uiForwardNodes: Long = 0
        private set

    override fun isFinished(): Boolean {
        if (!failfast) return false
        return !isLengthValid()
    }

    override fun isLengthValid(): Boolean = this <= limits && !hasInvalidNested

    override fun visitPlainText(message: PlainText, data: Unit) {
        uiChars += message.content.length
    }

    override fun visitAt(message: At, data: Unit) {
        val length = message.displayInGroup()
            ?: message.target.numberOfDigitsInDecimal
        uiChars += length + 1 // + `@`
    }

    private fun At.displayInGroup(): Long? {
        return if (context is Group) {
            context.getMember(target)?.nameCardOrNick?.length?.toLongUnsigned()
        } else {
            null
        }
    }

    override fun visitAtAll(message: AtAll, data: Unit) {
        uiChars += message.content.length
    }

    override fun visitFace(message: Face, data: Unit) {
        uiChars += 4
    }

    override fun visitImage(message: Image, data: Unit) {
        uiImages++
//        protocolTotal = TypicalMessageSize.image
    }

    override fun visitFlashImage(message: FlashImage, data: Unit) {
        visitImage(message.image, data)
    }

    override fun visitQuoteReply(message: QuoteReply, data: Unit) {
        message.source.originalMessage.accept(this)
    }

    override fun visitForwardMessage(message: ForwardMessage, data: Unit) {
        val nested = MessageLengthVerifierImpl(context, limits, failfast)
        nestedVerifiers.add(nested)
        
        for (node in message.nodeList) {
            if (nested.isFinished()) break
            nested.visitForwardMessageNode(node)
        }
        
        if (!nested.isLengthValid()) {
            hasInvalidNested = true
        }
    }

    fun visitForwardMessageNode(node: ForwardMessage.INode) {
        uiForwardNodes++
        node.messageChain.accept(this)
    }

    companion object {
        val Long.numberOfDigitsInDecimal: Long
            get() = if (this == 0L) 1 else 1 + log10(this.toDouble()).toLong()
    }
}


//private object TypicalMessageSize {
//    @Serializable
//    private class Elements(
//        @ProtoNumber(1) val elements: List<ImMsgBody.Elem>
//    )
//
//    val image: Long = kotlin.run {
//        val elems = MessageProtocolFacade.encode(
//            chain = Image("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg").toMessageChain(),
//            messageTarget = null,
//            withGeneralFlags = false,
//            isForward = false
//        )
//        ProtoBuf.encodeToByteArray(Elements.serializer(), Elements(elems)).size.toLongUnsigned()
//    }
//}