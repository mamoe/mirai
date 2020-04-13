/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/////////////////////////
//// IMPLEMENTATIONS ////
/////////////////////////


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

@JvmSynthetic
internal fun Message.followedByImpl(tail: Message): MessageChain {
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


@OptIn(MiraiExperimentalAPI::class)
@JvmSynthetic
internal fun Sequence<SingleMessage>.constrainSingleMessages(): List<SingleMessage> {
    val iterator = this.iterator()
    return constrainSingleMessagesImpl supplier@{
        if (iterator.hasNext()) {
            iterator.next()
        } else null
    }
}

@MiraiExperimentalAPI
@JvmSynthetic
internal inline fun constrainSingleMessagesImpl(iterator: () -> SingleMessage?): ArrayList<SingleMessage> {
    val list = ArrayList<SingleMessage>()
    var firstConstrainIndex = -1

    var next: SingleMessage?
    do {
        next = iterator()
        next?.let { singleMessage ->
            if (singleMessage is ConstrainSingle<*>) {
                if (firstConstrainIndex == -1) {
                    firstConstrainIndex = list.size // we are going to add one
                } else {
                    val key = singleMessage.key
                    val index = list.indexOfFirst(firstConstrainIndex) { it is ConstrainSingle<*> && it.key == key }
                    if (index != -1) {
                        list[index] = singleMessage
                        return@let
                    }
                }
            }

            list.add(singleMessage)
        } ?: return list
    } while (true)
}

@JvmSynthetic
@OptIn(MiraiExperimentalAPI::class)
internal fun Iterable<SingleMessage>.constrainSingleMessages(): List<SingleMessage> {
    val iterator = this.iterator()
    return constrainSingleMessagesImpl supplier@{
        if (iterator.hasNext()) {
            iterator.next()
        } else null
    }
}

@JvmSynthetic
internal inline fun <T> List<T>.indexOfFirst(offset: Int, predicate: (T) -> Boolean): Int {
    for (index in offset..this.lastIndex) {
        if (predicate(this[index]))
            return index
    }
    return -1
}


@OptIn(MiraiExperimentalAPI::class)
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
internal fun <M : Message> MessageChain.firstOrNullImpl(key: Message.Key<M>): M? = when (key) {
    At -> firstIsInstanceOrNull<At>()
    AtAll -> firstIsInstanceOrNull<AtAll>()
    PlainText -> firstIsInstanceOrNull<PlainText>()
    Image -> firstIsInstanceOrNull<Image>()
    OnlineImage -> firstIsInstanceOrNull<OnlineImage>()
    OfflineImage -> firstIsInstanceOrNull<OfflineImage>()
    GroupImage -> firstIsInstanceOrNull<GroupImage>()
    FriendImage -> firstIsInstanceOrNull<FriendImage>()
    Face -> firstIsInstanceOrNull<Face>()
    QuoteReply -> firstIsInstanceOrNull<QuoteReply>()
    MessageSource -> firstIsInstanceOrNull<MessageSource>()
    OnlineMessageSource -> firstIsInstanceOrNull<OnlineMessageSource>()
    OfflineMessageSource -> firstIsInstanceOrNull<OfflineMessageSource>()
    OnlineMessageSource.Outgoing -> firstIsInstanceOrNull<OnlineMessageSource.Outgoing>()
    OnlineMessageSource.Outgoing.ToGroup -> firstIsInstanceOrNull<OnlineMessageSource.Outgoing.ToGroup>()
    OnlineMessageSource.Outgoing.ToFriend -> firstIsInstanceOrNull<OnlineMessageSource.Outgoing.ToFriend>()
    OnlineMessageSource.Incoming -> firstIsInstanceOrNull<OnlineMessageSource.Incoming>()
    OnlineMessageSource.Incoming.FromGroup -> firstIsInstanceOrNull<OnlineMessageSource.Incoming.FromGroup>()
    OnlineMessageSource.Incoming.FromFriend -> firstIsInstanceOrNull<OnlineMessageSource.Incoming.FromFriend>()
    OnlineMessageSource -> firstIsInstanceOrNull<OnlineMessageSource>()
    XmlMessage -> firstIsInstanceOrNull<XmlMessage>()
    JsonMessage -> firstIsInstanceOrNull<JsonMessage>()
    RichMessage -> firstIsInstanceOrNull<RichMessage>()
    LightApp -> firstIsInstanceOrNull<LightApp>()
    PokeMessage -> firstIsInstanceOrNull<PokeMessage>()
    HummerMessage -> firstIsInstanceOrNull<HummerMessage>()
    FlashImage -> firstIsInstanceOrNull<FlashImage>()
    GroupFlashImage -> firstIsInstanceOrNull<GroupFlashImage>()
    FriendFlashImage -> firstIsInstanceOrNull<FriendFlashImage>()
    else -> null
} as M?

/**
 * 使用 [Collection] 作为委托的 [MessageChain]
 */
internal class MessageChainImplByCollection constructor(
    internal val delegate: Collection<SingleMessage> // 必须 constrainSingleMessages, 且为 immutable
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int get() = delegate.size
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()

    private var toStringTemp: String? = null
        get() = field ?: this.delegate.joinToString("") { it.toString() }.also { field = it }

    override fun toString(): String = toStringTemp!!

    private var contentToStringTemp: String? = null
        get() = field ?: this.delegate.joinToString("") { it.contentToString() }.also { field = it }

    override fun contentToString(): String = contentToStringTemp!!
}

/**
 * 使用 [Iterable] 作为委托的 [MessageChain]
 */
internal class MessageChainImplBySequence constructor(
    delegate: Sequence<SingleMessage> // 可以有重复 ConstrainSingle
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int by lazy { collected.size }

    /**
     * [Sequence] 可能只能消耗一遍, 因此需要先转为 [List]
     */
    private val collected: List<SingleMessage> by lazy { delegate.constrainSingleMessages() }
    override fun iterator(): Iterator<SingleMessage> = collected.iterator()

    private var toStringTemp: String? = null
        get() = field ?: this.joinToString("") { it.toString() }.also { field = it }

    override fun toString(): String = toStringTemp!!

    private var contentToStringTemp: String? = null
        get() = field ?: this.joinToString("") { it.contentToString() }.also { field = it }

    override fun contentToString(): String = contentToStringTemp!!
}

/**
 * 单个 [SingleMessage] 作为 [MessageChain]
 */
internal class SingleMessageChainImpl constructor(
    internal val delegate: SingleMessage
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int get() = 1
    override fun toString(): String = this.delegate.toString()
    override fun contentToString(): String = this.delegate.contentToString()
    override fun iterator(): Iterator<SingleMessage> = iterator { yield(delegate) }
}

