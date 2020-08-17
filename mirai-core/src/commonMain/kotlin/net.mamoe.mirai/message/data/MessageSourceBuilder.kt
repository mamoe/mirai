/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "INVISIBLE_MEMBER")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

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
@MiraiExperimentalAPI
@JvmName("copySource")
public fun MessageSource.copyAmend(
    block: MessageSourceAmender.() -> Unit
): OfflineMessageSource = toMutableOffline().apply(block)

/**
 * 仅于 [copyAmend] 中修改 [MessageSource]
 */
public interface MessageSourceAmender {
    public var kind: OfflineMessageSource.Kind
    public var fromUin: Long
    public var targetUin: Long
    public var id: Int
    public var time: Int
    public var internalId: Int

    public var originalMessage: MessageChain

    /** 从另一个 [MessageSource] 中复制 [id], [internalId], [time]*/
    public fun metadataFrom(another: MessageSource) {
        this.id = another.id
        this.internalId = another.internalId
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
 * - 消息元数据 (即 [MessageSource.id], [MessageSource.internalId], [MessageSource.time])
 *   元数据用于 [撤回][MessageSource.recall], [引用回复][MessageSource.quote], 和官方客户端定位原消息.
 *   可通过 [MessageSourceBuilder.id], [MessageSourceBuilder.time], [MessageSourceBuilder.internalId] 设置
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
 *     metadata(source) // 从另一个消息源复制 id, internalId, time
 *
 *     messages { // 指定消息内容
 *         +"hi"
 *     }
 * }
 * ```
 */
@JvmSynthetic
@MiraiExperimentalAPI
public fun Bot.buildMessageSource(block: MessageSourceBuilder.() -> Unit): MessageSource {
    val builder = MessageSourceBuilderImpl().apply(block)
    return constructMessageSource(
        builder.kind ?: error("You must call `Contact.sendTo(Contact)` when `buildMessageSource`"),
        builder.fromUin,
        builder.targetUin,
        builder.id,
        builder.time,
        builder.internalId,
        builder.originalMessages.build()
    )
}

/**
 * @see buildMessageSource
 */
public abstract class MessageSourceBuilder {
    internal abstract var kind: OfflineMessageSource.Kind?
    internal abstract var fromUin: Long
    internal abstract var targetUin: Long

    internal abstract var id: Int
    internal abstract var time: Int
    internal abstract var internalId: Int

    @PublishedApi
    internal val originalMessages: MessageChainBuilder = MessageChainBuilder()

    public fun time(from: MessageSource): MessageSourceBuilder = apply { this.time = from.time }
    public val now: Int get() = currentTimeSeconds.toInt()
    public fun time(value: Int): MessageSourceBuilder = apply { this.time = value }

    public fun internalId(from: MessageSource): MessageSourceBuilder = apply { this.internalId = from.internalId }
    public fun internalId(value: Int): MessageSourceBuilder = apply { this.internalId = value }

    public fun id(from: MessageSource): MessageSourceBuilder = apply { this.id = from.id }
    public fun id(value: Int): MessageSourceBuilder = apply { this.id = value }


    /**
     * 从另一个 [MessageSource] 复制 [id], [time], [internalId].
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
        this.id = source.id
        this.time = source.time
        this.fromUin = source.fromId
        this.targetUin = source.targetId
        this.internalId = source.internalId
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
    override var kind: OfflineMessageSource.Kind? = null
    override var fromUin: Long = 0
    override var targetUin: Long = 0

    override var id: Int = 0
    override var time: Int = currentTimeSeconds.toInt()
    override var internalId: Int = 0

    @JvmSynthetic
    override fun ContactOrBot.sendTo(target: ContactOrBot): MessageSourceBuilder {
        fromUin = if (this is Group) {
            Group.calculateGroupUinByGroupCode(this.id)
        } else this.id

        targetUin = if (target is Group) {
            Group.calculateGroupUinByGroupCode(target.id)
        } else target.id

        check(this != target) { "sender and target mustn't be the same" }

        kind = when {
            this is Group || target is Group -> OfflineMessageSource.Kind.GROUP
            this is Member || target is Member -> OfflineMessageSource.Kind.TEMP
            this is Bot && target is Friend -> OfflineMessageSource.Kind.FRIEND
            this is Friend && target is Bot -> OfflineMessageSource.Kind.FRIEND
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
    override var kind: Kind = determineKind(origin)
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
    override var bot: Bot = origin.bot
    override var id: Int = origin.id
    override var internalId: Int = origin.internalId
    override var time: Int = origin.time
    override var fromId: Long = origin.fromId
    override var targetId: Long = origin.targetId
    override var originalMessage: MessageChain = origin.originalMessage
}

private fun determineKind(source: MessageSource): OfflineMessageSource.Kind {
    return when {
        source.isAboutGroup() -> OfflineMessageSource.Kind.GROUP
        source.isAboutFriend() -> OfflineMessageSource.Kind.FRIEND
        source.isAboutTemp() -> OfflineMessageSource.Kind.TEMP
        else -> error("stub")
    }
}

internal class OfflineMessageSourceByOnline(
    private val onlineMessageSource: OnlineMessageSource
) : OfflineMessageSource() {
    override val kind: Kind
        get() = when {
            onlineMessageSource.isAboutGroup() -> Kind.GROUP
            onlineMessageSource.isAboutFriend() -> Kind.FRIEND
            onlineMessageSource.isAboutTemp() -> Kind.TEMP
            else -> error("stub")
        }
    override val bot: Bot get() = onlineMessageSource.bot
    override val id: Int get() = onlineMessageSource.id
    override val internalId: Int get() = onlineMessageSource.internalId
    override val time: Int get() = onlineMessageSource.time
    override val fromId: Long get() = onlineMessageSource.fromId
    override val targetId: Long get() = onlineMessageSource.targetId
    override val originalMessage: MessageChain get() = onlineMessageSource.originalMessage
}
