/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.MiraiLogger
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass


/**
 * 事件处理器.
 */
internal class Handler<in E : Event> internal constructor(
    parentJob: Job?,
    subscriberContext: CoroutineContext,
    @JvmField val handler: suspend (E) -> ListeningStatus,
    override val concurrencyKind: ConcurrencyKind,
    override val priority: EventPriority
) : Listener<E>, CompletableJob by SupervisorJob(parentJob) { // avoid being cancelled on handling event

    private val subscriberContext: CoroutineContext = subscriberContext + this // override Job.

    val lock: Mutex? = when (concurrencyKind) {
        ConcurrencyKind.LOCKED -> Mutex()
        else -> null
    }

    @Suppress("unused")
    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            // Inherit context.
            withContext(subscriberContext) { handler.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            subscriberContext[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: currentCoroutineContext()[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: kotlin.run {
                    val logger = if (event is BotEvent) event.bot.logger else MiraiLogger.TopLevel
                    val subscriberName = subscriberContext[CoroutineName]?.name ?: "<unnamed>"
                    val broadcasterName = currentCoroutineContext()[CoroutineName]?.name ?: "<unnamed>"
                    val message =
                        "An exception occurred when processing event. " +
                                "Subscriber scope: '$subscriberName'. " +
                                "Broadcaster scope: '$broadcasterName'"
                    logger.warning(message, e)
                }
            // this.complete() // do not `completeExceptionally`, otherwise parentJob will fai`l.
            // ListeningStatus.STOPPED

            // not stopping listening.
            ListeningStatus.LISTENING
        }
    }
}

internal class ListenerRegistry(
    val listener: Listener<Event>,
    val type: KClass<out Event>
)


internal object GlobalEventListeners {
    private val ALL_LEVEL_REGISTRIES: Map<EventPriority, ConcurrentLinkedQueue<ListenerRegistry>>

    fun clear() {
        ALL_LEVEL_REGISTRIES.forEach { (_, u) ->
            u.clear()
        }
    }

    init {
        val map =
            EnumMap<EventPriority, ConcurrentLinkedQueue<ListenerRegistry>>(EventPriority::class.java)
        EventPriority.values().forEach {
            map[it] = ConcurrentLinkedQueue()
        }
        ALL_LEVEL_REGISTRIES = map
    }

    operator fun get(priority: EventPriority): ConcurrentLinkedQueue<ListenerRegistry> =
        ALL_LEVEL_REGISTRIES[priority]!!
}


internal suspend fun <E : AbstractEvent> callAndRemoveIfRequired(event: E) {
    for (p in EventPriority.prioritiesExcludedMonitor) {
        val container = GlobalEventListeners[p]
        for (registry in container) {
            if (event.isIntercepted) return
            if (!registry.type.isInstance(event)) continue
            val listener = registry.listener
            process(container, registry, listener, event)
        }
    }

    if (event.isIntercepted) return
    val container = GlobalEventListeners[EventPriority.MONITOR]
    when (container.size) {
        0 -> return
        1 -> {
            val registry = container.firstOrNull() ?: return
            if (!registry.type.isInstance(event)) return
            process(container, registry, registry.listener, event)
        }
        else -> supervisorScope {
            for (registry in GlobalEventListeners[EventPriority.MONITOR]) {
                if (!registry.type.isInstance(event)) continue
                launch { process(container, registry, registry.listener, event) }
            }
        }
    }
}

private suspend fun <E : AbstractEvent> process(
    container: ConcurrentLinkedQueue<ListenerRegistry>,
    registry: ListenerRegistry,
    listener: Listener<Event>,
    event: E,
) {
    when (listener.concurrencyKind) {
        ConcurrencyKind.LOCKED -> {
            (listener as Handler).lock!!.withLock {
                if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                    container.remove(registry)
                }
            }
        }
        ConcurrencyKind.CONCURRENT -> {
            if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                container.remove(registry)
            }
        }
    }
}