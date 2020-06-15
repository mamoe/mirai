/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessage.DisplayStrategy
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Transient


/**
 * 合并转发消息
 *
 *
 *
 * ## 显示方案
 *
 * ### 移动端
 * 在移动客户端将会显示为卡片
 *
 * `<title>`: [DisplayStrategy.generateTitle]
 *
 * `<preview>`: [DisplayStrategy.generatePreview]
 *
 * `<summary>`: [DisplayStrategy.generateSummary]
 *
 * ```
 * |-------------------------|
 * | <title>                 |
 * | <preview>               |
 * |-------------------------|
 * | <summary>               |
 * |-------------------------|
 * ```
 *
 * 默认显示方案:
 * ```
 * |-------------------------|
 * | 群聊的聊天记录             |
 * | <消息 1>                 |
 * | <消息 2>                 |
 * | <消息 3>                 |
 * |-------------------------|
 * | 查看 3 条转发消息          |
 * |-------------------------|
 * ```
 *
 * ### PC 端
 * 在部分 PC 端显示为类似移动端的卡片, 在其他 PC 端显示为以下格式
 * ```
 * 鸽子 A 2020/04/23 11:27:54
 * 咕
 * 鸽子 B 2020/04/23 11:27:55
 * 咕
 * 鸽子 C 1970/01/01 08:00:00
 * 咕咕咕
 * ```
 *
 *
 * ## 构造
 * - 使用 [DSL][buildForwardMessage]
 * - 通过 [MessageEvent] 集合转换: [toForwardMessage]
 *
 *
 * @param [displayStrategy] 卡片显示方案
 *
 * @see buildForwardMessage
 */
class ForwardMessage @JvmOverloads constructor(
    /**
     * 消息列表
     */
    val nodeList: Collection<INode>,
    @Transient val displayStrategy: DisplayStrategy = DisplayStrategy.Default
) : MessageContent {
    init {
        require(nodeList.isNotEmpty()) {
            "Forward nodeList mustn't be empty"
        }
    }

    /**
     * @see ForwardMessage
     */
    abstract class DisplayStrategy {
        /**
         * 修改后卡片标题会变为 "转发的聊天记录", 而此函数的返回值会显示在 preview 前
         */
        open fun generateTitle(forward: ForwardMessage): String = "群聊的聊天记录"

        /**
         * 显示在消息列表中的预览.
         */
        open fun generateBrief(forward: ForwardMessage): String = "[聊天记录]"

        /**
         * 目前未发现在哪能显示
         */
        open fun generateSource(forward: ForwardMessage): String = "聊天记录"

        /**
         * 显示在卡片 body 中, 只会显示 sequence 前四个元素.
         * Java 用户: 使用 [sequenceOf] (`SequenceKt.sequenceOf`) 或 [asSequence] (`SequenceKt.asSequence`)
         */
        open fun generatePreview(forward: ForwardMessage): Sequence<String> =
            forward.nodeList.asSequence().map { it.senderName + ": " + it.message.contentToString() }

        /**
         * 显示在卡片底部
         */
        open fun generateSummary(forward: ForwardMessage): String = "查看 ${forward.nodeList.size} 条转发消息"

        companion object Default : DisplayStrategy() {
            @JvmSynthetic
            inline operator fun invoke(
                crossinline generateTitle: (forward: ForwardMessage) -> String = Default::generateTitle,
                crossinline generateBrief: (forward: ForwardMessage) -> String = Default::generateBrief,
                crossinline generateSource: (forward: ForwardMessage) -> String = Default::generateSource,
                crossinline generatePreview: (forward: ForwardMessage) -> Sequence<String> = Default::generatePreview,
                crossinline generateSummary: (forward: ForwardMessage) -> String = Default::generateSummary
            ): DisplayStrategy = object : DisplayStrategy() {
                override fun generateTitle(forward: ForwardMessage): String = generateTitle(forward)
                override fun generateBrief(forward: ForwardMessage): String = generateBrief(forward)
                override fun generateSource(forward: ForwardMessage): String = generateSource(forward)
                override fun generatePreview(forward: ForwardMessage): Sequence<String> = generatePreview(forward)
                override fun generateSummary(forward: ForwardMessage): String = generateSummary(forward)
            }
        }
    }


    data class Node(
        override val senderId: Long,
        override val time: Int,
        override val senderName: String,
        override val message: Message
    ) : INode

    interface INode {
        /**
         * 发送人 [User.id]
         */
        val senderId: Long

        /**
         * 时间戳秒
         */
        val time: Int

        /**
         * 发送人名称
         */
        val senderName: String

        /**
         * 消息内容
         */
        val message: Message
    }

    companion object Key : Message.Key<ForwardMessage> {
        override val typeName: String get() = "ForwardMessage"
    }

    override fun toString(): String = "[mirai:forward:$nodeList]"
    private val contentToString: String by lazy { nodeList.joinToString("\n") }

    @MiraiExperimentalAPI
    override fun contentToString(): String = contentToString
}


/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
fun Iterable<MessageEvent>.toForwardMessage(displayStrategy: DisplayStrategy = DisplayStrategy): ForwardMessage {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return ForwardMessage(emptyList(), displayStrategy)
    return ForwardMessage(
        this.map { ForwardMessage.Node(it.sender.id, it.time, it.senderName, it.message) }, displayStrategy
    )
}

/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
fun Message.toForwardMessage(
    sender: User,
    time: Int = currentTimeSeconds.toInt(),
    displayStrategy: DisplayStrategy = DisplayStrategy
): ForwardMessage = this.toForwardMessage(sender.id, sender.nameCardOrNick, time, displayStrategy)

/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
fun Message.toForwardMessage(
    senderId: Long,
    senderName: String,
    time: Int = currentTimeSeconds.toInt(),
    displayStrategy: DisplayStrategy = DisplayStrategy
): ForwardMessage = ForwardMessage(listOf(ForwardMessage.Node(senderId, time, senderName, this)), displayStrategy)

/**
 * 构造一条 [ForwardMessage]
 *
 * @see ForwardMessageBuilder 查看 DSL 帮助
 * @see ForwardMessage 查看转发消息说明
 */
@JvmSynthetic
inline fun buildForwardMessage(
    context: Contact,
    displayStrategy: DisplayStrategy = DisplayStrategy,
    block: ForwardMessageBuilder.() -> Unit
): ForwardMessage = ForwardMessageBuilder(context).apply { this.displayStrategy = displayStrategy }.apply(block).build()

/**
 * 使用 DSL 构建一个 [ForwardMessage].
 *
 * @see ForwardMessageBuilder 查看 DSL 帮助
 * @see ForwardMessage 查看转发消息说明
 */
@JvmSynthetic
inline fun MessageEvent.buildForwardMessage(
    context: Contact = this.subject,
    displayStrategy: DisplayStrategy = DisplayStrategy,
    block: ForwardMessageBuilder.() -> Unit
): ForwardMessage = ForwardMessageBuilder(context).apply {
    this.displayStrategy = displayStrategy
}.apply(block).build()

/**
 * 标记转发消息 DSL
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class ForwardMessageDsl

/**
 * 转发消息 DSL 构建器.
 *
 * # 总览
 *
 * 使用 DSL 构造一个转发:
 * ```
 * buildForwardMessage {
 *     123456789 named "鸽子 A" says "咕" // 意为 名为 "鸽子 A" 的用户 123456789 发送了一条内容为 "咕" 的消息
 *     100200300 named "鸽子 C" at 1582315452 says "咕咕咕" // at 设置时间 (在 PC 端显示, 在手机端不影响顺序)
 *     987654321 named "鸽子 B" says "咕" // 未指定时间, 则自动顺序安排时间
 *     myFriend says "咕" // User.says
 *     bot says { // 构造消息链, 同 `buildMessageChain`
 *         +"发个图片试试"
 *         +Image("{90CCED1C-2D64-313B-5D66-46625CAB31D7}.jpg")
 *     }
 *     val member: Member = ...
 *     member says "我是幸运群员" // 使用 `User says` 则会同时设置发送人名称
 * }
 * ```
 *
 * # 语法
 *
 * 下文中 `S` 代表消息发送人. 可接受: 发送人账号 id([Long] 或 [Int]) 或 [User]
 *
 * 下文中 `M` 代表消息内容. 可接受: [String], [Message], 或 [构造消息链][MessageChainBuilder] 的 DSL 代码块
 *
 * ## 陈述一条消息
 * 使用 [`infix fun S.says(M)`][ForwardMessageBuilder.says]
 *
 * 语句 `123456789 named "鸽子 A" says "咕"` 创建并添加了一条名为 "鸽子 A" 的用户 123456789 发送的内容为 "咕" 的消息
 *
 *
 * ### 陈述
 * 一条 '陈述' 必须包含以下属性:
 * - 发送人. 只可以作为 infix 函数的接收者 (receiver) 设置, 如 `sender says M`, `sender named "xxx"`, `sender at 123`
 * - 消息内容. 只可以通过 `says` 函数的参数设置, 即 `says M`.
 *
 * ### 组合陈述
 * 现支持的可选属性为 `named`, `at`
 *
 *
 * 最基础的陈述为 `S says M`. 可在 `says` 前按任意顺序添加组合属性:
 *
 * `S named "xxx" says M`;
 *
 * `S at 123456 says M`; 其中 `123456` 为发信时间
 *
 *
 * 属性的顺序并不重要. 如下两句陈述效果相同.
 *
 * `S named "xxx" at 123456 says M`;
 *
 * `S at 123456 named "xxx" says M`;
 *
 * ### 重复属性
 * 若属性有重复, **新属性会替换旧属性**.
 *
 * `S named "name1" named "name2" says M` 最终的发送人名称为 `"name2"`
 */
class ForwardMessageBuilder private constructor(
    /**
     * 消息语境. 可为 [Group] 或 [User]
     */
    val context: Contact,
    private val container: MutableList<ForwardMessage.INode>
) : MutableList<ForwardMessage.INode> by container {
    /**
     * @see ForwardMessage.displayStrategy
     */
    var displayStrategy: DisplayStrategy = DisplayStrategy

    private var built: Boolean = false
    private fun checkBuilt() {
        check(!built) { "ForwardMessageBuilder is already built therefore can't be modified" }
    }

    constructor(context: Contact) : this(context, mutableListOf())
    constructor(context: Contact, initialSize: Int) : this(context, ArrayList<ForwardMessage.INode>(initialSize))

    /**
     * 当前时间.
     * 在使用 [says] 时若不指定时间, 则会使用 [currentTime] 自增 1 的时间.
     */
    var currentTime: Int = currentTimeSeconds.toInt()

    inner class BuilderNode : ForwardMessage.INode {

        /**
         * 发送人 [User.id]
         */
        override var senderId: Long = 0

        /**
         * 时间戳秒
         */
        override var time: Int = currentTime++

        /**
         * 发送人名称
         */
        override var senderName: String = ""

        /**
         * 消息内容
         */
        override lateinit var message: Message


        /**
         * 指定发送人 id 和名称.
         */
        @ForwardMessageDsl
        infix fun sender(user: User): BuilderNode = apply { this.senderId(user.id); this.named(user.nameCardOrNick) }

        /**
         * 指定发送人 id.
         */
        @ForwardMessageDsl
        infix fun senderId(id: Int): BuilderNode = apply { this.senderId = id.toLongUnsigned() }

        /**
         * 指定发送人 id.
         */
        @ForwardMessageDsl
        infix fun senderId(id: Long): BuilderNode = apply { this.senderId = id }

        /**
         * 指定发送人名称.
         */
        @ForwardMessageDsl
        infix fun named(name: String): BuilderNode = apply { this.senderName = name }

        /**
         * 指定发送人名称.
         */
        @ForwardMessageDsl
        infix fun senderName(name: String): BuilderNode = apply { this.senderName = name }

        /**
         * 指定时间.
         * @time 时间戳, 单位为秒
         */
        @ForwardMessageDsl
        infix fun at(time: Int): BuilderNode = this.apply { this.time = time }

        /**
         * 指定时间.
         * @time 时间戳, 单位为秒
         */
        @ForwardMessageDsl
        infix fun time(time: Int): BuilderNode = this.apply { this.time = time }

        /**
         * 指定消息内容
         */
        @ForwardMessageDsl
        infix fun message(message: Message): BuilderNode = this.apply { this.message = message }

        /**
         * 指定消息内容
         */
        @ForwardMessageDsl
        infix fun message(message: String): BuilderNode = this.apply { this.message = message.toMessage() }

        /** 添加一条消息  */
        @ForwardMessageDsl
        infix fun says(message: Message): ForwardMessageBuilder = this@ForwardMessageBuilder.apply {
            checkBuilt()
            this@BuilderNode.message = message
            add(this@BuilderNode)
        }

        /** 添加一条消息  */
        @ForwardMessageDsl
        infix fun says(message: String): ForwardMessageBuilder = this.says(message.toMessage())

        /** 构造并添加一个 [MessageChain] */
        @ForwardMessageDsl
        inline infix fun says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
            says(MessageChainBuilder().apply(chain).asMessageChain())
    }

    // region general `says`

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    infix fun Long.says(message: String): ForwardMessageBuilder = says(message.toMessage())

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    infix fun Int.says(message: String): ForwardMessageBuilder =
        this.toLong().and(0xFFFF_FFFF).says(message.toMessage())

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    infix fun Long.says(message: Message): ForwardMessageBuilder =
        this@ForwardMessageBuilder.apply {
            checkBuilt()
            add(BuilderNode().apply {
                senderId = this@says
                this.message = message
            })
        }

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    infix fun Int.says(message: Message): ForwardMessageBuilder = this.toLong().and(0xFFFF_FFFF).says(message)

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    inline infix fun Long.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        says(MessageChainBuilder().apply(chain).asMessageChain())

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    inline infix fun Int.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this.toLong().and(0xFFFF_FFFF).says(chain)


    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    infix fun Bot.says(message: String): ForwardMessageBuilder = this.id named this.smartName() says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    infix fun User.says(message: String): ForwardMessageBuilder = this.id named this.nameCardOrNick says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    infix fun User.says(message: Message): ForwardMessageBuilder = this.id named this.nameCardOrNick says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    infix fun Bot.says(message: Message): ForwardMessageBuilder = this.id named this.smartName() says message

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    inline infix fun User.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this says (MessageChainBuilder().apply(chain).asMessageChain())

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    inline infix fun Bot.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this says (MessageChainBuilder().apply(chain).asMessageChain())

    // endregion


    // region timed

    /**
     * 为一条消息指定时间.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    infix fun Int.at(time: Int): BuilderNode = this.toLongUnsigned() at time

    /**
     * 为一条消息指定时间.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    infix fun Long.at(time: Int): BuilderNode = BuilderNode().apply { senderId = this@at;this.time = time }

    /**
     * 为一条消息指定时间和发送人名称.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    infix fun User.at(time: Int): BuilderNode = this.id named this.nameCardOrNick at time

    // endregion


    // region named

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    infix fun Int.named(name: String): BuilderNode = this.toLongUnsigned().named(name)

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    infix fun Long.named(name: String): BuilderNode =
        BuilderNode().apply { senderId = this@named;this.senderName = name }

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    infix fun User.named(name: String): BuilderNode = this.id.named(name)

    // endregion

    /** 构造 [ForwardMessage] */
    fun build(): ForwardMessage = ForwardMessage(container.toList(), this.displayStrategy)
    internal fun Bot.smartName(): String = when (val c = this@ForwardMessageBuilder.context) {
        is Group -> c.botAsMember.nameCardOrNick
        else -> nick
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Int.toLongUnsigned(): Long = this.toLong().and(0xFFFF_FFFF)
