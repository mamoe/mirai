/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("CommonUtils") // maintain binary compatibility

package net.mamoe.mirai.console.internal.util

import io.github.karlatemp.caller.StackFrame
import net.mamoe.mirai.console.internal.plugin.implOrNull
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <reified E : Throwable, R> runIgnoreException(block: () -> R): R? {
    try {
        return block()
    } catch (e: Throwable) {
        if (e is E) return null
        throw e
    }
}

internal inline fun <reified E : Throwable> runIgnoreException(block: () -> Unit): Unit? {
    try {
        return block()
    } catch (e: Throwable) {
        if (e is E) return null
        throw e
    }
}

internal fun StackFrame.findLoader(): ClassLoader? {
    classInstance?.let { return it.classLoader }
    return runCatching {
        JvmPluginLoader.implOrNull?.findLoadedClass(className)?.classLoader
    }.getOrNull()
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "UnusedParameter")
@kotlin.internal.LowPriorityInOverloadResolution
internal inline fun <T : Any> T?.ifNull(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return this ?: block()
}

@Suppress("DeprecatedCallableAddReplaceWith", "UnusedParameter", "UNUSED_PARAMETER")
@Deprecated("Useless ifNull on not null value.") // diagnostic deprecation
@JvmName("ifNull1")
internal inline fun <T : Any> T.ifNull(block: () -> T): T = this

@PublishedApi
internal inline fun assertionError(message: () -> String = { "Reached an unexpected branch." }): Nothing {
    contract { callsInPlace(message, InvocationKind.EXACTLY_ONCE) }
    throw AssertionError(message())
}

@PublishedApi
internal inline fun assertUnreachable(message: () -> String = { "Reached an unexpected branch." }): Nothing {
    contract { callsInPlace(message, InvocationKind.EXACTLY_ONCE) }
    throw AssertionError(message())
}

@MarkerUnreachableClause
@PublishedApi
internal inline val UNREACHABLE_CLAUSE: Nothing
    get() = assertUnreachable()

@DslMarker
private annotation class MarkerUnreachableClause