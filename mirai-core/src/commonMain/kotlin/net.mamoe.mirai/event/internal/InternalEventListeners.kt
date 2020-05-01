/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:OptIn(MiraiInternalAPI::class)

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmField
import kotlin.reflect.KClass


@PublishedApi
internal fun <L : Listener<E>, E : Event> KClass<out E>.subscribeInternal(listener: L): L {
    with(GlobalEventListeners[listener.priority]) {
        @Suppress("UNCHECKED_CAST")
        val node = ListenerNode(listener as Listener<Event>, this@subscribeInternal)
        @OptIn(MiraiInternalAPI::class)
        addLast(node)
        listener.invokeOnCompletion {
            @OptIn(MiraiInternalAPI::class)
            this.remove(node)
        }
    }
    return listener
}


@PublishedApi
@Suppress("FunctionName")
internal fun <E : Event> CoroutineScope.Handler(
    coroutineContext: CoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind,
    priority: Listener.EventPriority = Listener.EventPriority.NORMAL,
    handler: suspend (E) -> ListeningStatus
): Handler<E> {
    @OptIn(ExperimentalCoroutinesApi::class) // don't remove
    val context = this.newCoroutineContext(coroutineContext)
    return Handler(context[Job], context, handler, concurrencyKind, priority)
}

/**
 * 事件处理器.
 */
@PublishedApi
internal class Handler<in E : Event>
@PublishedApi internal constructor(
    parentJob: Job?,
    subscriberContext: CoroutineContext,
    @JvmField val handler: suspend (E) -> ListeningStatus,
    override val concurrencyKind: Listener.ConcurrencyKind,
    override val priority: Listener.EventPriority
) : Listener<E>, CompletableJob by SupervisorJob(parentJob) { // avoid being cancelled on handling event

    private val subscriberContext: CoroutineContext = subscriberContext + this // override Job.


    @MiraiInternalAPI
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
                    MiraiLogger.warning(
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

internal class ListenerNode(
    val listener: Listener<Event>,
    val owner: KClass<out Event>
)
internal expect object GlobalEventListeners {
    operator fun get(priority: Listener.EventPriority): LockFreeLinkedList<ListenerNode>
}

internal expect class MiraiAtomicBoolean(initial: Boolean) {

    fun compareAndSet(expect: Boolean, update: Boolean): Boolean

    var value: Boolean
}


// inline: NO extra Continuation
@Suppress("UNCHECKED_CAST")
internal suspend inline fun Event.broadcastInternal() = coroutineScope {
    if (EventDisabled) return@coroutineScope
    callAndRemoveIfRequired(this@broadcastInternal as? AbstractEvent ?: error("Events must extends AbstractEvent"))
}

@OptIn(MiraiInternalAPI::class)
private suspend fun <E : AbstractEvent> callAndRemoveIfRequired(
    event: E
) {
    coroutineScope {
        for (p in Listener.EventPriority.values()) {
            GlobalEventListeners[p].forEachNode { eventNode ->
                if (event.isIntercepted) {
                    return@coroutineScope
                }
                val node = eventNode.nodeValue
                if (!node.owner.isInstance(event)) return@forEachNode
                val listener = node.listener
                when (listener.concurrencyKind) {
                    Listener.ConcurrencyKind.LOCKED -> {
                        (listener as Handler).lock!!.withLock {
                            kotlin.runCatching {
                                when (listener.onEvent(event)) {
                                    ListeningStatus.STOPPED -> {
                                        removeNode(eventNode)
                                    }
                                    else -> {
                                    }
                                }
                            }.onFailure {
                                TODO("Exception catching")
                            }
                        }
                    }
                    Listener.ConcurrencyKind.CONCURRENT -> {
                        kotlin.runCatching {
                            when (listener.onEvent(event)) {
                                ListeningStatus.STOPPED -> {
                                    removeNode(eventNode)
                                }
                                else -> {
                                }
                            }
                        }.onFailure {
                            TODO("Exception catching")
                        }
                    }
                }
            }
        }
    }
}