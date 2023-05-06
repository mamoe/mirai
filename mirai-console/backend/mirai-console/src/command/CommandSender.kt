/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "FunctionName",
    "unused", "MemberVisibilityCanBePrivate"
)

package net.mamoe.mirai.console.command

import kotlinx.coroutines.CoroutineScope
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.asMemberCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.asTempCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.internal.data.castOrNull
import net.mamoe.mirai.console.internal.data.qualifiedNameOrTip
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.Permittee
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.childScopeContext
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

// region base interface define

/**
 * 指令发送者.
 *
 * 只有 [CommandSender] 才能 [执行指令][CommandManager.executeCommand]
 *
 * ## 获得指令发送者
 * - [MessageEvent.toCommandSender]
 * - [FriendMessageEvent.toCommandSender]
 * - [GroupMessageEvent.toCommandSender]
 * - [StrangerMessageEvent.toCommandSender]
 * - [OtherClientMessageEvent.toCommandSender]
 * - [MessageSyncEvent.toCommandSender]
 *
 * - [Member.asCommandSender]
 * - [NormalMember.asTempCommandSender]
 * - [Member.asMemberCommandSender]
 * - [Friend.asCommandSender]
 * - [User.asCommandSender]
 * - [Stranger.asCommandSender]
 * - [OtherClient.asCommandSender]
 *
 * ## 实现 [CommandSender]
 * 在任何时候都不要实现 [CommandSender] (包括使用委托). 必须使用上述扩展获取 [CommandSender] 实例.
 *
 * 除了以下情况:
 * - Console 前端可实现 [ConsoleCommandSender]
 * - 插件可继承 [AbstractPluginCustomCommandSender]
 *
 * ## 子类型
 *
 * 所有 [CommandSender] 都应继承 [AbstractCommandSender].
 *
 * [AbstractCommandSender] 是密封类, 一级子类为:
 * - [AbstractUserCommandSender] 代表用户
 * - [AbstractPluginCustomCommandSender] 代表插件
 * - [ConsoleCommandSender] 代表控制台
 *
 * 二级子类, 当指令由插件 [主动执行][CommandManager.executeCommand] 时, 插件应使用 [toCommandSender] 或 [asCommandSender], 因此,
 * - 若在群聊环境, 对应 [CommandSender] 为 [MemberCommandSender]
 * - 若在私聊环境, 对应 [CommandSender] 为 [FriendCommandSender]
 * - 若在临时会话环境, 对应 [CommandSender] 为 [TempCommandSender]
 * - 若在陌生人会话环境, 对应 [CommandSender] 为 [StrangerCommandSender]
 * - 若在其他客户端会话环境, 对应 [CommandSender] 为 [OtherClientCommandSender]
 *
 * 三级子类, 当真实收到由用户执行的指令时:
 * - 若在群聊环境, 对应 [CommandSender] 为 [MemberCommandSenderOnMessage]
 * - 若在私聊环境, 对应 [CommandSender] 为 [FriendCommandSenderOnMessage]
 * - 若在临时会话环境, 对应 [CommandSender] 为 [TempCommandSenderOnMessage]
 * - 若在陌生人会话环境, 对应 [CommandSender] 为 [StrangerCommandSenderOnMessage]
 * - 若在其他客户端会话环境, 对应 [CommandSender] 为 [OtherClientCommandSenderOnMessage]
 *
 *
 * 类型关系如图. 箭头指向的是父类.
 *
 * ```
 *                 CoroutineScope
 *                        ↑
 *                        |
 *     +----------> CommandSender <---------+---------------------+---------------------------------------------------+
 *     |                                      ↑                 |                     |                               |
 *     |                                      |                 |                     |                               |
 *  SystemCommandSender <-------+             |     UserCommandSender   GroupAwareCommandSender     CommandSenderOnMessage
 *     ↑                        |             |                 ↑                     ↑                               ↑
 *  PluginCustomCommandSender   |             |                 |                     |                               |
 *     ↑                        |             |                 |                     |                               |
 *     |          +-------------+    AbstractCommandSender      |                     |                               |
 *     |          |                           ↑                 |                     |                               |
 *     |          |                           | sealed          |                     |                               |
 *     |          |           +---------------+-------------+   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |
 *     |          |           |               |             |   |                     |                               |      }
 *     |     ConsoleCommandSender             |          AbstractUserCommandSender    |                               |      }
 *     |                                      |             ↑                         |                               |      } 一级子类
 *   AbstractPluginCustomCommandSender -------+             |                         |                               |      }
 *                                                          |                         |                               |      }
 *                                                          |                         |                               |
 *                                                          |                         |                               |
 *                                                          | sealed                  |                               |
 *                                                          |                         |                               |
 *                                   +----------------------+                         |                               |
 *                                   |                      |                         |                               |
 *                                   |                      +------+------------+---------------+                     |
 *                                   |                             |                            |                     |
 *                                   |                             |                            |                     |      }
 *                           FriendCommandSender          MemberCommandSender         GroupTempCommandSender          |      } 二级子类
 *                                   ↑                             ↑                            ↑                     |      }
 *                                   |                             |                            |                     |
 *                                   |                             |                            |                     |      }
 *                      FriendCommandSenderOnMessage  MemberCommandSenderOnMessage  GroupTempCommandSenderOnMessage   |      } 三级子类
 *                                   |                             |                            |                     |      }
 *                                   |                             |                            |                     |
 *                                   +-----------------------------+----------------------------+---------------------+
 * ```
 *
 * ## Scoping: [MessageScope]
 * 在处理多个消息对象时, 可通过 [MessageScope] 简化操作.
 *
 * 查看 [MessageScope] 以获取更多信息.
 *
 * @see ConsoleCommandSender 控制台
 * @see UserCommandSender  [User] ([群成员][Member], [好友][Friend])
 * @see toCommandSender
 * @see asCommandSender
 * @see PluginCustomCommandSender 自定义 CommandSender
 */
public interface CommandSender : CoroutineScope, Permittee {
    /**
     * 与这个 [CommandSender] 相关的 [Bot].
     * 当通过控制台执行时为 `null`.
     */
    public val bot: Bot?

    /**
     * 与这个 [CommandSender] 相关的 [Contact].
     *
     * - 当一个群员执行指令时, [subject] 为所在 [群][Group]
     * - 当通过控制台执行时为 `null`.
     */
    public val subject: Contact?

    /**
     * 指令原始发送*人*.
     * - 当通过控制台执行时为 `null`.
     */
    public val user: User?

    /**
     * [User.nameCardOrNick] 或 [ConsoleCommandSender.NAME]
     */
    public val name: String

    /**
     * 立刻发送一条消息.
     *
     * 对于 [MemberCommandSender], 这个函数总是发送给所在群
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message): MessageReceipt<Contact>?

    /**
     * 立刻发送一条消息.
     *
     * 对于 [MemberCommandSender], 这个函数总是发送给所在群
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String): MessageReceipt<Contact>?

    public companion object {

        ///////////////////////////////////////////////////////////////////////////
        // Constructors
        ///////////////////////////////////////////////////////////////////////////

        /**
         * 构造 [FriendCommandSenderOnMessage]
         */
        @JvmName("from")
        @JvmStatic
        public fun FriendMessageEvent.toCommandSender(): FriendCommandSenderOnMessage =
            FriendCommandSenderOnMessage(this)

        /**
         * 构造 [MemberCommandSenderOnMessage]
         */
        @JvmName("from")
        @JvmStatic
        public fun GroupMessageEvent.toCommandSender(): MemberCommandSenderOnMessage =
            MemberCommandSenderOnMessage(this)

        /**
         * 构造 [TempCommandSenderOnMessage]
         */
        @JvmStatic
        @JvmName("from")
        public fun GroupTempMessageEvent.toCommandSender(): GroupTempCommandSenderOnMessage =
            GroupTempCommandSenderOnMessage(this)

        /**
         * 构造 [StrangerCommandSenderOnMessage]
         */
        @JvmStatic
        @JvmName("from")
        public fun StrangerMessageEvent.toCommandSender(): StrangerCommandSenderOnMessage =
            StrangerCommandSenderOnMessage(this)

        /**
         * 构造 [OtherClientCommandSenderOnMessage]
         */
        @JvmStatic
        @JvmName("from")
        public fun OtherClientMessageEvent.toCommandSender(): OtherClientCommandSenderOnMessage =
            OtherClientCommandSenderOnMessage(this)

        /**
         * 构造 [OtherClientCommandSenderOnMessageSync]
         * @since 2.13
         */
        @JvmStatic
        @JvmName("from")
        public fun MessageSyncEvent.toCommandSender(): OtherClientCommandSenderOnMessageSync =
            OtherClientCommandSenderOnMessageSync(this)

        /**
         * 构造 [CommandSenderOnMessage]
         */
        @JvmStatic
        @JvmName("from")
        @Suppress("UNCHECKED_CAST")
        public fun <T : MessageEvent> T.toCommandSender(): CommandSenderOnMessage<T> = when (this) {
            is FriendMessageEvent -> toCommandSender()
            is GroupMessageEvent -> toCommandSender()
            is GroupTempMessageEvent -> toCommandSender()
            is StrangerMessageEvent -> toCommandSender()
            is OtherClientMessageEvent -> toCommandSender()
            is MessageSyncEvent -> toCommandSender()
            else -> throw IllegalArgumentException("Unsupported MessageEvent: ${this::class.qualifiedNameOrTip}")
        } as CommandSenderOnMessage<T>

        /**
         * 得到 [TempCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun NormalMember.asTempCommandSender(): GroupTempCommandSender = GroupTempCommandSender(this)

        /**
         * 得到 [MemberCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun Member.asMemberCommandSender(): MemberCommandSender = MemberCommandSender(this)

        /**
         * 得到 [MemberCommandSender] 或 [TempCommandSender]
         * @see asTempCommandSender
         * @see asMemberCommandSender
         */
        @JvmStatic
        @JvmName("of")
        public fun Member.asCommandSender(isTemp: Boolean): UserCommandSender {
            return if (isTemp && this is NormalMember) asTempCommandSender() else asMemberCommandSender()
        }

        /**
         * 得到 [FriendCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

        /**
         * 得到 [StrangerCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun Stranger.asCommandSender(): StrangerCommandSender = StrangerCommandSender(this)

        /**
         * 得到 [OtherClientCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun OtherClient.asCommandSender(): OtherClientCommandSender = OtherClientCommandSender(this)

        /**
         * 得到 [UserCommandSender]
         *
         * @param isTemp
         */
        @JvmStatic
        @JvmName("of")
        public fun User.asCommandSender(isTemp: Boolean): UserCommandSender = when (this) {
            is Friend -> this.asCommandSender()
            is Member -> if (isTemp && this is NormalMember) GroupTempCommandSender(this) else MemberCommandSender(this)
            is Stranger -> this.asCommandSender()
            else -> error("stub")
        }
    }
}

/**
 * 一个来自内部系统的命令执行者.
 *
 * 包括
 * - [PluginCustomCommandSender] - 来自插件的命令执行者
 * - [ConsoleCommandSender] - 控制台
 */
public sealed interface SystemCommandSender : CommandSender {
    /**
     * 当前 [SystemCommandSender] 是否支持 Ansi 信息
     *
     * @see AnsiMessageBuilder
     * @see CommandSender.sendAnsiMessage
     */
    public val isAnsiSupported: Boolean
}

/**
 * 一个来自插件自行实现的 [CommandSender].
 *
 * [PluginCustomCommandSender] 不一定拥有全部的权限. [PluginCustomCommandSender] 可以以其他身份执行命令.
 * 默认情况下 [PluginCustomCommandSender] 以 [ConsoleCommandSender] 的身份执行命令
 *
 * @see PermitteeId
 * @see AbstractPluginCustomCommandSender
 */
public interface PluginCustomCommandSender : CommandSender, SystemCommandSender {
    override val isAnsiSupported: Boolean get() = false
    override val permitteeId: PermitteeId get() = AbstractPermitteeId.Console
    public val owner: Plugin

    override suspend fun sendMessage(message: Message): Nothing? {
        return sendMessage(message.toString())
    }

    override suspend fun sendMessage(message: String): Nothing?
}

/**
 * 控制台指令执行者. 代表由控制台执行指令
 *
 * 控制台拥有一切指令的执行权限.
 *
 * 不建议在 [CompositeCommand] 中使用 [ConsoleCommandSender],
 * 使用 [SystemCommandSender] 以允许其他插件执行
 *
 */
public object ConsoleCommandSender : AbstractCommandSender(), SystemCommandSender {
    public const val NAME: String = "ConsoleCommandSender"

    public override val bot: Nothing? get() = null
    public override val subject: Nothing? get() = null
    public override val user: Nothing? get() = null
    public override val name: String get() = NAME
    public override fun toString(): String = NAME

    public override val permitteeId: AbstractPermitteeId.Console = AbstractPermitteeId.Console

    public override val coroutineContext: CoroutineContext by lazy { MiraiConsole.childScopeContext(NAME) }

    @OptIn(ConsoleFrontEndImplementation::class)
    override val isAnsiSupported: Boolean
        get() = MiraiConsoleImplementation.getInstance().isAnsiSupported

    @OptIn(ConsoleFrontEndImplementation::class)
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): Nothing? {
        MiraiConsoleImplementation.getInstance().consoleCommandSender.sendMessage(message)
        return null
    }

    @OptIn(ConsoleFrontEndImplementation::class)
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): Nothing? {
        MiraiConsoleImplementation.getInstance().consoleCommandSender.sendMessage(message)
        return null
    }
}

/**
 * 知道 [Group] 环境的 [UserCommandSender]
 *
 * 可能的子类:
 *
 * - [MemberCommandSender] 代表一个 [群员][Member] 执行指令
 * - [TempCommandSender] 代表一个 [群员][Member] 通过临时会话执行指令
 */
public interface GroupAwareCommandSender : UserCommandSender {
    public val group: Group
}

/**
 * 代表一个用户执行指令
 *
 * @see MemberCommandSender 代表一个 [群员][Member] 执行指令
 * @see FriendCommandSender 代表一个 [好友][Friend] 执行指令
 * @see TempCommandSender 代表一个 [群员][Member] 通过临时会话执行指令
 * @see StrangerCommandSender 代表一个 [陌生人][Stranger] 执行指令
 *
 * @see CommandSenderOnMessage
 */
public interface UserCommandSender : CommandSender {
    /**
     * @see MessageEvent.sender
     */
    public override val user: User // override nullability

    /**
     * @see MessageEvent.subject
     */
    public override val subject: Contact // override nullability
    public override val bot: Bot // override nullability
}

/**
 * 代表一个真实 [用户][User] 主动私聊机器人或在群内发送消息而执行指令
 *
 * @see MemberCommandSenderOnMessage 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 * @see FriendCommandSenderOnMessage 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 * @see TempCommandSenderOnMessage 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 */
public interface CommandSenderOnMessage<T : MessageEvent> : CommandSender {

    /**
     * 消息源 [MessageEvent]
     */
    public val fromEvent: T
}

/**
 * 所有 [CommandSender] 都必须继承自此对象.
 * @see CommandSender 查看更多信息
 */
public sealed class AbstractCommandSender : CommandSender, CoroutineScope {
    public abstract override val bot: Bot?
    public abstract override val subject: Contact?
    public abstract override val user: User?
    public abstract override fun toString(): String
}
// endregion

// region extension functions

/**
 * 当 [this] 为 [ConsoleCommandSender] 时返回 `true`
 */
public fun CommandSender.isConsole(): Boolean {
    contract {
        returns(true) implies (this@isConsole is ConsoleCommandSender)
    }
    return this is ConsoleCommandSender
}

/**
 * 当 [this] 为 [SystemCommandSender] 时返回 `true`
 */
public fun CommandSender.isSystem(): Boolean {
    contract {
        returns(true) implies (this@isSystem is SystemCommandSender)
    }
    return this is SystemCommandSender
}

/**
 * 当 [this] 不为 [ConsoleCommandSender] 时返回 `true`
 */
public fun CommandSender.isNotConsole(): Boolean {
    contract {
        returns(true) implies (this@isNotConsole !is ConsoleCommandSender)
    }
    return this !is ConsoleCommandSender
}

/**
 * 当 [this] 为 [UserCommandSender] 时返回 `true`
 */
public fun CommandSender.isUser(): Boolean {
    contract {
        returns(true) implies (this@isUser is UserCommandSender)
    }
    return this is UserCommandSender
}

/**
 * 当 [this] 不为 [UserCommandSender], 即为 [SystemCommandSender] 时返回 `true`
 */
public fun CommandSender.isNotUser(): Boolean {
    contract {
        returns(true) implies (this@isNotUser is SystemCommandSender)
    }
    return this !is UserCommandSender
}

/**
 * 折叠 [CommandSender] 的可能性.
 *
 * - 当 [this] 为 [SystemCommandSender] 时执行 [ifIsSystem]
 * - 当 [this] 为 [UserCommandSender] 时执行 [ifIsUser]
 * - 否则执行 [otherwise]
 *
 * ### 示例
 * ```
 * // 当一个指令执行过程出错
 * val exception: Exception = ...
 *
 * sender.fold(
 *     ifIsSystem = { // this: SystemCommandSender
 *         sendMessage(exception.stackTraceToString()) // 展示整个 stacktrace
 *     },
 *     ifIsUser = { // this: UserCommandSender
 *         sendMessage(exception.message.toString()) // 只展示 Exception.message
 *     }
 * )
 * ```
 *
 * @return [ifIsSystem], [ifIsUser] 或 [otherwise] 执行结果.
 */
@JvmSynthetic
public inline fun <R> CommandSender.fold(
    ifIsSystem: SystemCommandSender.() -> R,
    ifIsUser: UserCommandSender.() -> R,
    otherwise: CommandSender.() -> R = { error("CommandSender ${this::class.qualifiedName} is not supported") },
): R {
    contract {
        callsInPlace(ifIsSystem, InvocationKind.AT_MOST_ONCE)
        callsInPlace(ifIsUser, InvocationKind.AT_MOST_ONCE)
        callsInPlace(otherwise, InvocationKind.AT_MOST_ONCE)
    }
    return when (val sender = this) {
        is SystemCommandSender -> ifIsSystem(sender)
        is UserCommandSender -> ifIsUser(sender)
        else -> otherwise(sender)
    }
}

/**
 * 折叠 [UserCommandSender] 的两种可能性, 即在群内发送或在私聊环境发送.
 *
 * - 当 [this] 为 [MemberCommandSender] 时执行 [inGroup]
 * - 当 [this] 为 [TempCommandSender] 或 [FriendCommandSender] 时执行 [inPrivate]
 *
 * ### 实验性 API
 *
 * 这是预览版本 API. 如果你对 [UserCommandSender.foldContext] 有建议, 请在 [mamoe/mirai-console/issues](https://github.com/mamoe/mirai-console/issues/new) 提交.
 *
 * @return [inGroup] 或 [inPrivate] 执行结果.
 */
@JvmSynthetic
@ConsoleExperimentalApi
public inline fun <R> UserCommandSender.foldContext(
    inGroup: MemberCommandSender.() -> R,
    inPrivate: UserCommandSender.() -> R,
): R {
    contract {
        callsInPlace(inGroup, InvocationKind.AT_MOST_ONCE)
        callsInPlace(inPrivate, InvocationKind.AT_MOST_ONCE)
    }
    return when (val sender = this) {
        is MemberCommandSender -> inGroup(sender)
        else -> inPrivate(sender)
    }
}

/**
 * 尝试获取 [Bot].
 *
 * 当 [UserCommandSender] 时返回 [UserCommandSender.bot], 否则返回 `null`
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回非 `null` 实例, Kotlin 编译器认为 [this] 是 [UserCommandSender] 实例并执行智能类型转换.
 * - 若返回 `null`, Kotlin 编译器认为 [this] 是 [SystemCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.getBotOrNull(): Bot? {
    contract {
        returns(null) implies (this@getBotOrNull is SystemCommandSender)
        returnsNotNull() implies (this@getBotOrNull is UserCommandSender)
    }
    return this.castOrNull<UserCommandSender>()?.bot
}

/**
 * 尝试获取 [Group].
 *
 * 当 [GroupAwareCommandSender] 时返回 [GroupAwareCommandSender.group], 否则返回 `null`
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回非 `null` 实例, Kotlin 编译器认为 [this] 是 [GroupAwareCommandSender] 实例并执行智能类型转换.
 * - 若返回 `null`, Kotlin 编译器认为 [this] 是 [FriendCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.getGroupOrNull(): Group? {
    contract {
        returns(null) implies (this@getGroupOrNull is FriendCommandSender)
        returnsNotNull() implies (this@getGroupOrNull is GroupAwareCommandSender)
    }
    return this.castOrNull<GroupAwareCommandSender>()?.group
}

// endregion

// region implementations

///////////////////////////////////////////////////////////////////////////
// UserCommandSender
///////////////////////////////////////////////////////////////////////////

/**
 * [UserCommandSender] 的实现
 */
public sealed class AbstractUserCommandSender : UserCommandSender, AbstractCommandSender() {
    public override val bot: Bot get() = user.bot // don't final
    public final override val name: String get() = user.nameCardOrNick

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Contact> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Contact> = user.sendMessage(message)
}

/**
 * 代表一个 [好友][Friend] 执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see FriendCommandSenderOnMessage 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 */
public open class FriendCommandSender internal constructor(
    public final override val user: Friend,
) : AbstractUserCommandSender(), CoroutineScope by user.childScope("FriendCommandSender") {
    public override val subject: Contact get() = user
    public override fun toString(): String = "FriendCommandSender($user)"

    public override val permitteeId: PermitteeId = AbstractPermitteeId.ExactFriend(user.id)

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Friend> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Friend> = user.sendMessage(message)
}

/**
 * 代表一个 [群员][Member] 执行指令, 但不一定是通过群内发消息方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see MemberCommandSenderOnMessage 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 */
public open class MemberCommandSender internal constructor(
    public final override val user: Member,
) : AbstractUserCommandSender(), GroupAwareCommandSender, CoroutineScope by user.childScope("MemberCommandSender") {
    public final override val group: Group get() = user.group
    public override val subject: Group get() = group
    public override fun toString(): String = "MemberCommandSender($user)"

    public override val permitteeId: PermitteeId = AbstractPermitteeId.ExactMember(group.id, user.id)

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Group> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Group> = subject.sendMessage(message)
}

/**
 * 代表一个 [群员][Member] 通过临时会话执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see TempCommandSenderOnMessage 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此事件会变动. 原 TempCommandSender 已更改为 GroupTempCommandSender",
    replaceWith = ReplaceWith("GroupTempCommandSender", "net.mamoe.mirai.console.command.GroupTempCommandSender"),
    DeprecationLevel.HIDDEN
)
@DeprecatedSinceMirai(errorSince = "2.0", hiddenSince = "2.10")
public sealed class TempCommandSender(
    public override val user: NormalMember,
) : AbstractUserCommandSender(), GroupAwareCommandSender, CoroutineScope by user.childScope("TempCommandSender")

/**
 * 代表一个 [群员][Member] 通过临时会话执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see TempCommandSenderOnMessage 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
public open class GroupTempCommandSender internal constructor(
    public final override val user: NormalMember,
) : @Suppress("DEPRECATION_ERROR") TempCommandSender(user),
    CoroutineScope by user.childScope("GroupTempCommandSender") {
    public override val group: Group get() = user.group
    public override val subject: NormalMember get() = user
    public override fun toString(): String = "GroupTempCommandSender($user)"

    public override val permitteeId: PermitteeId =
        AbstractPermitteeId.ExactGroupTemp(user.group.id, user.id)

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Member> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Member> = user.sendMessage(message)
}

/**
 * 代表一个 [陌生人][Stranger] 通过私聊执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see StrangerCommandSenderOnMessage 代表一个 [陌生人][Stranger] 主动在私聊发送消息执行指令
 */
public open class StrangerCommandSender internal constructor(
    public final override val user: Stranger,
) : AbstractUserCommandSender(), CoroutineScope by user.childScope("StrangerCommandSender") {
    public override val subject: Stranger get() = user
    public override fun toString(): String = "StrangerCommandSender($user)"

    public override val permitteeId: PermitteeId = AbstractPermitteeId.ExactStranger(user.id)

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Stranger> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Stranger> = user.sendMessage(message)
}

/**
 * 代表一个 [其他客户端][OtherClient] 通过私聊执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.executeCommand])
 * @see OtherClientCommandSenderOnMessage 代表一个[其他客户端][OtherClient] 主动在私聊发送消息执行指令
 */
public open class OtherClientCommandSender internal constructor(
    public val client: OtherClient,
) : AbstractCommandSender(), CoroutineScope by client.childScope("OtherClientCommandSender") {
    public final override val user: Friend get() = client.bot.asFriend
    public final override val bot: Bot get() = client.bot
    public final override val name: String get() = client.bot.nick
    public override val subject: Friend get() = user
    public override fun toString(): String = "OtherClientCommandSender($user)"

    public override val permitteeId: PermitteeId = AbstractPermitteeId.AnyOtherClient

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<OtherClient> =
        sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<OtherClient> = client.sendMessage(message)
}

///////////////////////////////////////////////////////////////////////////
// CommandSenderOnMessage
///////////////////////////////////////////////////////////////////////////

/**
 * 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 * @see FriendCommandSender 代表一个 [好友][Friend] 执行指令, 但不一定是通过私聊方式
 */
public class FriendCommandSenderOnMessage internal constructor(
    public override val fromEvent: FriendMessageEvent,
) : FriendCommandSender(fromEvent.sender), CommandSenderOnMessage<FriendMessageEvent>

/**
 * 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 * @see MemberCommandSender 代表一个 [群员][Member] 执行指令, 但不一定是通过群内发消息方式
 */
public class MemberCommandSenderOnMessage internal constructor(
    public override val fromEvent: GroupMessageEvent,
) : MemberCommandSender(fromEvent.sender), CommandSenderOnMessage<GroupMessageEvent>

/**
 * 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 * @see TempCommandSender 代表一个 [群员][Member] 执行指令, 但不一定是通过私聊方式
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此事件会变动. 原 TempCommandSenderOnMessage 已更改为 GroupTempCommandSenderOnMessage",
    replaceWith = ReplaceWith(
        "GroupTempCommandSenderOnMessage",
        "net.mamoe.mirai.console.command.GroupTempCommandSenderOnMessage"
    ),
    DeprecationLevel.HIDDEN
)
@DeprecatedSinceMirai(errorSince = "2.0", hiddenSince = "2.10")
public sealed class TempCommandSenderOnMessage(
    public override val fromEvent: GroupTempMessageEvent,
) : GroupTempCommandSender(fromEvent.sender), CommandSenderOnMessage<GroupTempMessageEvent>

/**
 * 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 * @see TempCommandSender 代表一个 [群员][Member] 执行指令, 但不一定是通过私聊方式
 */
public class GroupTempCommandSenderOnMessage internal constructor(
    public override val fromEvent: GroupTempMessageEvent,
) : @Suppress("DEPRECATION_ERROR") TempCommandSenderOnMessage(fromEvent), CommandSenderOnMessage<GroupTempMessageEvent>

/**
 * 代表一个 [陌生人][Stranger] 主动在私聊发送消息执行指令
 * @see StrangerCommandSender 代表一个 [陌生人][Stranger] 执行指令, 但不一定是通过私聊方式
 */
public class StrangerCommandSenderOnMessage internal constructor(
    public override val fromEvent: StrangerMessageEvent,
) : StrangerCommandSender(fromEvent.sender), CommandSenderOnMessage<StrangerMessageEvent>

/**
 * 代表一个 [其他客户端][OtherClient] 主动在私聊发送消息执行指令
 * @see OtherClientCommandSender 代表一个 [其他客户端][OtherClient] 执行指令, 但不一定是通过私聊方式
 */
public class OtherClientCommandSenderOnMessage internal constructor(
    public override val fromEvent: OtherClientMessageEvent,
) : OtherClientCommandSender(fromEvent.client), CommandSenderOnMessage<OtherClientMessageEvent>

/**
 * 代表一个 [其他客户端][OtherClient] 主动在群内、好友聊天等发送消息执行指令
 * @see OtherClientCommandSender 代表一个 [其他客户端][OtherClient] 执行指令, 但不一定是通过私聊方式
 * @since 2.13
 */
public class OtherClientCommandSenderOnMessageSync internal constructor(
    public override val fromEvent: MessageSyncEvent,
) : OtherClientCommandSender(fromEvent.client), CommandSenderOnMessage<MessageSyncEvent>

// endregion

// region PluginCustomCommandSender implementations
/**
 * 所有 [PluginCustomCommandSender] 的父类
 *
 * Java 见 [AbstractPluginCustomCommandSenderJ]
 */
public abstract class AbstractPluginCustomCommandSender(
    final override val owner: Plugin,
) : AbstractCommandSender(), PluginCustomCommandSender {
    override val coroutineContext: CoroutineContext by lazy {
        if (owner is CoroutineScope) {
            return@lazy owner.childScopeContext(name)
        }
        // Fallback
        return@lazy MiraiConsole.childScopeContext("PluginCustomCommandSender-$name")
    }
    override val name: String get() = owner.name
    override fun toString(): String = name

    override val isAnsiSupported: Boolean get() = false
    override val permitteeId: PermitteeId get() = AbstractPermitteeId.Console

    override val bot: Bot? get() = null
    override val subject: Contact? get() = null
    override val user: User? get() = null

    override suspend fun sendMessage(message: String): Nothing? {
        sendMessage0(message)
        return null
    }

    protected abstract suspend fun sendMessage0(message: String)
}

@JavaFriendlyApi
public abstract class AbstractPluginCustomCommandSenderJ(
    owner: Plugin
) : AbstractPluginCustomCommandSender(owner) {
    protected abstract fun sendMessageImpl(message: String)

    final override suspend fun sendMessage(message: String): Nothing? {
        sendMessageImpl(message)
        return null
    }

    final override suspend fun sendMessage0(message: String) {
        sendMessageImpl(message)
    }
}
// endregion

