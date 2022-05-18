/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.event.EventChannelToEventDispatcherAdapter
import net.mamoe.mirai.internal.event.InternalEventMechanism
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmInline

/**
 * All events will be caught and forwarded to [EventDispatcher]. Invocation of [Event.broadcast] and [EventDispatcher.broadcast] are effectively equal.
 */
internal interface EventDispatcher {
    val isActive: Boolean

    /**
     * Broadcast an event using [EventChannel]. It's safe to use this function internally.
     */
    suspend fun broadcast(event: Event)

    fun broadcastAsync(event: Event, additionalContext: CoroutineContext = EmptyCoroutineContext): EventBroadcastJob

    fun broadcastAsync(
        additionalContext: CoroutineContext = EmptyCoroutineContext,
        event: suspend () -> Event?,
    ): EventBroadcastJob

    /**
     * Join all jobs. Joins also jobs launched during this call.
     */
    @TestOnly
    suspend fun joinBroadcast() {
        throw UnsupportedOperationException("joinBroadcast is only supported in TestEventDispatcherImpl")
    }

    companion object : ComponentKey<EventDispatcher>
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

    inline fun thenBroadcast(eventDispatcher: EventDispatcher, crossinline event: suspend () -> Event?) {
        eventDispatcher.broadcastAsync {
            job.join()
            event()
        }
    }
}

/**
 * If `true`, all event listeners runs directly in the broadcaster's thread until first suspension.
 *
 * If there is no suspension point in the listener, the coroutine executing [Event.broadcast] will not suspend,
 * so the thread before and after execution will be the same and no other code is being executed if there is only one thread.
 *
 * This is useful for tests to not depend on `delay`
 */
internal var EVENT_LAUNCH_UNDISPATCHED: Boolean by lateinitMutableProperty {
    systemProp("mirai.event.launch.undispatched", false)
}

internal val SHOW_VERBOSE_EVENT: Boolean by lazy { systemProp("mirai.event.show.verbose.events", false) }

internal open class EventDispatcherImpl(
    lifecycleContext: CoroutineContext,
    protected val logger: MiraiLogger,
) : EventDispatcher,
    CoroutineScope by lifecycleContext
        .addNameHierarchically("EventDispatcher")
        .childScope() {

    override val isActive: Boolean
        get() = this.coroutineContext.isActive

    @OptIn(InternalEventMechanism::class)
    override suspend fun broadcast(event: Event) {
        try {
            EventChannelToEventDispatcherAdapter.instance.broadcastEventImpl(event)
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
            additionalContext,
            start = CoroutineStart.UNDISPATCHED
        ) { broadcast(event) }
        // UNDISPATCHED: starts the coroutine NOW in the current thread until its first suspension point,
        // so that after `broadcastAsync` the job is always already started and `joinBroadcast` will work normally.
        return EventBroadcastJob(job)
    }

    override fun broadcastAsync(additionalContext: CoroutineContext, event: suspend () -> Event?): EventBroadcastJob {
        val job = launch(
            additionalContext,
            start = CoroutineStart.UNDISPATCHED
        ) {
            event()?.let { broadcast(it) }
        }
        // UNDISPATCHED: starts the coroutine NOW in the current thread until its first suspension point,
        // so that after `broadcastAsync` the job is always already started and `joinBroadcast` will work normally.
        return EventBroadcastJob(job)
    }

    protected fun optimizeEventToString(event: Event): String {
        val qualified = event::class.qualifiedName ?: return event.toString()
        return qualified.substringAfter("net.mamoe.mirai.event.events.", "").ifEmpty { event.toString() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // broadcast
    ///////////////////////////////////////////////////////////////////////////

}