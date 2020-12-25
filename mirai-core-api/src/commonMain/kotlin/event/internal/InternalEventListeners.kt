/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.MiraiLogger
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass


/**
 * 事件处理器.
 */
internal class Handler<in E : Event> internal constructor(
    parentJob: Job?,
    subscriberContext: CoroutineContext,
    @JvmField val handler: suspend (E) -> ListeningStatus,
    override val concurrencyKind: Listener.ConcurrencyKind,
    override val priority: Listener.EventPriority
) : Listener<E>, CompletableJob by SupervisorJob(parentJob) { // avoid being cancelled on handling event

    private val subscriberContext: CoroutineContext = subscriberContext + this // override Job.

    val lock: Mutex? = when (concurrencyKind) {
        Listener.ConcurrencyKind.LOCKED -> Mutex()
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
                ?: coroutineContext[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: kotlin.run {
                    @Suppress("DEPRECATION")
                    (if (event is BotEvent) event.bot.logger else MiraiLogger.TopLevel)
                        .warning(
                            """Event processing: An exception occurred but no CoroutineExceptionHandler found, 
                        either in coroutineContext from Handler job, or in subscriberContext""".trimIndent(), e
                        )
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

    init {
        val map =
            EnumMap<Listener.EventPriority, ConcurrentLinkedQueue<ListenerRegistry>>(Listener.EventPriority::class.java)
        EventPriority.values().forEach {
            map[it] = ConcurrentLinkedQueue()
        }
        this.ALL_LEVEL_REGISTRIES = map
    }

    operator fun get(priority: Listener.EventPriority): ConcurrentLinkedQueue<ListenerRegistry> =
        ALL_LEVEL_REGISTRIES[priority]!!
}


// inline: NO extra Continuation
@Suppress("UNCHECKED_CAST")
internal suspend inline fun AbstractEvent.broadcastInternal() {
    if (EventDisabled) return
    callAndRemoveIfRequired(this@broadcastInternal)
}

internal inline fun <E, T : Iterable<E>> T.forEach0(block: T.(E) -> Unit) {
    forEach { block(it) }
}

@Suppress("DuplicatedCode")
internal suspend inline fun <E : AbstractEvent> callAndRemoveIfRequired(
    event: E
) {
    for (p in Listener.EventPriority.prioritiesExcludedMonitor) {
        GlobalEventListeners[p].forEach0 { registeredRegistry ->
            if (event.isIntercepted) {
                return
            }
            if (!registeredRegistry.type.isInstance(event)) return@forEach0
            val listener = registeredRegistry.listener
            when (listener.concurrencyKind) {
                Listener.ConcurrencyKind.LOCKED -> {
                    (listener as Handler).lock!!.withLock {
                        if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                            remove(registeredRegistry)
                        }
                    }
                }
                Listener.ConcurrencyKind.CONCURRENT -> {
                    if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                        remove(registeredRegistry)
                    }
                }
            }
        }
    }
    coroutineScope {
        GlobalEventListeners[EventPriority.MONITOR].forEach0 { registeredRegistry ->
            if (event.isIntercepted) {
                return@coroutineScope
            }
            if (!registeredRegistry.type.isInstance(event)) return@forEach0
            val listener = registeredRegistry.listener
            launch {
                when (listener.concurrencyKind) {
                    Listener.ConcurrencyKind.LOCKED -> {
                        (listener as Handler).lock!!.withLock {
                            if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                                remove(registeredRegistry)
                            }
                        }
                    }
                    Listener.ConcurrencyKind.CONCURRENT -> {
                        if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                            remove(registeredRegistry)
                        }
                    }
                }
            }
        }
    }
}