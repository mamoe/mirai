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

package net.mamoe.mirai.console.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.fold
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import kotlin.internal.InlineOnly
import kotlin.internal.LowPriorityInOverloadResolution

/**
 * 表示几个消息对象的 '域', 即消息对象的集合. 用于最小化将同一条消息发送给多个类型不同的目标的付出.
 *
 * ## 支持的消息对象类型
 * [Contact], [CommandSender], [MessageScope] (递归).
 *
 * 在下文, `A` 或 `B` 指代这三种类型的其中两种, 允许排列组合. 如 `A.scopeWith(B)` 可能表示 `Contact.scopeWith(MessageScope)`.
 *
 * ## 获得 [MessageScope]
 * - `A.asMessageScope()`.
 * - `C<A>.toMessageScope()`. 其中 `C` 表示 `Iterable`, `Sequence`, `Flow`, `Array` 其中任一.
 *
 * ## 连接 [MessageScope]
 * - `A.scopeWith(vararg B)`.
 * - `A.scopeWith(vararg A)`.
 * - `A.scopeWithNotNull(vararg B?)`. 类似 [listOfNotNull].
 * - `A.scopeWithNotNull(vararg A?)`. 类似 [listOfNotNull].
 *
 * ## 自动去重
 * 在连接时, [MessageScope] 会自动根据真实的 [收信对象][CommandSender.subject] 去重.
 *
 * 如 `member.asCommandSender().scopeWith(member.group)`,
 * 返回的 [MessageScope] 实际上只包含 `member.group`. 因为 `member.asCommandSender()` 的最终收信对象就是 `member.group`.
 *
 * 因此在使用 [scopeWith] 时, 无需考虑重复性, 只需要把希望发送的目标全部列入.
 *
 * ## 使用 [MessageScope]
 * 在 `scopeWith` 或 `scopeWithNotNull` 后加 `lambda` 参数即可表示使用 [MessageScope].
 * 如:
 * ```
 * A.scopeWith(B) { // this: MessageScope
 *     sendMessage(...)
 * }
 * ```
 *
 * ## 典例
 * 在处理指令时, 目标群对象可能与发件人群对象不同, 如用户在 A 群发指令, 以禁言 B 群的成员.
 * 此时机器人可能需要同时广播通知到 A 群和 B 群.
 *
 * 由于 [CommandSender] 与 [Contact] 无公共接口, 无法使用 [listOfNotNull] 遍历处理. [MessageScope] 就是设计为解决这样的问题.
 *
 * ```
 * // 在一个 CompositeCommand 内
 * @Handler
 * suspend fun CommandSender.handle(target: Member) {
 *     val duration = Random.nextInt(1, 15)
 *     target.mute(duration)
 *
 *     // 不使用 MessageScope, 无用的样板代码
 *     val thisGroup = this.getGroupOrNull()
 *     val message = "${this.name} 禁言 ${target.nameCardOrNick} $duration 秒"
 *     if (target.group != thisGroup) {
 *         target.group.sendMessage(message)
 *     }
 *     sendMessage(message)
 *
 *     // 使用 MessageScope, 清晰逻辑
 *     // 表示至少发送给 `this`, 当 `this` 的真实发信对象与 `target.group` 不同时, 还额外发送给 `target.group`
 *     this.scopeWithNotNull(target.group) {
 *         sendMessage("${name} 禁言了 ${target.nameCardOrNick} $duration 秒")
 *     }
 *
 *     // 同样地, 可以扩展用法, 同时私聊指令执行者:
 *     // this.scopeWithNotNull(
 *     //    target,
 *     //    target.group
 *     // ) { ... }
 * }
 * ```
 */
public interface MessageScope {
    /**
     * 如果此 [MessageScope], 仅包含一个消息对象, 则 [realTarget] 指向这个对象.
     *
     * 对于 [CommandSender] 作为 [MessageScope], [realTarget] 总是指令执行者 [User], 即 [CommandSender.user]
     *
     * [realTarget] 用于 [MessageScope.invoke] 时的去重.
     *
     * @suppress 此 API 不稳定, 可能在任何时间被修改
     */
    @ConsoleExperimentalAPI
    public val realTarget: Any?

    /**
     * 立刻以此发送消息给所有在此 [MessageScope] 下的消息对象
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message)

    /**
     * 立刻以此发送消息给所有在此 [MessageScope] 下的消息对象
     */
    @JvmDefault
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String)
}

/**
 * 使用 [MessageScope] 里的所有消息对象. 与 [kotlin.run] 相同.
 */
@JvmSynthetic
public inline operator fun <R, MS : MessageScope> MS.invoke(action: MS.() -> R): R = this.action()

///////////////////////////////////////////////////////////////////////////
// Builders
///////////////////////////////////////////////////////////////////////////

/*
 * 实现提示: 以下所有代码都通过 codegen 模块中 net.mamoe.mirai.console.codegen.MessageScopeCodegen 生成. 请不要手动修改它们.
 */

//// region MessageScopeBuilders CODEGEN ////

public fun Contact.asMessageScope(): MessageScope = createScopeDelegate(this)

public fun CommandSender.asMessageScope(): MessageScope = createScopeDelegate(this)

@LowPriorityInOverloadResolution
public fun Contact.scopeWith(vararg others: Contact): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun Contact.scopeWith(vararg others: CommandSender): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun Contact.scopeWith(vararg others: MessageScope): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender.scopeWith(vararg others: Contact): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender.scopeWith(vararg others: CommandSender): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender.scopeWith(vararg others: MessageScope): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope.scopeWith(vararg others: Contact): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope.scopeWith(vararg others: CommandSender): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope.scopeWith(vararg others: MessageScope): MessageScope {
    return others.fold(this.asMessageScope()) { acc, other -> CombinedScope(acc, other.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun Contact?.scopeWithNotNull(vararg others: Contact?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun Contact?.scopeWithNotNull(vararg others: CommandSender?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun Contact?.scopeWithNotNull(vararg others: MessageScope?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender?.scopeWithNotNull(vararg others: Contact?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender?.scopeWithNotNull(vararg others: CommandSender?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun CommandSender?.scopeWithNotNull(vararg others: MessageScope?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope?.scopeWithNotNull(vararg others: Contact?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope?.scopeWithNotNull(vararg others: CommandSender?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

@LowPriorityInOverloadResolution
public fun MessageScope?.scopeWithNotNull(vararg others: MessageScope?): MessageScope {
    return others.fold(this.asMessageScopeOrNoop()) { acc, other -> acc.scopeWithNotNull(other?.asMessageScope()) }
}

public fun Contact.scopeWith(other: Contact): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun Contact.scopeWith(other: CommandSender): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun Contact.scopeWith(other: MessageScope): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun CommandSender.scopeWith(other: Contact): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun CommandSender.scopeWith(other: CommandSender): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun CommandSender.scopeWith(other: MessageScope): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun MessageScope.scopeWith(other: Contact): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun MessageScope.scopeWith(other: CommandSender): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun MessageScope.scopeWith(other: MessageScope): MessageScope {
    return CombinedScope(asMessageScope(), other.asMessageScope())
}

public fun Contact?.scopeWithNotNull(other: Contact?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun Contact?.scopeWithNotNull(other: CommandSender?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun Contact?.scopeWithNotNull(other: MessageScope?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun CommandSender?.scopeWithNotNull(other: Contact?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun CommandSender?.scopeWithNotNull(other: CommandSender?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun CommandSender?.scopeWithNotNull(other: MessageScope?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun MessageScope?.scopeWithNotNull(other: Contact?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun MessageScope?.scopeWithNotNull(other: CommandSender?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public fun MessageScope?.scopeWithNotNull(other: MessageScope?): MessageScope {
    @Suppress("DuplicatedCode")
    return when {
        this == null && other == null -> NoopMessageScope
        this == null && other != null -> other.asMessageScope()
        this != null && other == null -> this.asMessageScope()
        this != null && other != null -> CombinedScope(asMessageScope(), other.asMessageScope())
        else -> null!!
    }
}

public inline fun <R> Contact.scopeWith(vararg others: Contact, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> Contact.scopeWith(vararg others: CommandSender, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> Contact.scopeWith(vararg others: MessageScope, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> CommandSender.scopeWith(vararg others: Contact, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> CommandSender.scopeWith(vararg others: CommandSender, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> CommandSender.scopeWith(vararg others: MessageScope, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> MessageScope.scopeWith(vararg others: Contact, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> MessageScope.scopeWith(vararg others: CommandSender, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> MessageScope.scopeWith(vararg others: MessageScope, action: MessageScope.() -> R): R {
    return scopeWith(*others).invoke(action)
}

public inline fun <R> Contact?.scopeWithNotNull(vararg others: Contact?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> Contact?.scopeWithNotNull(vararg others: CommandSender?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> Contact?.scopeWithNotNull(vararg others: MessageScope?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> CommandSender?.scopeWithNotNull(vararg others: Contact?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> CommandSender?.scopeWithNotNull(vararg others: CommandSender?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> CommandSender?.scopeWithNotNull(vararg others: MessageScope?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> MessageScope?.scopeWithNotNull(vararg others: Contact?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> MessageScope?.scopeWithNotNull(vararg others: CommandSender?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

public inline fun <R> MessageScope?.scopeWithNotNull(vararg others: MessageScope?, action: MessageScope.() -> R): R {
    return scopeWithNotNull(*others).invoke(action)
}

@Deprecated(
    "Senseless scopeWith. Use asMessageScope.",
    ReplaceWith("this.asMessageScope()", "net.mamoe.mirai.console.util.asMessageScope")
)
public inline fun Contact.scopeWith(): MessageScope = asMessageScope()

@Deprecated(
    "Senseless scopeWith. Use asMessageScope.",
    ReplaceWith("this.asMessageScope()", "net.mamoe.mirai.console.util.asMessageScope")
)
public inline fun CommandSender.scopeWith(): MessageScope = asMessageScope()

@Deprecated(
    "Senseless scopeWith. Use asMessageScope.",
    ReplaceWith("this.asMessageScope()", "net.mamoe.mirai.console.util.asMessageScope")
)
public inline fun MessageScope.scopeWith(): MessageScope = asMessageScope()

@Deprecated(
    "Senseless scopeWith. Use .asMessageScope().invoke.",
    ReplaceWith(
        "this.asMessageScope()(action)",
        "net.mamoe.mirai.console.util.asMessageScope",
        "net.mamoe.mirai.console.util.invoke"
    )
)
public inline fun <R> Contact.scopeWith(action: MessageScope.() -> R): R = asMessageScope()(action)

@Deprecated(
    "Senseless scopeWith. Use .asMessageScope().invoke.",
    ReplaceWith(
        "this.asMessageScope()(action)",
        "net.mamoe.mirai.console.util.asMessageScope",
        "net.mamoe.mirai.console.util.invoke"
    )
)
public inline fun <R> CommandSender.scopeWith(action: MessageScope.() -> R): R = asMessageScope()(action)

@Deprecated(
    "Senseless scopeWith. Use .asMessageScope().invoke.",
    ReplaceWith(
        "this.asMessageScope()(action)",
        "net.mamoe.mirai.console.util.asMessageScope",
        "net.mamoe.mirai.console.util.invoke"
    )
)
public inline fun <R> MessageScope.scopeWith(action: MessageScope.() -> R): R = asMessageScope()(action)

//// endregion MessageScopeBuilders CODEGEN ////

//// region IterableMessageScopeBuilders CODEGEN ////

@JvmName("toMessageScopeContactIterable")
public fun Iterable<Contact?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeCommandSenderIterable")
public fun Iterable<CommandSender?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeMessageScopeIterable")
public fun Iterable<MessageScope?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeContactSequence")
public fun Sequence<Contact?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeCommandSenderSequence")
public fun Sequence<CommandSender?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeMessageScopeSequence")
public fun Sequence<MessageScope?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeContactArray")
public fun Array<Contact?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeCommandSenderArray")
public fun Array<CommandSender?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmName("toMessageScopeMessageScopeArray")
public fun Array<MessageScope?>.toMessageScope(): MessageScope {
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScopeOrNoop()
        )
    }
}

@JvmSynthetic
@JvmName("toMessageScopeContactFlow")
public suspend fun Flow<Contact>.toMessageScope(): MessageScope { // Flow<Any?>.firstOrNull isn't yet supported
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScope()
        )
    }
}

@JvmSynthetic
@JvmName("toMessageScopeCommandSenderFlow")
public suspend fun Flow<CommandSender>.toMessageScope(): MessageScope { // Flow<Any?>.firstOrNull isn't yet supported
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScope()
        )
    }
}

@JvmSynthetic
@JvmName("toMessageScopeMessageScopeFlow")
public suspend fun Flow<MessageScope>.toMessageScope(): MessageScope { // Flow<Any?>.firstOrNull isn't yet supported
    return this.fold(this.firstOrNull().asMessageScopeOrNoop()) { acc, messageScope ->
        CombinedScope(
            acc,
            messageScope.asMessageScope()
        )
    }
}

//// endregion IterableMessageScopeBuilders CODEGEN ////

///////////////////////////////////////////////////////////////////////////
// Internals
///////////////////////////////////////////////////////////////////////////

// [MessageScope] 实现

@PublishedApi
@InlineOnly
internal inline fun MessageScope.asMessageScope(): MessageScope = this

@InlineOnly
private inline fun MessageScope?.asMessageScopeOrNoop(): MessageScope = this?.asMessageScope() ?: NoopMessageScope

@InlineOnly
private inline fun Contact?.asMessageScopeOrNoop(): MessageScope = this?.asMessageScope() ?: NoopMessageScope

@InlineOnly
private inline fun CommandSender?.asMessageScopeOrNoop(): MessageScope = this?.asMessageScope() ?: NoopMessageScope

@InlineOnly
private inline fun createScopeDelegate(o: CommandSender) = CommandSenderAsMessageScope(o)

@InlineOnly
private inline fun createScopeDelegate(o: Contact) = ContactAsMessageScope(o)

private fun MessageScope.asSequence(): Sequence<MessageScope> {
    return if (this is CombinedScope) {
        sequenceOf(this.first.asSequence(), this.second.asSequence()).flatten()
    } else sequenceOf(this)
}

private class CombinedScope(
    private val first: MessageScope,
    private val second: MessageScope
) : MessageScope {
    override val realTarget: Any? get() = null

    private val targets: List<MessageScope> by lazy {
        this.asSequence().distinctBy { it.realTarget }.toList()
    }

    override suspend fun sendMessage(message: Message) {
        for (target in targets) {
            target.sendMessage(message)
        }
    }

    override suspend fun sendMessage(message: String) {
        for (target in targets) {
            target.sendMessage(message)
        }
    }
}

private class CommandSenderAsMessageScope(
    private val sender: CommandSender
) : MessageScope {
    override val realTarget: Any?
        get() = sender.user ?: sender // ConsoleCommandSender

    override suspend fun sendMessage(message: Message) {
        sender.sendMessage(message)
    }

    override suspend fun sendMessage(message: String) {
        sender.sendMessage(message)
    }
}

private class ContactAsMessageScope(
    private val sender: Contact
) : MessageScope {
    override val realTarget: Any?
        get() = sender

    override suspend fun sendMessage(message: Message) {
        sender.sendMessage(message)
    }

    override suspend fun sendMessage(message: String) {
        sender.sendMessage(message)
    }
}

private object NoopMessageScope : MessageScope {
    override val realTarget: Any?
        get() = null

    override suspend fun sendMessage(message: Message) {
    }

    override suspend fun sendMessage(message: String) {
    }
}