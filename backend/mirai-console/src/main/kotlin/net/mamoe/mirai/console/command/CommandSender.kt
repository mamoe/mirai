/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME", "FunctionName", "SuspendFunctionOnCoroutineScope",
    "unused", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER"
)

package net.mamoe.mirai.console.command

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.execute
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.asMemberCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.asTempCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender.INSTANCE
import net.mamoe.mirai.console.command.description.CommandArgumentParserException
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.internal.data.castOrNull
import net.mamoe.mirai.console.internal.plugin.rootCauseOrSelf
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScopeContext
import net.mamoe.mirai.console.util.MessageScope
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.internal.LowPriorityInOverloadResolution

/**
 * 指令发送者.
 *
 * 只有 [CommandSender] 才能 [执行指令][CommandManager.execute]
 *
 * ## 获得指令发送者
 * - [MessageEvent.toCommandSender]
 * - [FriendMessageEvent.toCommandSender]
 * - [GroupMessageEvent.toCommandSender]
 * - [TempMessageEvent.toCommandSender]
 *
 * - [Member.asCommandSender]
 * - [Member.asTempCommandSender]]
 * - [Member.asMemberCommandSender]]
 * - [Friend.asCommandSender]
 * - [User.asCommandSender]
 *
 * ## 实现 [CommandSender]
 * 除 Console 前端外, 在任何时候都不要实现 [CommandSender] (包括使用委托). 必须使用上述扩展获取 [CommandSender] 实例.
 *
 * Console 前端可实现 [ConsoleCommandSender]
 *
 * ## 子类型
 *
 * 所有 [CommandSender] 都应继承 [AbstractCommandSender].
 *
 * [AbstractCommandSender] 是密封类, 一级子类为:
 * - [AbstractUserCommandSender] 代表用户
 * - [ConsoleCommandSender] 代表控制台
 *
 * 二级子类, 当指令由插件 [主动执行][CommandManager.execute] 时, 插件应使用 [toCommandSender] 或 [asCommandSender], 因此,
 * - 若在群聊环境, 对应 [CommandSender] 为 [MemberCommandSender]
 * - 若在私聊环境, 对应 [CommandSender] 为 [FriendCommandSender]
 * - 若在临时会话环境, 对应 [CommandSender] 为 [TempCommandSender]
 *
 * 三级子类, 当真实收到由用户执行的指令时:
 * - 若在群聊环境, 对应 [CommandSender] 为 [MemberCommandSenderOnMessage]
 * - 若在私聊环境, 对应 [CommandSender] 为 [FriendCommandSenderOnMessage]
 * - 若在临时会话环境, 对应 [CommandSender] 为 [TempCommandSenderOnMessage]
 *
 * 类型关系如图. 箭头指向的是父类.
 *
 * ```
 *                 CoroutineScope
 *                        ↑
 *                        |
 *                  CommandSender <---------+---------------+-------------------------------+
 *                        ↑                 |               |                               |
 *                        |                 |               |                               |
 *                        |     UserCommandSender   GroupAwareCommandSender     CommandSenderOnMessage
 *                        |                 ↑               ↑                               ↑
 *                        |                 |               |                               |
 *               AbstractCommandSender      |               |                               |
 *                        ↑                 |               |                               |
 *                        | sealed          |               |                               |
 *          +-------------+-------------+   |               |                               |
 *          |                           |   |               |                               |
 *          |                           |   |               |                               |      }
 * ConsoleCommandSender    AbstractUserCommandSender        |                               |      } 一级子类
 *                                      ↑                   |                               |      }
 *                                      | sealed            |                               |
 *                                      |                   |                               |
 *               +----------------------+                   |                               |
 *               |                      |                   |                               |
 *               |                      +------+------------+---------------+               |
 *               |                             |                            |               |
 *               |                             |                            |               |      }
 *       FriendCommandSender          MemberCommandSender           TempCommandSender       |      } 二级子类
 *               ↑                             ↑                            ↑               |      }
 *               |                             |                            |               |
 *               |                             |                            |               |      }
 *  FriendCommandSenderOnMessage  MemberCommandSenderOnMessage  TempCommandSenderOnMessage  |      } 三级子类
 *               |                             |                            |               |      }
 *               |                             |                            |               |
 *               +-----------------------------+----------------------------+---------------+
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
 */
public interface CommandSender : CoroutineScope {
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
    @JvmDefault
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String): MessageReceipt<Contact>?

    @ConsoleExperimentalAPI("This is unstable and might get changed")
    public suspend fun catchExecutionException(e: Throwable)

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
        public fun TempMessageEvent.toCommandSender(): TempCommandSenderOnMessage = TempCommandSenderOnMessage(this)

        /**
         * 构造 [CommandSenderOnMessage]
         */
        @JvmStatic
        @JvmName("from")
        @Suppress("UNCHECKED_CAST")
        public fun <T : MessageEvent> T.toCommandSender(): CommandSenderOnMessage<T> = when (this) {
            is FriendMessageEvent -> toCommandSender()
            is GroupMessageEvent -> toCommandSender()
            is TempMessageEvent -> toCommandSender()
            else -> throw IllegalArgumentException("Unsupported MessageEvent: ${this::class.qualifiedNameOrTip}")
        } as CommandSenderOnMessage<T>

        /**
         * 得到 [TempCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun Member.asTempCommandSender(): TempCommandSender = TempCommandSender(this)

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
            return if (isTemp) asTempCommandSender() else asMemberCommandSender()
        }

        /**
         * 得到 [FriendCommandSender]
         */
        @JvmStatic
        @JvmName("of")
        public fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

        /**
         * 得到 [UserCommandSender]
         *
         * @param isTemp
         */
        @JvmStatic
        @JvmName("of")
        public fun User.asCommandSender(isTemp: Boolean): UserCommandSender = when (this) {
            is Friend -> this.asCommandSender()
            is Member -> if (isTemp) TempCommandSender(this) else MemberCommandSender(this)
            else -> error("stub")
        }
    }
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

    @ConsoleExperimentalAPI("This is unstable and might get changed")
    override suspend fun catchExecutionException(e: Throwable) {
        if (this is CommandSenderOnMessage<*>) {
            val cause = e.rootCauseOrSelf

            val message = cause
                .takeIf { it is CommandArgumentParserException }?.message
                ?: "${cause::class.simpleName.orEmpty()}: ${cause.message}"

            // TODO: 2020/8/30 优化 net.mamoe.mirai.console.command.CommandSender.catchExecutionException

            sendMessage(message) // \n\n60 秒内发送 stacktrace 查看堆栈信息
            this@AbstractCommandSender.launch(CoroutineName("stacktrace delayer from command")) {
                if (fromEvent.nextMessageOrNull(60_000) {
                        it.message.contentEquals("stacktrace") || it.message.contentEquals("stack")
                    } != null) {
                    sendMessage(e.stackTraceToString())
                }
            }
        } else {
            sendMessage(e.stackTraceToString())
        }
    }
}

/**
 * 当 [this] 为 [AbstractCommandSender] 时返回.
 *
 * 正常情况下, 所有 [CommandSender] 都应该继承 [AbstractCommandSender]
 *
 * 在需要类型智能转换等情况时可使用此函数.
 *
 * ### 契约
 * 本函数定义契约,
 * - 若函数正常返回, Kotlin 编译器认为 [this] 是 [AbstractCommandSender] 实例并执行智能类型转换.
 *
 * @return `this`
 */
public fun CommandSender.checkIsAbstractCommandSender(): AbstractCommandSender {
    contract {
        returns() implies (this@checkIsAbstractCommandSender is AbstractCommandSender)
    }
    check(this is AbstractCommandSender) { "A CommandSender must extend AbstractCommandSender" }
    return this
}

/**
 * 当 [this] 为 [AbstractUserCommandSender] 时返回.
 *
 * 正常情况下, 所有 [UserCommandSender] 都应该继承 [AbstractUserCommandSender]
 *
 * 在需要类型智能转换等情况时可使用此函数.
 *
 * ### 契约
 * 本函数定义契约,
 * - 若函数正常返回, Kotlin 编译器认为 [this] 是 [AbstractUserCommandSender] 实例并执行智能类型转换.
 *
 * @return `this`
 */
public fun UserCommandSender.checkIsAbstractUserCommandSender(): AbstractUserCommandSender {
    contract {
        returns() implies (this@checkIsAbstractUserCommandSender is AbstractUserCommandSender)
    }
    check(this is AbstractUserCommandSender) { "A UserCommandSender must extend AbstractUserCommandSender" }
    return this
}

/**
 * 当 [this] 为 [ConsoleCommandSender] 时返回 `true`
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回 `true`, Kotlin 编译器认为 [this] 是 [ConsoleCommandSender] 实例并执行智能类型转换.
 * - 若返回 `false`, Kotlin 编译器认为 [this] 是 [UserCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.isConsole(): Boolean {
    contract {
        returns(true) implies (this@isConsole is ConsoleCommandSender)
        returns(false) implies (this@isConsole is UserCommandSender)
    }
    this.checkIsAbstractCommandSender()
    return this is ConsoleCommandSender
}

/**
 * 当 [this] 不为 [ConsoleCommandSender], 即为 [UserCommandSender] 时返回 `true`.
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回 `true`, Kotlin 编译器认为 [this] 是 [UserCommandSender] 实例并执行智能类型转换.
 * - 若返回 `false`, Kotlin 编译器认为 [this] 是 [ConsoleCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.isNotConsole(): Boolean {
    contract {
        returns(true) implies (this@isNotConsole is UserCommandSender)
        returns(false) implies (this@isNotConsole is ConsoleCommandSender)
    }
    this.checkIsAbstractCommandSender()
    return this !is ConsoleCommandSender
}

/**
 * 当 [this] 为 [UserCommandSender] 时返回 `true`
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回 `true`, Kotlin 编译器认为 [this] 是 [UserCommandSender] 实例并执行智能类型转换.
 * - 若返回 `false`, Kotlin 编译器认为 [this] 是 [ConsoleCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.isUser(): Boolean {
    contract {
        returns(true) implies (this@isUser is UserCommandSender)
        returns(false) implies (this@isUser is ConsoleCommandSender)
    }
    this.checkIsAbstractCommandSender()
    return this is UserCommandSender
}

/**
 * 当 [this] 不为 [UserCommandSender], 即为 [ConsoleCommandSender] 时返回 `true`
 *
 * ### 契约
 * 本函数定义契约,
 * - 若返回 `true`, Kotlin 编译器认为 [this] 是 [ConsoleCommandSender] 实例并执行智能类型转换.
 * - 若返回 `false`, Kotlin 编译器认为 [this] 是 [UserCommandSender] 实例并执行智能类型转换.
 */
public fun CommandSender.isNotUser(): Boolean {
    contract {
        returns(true) implies (this@isNotUser is ConsoleCommandSender)
        returns(false) implies (this@isNotUser is UserCommandSender)
    }
    this.checkIsAbstractCommandSender()
    return this !is UserCommandSender
}

/**
 * 折叠 [AbstractCommandSender] 的两种可能性.
 *
 * - 当 [this] 为 [ConsoleCommandSender] 时执行 [ifIsConsole]
 * - 当 [this] 为 [UserCommandSender] 时执行 [ifIsUser]
 *
 * ### 示例
 * ```
 * // 当一个指令执行过程出错
 * val exception: Exception = ...
 *
 * sender.fold(
 *     ifIsConsole = { // this: ConsoleCommandSender
 *         sendMessage(exception.stackTraceToString()) // 展示整个 stacktrace
 *     },
 *     ifIsUser = { // this: UserCommandSender
 *         sendMessage(exception.message.toString()) // 只展示 Exception.message
 *     }
 * )
 * ```
 *
 * @return [ifIsConsole] 或 [ifIsUser] 执行结果.
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
public inline fun <R> CommandSender.fold(
    ifIsConsole: ConsoleCommandSender.() -> R,
    ifIsUser: UserCommandSender.() -> R,
): R {
    contract {
        callsInPlace(ifIsConsole, InvocationKind.AT_MOST_ONCE)
        callsInPlace(ifIsUser, InvocationKind.AT_MOST_ONCE)
    }
    return when (val sender = this.checkIsAbstractCommandSender()) {
        is ConsoleCommandSender -> ifIsConsole(sender)
        is AbstractUserCommandSender -> ifIsUser(sender)
    }
}

/**
 * 折叠 [AbstractCommandSender] 的两种可能性, 即在群内发送或在私聊环境发送.
 *
 * - 当 [this] 为 [MemberCommandSender] 时执行 [inGroup]
 * - 当 [this] 为 [TempCommandSender] 或 [FriendCommandSender] 时执行 [inPrivate]
 *
 * ### 实验性 API
 *
 * 这是预览版本 API. 如果你对 [UserCommandSender.fold] 有建议, 请在 [mamoe/mirai-console/issues](https://github.com/mamoe/mirai-console/issues/new) 提交.
 *
 * @return [inGroup] 或 [inPrivate] 执行结果.
 */
@JvmSynthetic
@ConsoleExperimentalAPI
public inline fun <R> UserCommandSender.foldContext(
    inGroup: MemberCommandSender.() -> R,
    inPrivate: UserCommandSender.() -> R,
): R {
    contract {
        callsInPlace(inGroup, InvocationKind.AT_MOST_ONCE)
        callsInPlace(inPrivate, InvocationKind.AT_MOST_ONCE)
    }
    return when (val sender = this.checkIsAbstractUserCommandSender()) {
        is MemberCommandSender -> inGroup(sender)
        else -> inPrivate(sender)
    }
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
public fun CommandSender.getBotOrNull(): Bot? {
    contract {
        returns(null) implies (this@getBotOrNull is AbstractUserCommandSender)
        returnsNotNull() implies (this@getBotOrNull is ConsoleCommandSender)
    }
    return this.castOrNull<UserCommandSender>()?.bot
}

/**
 * 控制台指令执行者. 代表由控制台执行指令
 * @see INSTANCE
 */
// 前端实现
public abstract class ConsoleCommandSender @ConsoleFrontEndImplementation constructor() : AbstractCommandSender() {
    public final override val bot: Nothing? get() = null
    public final override val subject: Nothing? get() = null
    public final override val user: Nothing? get() = null
    public final override val name: String get() = NAME
    public final override fun toString(): String = NAME

    public companion object INSTANCE : ConsoleCommandSender(), CoroutineScope {
        public const val NAME: String = "ConsoleCommandSender"
        public override val coroutineContext: CoroutineContext by lazy { MiraiConsole.childScopeContext(NAME) }
        public override suspend fun sendMessage(message: Message): Nothing? {
            MiraiConsoleImplementationBridge.consoleCommandSender.sendMessage(message)
            return null
        }

        public override suspend fun sendMessage(message: String): MessageReceipt<User>? {
            MiraiConsoleImplementationBridge.consoleCommandSender.sendMessage(message)
            return null
        }
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

///////////////////////////////////////////////////////////////////////////
// UserCommandSender
///////////////////////////////////////////////////////////////////////////

/**
 * 代表一个用户执行指令
 *
 * @see MemberCommandSender 代表一个 [群员][Member] 执行指令
 * @see FriendCommandSender 代表一个 [好友][Friend] 执行指令
 * @see TempCommandSender 代表一个 [群员][Member] 通过临时会话执行指令
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
 * 代表一个 [好友][Friend] 执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.execute])
 * @see FriendCommandSenderOnMessage 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 */
public open class FriendCommandSender internal constructor(
    public final override val user: Friend
) : AbstractUserCommandSender(), CoroutineScope by user.childScope("FriendCommandSender") {
    public override val subject: Contact get() = user
    public override fun toString(): String = "FriendCommandSender($user)"

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Friend> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Friend> = user.sendMessage(message)
}

/**
 * 代表一个 [群员][Member] 执行指令, 但不一定是通过群内发消息方式, 也有可能是由插件在代码直接执行 ([CommandManager.execute])
 * @see MemberCommandSenderOnMessage 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 */
public open class MemberCommandSender internal constructor(
    public final override val user: Member
) : AbstractUserCommandSender(),
    GroupAwareCommandSender,
    CoroutineScope by user.childScope("MemberCommandSender") {
    public override val group: Group get() = user.group
    public override val subject: Group get() = group
    public override fun toString(): String = "MemberCommandSender($user)"

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Group> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Group> = subject.sendMessage(message)
}

/**
 * 代表一个 [群员][Member] 通过临时会话执行指令, 但不一定是通过私聊方式, 也有可能是由插件在代码直接执行 ([CommandManager.execute])
 * @see TempCommandSenderOnMessage 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 */
public open class TempCommandSender internal constructor(
    public final override val user: Member
) : AbstractUserCommandSender(),
    GroupAwareCommandSender,
    CoroutineScope by user.childScope("TempCommandSender") {
    public override val group: Group get() = user.group
    public override val subject: Contact get() = group
    public override fun toString(): String = "TempCommandSender($user)"

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Member> = sendMessage(PlainText(message))

    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Member> = user.sendMessage(message)
}

///////////////////////////////////////////////////////////////////////////
// CommandSenderOnMessage
///////////////////////////////////////////////////////////////////////////

/**
 * 代表一个真实 [用户][User] 主动私聊机器人或在群内发送消息而执行指令
 *
 * @see MemberCommandSenderOnMessage 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 * @see FriendCommandSenderOnMessage 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 * @see TempCommandSenderOnMessage 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 */
public interface CommandSenderOnMessage<T : MessageEvent> :
    CommandSender,
    MessageEventExtensions<User, Contact> {

    /**
     * 消息源 [MessageEvent]
     */
    public val fromEvent: T
}

/**
 * 代表一个真实的 [好友][Friend] 主动在私聊消息执行指令
 * @see FriendCommandSender 代表一个 [好友][Friend] 执行指令, 但不一定是通过私聊方式
 */
public class FriendCommandSenderOnMessage internal constructor(
    public override val fromEvent: FriendMessageEvent
) : FriendCommandSender(fromEvent.sender),
    CommandSenderOnMessage<FriendMessageEvent>,
    MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Friend get() = fromEvent.subject
    public override val bot: Bot get() = super.bot
}

/**
 * 代表一个真实的 [群员][Member] 主动在群内发送消息执行指令.
 * @see MemberCommandSender 代表一个 [群员][Member] 执行指令, 但不一定是通过群内发消息方式
 */
public class MemberCommandSenderOnMessage internal constructor(
    public override val fromEvent: GroupMessageEvent
) : MemberCommandSender(fromEvent.sender),
    CommandSenderOnMessage<GroupMessageEvent>,
    MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Group get() = fromEvent.subject
    public override val bot: Bot get() = super.bot
}

/**
 * 代表一个 [群员][Member] 主动在临时会话发送消息执行指令
 * @see TempCommandSender 代表一个 [群员][Member] 通过临时会话执行指令, 但不一定是通过私聊方式
 */
public class TempCommandSenderOnMessage internal constructor(
    public override val fromEvent: TempMessageEvent
) : TempCommandSender(fromEvent.sender),
    CommandSenderOnMessage<TempMessageEvent>,
    MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Member get() = fromEvent.subject
    public override val bot: Bot get() = super.bot
}