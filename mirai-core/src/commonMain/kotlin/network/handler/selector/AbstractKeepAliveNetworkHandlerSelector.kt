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
import kotlinx.coroutines.yield
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.toLongUnsigned

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
        private val exceptionCollector: ExceptionCollector = ExceptionCollector()

        tailrec suspend fun run(): H {
            if (attempted >= maxAttempts) {
                throw IllegalStateException(
                    "Failed to resume instance. Maximum attempts reached.",
                    exceptionCollector.getLast()
                )
            }
            yield() // Avoid endless recursion.
            val current = getCurrentInstanceOrNull()
            return if (current != null) {
                when (val thisState = current.state) {
                    NetworkHandler.State.CLOSED -> {
                        if (this@AbstractKeepAliveNetworkHandlerSelector.current.compareAndSet(current, null)) {
                            // invalidate the instance and try again.

                            exceptionCollector.collectException(current.getLastFailure())
                        }
                        attempted += 1
                        run() // will create new instance.
                    }
                    NetworkHandler.State.CONNECTING,
                    NetworkHandler.State.INITIALIZED -> {
                        current.resumeConnection() // once finished, it should has been LOADING or OK
                        check(current.state != thisState) { "Internal error: State is still $thisState after successful resumeConnection." } // this should not happen.
                        return run() // does not count for an attempt.
                    }
                    NetworkHandler.State.LOADING -> {
                        return current
                    }
                    NetworkHandler.State.OK -> {
                        current.resumeConnection()
                        return current
                    }
                }
            } else {
                refreshInstance()
                run() // directly retry, does not count for attempts.
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