/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 树状消息链的一个节点. [element] 为当前元素, [tail] 为下一个元素.
 *
 * **注意:** 这是内部 API. 不要在任何情况下使用它.
 * [Message.plus] 等连接消息链的 API 已经有性能优化, 会在合适的时机创建 [CombinedMessage], 不需要自行考虑创建 [CombinedMessage].
 *
 * @since 2.12
 */
@MiraiInternalApi
@Suppress("EXPOSED_SUPER_CLASS")
public class CombinedMessage @MessageChainConstructor constructor(
    @MiraiInternalApi public val element: Message, // element 和 tail 必须提前处理 constrain single. 见 Message.followedBy
    @MiraiInternalApi public val tail: Message,
    @MiraiInternalApi public override val hasConstrainSingle: Boolean
) : AbstractMessageChain(), List<SingleMessage> {
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitCombinedMessage(this, data)
    }

    override fun <D> acceptChildren(visitor: MessageVisitor<D, *>, data: D) {
        element.accept(visitor, data)
        tail.accept(visitor, data)
    }

    override val size: Int by lazy {
        if (slowList.isInitialized()) return@lazy slowList.value.size
        var size = 0
        val visitor = object : RecursiveMessageVisitor<Unit>() {
            override fun visitMessageChain(messageChain: MessageChain, data: Unit) {
                if (messageChain is DirectSizeAccess) {
                    size += messageChain.size
                    return
                }
                return super.visitMessageChain(messageChain, data)
            }

            override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                size++
            }
        }
        element.accept(visitor)
        tail.accept(visitor)
        size
    }

    override fun isEmpty(): Boolean {
        if (slowList.isInitialized()) return slowList.value.isEmpty()
        return element is MessageChain && element.isEmpty() && tail is MessageChain && tail.isEmpty()
    }

    override fun contains(element: SingleMessage): Boolean {
        if (slowList.isInitialized()) return slowList.value.contains(element)
        if (this.element == element) return true
        if (this.tail == element) return true
        if (this.element is MessageChain && this.element.contains(element)) return true
        if (this.tail is MessageChain && this.tail.contains(element)) return true
        return false
    }


    private val toStringTemp: String by lazy {
        if (slowList.isInitialized()) return@lazy slowList.value.toString()
        buildString {
            accept(object : RecursiveMessageVisitor<Unit>() {
                override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                    append(message.toString())
                }

                override fun visitMessageChain(messageChain: MessageChain, data: Unit) {
                    if (messageChain is DirectToStringAccess) {
                        append(messageChain.toString())
                    } else {
                        super.visitMessageChain(messageChain, data)
                    }
                }
            })
        }
    }

    private val contentToStingTemp: String by lazy {
        if (slowList.isInitialized()) return@lazy slowList.value.contentToString()
        buildString {
            accept(object : RecursiveMessageVisitor<Unit>() {
                override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                    append(message.contentToString())
                }

                override fun visitMessageChain(messageChain: MessageChain, data: Unit) {
                    if (messageChain is DirectToStringAccess) {
                        append(messageChain.contentToString())
                    } else {
                        super.visitMessageChain(messageChain, data)
                    }
                }
            })
        }
    }

    override fun toString(): String = toStringTemp
    override fun contentToString(): String = contentToStingTemp

    override fun containsAll(elements: Collection<SingleMessage>): Boolean {
        if (slowList.isInitialized()) return slowList.value.containsAll(elements)
        if (elements.isEmpty()) return true
        if (this.isEmpty()) return false
        val remaining = elements.toMutableList()
        accept(object : RecursiveMessageVisitor<Unit>() {
            override fun isFinished(): Boolean = remaining.isEmpty()
            override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                remaining.remove(message)
            }
        })
        return remaining.isEmpty()
    }

    override fun iterator(): Iterator<SingleMessage> {
        if (slowList.isInitialized()) return slowList.value.iterator()
        suspend fun SequenceScope<SingleMessage>.yieldMessage(element: Message) {
            when (element) {
                is SingleMessage -> {
                    yield(element)
                }
                is MessageChain -> {
                    yieldAll(element.iterator())
                }
            }
        }

        return iterator {
            yieldMessage(element)
            yieldMessage(tail)
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<SingleMessage> {
        if (slowList.isInitialized()) return slowList.value.subList(fromIndex, toIndex)
        if (fromIndex < 0 || fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex")

        return buildList {
            accept(object : RecursiveMessageVisitor<Unit>() {
                private var currentIndex = 0
                override fun isFinished(): Boolean = currentIndex >= toIndex

                override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                    if (isFinished()) return
                    if (currentIndex >= fromIndex) {
                        add(message)
                    }
                    currentIndex++
                }
            })
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // slow operations
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 仅在必要时初始化来优化性能. 基于 [ConstrainSingle] 的情况, 它可能会有立方级别的时间复杂度.
     */
    internal val slowList: Lazy<MessageChain> = lazy {
        sequenceOf(element, tail).toMessageChain()
    }

    // [MessageChain] implements [RandomAccess] so we should ensure that property here.
    override fun get(index: Int): SingleMessage = slowList.value[index]
    override fun indexOf(element: SingleMessage): Int = slowList.value.indexOf(element)
    override fun lastIndexOf(element: SingleMessage): Int = slowList.value.lastIndexOf(element)
    override fun listIterator(): ListIterator<SingleMessage> = slowList.value.listIterator()
    override fun listIterator(index: Int): ListIterator<SingleMessage> = slowList.value.listIterator(index)


}

internal interface DirectSizeAccess : MessageChain
internal interface DirectToStringAccess : MessageChain