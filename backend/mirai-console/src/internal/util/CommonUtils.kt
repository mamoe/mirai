/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("CommonUtils") // maintain binary compatibility

package net.mamoe.mirai.console.internal.util

import io.github.karlatemp.caller.StackFrame
import net.mamoe.mirai.console.internal.plugin.BuiltInJvmPluginLoaderImpl
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
        BuiltInJvmPluginLoaderImpl.classLoaders.firstOrNull { it.findClass(className, true) != null }
    }.getOrNull()
}

@PublishedApi
internal inline fun assertionError(message: () -> String): Nothing {
    contract { callsInPlace(message, InvocationKind.EXACTLY_ONCE) }
    throw AssertionError(message())
}