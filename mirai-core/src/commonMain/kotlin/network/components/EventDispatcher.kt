/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.addNameHierarchically
import net.mamoe.mirai.utils.childScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * All events will be caught and forwarded to [EventDispatcher]. Invocation of [Event.broadcast] and [EventDispatcher.broadcast] are effectively equal.
 */
internal interface EventDispatcher {
    /**
     * Implementor must call `event.broadcast()` within a coroutine with [EventDispatcherScopeFlag]
     */
    suspend fun broadcast(event: Event)

    /**
     * Implementor must call `event.broadcast()` within a coroutine with [EventDispatcherScopeFlag]
     */
    fun broadcastAsync(event: Event, additionalContext: CoroutineContext = EmptyCoroutineContext): EventBroadcastJob

    /**
     * Join all jobs. Joins also jobs launched during this call.
     */
    @TestOnly
    suspend fun joinBroadcast() {
        throw UnsupportedOperationException("joinBroadcast is only supported in TestEventDispatcherImpl")
    }

    companion object : ComponentKey<EventDispatcher>
}

internal object EventDispatcherScopeFlag : CoroutineContext.Element, CoroutineContext.Key<EventDispatcherScopeFlag> {
    override val key: CoroutineContext.Key<*> get() = this
}

@JvmInline
internal value class EventBroadcastJob(
    val job: Job
) {
    inline fun onSuccess(crossinline action: () -> Unit) {
        job.invokeOnCompletion {
            if (it == null) action()
        }
    }
}


internal open class EventDispatcherImpl(
    lifecycleContext: CoroutineContext,
    protected val logger: MiraiLogger,
) : EventDispatcher,
    CoroutineScope by lifecycleContext
        .addNameHierarchically("EventDispatcher")
        .childScope() {

    override suspend fun broadcast(event: Event) {
        try {
            withContext(EventDispatcherScopeFlag) {
                event.broadcast()
            }
        } catch (e: Exception) {
            if (e is CancellationException) return
            if (logger.isEnabled) {
                val msg = optimizeEventToString(event)
                logger.error(IllegalStateException("Exception while broadcasting event '$msg'", e))
            }
        }
    }

    override fun broadcastAsync(event: Event, additionalContext: CoroutineContext): EventBroadcastJob {
        val job = launch(
            additionalContext + EventDispatcherScopeFlag,
            start = CoroutineStart.UNDISPATCHED
        ) { broadcast(event) }
        // UNDISPATCHED: starts the coroutine NOW in the current thread until its first suspension point,
        // so that after `broadcastAsync` the job is always already started and `joinBroadcast` will work normally.
        return EventBroadcastJob(job)
    }

    protected fun optimizeEventToString(event: Event): String {
        val qualified = event::class.java.canonicalName ?: return event.toString()
        return qualified.substringAfter("net.mamoe.mirai.event.events.", "").ifEmpty { event.toString() }
    }
}