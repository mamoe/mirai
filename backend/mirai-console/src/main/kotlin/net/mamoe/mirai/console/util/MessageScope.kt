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
import net.mamoe.mirai.message.data.Message
import kotlin.internal.InlineOnly
import kotlin.internal.LowPriorityInOverloadResolution

@ConsoleExperimentalAPI
public interface MessageScope {
    /**
     * 立刻发送一条消息.
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message)

    /**
     * 立刻发送一条消息.
     */
    @JvmDefault
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String)
}

@JvmSynthetic
public inline operator fun <R, MS : MessageScope> MS.invoke(action: MS.() -> R): R = this.action()

///////////////////////////////////////////////////////////////////////////
// Builders
///////////////////////////////////////////////////////////////////////////

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

// these three are for codegen

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

private class CombinedScope(
    private val first: MessageScope,
    private val second: MessageScope
) : MessageScope {
    override suspend fun sendMessage(message: Message) {
        first.sendMessage(message)
        second.sendMessage(message)
    }

    override suspend fun sendMessage(message: String) {
        first.sendMessage(message)
        second.sendMessage(message)
    }
}

private class CommandSenderAsMessageScope(
    private val sender: CommandSender
) : MessageScope {
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
    override suspend fun sendMessage(message: Message) {
        sender.sendMessage(message)
    }

    override suspend fun sendMessage(message: String) {
        sender.sendMessage(message)
    }
}

private object NoopMessageScope : MessageScope {
    override suspend fun sendMessage(message: Message) {
    }

    override suspend fun sendMessage(message: String) {
    }
}