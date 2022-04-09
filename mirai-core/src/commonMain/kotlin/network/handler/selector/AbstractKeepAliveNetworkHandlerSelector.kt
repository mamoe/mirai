/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.RetryLaterException
import net.mamoe.mirai.utils.*

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

    private inner class AwaitResumeInstance {
        private inline fun logIfEnabled(block: () -> String) {
            if (SELECTOR_LOGGING) {
                logger.debug { "Attempt #$attempted: ${block.invoke()}" }
            }
        }

        private var attempted: Int = 0
        private var lastNetwork: H? = null
        private val exceptionCollector: ExceptionCollector = object : ExceptionCollector() {
            override fun beforeCollect(throwable: Throwable) {
                lastNetwork?.logger?.warning(throwable)
            }
        }

        suspend fun run(): H {
            return runImpl().also {
                exceptionCollector.dispose()
                lastNetwork = null // help gc
            }
        }

        private tailrec suspend fun runImpl(): H {
            if (attempted >= maxAttempts) {
                logIfEnabled { "Max attempt $maxAttempts reached." }
                throw MaxAttemptsReachedException(exceptionCollector.getLast())
//                throw exceptionCollector.getLast()?.apply { addSuppressed(MaxAttemptsReachedException(null)) }
//                    ?: MaxAttemptsReachedException(null)
            }
            if (!currentCoroutineContext().isActive) {
                yield() // check cancellation
            }
            val current = getCurrentInstanceOrNull()
            lastNetwork = current

            if (current != null) {
                if (current.context[SsoProcessor].firstLoginResult.value?.canRecoverOnFirstLogin == false) {
                    // == null 只表示
                    // == false 表示第一次登录失败, 且此失败没必要重试
                    logIfEnabled { "[FIRST LOGIN ERROR] current = $current" }
                    logIfEnabled { "[FIRST LOGIN ERROR] current.state = ${current.state}" }
                    throw current.getLastFailure() ?: exceptionCollector.getLast() ?: error("Failed to login with unknown reason.")
                }
            }

            /**
             * @return `false` if failed
             */
            suspend fun H.resumeInstanceCatchingException(): Boolean {
                logIfEnabled { "Try resumeConnection" }
                try {
                    resumeConnection() // once finished, it should has been LOADING or OK
                    return true
                } catch (e: LoginFailedException) {
                    logIfEnabled { "... failed with LoginFailedException $e" }
                    logIfEnabled { "CLOSING SELECTOR" }
                    close(e)
                    logIfEnabled { "... CLOSED" }
                    exceptionCollector.collect(e)
                    if (e is RetryLaterException) {
                        return false
                    }
                    // LoginFailedException is not resumable
                    exceptionCollector.throwLast()
                } catch (e: NetworkException) {
                    logIfEnabled { "... failed with NetworkException $e" }
                    logIfEnabled { "... recoverable=${e.recoverable}" }
                    exceptionCollector.collect(e)
                    if (e.recoverable) {
                        logIfEnabled { "IGNORING" }
                        // allow recoverable NetworkException, see #1361
                    } else {
                        logIfEnabled { "CLOSING SELECTOR" }
                        close(e)
                        logIfEnabled { "... CLOSED" }
                    }
                    return false
                } catch (e: Exception) {
                    logIfEnabled { "... failed with $e" }
                    logIfEnabled { "CLOSING SELECTOR" }
                    close(e)
                    logIfEnabled { "... CLOSED" }
                    // exception will be collected by `exceptionCollector.collectException(current.getLastFailure())`
                    // so that duplicated exceptions are ignored in logging
                    return false
                }
            }

            logIfEnabled { "current.state = ${current?.state}" }
            return if (current != null) {
                when (current.state) {
                    NetworkHandler.State.CLOSED -> {
                        if (this@AbstractKeepAliveNetworkHandlerSelector.current.compareAndSet(current, null)) {
                            logIfEnabled { "... Set current to null." }
                            // invalidate the instance and try again.

                            val lastFailure = current.getLastFailure()?.unwrapCancellationException()
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
                    NetworkHandler.State.CONNECTING,
                    NetworkHandler.State.INITIALIZED,
                    -> {
                        if (!current.resumeInstanceCatchingException()) {
                            attempted += 1
                        }
                        return runImpl()
                    }
                    NetworkHandler.State.LOADING -> {
                        logIfEnabled { "RETURN" }
                        return current
                    }
                    NetworkHandler.State.OK -> {
                        current.resumeInstanceCatchingException()
                        logIfEnabled { "RETURN" }
                        return current
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

    protected open fun refreshInstance() {
        synchronized(this) { // avoid concurrent `createInstance()`
            if (getCurrentInstanceOrNull() == null) this.current.compareAndSet(null, createInstance())
        }
    }

    companion object {
        @JvmField
        var DEFAULT_MAX_ATTEMPTS =
            systemProp("mirai.network.handler.selector.max.attempts", Long.MAX_VALUE)
                .coerceIn(1..Int.MAX_VALUE.toLongUnsigned()).toInt()

        /**
         * millis
         */
        @JvmField
        var RECONNECT_DELAY = systemProp("mirai.network.reconnect.delay", 3000L)
            .coerceIn(0..Long.MAX_VALUE)

        @JvmField
        var SELECTOR_LOGGING = systemProp("mirai.network.handle.selector.logging", false)
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
