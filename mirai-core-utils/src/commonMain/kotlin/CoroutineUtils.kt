/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


@file:JvmName("CoroutineUtilsKt_common")

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName

public expect suspend inline fun <R> runBIO(
    noinline block: () -> R,
): R

public expect suspend inline fun <T, R> T.runBIO(
    crossinline block: T.() -> R,
): R

public inline fun CoroutineScope.launchWithPermit(
    semaphore: Semaphore,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> Unit,
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
    default: () -> U,
): U = this[key] ?: default()

public inline fun <E : CoroutineContext.Element> CoroutineContext.addIfAbsent(
    key: CoroutineContext.Key<E>,
    default: () -> CoroutineContext.Element,
): CoroutineContext = if (this[key] == null) this + default() else this

public inline fun CoroutineContext.addNameIfAbsent(
    name: () -> String,
): CoroutineContext = addIfAbsent(CoroutineName) { CoroutineName(name()) }

public fun CoroutineContext.addNameHierarchically(
    name: String,
): CoroutineContext = this + CoroutineName(this[CoroutineName]?.name?.plus('.')?.plus(name) ?: name)

public fun CoroutineContext.hierarchicalName(
    name: String,
): CoroutineName = CoroutineName(this[CoroutineName]?.name?.plus('.')?.plus(name) ?: name)

public fun CoroutineScope.hierarchicalName(
    name: String,
): CoroutineName = this.coroutineContext.hierarchicalName(name)

public fun CoroutineContext.newCoroutineContextWithSupervisorJob(name: String? = null): CoroutineContext =
    this + CoroutineName(name ?: "<unnamed>") + SupervisorJob(this[Job])

public fun CoroutineScope.childScope(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope =
    CoroutineScope(this.childScopeContext(name, context))

public fun CoroutineContext.childScope(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope =
    CoroutineScope(this.childScopeContext(name, context))

public fun CoroutineScope.childScopeContext(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineContext =
    this.coroutineContext.childScopeContext(name, context)

public fun CoroutineContext.childScopeContext(
    name: String? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineContext =
    this.newCoroutineContextWithSupervisorJob(name) + context.let {
        if (name != null) it + CoroutineName(name)
        else it
    }

public fun Throwable.unwrapCancellationException(addSuppressed: Boolean = true): Throwable =
    unwrap<CancellationException>(addSuppressed)

/**
 * For code
 * ```
 * try {
 *   job(new)
 * } catch (e: Throwable) {
 *   throw IllegalStateException("Exception in attached Job '$name'", e.unwrapCancellationException())
 * }
 * ```
 *
 * Original stacktrace, you mainly see `StateSwitchingException` which is useless to locate the code where real cause `ForceOfflineException` is thrown.
 * ```
 * Exception in thread "DefaultDispatcher-worker-1 @BotInitProcessor.init#7" java.lang.IllegalStateException: Exception in attached Job 'BotInitProcessor.init'
 *   at net.mamoe.mirai.internal.network.handler.state.JobAttachStateObserver$stateChanged0$1.invokeSuspend(JobAttachStateObserver.kt:40)
 *   at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
 *   at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:104)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)
 * Caused by: StateSwitchingException(old=StateLoading, new=StateClosed, cause=net.mamoe.mirai.internal.network.impl.netty.ForceOfflineException: Closed by MessageSvc.PushForceOffline: net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline@4abf6d30)
 *   at net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport.setStateImpl$mirai_core(NetworkHandlerSupport.kt:258)
 *   at net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler.close(NettyNetworkHandler.kt:404)
 * ```
 *
 * Real stacktrace (with [unwrapCancellationException]), you directly have `ForceOfflineException`, also you wont lose information of `StateSwitchingException`
 * ```
 * Exception in thread "DefaultDispatcher-worker-2 @BotInitProcessor.init#7" java.lang.IllegalStateException: Exception in attached Job 'BotInitProcessor.init'
 *   at net.mamoe.mirai.internal.network.handler.state.JobAttachStateObserver$stateChanged0$1.invokeSuspend(JobAttachStateObserver.kt:40)
 *   at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
 *   at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:104)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
 *   at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)
 * Caused by: net.mamoe.mirai.internal.network.impl.netty.ForceOfflineException: Closed by MessageSvc.PushForceOffline: net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline@62f65f94
 *   at net.mamoe.mirai.utils.MiraiUtils__CoroutineUtilsKt.unwrapCancellationException(CoroutineUtils.kt:141)
 *   at net.mamoe.mirai.utils.MiraiUtils.unwrapCancellationException(Unknown Source)
 *   ... 7 more
 *   Suppressed: StateSwitchingException(old=StateLoading, new=StateClosed, cause=net.mamoe.mirai.internal.network.impl.netty.ForceOfflineException: Closed by MessageSvc.PushForceOffline: net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline@62f65f94)
 *     at net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport.setStateImpl$mirai_core(NetworkHandlerSupport.kt:258)
 *     at net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler.close(NettyNetworkHandler.kt:404)
 * ```
 */
@Suppress("unused")
public expect inline fun <reified E> Throwable.unwrap(addSuppressed: Boolean = true): Throwable

public val CoroutineContext.coroutineName: String get() = this[CoroutineName]?.name ?: "unnamed"