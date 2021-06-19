/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "DeprecatedCallableAddReplaceWith")
@Deprecated(
    message = "Use runBIO which delegates to `runInterruptible`. " +
            "Technically remove suspend call in `block` and remove CoroutineScope parameter usages.",
    level = DeprecationLevel.WARNING
)
@kotlin.internal.LowPriorityInOverloadResolution
public suspend inline fun <R> runBIO(
    noinline block: suspend CoroutineScope.() -> R
): R = withContext(Dispatchers.IO, block)

public suspend inline fun <R> runBIO(
    noinline block: () -> R
): R = runInterruptible(context = Dispatchers.IO, block = block)

public suspend inline fun <T, R> T.runBIO(
    crossinline block: T.() -> R
): R = runInterruptible(context = Dispatchers.IO, block = { block() })

public inline fun CoroutineScope.launchWithPermit(
    semaphore: Semaphore,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit
): Job {
    return launch(coroutineContext) {
        semaphore.withPermit { block() }
    }
}

/**
 * Creates a child scope of the receiver scope.
 */
public fun CoroutineScope.childScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope = this.coroutineContext.childScope(coroutineContext)

/**
 * Creates a child scope of the receiver context scope.
 */
public fun CoroutineContext.childScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope = CoroutineScope(this.childScopeContext(coroutineContext))

/**
 * Creates a child scope of the receiver context scope.
 */
public fun CoroutineContext.childScopeContext(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): CoroutineContext {
    val ctx = this + coroutineContext
    val job = ctx[Job] ?: return ctx + SupervisorJob()
    return ctx + SupervisorJob(job)
}

public inline fun <E : U, U : CoroutineContext.Element> CoroutineContext.getOrElse(
    key: CoroutineContext.Key<E>,
    default: () -> U
): U = this[key] ?: default()

public inline fun <E : CoroutineContext.Element> CoroutineContext.addIfAbsent(
    key: CoroutineContext.Key<E>,
    default: () -> CoroutineContext.Element
): CoroutineContext = if (this[key] == null) this + default() else this

public inline fun CoroutineContext.addNameIfAbsent(
    name: () -> String
): CoroutineContext = addIfAbsent(CoroutineName) { CoroutineName(name()) }

public fun CoroutineContext.addNameHierarchically(
    name: String
): CoroutineContext = this + CoroutineName(this[CoroutineName]?.name?.plus('.')?.plus(name) ?: name)

public fun CoroutineContext.hierarchicalName(
    name: String
): CoroutineName = CoroutineName(this[CoroutineName]?.name?.plus('.')?.plus(name) ?: name)

public fun CoroutineScope.hierarchicalName(
    name: String
): CoroutineName = this.coroutineContext.hierarchicalName(name)

public inline fun <R> runUnwrapCancellationException(block: () -> R): R {
    try {
        return block()
    } catch (e: CancellationException) {
        // e is like `Exception in thread "main" kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=JobImpl{Cancelled}@f252f300`
        // and this is useless.
        if (e.suppressedExceptions.isNotEmpty()) throw e // preserve details.
        throw e.findCause { it !is CancellationException } ?: e
    }
}

public fun Throwable.unwrapCancellationException(): Throwable = unwrap<CancellationException>()

public inline fun <reified E> Throwable.unwrap(): Throwable {
    if (this !is E) return this
    if (suppressedExceptions.isNotEmpty()) return this
    return this.findCause { it !is E } ?: this
}