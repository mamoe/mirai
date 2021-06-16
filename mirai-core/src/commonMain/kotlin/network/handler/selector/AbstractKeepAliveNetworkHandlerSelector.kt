/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.toLongUnsigned
import net.mamoe.mirai.utils.unwrapCancellationException

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
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
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
                throw exceptionCollector.getLast() ?: MaxAttemptsReachedException(null)
            }
            yield() // Avoid endless recursion.
            val current = getCurrentInstanceOrNull()
            lastNetwork = current

            suspend fun H.resumeInstanceCatchingException() {
                try {
                    resumeConnection() // once finished, it should has been LOADING or OK
                } catch (e: Exception) {
                    close(e)
                    // exception will be collected by `exceptionCollector.collectException(current.getLastFailure())`
                    // so that duplicated exceptions are ignored in logging
                }
            }

            return if (current != null) {
                when (val thisState = current.state) {
                    NetworkHandler.State.CLOSED -> {
                        if (this@AbstractKeepAliveNetworkHandlerSelector.current.compareAndSet(current, null)) {
                            // invalidate the instance and try again.

                            val lastFailure = current.getLastFailure()?.unwrapCancellationException()
                            exceptionCollector.collectException(lastFailure)
                        }
                        if (attempted > 1) {
                            delay(3000) // make it slower to avoid massive reconnection on network failure.
                        }
                        attempted += 1
                        runImpl() // will create new instance (see the `else` branch).
                    }
                    NetworkHandler.State.CONNECTING,
                    NetworkHandler.State.INITIALIZED -> {
                        current.resumeInstanceCatchingException()
                        return runImpl() // does not count for an attempt.
                    }
                    NetworkHandler.State.LOADING -> {
                        return current
                    }
                    NetworkHandler.State.OK -> {
                        current.resumeInstanceCatchingException()
                        return current
                    }
                }
            } else {
                refreshInstance()
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
    }
}

@Suppress("FunctionName")
internal fun <H : NetworkHandler> KeepAliveNetworkHandlerSelector(
    maxAttempts: Int,
    createInstance: () -> H
): AbstractKeepAliveNetworkHandlerSelector<H> {
    return object : AbstractKeepAliveNetworkHandlerSelector<H>(maxAttempts) {
        override fun createInstance(): H = createInstance()
    }
}

@Suppress("FunctionName")
internal inline fun <H : NetworkHandler> KeepAliveNetworkHandlerSelector(crossinline createInstance: () -> H): AbstractKeepAliveNetworkHandlerSelector<H> {
    return object : AbstractKeepAliveNetworkHandlerSelector<H>() {
        override fun createInstance(): H = createInstance()
    }
}