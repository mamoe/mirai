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
import org.jetbrains.annotations.TestOnly

/**
 * A lazy stateful implementation of [NetworkHandlerSelector].
 *
 * - Calls [factory.create][NetworkHandlerFactory.create] to create [NetworkHandler]s.
 * - Re-initialize [NetworkHandler] instances if the old one is dead.
 * - Suspends requests when connection is not available.
 *
 * No connection is created until first invocation of [getResumedInstance],
 * and new connections are created only when calling [getResumedInstance] if the old connection was dead.
 */
// may be replaced with a better name.
internal abstract class AbstractKeepAliveNetworkHandlerSelector<H : NetworkHandler> : NetworkHandlerSelector<H> {
    private val current = atomic<H?>(null)

    @TestOnly
    internal fun setCurrent(h: H) {
        current.value = h
    }

    protected abstract fun createInstance(): H

    final override fun getResumedInstance(): H? = current.value

    final override tailrec suspend fun awaitResumeInstance(): H { // TODO: 2021/4/18 max 5 retry
        yield()
        val current = getResumedInstance()
        return if (current != null) {
            when (current.state) {
                NetworkHandler.State.CLOSED -> {
                    this.current.compareAndSet(current, null) // invalidate the instance and try again.
                    awaitResumeInstance() // will create new instance.
                }
                NetworkHandler.State.CONNECTION_LOST,
                NetworkHandler.State.CONNECTING,
                NetworkHandler.State.INITIALIZED -> {
                    current.resumeConnection()
                    return awaitResumeInstance()
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
            this.current.compareAndSet(current, createInstance())
            awaitResumeInstance()
        }
    }
}