/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "INVISIBLE_MEMBER")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutFriend
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutGroup
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutTemp
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.currentTimeSeconds

/**
 * 将在线消息源转换为离线消息源.
 */
@JvmName("toOfflineMessageSource")
public fun OnlineMessageSource.toOffline(): OfflineMessageSource =
    OfflineMessageSourceByOnline(this)

///////////////
//// AMEND ////
///////////////


/**
 * 复制这个消息源, 并以 [block] 修改
 *
 * @see buildMessageSource 查看更多说明
 */
@MiraiExperimentalApi
@JvmName("copySource")
public fun MessageSource.copyAmend(
    block: MessageSourceAmender.() -> Unit
): OfflineMessageSource = toMutableOffline().apply(block)

/**
 * 仅于 [copyAmend] 中修改 [MessageSource]
 */
public interface MessageSourceAmender {
    public var kind: MessageSourceKind
    public var fromUin: Long
    public var targetUin: Long
    public var ids: IntArray
    public var time: Int
    public var internalIds: IntArray

    public var originalMessage: MessageChain

    /** 从另一个 [MessageSource] 中复制 [ids], [internalIds], [time]*/
    public fun metadataFrom(another: MessageSource) {
        this.ids = another.ids
        this.internalIds = another.internalIds
        this.time = another.time
    }
}


///////////////
//// BUILD ////
///////////////


/**
 * 构建一个 [OfflineMessageSource]
 *
 * ### 参数
 * 一个 [OfflineMessageSource] 须要以下参数:
 * - 发送人和发送目标: 通过 [MessageSourceBuilder.sendTo] 设置
 * - 消息元数据 (即 [MessageSource.ids], [MessageSource.internalIds], [MessageSource.time])
 *   元数据用于 [撤回][MessageSource.recall], [引用回复][MessageSource.quote], 和官方客户端定位原消息.
 *   可通过 [MessageSourceBuilder.ids], [MessageSourceBuilder.time], [MessageSourceBuilder.internalIds] 设置
 *   可通过 [MessageSourceBuilder.metadata] 从另一个 [MessageSource] 复制
 * - 消息内容: 通过 [MessageSourceBuilder.messages] 设置
 *
 * ### 性质
 * - 当两个消息的元数据相同时, 他们在群中会是同一条消息. 可通过此特性决定官方客户端 "定位原消息" 的目标
 * - 发送人的信息和消息内容会在官方客户端显示在引用回复中.
 *
 * ### 实例
 * ```
 * bot.buildMessageSource {
 *     bot sendTo target // 指定发送人和发送目标
 *     metadata(source) // 从另一个消息源复制 ids, internalIds, time
 *
 *     messages { // 指定消息内容
 *         +"hi"
 *     }
 * }
 * ```
 */
@JvmSynthetic
@MiraiExperimentalApi
public fun Bot.buildMessageSource(block: MessageSourceBuilder.() -> Unit): MessageSource {
    val builder = MessageSourceBuilderImpl().apply(block)
    return Mirai.constructMessageSource(
        this.id,
        builder.kind ?: error("You must call `Contact.sendTo(Contact)` when `buildMessageSource`"),
        builder.fromUin,
        builder.targetUin,
        builder.ids,
        builder.time,
        builder.internalIds,
        builder.originalMessages.build()
    )
}

/**
 * @see buildMessageSource
 */
public abstract class MessageSourceBuilder {
    internal abstract var kind: MessageSourceKind?
    internal abstract var fromUin: Long
    internal abstract var targetUin: Long

    internal abstract var ids: IntArray
    internal abstract var time: Int
    internal abstract var internalIds: IntArray

    @PublishedApi
    internal val originalMessages: MessageChainBuilder = MessageChainBuilder()

    public fun time(from: MessageSource): MessageSourceBuilder = apply { this.time = from.time }
    public val now: Int get() = currentTimeSeconds().toInt()
    public fun time(value: Int): MessageSourceBuilder = apply { this.time = value }

    public fun internalId(from: MessageSource): MessageSourceBuilder = apply { this.internalIds = from.internalIds }
    public fun internalId(vararg value: Int): MessageSourceBuilder = apply { this.internalIds = value }

    public fun id(from: MessageSource): MessageSourceBuilder = apply { this.ids = from.ids }
    public fun id(vararg value: Int): MessageSourceBuilder = apply { this.ids = value }


    /**
     * 从另一个 [MessageSource] 复制 [ids], [time], [internalIds].
     * 这三个数据决定官方客户端能 "定位" 到的原消息
     */
    public fun metadata(from: MessageSource): MessageSourceBuilder = apply {
        id(from)
        internalId(from)
        time(from)
    }

    /**
     * 从另一个 [MessageSource] 复制所有信息, 包括消息内容. 不会清空已有消息.
     */
    public fun allFrom(source: MessageSource): MessageSourceBuilder {
        this.kind = determineKind(source)
        this.ids = source.ids
        this.time = source.time
        this.fromUin = source.fromId
        this.targetUin = source.targetId
        this.internalIds = source.internalIds
        this.originalMessages.addAll(source.originalMessage)
        return this
    }


    /**
     * 从另一个 [MessageSource] 复制 [消息内容][MessageSource.originalMessage]. 不会清空已有消息.
     */
    public fun messagesFrom(source: MessageSource): MessageSourceBuilder = apply {
        this.originalMessages.addAll(source.originalMessage)
    }

    public fun messages(messages: Iterable<Message>): MessageSourceBuilder = apply {
        this.originalMessages.addAll(messages)
    }

    public fun messages(vararg message: Message): MessageSourceBuilder = apply {
        for (it in message) {
            this.originalMessages.add(it)
        }
    }

    @JvmSynthetic
    public inline fun messages(block: MessageChainBuilder.() -> Unit): MessageSourceBuilder = apply {
        this.originalMessages.apply(block)
    }

    public fun clearMessages(): MessageSourceBuilder = apply { this.originalMessages.clear() }

    /**
     * 设置 [发送人][this] 和 [发送目标][target], 并自动判断 [kind]
     */
    @JvmSynthetic
    public abstract infix fun ContactOrBot.sendTo(target: ContactOrBot): MessageSourceBuilder

    public fun setSenderAndTarget(sender: ContactOrBot, target: ContactOrBot): MessageSourceBuilder =
        sender sendTo target
}


//////////////////
//// INTERNAL ////
//////////////////


internal class MessageSourceBuilderImpl : MessageSourceBuilder() {
    override var kind: MessageSourceKind? = null
    override var fromUin: Long = 0
    override var targetUin: Long = 0

    override var ids: IntArray = intArrayOf()
    override var time: Int = currentTimeSeconds().toInt()
    override var internalIds: IntArray = intArrayOf()

    @JvmSynthetic
    override fun ContactOrBot.sendTo(target: ContactOrBot): MessageSourceBuilder {
        fromUin = if (this is Group) {
            Mirai.calculateGroupUinByGroupCode(this.id)
        } else this.id

        targetUin = if (target is Group) {
            Mirai.calculateGroupUinByGroupCode(target.id)
        } else target.id

        check(this != target) { "sender and target mustn't be the same" }

        kind = when {
            this is Group || target is Group -> MessageSourceKind.GROUP
            this is Member || target is Member -> MessageSourceKind.TEMP
            this is Bot && target is Friend -> MessageSourceKind.FRIEND
            this is Friend && target is Bot -> MessageSourceKind.FRIEND
            this is Stranger || target is Stranger -> MessageSourceKind.STRANGER
            else -> throw IllegalArgumentException("Cannot determine source kind for sender $this and target $target")
        }
        return this@MessageSourceBuilderImpl
    }
}


@JvmSynthetic
internal fun MessageSource.toMutableOffline(): MutableOfflineMessageSourceByOnline =
    MutableOfflineMessageSourceByOnline(this)

internal class MutableOfflineMessageSourceByOnline(
    origin: MessageSource
) : OfflineMessageSource(), MessageSourceAmender {
    override var kind: MessageSourceKind = determineKind(origin)
    override var fromUin: Long
        get() = fromId
        set(value) {
            fromId = value
        }
    override var targetUin: Long
        get() = targetId
        set(value) {
            targetId = value
        }
    override val botId: Long = origin.botId
    override var ids: IntArray = origin.ids
    override var internalIds: IntArray = origin.internalIds
    override var time: Int = origin.time
    override var fromId: Long = origin.fromId
    override var targetId: Long = origin.targetId
    override var originalMessage: MessageChain = origin.originalMessage
}

private fun determineKind(source: MessageSource): MessageSourceKind {
    return when {
        source.isAboutGroup() -> MessageSourceKind.GROUP
        source.isAboutFriend() -> MessageSourceKind.FRIEND
        source.isAboutTemp() -> MessageSourceKind.TEMP
        else -> error("stub")
    }
}

internal class OfflineMessageSourceByOnline(
    private val onlineMessageSource: OnlineMessageSource
) : OfflineMessageSource() {
    override val kind: MessageSourceKind
        get() = onlineMessageSource.kind
    override val botId: Long get() = onlineMessageSource.botId
    override val ids: IntArray get() = onlineMessageSource.ids
    override val internalIds: IntArray get() = onlineMessageSource.internalIds
    override val time: Int get() = onlineMessageSource.time
    override val fromId: Long get() = onlineMessageSource.fromId
    override val targetId: Long get() = onlineMessageSource.targetId
    override val originalMessage: MessageChain get() = onlineMessageSource.originalMessage
}
