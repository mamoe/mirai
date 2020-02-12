/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventDisabled
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

@PublishedApi
internal fun <L : Listener<E>, E : Event> KClass<out E>.subscribeInternal(listener: L): L {
    this.listeners().addLast(listener)
    return listener
}

@PublishedApi
@Suppress("FunctionName")
internal fun <E : Event> CoroutineScope.Handler(handler: suspend (E) -> ListeningStatus): Handler<E> {
    return Handler(coroutineContext[Job], coroutineContext, handler)
}

private inline fun inline(block: () -> Unit) = block()
/**
 * 事件处理器.
 */
@PublishedApi
internal class Handler<in E : Event>
@PublishedApi internal constructor(parentJob: Job?, private val subscriberContext: CoroutineContext, @JvmField val handler: suspend (E) -> ListeningStatus) :
    Listener<E>, CompletableJob by Job(parentJob) {

    @UseExperimental(MiraiDebugAPI::class)
    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            // Inherit context.
            withContext(subscriberContext) { handler.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            subscriberContext[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: coroutineContext[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: inline {
                    @Suppress("DEPRECATION")
                    MiraiLogger.warning(
                        """Event processing: An exception occurred but no CoroutineExceptionHandler found, 
                        either in coroutineContext from Handler job, or in subscriberContext""".trimIndent()
                    )
                    e.logStacktrace("Event processing(No CoroutineExceptionHandler found)")
                }
            // this.complete() // do not `completeExceptionally`, otherwise parentJob will fai`l.
            // ListeningStatus.STOPPED

            // not stopping listening.
            ListeningStatus.LISTENING
        }
    }
}

/**
 * 这个事件类的监听器 list
 */
internal fun <E : Event> KClass<out E>.listeners(): EventListeners<E> = EventListenerManager.get(this)

internal class EventListeners<E : Event> : LockFreeLinkedList<Listener<E>>()

/**
 * 管理每个事件 class 的 [EventListeners].
 * [EventListeners] 是 lazy 的: 它们只会在被需要的时候才创建和存储.
 */
internal object EventListenerManager {
    private data class Registry<E : Event>(val clazz: KClass<E>, val listeners: EventListeners<E>)

    private val registries = LockFreeLinkedList<Registry<*>>()

    @Suppress("UNCHECKED_CAST")
    internal fun <E : Event> get(clazz: KClass<out E>): EventListeners<E> {
        return registries.filteringGetOrAdd({ it.clazz == clazz }) {
            Registry(
                clazz,
                EventListeners()
            )
        }.listeners as EventListeners<E>
    }
}

// inline: NO extra Continuation
@Suppress("UNCHECKED_CAST")
internal suspend inline fun Event.broadcastInternal() {
    if (EventDisabled) return

    callAndRemoveIfRequired(this::class.listeners())

    var supertypes = this::class.supertypes
    while (true) {
        val superSubscribableType = supertypes.firstOrNull {
            it.classifier as? KClass<out Event> != null
        }

        superSubscribableType?.let {
            callAndRemoveIfRequired((it.classifier as KClass<out Event>).listeners())
        }

        supertypes = (superSubscribableType?.classifier as? KClass<*>)?.supertypes ?: return
    }
}

private suspend inline fun <E : Event> E.callAndRemoveIfRequired(listeners: EventListeners<E>) {
    // atomic foreach
    listeners.forEach {
        if (it.onEvent(this) == ListeningStatus.STOPPED) {
            listeners.remove(it) // atomic remove
        }
    }
}