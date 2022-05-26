/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

actual suspend inline fun <R> runBIO(noinline block: () -> R): R {
    return block()
}

actual suspend inline fun <T, R> T.runBIO(crossinline block: T.() -> R): R {
    TODO("Not yet implemented")
}

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
actual inline fun <reified E> Throwable.unwrap(): Throwable {
    if (this !is E) return this
    return this.findCause { it !is E }
        ?.also { it.addSuppressed(this) }
        ?: this
}