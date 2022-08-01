/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessage.DisplayStrategy
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * 未通过 [DisplayStrategy] 渲染的合并转发消息. [RawForwardMessage] 仅作为一个中间件, 用于 [ForwardMessageBuilder].
 *
 * [RawForwardMessage] 可以序列化保存, 也可以被多次[渲染][RawForwardMessage.render]产生不同格式的 [ForwardMessage].
 */
@Serializable
public data class RawForwardMessage(
    /**
     * 消息列表
     */
    val nodeList: List<ForwardMessage.Node>
) {
    /**
     * 渲染这个 [RawForwardMessage] 并产生可以发送的 [ForwardMessage]
     */
    public fun render(displayStrategy: DisplayStrategy): ForwardMessage = ForwardMessage(
        preview = displayStrategy.generatePreview(this),
        title = displayStrategy.generateTitle(this),
        brief = displayStrategy.generateBrief(this),
        source = displayStrategy.generateSource(this),
        summary = displayStrategy.generateSummary(this),
        nodeList = nodeList,
    )
}

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
 * - `<title>`: [DisplayStrategy.generateTitle]
 * - `<preview>`: [DisplayStrategy.generatePreview]
 * - `<summary>`: [DisplayStrategy.generateSummary]
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
 * | 查看3条转发消息          |
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
 * - 使用构建器 [ForwardMessageBuilder]
 * - 使用 [DSL][buildForwardMessage]
 * - 通过 [MessageEvent] 集合转换: [toForwardMessage]
 *
 * @see buildForwardMessage
 */
@SerialName(ForwardMessage.SERIAL_NAME)
@Serializable
public data class ForwardMessage(
    val preview: List<String>,
    val title: String,
    val brief: String,
    val source: String,
    val summary: String,
    val nodeList: List<Node>,
) : MessageContent, ConstrainSingle {
    override val key: MessageKey<ForwardMessage> get() = Key

    override fun contentToString(): String = "[转发消息]"

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitForwardMessage(this, data)
    }

    // use data-class generated toString()


    /**
     * 合并转发卡片展示策略. 用于 [RawForwardMessage] 的 [渲染][RawForwardMessage.render].
     *
     * @see ForwardMessage
     */
    public interface DisplayStrategy {
        /**
         * 修改后卡片标题会变为 "转发的聊天记录", 而此函数的返回值会显示在 preview 前
         */
        public fun generateTitle(forward: RawForwardMessage): String = "群聊的聊天记录"

        /**
         * 显示在消息列表中的预览.
         */
        public fun generateBrief(forward: RawForwardMessage): String = "[聊天记录]"

        /**
         * 目前未发现在哪能显示
         */
        public fun generateSource(forward: RawForwardMessage): String = "聊天记录"

        /**
         * 显示在卡片 body 中, 只会显示 sequence 前四个元素.
         */
        public fun generatePreview(forward: RawForwardMessage): List<String> =
            forward.nodeList.map { it.senderName + ": " + it.messageChain.contentToString() }

        /**
         * 显示在卡片底部
         */
        public fun generateSummary(forward: RawForwardMessage): String = "查看${forward.nodeList.size}条转发消息"

        /**
         * 默认的, 与官方客户端相似的展示方案.
         */
        public companion object Default : DisplayStrategy
    }


    /**
     * 消息节点
     */
    @Serializable
    public data class Node(
        /**
         * 发送人 [User.id]
         */
        override val senderId: Long,
        /**
         * 时间戳 秒
         */
        override val time: Int,
        /**
         * 发送人昵称
         */
        override val senderName: String,
        /**
         * 消息内容
         */
        override val messageChain: MessageChain
    ) : INode {
        public constructor(
            senderId: Long,
            time: Int,
            senderName: String,
            message: Message
        ) : this(senderId, time, senderName, message.toMessageChain())
    }

    /**
     * 请构造 [Node]
     */
    @MiraiExperimentalApi
    public interface INode {
        /**
         * 发送人 [User.id]
         */
        public val senderId: Long

        /**
         * 时间戳秒
         */
        public val time: Int

        /**
         * 发送人名称
         */
        public val senderName: String

        /**
         * 消息内容
         */
        public val messageChain: MessageChain
    }

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, ForwardMessage>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "ForwardMessage"
    }
}


/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
public fun Iterable<MessageEvent>.toForwardMessage(displayStrategy: DisplayStrategy = DisplayStrategy): ForwardMessage {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return RawForwardMessage(emptyList()).render(displayStrategy)
    return RawForwardMessage(
        this.map { ForwardMessage.Node(it.sender.id, it.time, it.senderName, it.message) }
    ).render(displayStrategy)
}

/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
public fun Message.toForwardMessage(
    sender: User,
    time: Int = currentTimeSeconds().toInt(),
    displayStrategy: DisplayStrategy = DisplayStrategy
): ForwardMessage = this.toForwardMessage(sender.id, sender.nameCardOrNick, time, displayStrategy)

/**
 * 转换为 [ForwardMessage]
 */
@JvmOverloads
public fun Message.toForwardMessage(
    senderId: Long,
    senderName: String,
    time: Int = currentTimeSeconds().toInt(),
    displayStrategy: DisplayStrategy = DisplayStrategy
): ForwardMessage =
    RawForwardMessage(listOf(ForwardMessage.Node(senderId, time, senderName, this))).render(displayStrategy)

/**
 * 构造一条 [ForwardMessage]
 *
 * @see ForwardMessageBuilder 查看 DSL 帮助
 * @see ForwardMessage 查看转发消息说明
 */
@JvmSynthetic
public inline fun buildForwardMessage(
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
public inline fun MessageEvent.buildForwardMessage(
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
public annotation class ForwardMessageDsl

/**
 * 转发消息 DSL 构建器.
 *
 * # 总览
 *
 *
 * 在 Java 使用一般方法添加消息构建转发:
 * ```
 * ForwardMessageBuilder builder = new ForwardMessageBuilder(group);
 *
 * builder.add(123456L, "群员A", new PlainText("消息"))
 * builder.add(user, new PlainText("msg"));
 * builder.add(user, (chain) -> {
 *     chain.add(new At(user));
 *     chain.add(new PlainText("Hello"))
 * }); // 使用 Java 8 lambda 语法, MessageChainBuilder 快速构建消息链
 * builder.add(event); // 可添加 MessageEvent
 *
 * builder.setDisplayStrategy(new ForwardMessage.DisplayStrategy() {
 * }); // 可选, 修改显示策略
 *
 * ForwardMessage forward = builder.build();
 * group.sendMessage(forward);
 * ```
 *
 * 在 Kotlin 使用一般方法添加消息构建转发:
 * ```
 * val forward: ForwardMessage = buildForwardMessage {
 *     add(123456L, "群员A", PlainText("消息"))
 *     add(user, PlainText("msg"))
 *     add(user) {
 *         // this: MessageChainBuilder
 *         add(At(user))
 *         add(PlainText("msg"))
 *     };
 *     add(event) // 可添加 MessageEvent
 * }
 *
 * group.sendMessage(forward);
 * friend.sendMessage(forward);
 * ```
 *
 * 在 Kotlin 也可以使用 DSL 构建一个转发:
 * ```
 * buildForwardMessage {
 *     123456789 named "鸽子 A" says "咕" // 意为 名为 "鸽子 A" 的用户 123456789 发送了一条内容为 "咕" 的消息
 *     100200300 named "鸽子 C" at 1582315452 says "咕咕咕" // at 设置时间 (在 PC 端显示, 在手机端不影响顺序)
 *     987654321 named "鸽子 B" says "咕" // 未指定时间, 则自动顺序安排时间
 *     myFriend says "咕" // User.says
 *     bot says { // 构造消息链, 同 `buildMessageChain`
 *         add("发个图片试试")
 *         add(Image("{90CCED1C-2D64-313B-5D66-46625CAB31D7}.jpg"))
 *     }
 *     val member: Member = ...
 *     member says "我是幸运群员" // 使用 `User says` 则会同时设置发送人名称
 * }
 * ```
 * DSL 语法在下文详细解释.
 *
 * 上述示例效果一样, 根据个人偏好选择.
 *
 * # Kotlin DSL 语法
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
public class ForwardMessageBuilder private constructor(
    /**
     * 消息语境. 可为 [Group] 或 [User]. 用来确定某 ID 的用户的昵称.
     */
    public val context: Contact,
    private val container: MutableList<ForwardMessage.INode>
) : MutableList<ForwardMessage.INode> by container {
    /**
     * @see RawForwardMessage.render
     */
    public var displayStrategy: DisplayStrategy = DisplayStrategy

    private var built: Boolean = false
    private fun checkBuilt() {
        check(!built) { "ForwardMessageBuilder is already built therefore can't be modified" }
    }

    public constructor(context: Contact) : this(context, mutableListOf())
    public constructor(context: Contact, initialSize: Int) : this(context, ArrayList<ForwardMessage.INode>(initialSize))

    /**
     * 当前时间.
     * 在使用 [says] 时若不指定时间, 则会使用 [currentTime] 自增 1 的时间.
     */
    public var currentTime: Int = currentTimeSeconds().toInt()

    public inner class BuilderNode internal constructor() : ForwardMessage.INode {

        /**
         * 发送人 [User.id]
         */
        public override var senderId: Long = 0

        /**
         * 时间戳秒
         */
        public override var time: Int = currentTime++

        /**
         * 发送人名称
         */
        public override var senderName: String = ""

        /**
         * 消息内容
         */
        public override lateinit var messageChain: MessageChain


        /**
         * 指定发送人 id 和名称.
         */
        @ForwardMessageDsl
        public infix fun sender(user: User): BuilderNode =
            apply { this.senderId(user.id); this.named(user.nameCardOrNick) }

        /**
         * 指定发送人 id 和名称.
         * @since 2.6
         */
        @ForwardMessageDsl
        public infix fun sender(user: UserOrBot): BuilderNode =
            apply { this.senderId(user.id); this.named(user.nameCardOrNick) }

        /**
         * 指定发送人 id.
         */
        @ForwardMessageDsl
        public infix fun senderId(id: Int): BuilderNode = apply { this.senderId = id.toLongUnsigned() }

        /**
         * 指定发送人 id.
         */
        @ForwardMessageDsl
        public infix fun senderId(id: Long): BuilderNode = apply { this.senderId = id }

        /**
         * 指定发送人名称.
         */
        @ForwardMessageDsl
        public infix fun named(name: String): BuilderNode = apply { this.senderName = name }

        /**
         * 指定发送人名称.
         */
        @ForwardMessageDsl
        public infix fun senderName(name: String): BuilderNode = apply { this.senderName = name }

        /**
         * 指定时间.
         * @time 时间戳, 单位为秒
         */
        @ForwardMessageDsl
        public infix fun at(time: Int): BuilderNode = this.apply { this.time = time }

        /**
         * 指定时间.
         * @time 时间戳, 单位为秒
         */
        @ForwardMessageDsl
        public infix fun time(time: Int): BuilderNode = this.apply { this.time = time }

        /**
         * 指定消息内容
         */
        @ForwardMessageDsl
        public infix fun message(message: Message): BuilderNode =
            this.apply { this.messageChain = message.toMessageChain() }

        /**
         * 指定消息内容
         */
        @ForwardMessageDsl
        public infix fun message(message: String): BuilderNode =
            this.apply { this.messageChain = PlainText(message).toMessageChain() }

        /** 添加一条消息  */
        @ForwardMessageDsl
        public infix fun says(message: Message): ForwardMessageBuilder = this@ForwardMessageBuilder.apply {
            checkBuilt()
            this@BuilderNode.messageChain = message.toMessageChain()
            add(this@BuilderNode)
        }

        /** 添加一条消息  */
        @ForwardMessageDsl
        public infix fun says(message: String): ForwardMessageBuilder = this.says(PlainText(message))

        /** 构造并添加一个 [MessageChain] */
        @ForwardMessageDsl
        public inline infix fun says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
            says(MessageChainBuilder().apply(chain).asMessageChain())

        override fun toString(): String {
            return "BuilderNode(senderId=$senderId, time=$time, senderName='$senderName', messageChain=$messageChain)"
        }
    }

    // region general `says`

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    public infix fun Long.says(message: String): ForwardMessageBuilder = says(PlainText(message))

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    public infix fun Int.says(message: String): ForwardMessageBuilder =
        this.toLong().and(0xFFFF_FFFF).says(PlainText(message))

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    public infix fun Long.says(message: Message): ForwardMessageBuilder =
        this@ForwardMessageBuilder.apply {
            checkBuilt()
            add(BuilderNode().apply {
                senderId = this@says
                this.messageChain = message.toMessageChain()
            })
        }

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    public infix fun Int.says(message: Message): ForwardMessageBuilder = this.toLong().and(0xFFFF_FFFF).says(message)

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    public inline infix fun Long.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        says(MessageChainBuilder().apply(chain).asMessageChain())

    /** 添加一条消息, 自动按顺序调整时间  */
    @ForwardMessageDsl
    public inline infix fun Int.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this.toLong().and(0xFFFF_FFFF).says(chain)


    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    public infix fun Bot.says(message: String): ForwardMessageBuilder = this.id named this.smartName() says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    public infix fun User.says(message: String): ForwardMessageBuilder = this.id named this.nameCardOrNick says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    public infix fun User.says(message: Message): ForwardMessageBuilder = this.id named this.nameCardOrNick says message

    /**
     * 添加一条消息, 自动按顺序调整时间
     * @since 2.6
     */
    @ForwardMessageDsl
    public infix fun UserOrBot.says(message: Message): ForwardMessageBuilder =
        this.id named this.nameCardOrNick says message

    /** 添加一条消息, 自动按顺序调整时间 */
    @ForwardMessageDsl
    public infix fun Bot.says(message: Message): ForwardMessageBuilder = this.id named this.smartName() says message

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    public inline infix fun User.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this says (MessageChainBuilder().apply(chain).asMessageChain())

    /**
     * 构造并添加一个 [MessageChain], 自动按顺序调整时间
     * @since 2.6
     */
    @ForwardMessageDsl
    public inline infix fun UserOrBot.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this says (MessageChainBuilder().apply(chain).asMessageChain())

    /** 构造并添加一个 [MessageChain], 自动按顺序调整时间 */
    @ForwardMessageDsl
    public inline infix fun Bot.says(chain: @ForwardMessageDsl MessageChainBuilder.() -> Unit): ForwardMessageBuilder =
        this says (MessageChainBuilder().apply(chain).asMessageChain())

    // endregion


    // region timed

    /**
     * 为一条消息指定时间.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    public infix fun Int.at(time: Int): BuilderNode = this.toLongUnsigned() at time

    /**
     * 为一条消息指定时间.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    public infix fun Long.at(time: Int): BuilderNode = BuilderNode().apply { senderId = this@at;this.time = time }

    /**
     * 为一条消息指定时间和发送人名称.
     * @time 时间戳, 单位为秒
     */
    @ForwardMessageDsl
    public infix fun User.at(time: Int): BuilderNode = this.id named this.nameCardOrNick at time

    /**
     * 为一条消息指定时间和发送人名称.
     * @time 时间戳, 单位为秒
     * @since 2.6
     */
    @ForwardMessageDsl
    public infix fun UserOrBot.at(time: Int): BuilderNode = this.id named this.nameCardOrNick at time

    // endregion


    // region named

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    public infix fun Int.named(name: String): BuilderNode = this.toLongUnsigned().named(name)

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    public infix fun Long.named(name: String): BuilderNode =
        BuilderNode().apply { senderId = this@named;this.senderName = name }

    /** 为一条消息指定发送人名称. */
    @ForwardMessageDsl
    public infix fun User.named(name: String): BuilderNode = this.id.named(name)

    /**
     * 为一条消息指定发送人名称.
     * @since 2.6
     */
    @ForwardMessageDsl
    public infix fun UserOrBot.named(name: String): BuilderNode = this.id.named(name)

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution // due to inference problem, `add(MessageEvent)` results in resolution ambiguity
    override fun add(element: ForwardMessage.INode): Boolean = container.add(element)

    /**
     * 从消息事件添加一个消息
     *
     * `event.sender named event.senderName at event.time says event.message`
     *
     * @since 2.1
     */
    public fun add(event: MessageEvent): ForwardMessageBuilder {
        return event.sender named event.senderName at event.time says event.message
    }

    // endregion

    // region common

    /**
     * 添加一个消息. 若不指定 [time], 将会使用 [currentTime] 自增 1 的时间.
     * @since 2.6
     */
    @JvmOverloads
    public fun add(sender: User, message: Message, time: Int = -1): ForwardMessageBuilder {
        return if (time == -1) sender says message
        else sender at time says message
    }

    /**
     * 添加一个消息. 若不指定 [time], 将会使用 [currentTime] 自增 1 的时间.
     * @since 2.6
     */
    @JvmOverloads
    public fun add(sender: UserOrBot, message: Message, time: Int = -1): ForwardMessageBuilder {
        return if (time == -1) sender says message
        else sender at time says message
    }

    /**
     * 添加一个消息. 若不指定 [time], 将会使用 [currentTime] 自增 1 的时间.
     * @since 2.6
     */
    @JvmOverloads
    public fun add(senderId: Long, senderName: String, message: Message, time: Int = -1): ForwardMessageBuilder {
        return if (time == -1) senderId named senderName says message
        else senderId named senderName at time says message
    }

    /**
     * 构建并添加一个消息. 若不指定 [time], 将会使用 [currentTime] 自增 1 的时间.
     * @since 2.6
     * @see buildMessageChain
     */
    @JvmOverloads
    public inline fun add(
        senderId: Long,
        senderName: String,
        time: Int = -1,
        builderAction: MessageChainBuilder.() -> Unit
    ): ForwardMessageBuilder {
        return add(senderId, senderName, time = time, message = buildMessageChain(builderAction))
    }

    /**
     * 构建并添加一个消息. 若不指定 [time], 将会使用 [currentTime] 自增 1 的时间.
     * @since 2.6
     * @see buildMessageChain
     */
    @JvmOverloads
    public inline fun add(
        sender: UserOrBot,
        time: Int = -1,
        builderAction: MessageChainBuilder.() -> Unit
    ): ForwardMessageBuilder {
        return add(sender, time = time, message = buildMessageChain(builderAction))
    }

    // endregion

    /**
     * 构造 [RawForwardMessage]. [RawForwardMessage] 可以被多个 [DisplayStrategy] [渲染][RawForwardMessage.render].
     * @since 2.6
     */
    public fun toRawForwardMessage(): RawForwardMessage = RawForwardMessage(container.map {
        ForwardMessage.Node(it.senderId, it.time, it.senderName, it.messageChain)
    })

    /**
     * 使用 [displayStrategy] 渲染并构造可以发送的 [ForwardMessage].
     */
    public fun build(): ForwardMessage = toRawForwardMessage().render(this.displayStrategy)

    internal fun Bot.smartName(): String = when (val c = this@ForwardMessageBuilder.context) {
        is Group -> c.botAsMember.nameCardOrNick
        else -> nick
    }
}

private fun ForwardMessage.INode.toNode(): ForwardMessage.Node {
    return ForwardMessage.Node(senderId, time, senderName, messageChain)
}
