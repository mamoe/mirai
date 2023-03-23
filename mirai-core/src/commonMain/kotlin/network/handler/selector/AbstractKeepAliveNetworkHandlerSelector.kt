/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import io.ktor.client.utils.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.utils.*
import kotlin.jvm.Volatile

/**
 * A lazy stateful implementation of [NetworkHandlerSelector].
 *
 * - Calls [factory.create][NetworkHandlerFactory.create] to create [NetworkHandler]s.
 * - Re-initialize [NetworkHandler] instances if the old one is dead.
 * - Suspends requests when connection is not available.
 *
 * No connection is created until first invocation of [getCurrentInstanceOrNull],
 * and new connections are created only when calling [getCurrentInstanceOrNull] if the old connection was dead.
 */
// may be replaced with a better name.
internal abstract class AbstractKeepAliveNetworkHandlerSelector<H : NetworkHandler>(
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    private val logger: MiraiLogger,
) : NetworkHandlerSelector<H> {

    init {
        require(maxAttempts >= 1) { "maxAttempts must >= 1" }
    }

    private val current = atomic<H?>(null)

    @net.mamoe.mirai.utils.TestOnly
    internal fun setCurrent(h: H) {
        current.value = h
    }

    protected abstract fun createInstance(): H

    final override fun getCurrentInstanceOrNull(): H? = current.value

    final override tailrec fun getCurrentInstanceOrCreate(): H {
        getCurrentInstanceOrNull()?.let { return it }
        refreshInstance()
        return getCurrentInstanceOrCreate()
    }

    final override suspend fun awaitResumeInstance(): H = AwaitResumeInstance().run()

    private inner class AwaitResumeInstance : SynchronizedObject() {
        private inline fun logIfEnabled(block: () -> String) {
            if (SELECTOR_LOGGING) {
                logger.debug { "Attempt #$attempted: ${block.invoke()}" }
            }
        }

        private var attempted: Int = 0
        private var lastNetwork: H? = null

        @Volatile
        private var _exceptionCollector: ExceptionCollector? = null

        // lazily initialize ExceptionCollector.
        private val exceptionCollector: ExceptionCollector
            get() {
                _exceptionCollector?.let { return it }
                return synchronized(this) {
                    _exceptionCollector?.let { return it }
                    object : ExceptionCollector() {
                        override fun beforeCollect(throwable: Throwable) {
                            lastNetwork?.logger?.warning("Exception in resumeConnection.", throwable)
                        }
                    }.also {
                        _exceptionCollector = it
                    }
                }
            }

        /**
         * 尝试重置状态, 捕获并处理发生的可被挽救的异常, 重新抛出其他异常. 每次此函数运行时都会创建新的 [H].
         * 抛出异常: 表示 [NetworkHandler.resumeConnection] 抛出了某个异常, 且这个问题不可挽救 (需要停止 selector).
         *
         * 只有 [NetworkException] 是期望的异常 (根据 [NetworkException.recoverable] 决定是否可挽救). 任何其他异常都是未期望的, 将会被原封不动地抛出.
         */
        @Throws(
            NetworkException::class, MaxAttemptsReachedException::class, CancellationException::class, Throwable::class
        )
        suspend fun run(): H {
            return try {
                runImpl()
            } finally {
                exceptionCollector.dispose()
                lastNetwork = null // help gc
            }
        }

        private tailrec suspend fun runImpl(): H {
            if (attempted >= maxAttempts) {
                logIfEnabled { "Max attempt $maxAttempts reached." }
                throw MaxAttemptsReachedException(exceptionCollector.getLast())
            }
            if (!currentCoroutineContext().isActive) {
                yield() // check cancellation
            }
            val current = getCurrentInstanceOrNull()
            lastNetwork = current

            if (current != null) {
                if (current.context[SsoProcessor].firstLoginResult?.canRecoverOnFirstLogin == false) {
                    // == null 只表示
                    // == false 表示第一次登录失败, 且此失败没必要重试
                    logIfEnabled { "[FIRST LOGIN ERROR] current = $current" }
                    logIfEnabled { "[FIRST LOGIN ERROR] current.state = ${current.state}" }
                    throw current.getLastFailure() ?: exceptionCollector.getLast()
                    ?: error("Failed to login with unknown reason.")
                }
            }

            /**
             * 执行 [NetworkHandler.resumeConnection].
             *
             * 本函数有三个终止状态:
             * - 返回 `true`: 表示 [NetworkHandler.resumeConnection] 正常返回.
             * - 返回 `false`: 表示 [NetworkHandler.resumeConnection] 抛出了某个异常, 但这个问题可以挽救 (需要重置状态).
             * - 抛出异常: 表示 [NetworkHandler.resumeConnection] 抛出了某个异常, 且这个问题不可挽救 (需要停止 selector).
             *
             * 只有 [NetworkException] 是期望的异常 (根据 [NetworkException.recoverable] 决定是否可挽救). 任何其他异常都是未期望的, 将会被原封不动地抛出.
             */
            @Throws(NetworkException::class, CancellationException::class, Throwable::class)
            suspend fun H.resumeInstanceCatchingException(): Boolean {
                logIfEnabled { "Try resumeConnection" }
                try {
                    resumeConnection()
                    return true // 一切正常
                } catch (e: NetworkException) {
                    logIfEnabled { "... failed with NetworkException (recoverable=${e.recoverable}): $e" }
                    exceptionCollector.collect(e)
                    close(e) // close H

                    logIfEnabled { "CLOSED instance" }
                    if (e.recoverable) {
                        // 可挽救
                        return false
                    } else {
                        // 不可挽救
                        exceptionCollector.throwLast()
                    }
                } catch (e: TimeoutCancellationException) {
                    logIfEnabled { "... failed with TimeoutCancellationException: $e" }
                    exceptionCollector.collect(e)
                    close(e) // close H
                    logIfEnabled { "CLOSED instance" }
                    // 发包超时 可挽救
                    return false
                } catch (e: Throwable) {
                    // 不可挽救
                    logIfEnabled { "... failed with unexpected: $e" }
                    exceptionCollector.collect(e)
                    close(e) // close H
                    logIfEnabled { "CLOSED instance" }
                    exceptionCollector.throwLast()
                }
            }

            logIfEnabled { "current.state = ${current?.state}" }
            return if (current != null) {
                when (current.state) {
                    NetworkHandler.State.CLOSED -> {
                        current.resumeInstanceCatchingException()
                        // This may return false, meaning the error causing the state to be CLOSED is recoverable.
                        // Otherwise, it throws, meaning it is unrecoverable.

                        if (compareAndSetCurrent(current, null)) {
                            logIfEnabled { "... Set current to null." }
                            // invalidate the instance and try again.

                            // NetworkHandler is CancellationException if closed by `NetworkHandler.close`
                            // `unwrapCancellationException` will give a rather long and complicated trace showing all internal traces.
                            // The traces may help locate exactly where the `NetworkHandler.close` is called,
                            // but this is not usually desired.
                            val lastFailure =
                                current.getLastFailure()?.unwrapCancellationException(NetworkHandler.CANCELLATION_TRACE)

                            logIfEnabled { "...    Last failure was $lastFailure." }
                            exceptionCollector.collectException(lastFailure)
                        }
                        if (attempted > 1) {
                            logIfEnabled { "... Delaying ${RECONNECT_DELAY.millisToHumanReadableString()}." }
                            delay(RECONNECT_DELAY) // make it slower to avoid massive reconnection on network failure.
                        }
                        attempted += 1
                        runImpl() // will create new instance (see the `else` branch).
                    }
                    NetworkHandler.State.INITIALIZED -> {
                        if (!current.resumeInstanceCatchingException()) {
                            attempted += 1
                            return runImpl()
                        }
                        logIfEnabled { "RETURN" }
                        return current
                    }
                    NetworkHandler.State.CONNECTING -> {
                        logIfEnabled { "RETURN" }
                        // can send packet
                        return current
                    }
                    NetworkHandler.State.LOADING -> {
                        logIfEnabled { "RETURN" }
                        return current
                    }
                    NetworkHandler.State.OK -> {
                        if (current.resumeInstanceCatchingException()) {
                            logIfEnabled { "RETURN" }
                            return current
                        } else {
                            attempted += 1
                            return runImpl()
                        }
                    }
                }
            } else {
                logIfEnabled { "Creating new instance." }
                refreshInstance()
                logIfEnabled { "... Created." }
                runImpl() // directly retry, does not count for attempts.
            }
        }
    }

    private fun compareAndSetCurrent(expect: H?, update: H?) =
        current.compareAndSet(expect, update) // to enable compiler optimization

    private val lock = SynchronizedObject()
    protected open fun refreshInstance() {
        synchronized(lock) { // avoid concurrent `createInstance()`
            if (getCurrentInstanceOrNull() == null) this.current.compareAndSet(null, createInstance())
        }
    }


    companion object {
        var DEFAULT_MAX_ATTEMPTS by atomic(
            systemProp(
                "mirai.network.handler.selector.max.attempts", Long.MAX_VALUE
            ).coerceIn(1..Int.MAX_VALUE.toLongUnsigned()).toInt()
        )

        /**
         * millis
         */
        var RECONNECT_DELAY by atomic(systemProp("mirai.network.reconnect.delay", 3000L).coerceIn(0..Long.MAX_VALUE))

        var SELECTOR_LOGGING by atomic(systemProp("mirai.network.handler.selector.logging", false))
    }
}

@Suppress("FunctionName")
internal fun <H : NetworkHandler> KeepAliveNetworkHandlerSelector(
    maxAttempts: Int,
    logger: MiraiLogger,
    createInstance: () -> H,
): AbstractKeepAliveNetworkHandlerSelector<H> {
    return object : AbstractKeepAliveNetworkHandlerSelector<H>(maxAttempts, logger) {
        override fun createInstance(): H = createInstance()
    }
}

@Suppress("FunctionName")
internal inline fun <H : NetworkHandler> KeepAliveNetworkHandlerSelector(
    logger: MiraiLogger,
    crossinline createInstance: () -> H,
): AbstractKeepAliveNetworkHandlerSelector<H> {
    return object : AbstractKeepAliveNetworkHandlerSelector<H>(logger = logger) {
        override fun createInstance(): H = createInstance()
    }
}
