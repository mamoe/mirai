package net.mamoe.mirai.event.internal

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

/**
 * 设置为 `true` 以关闭事件.
 * 所有的 `subscribe` 都能正常添加到监听器列表, 但所有的广播都会直接返回.
 */
var EventDisabled = false

@PublishedApi
internal fun <L : Listener<E>, E : Subscribable> KClass<out E>.subscribeInternal(listener: L): L {
    this.listeners().addLast(listener)
    return listener
}

@PublishedApi
@Suppress("FunctionName")
internal fun <E : Subscribable> CoroutineScope.Handler(handler: suspend (E) -> ListeningStatus): Handler<E> {
    return Handler(coroutineContext[Job], coroutineContext, handler)
}

/**
 * 事件处理器.
 */
@PublishedApi
internal class Handler<in E : Subscribable>
@PublishedApi internal constructor(parentJob: Job?, private val subscriberContext: CoroutineContext, @JvmField val handler: suspend (E) -> ListeningStatus) :
    Listener<E>, CompletableJob by Job(parentJob) {

    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            // Inherit context.
            withContext(subscriberContext) { handler.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            e.logStacktrace()
            // this.complete() // do not `completeExceptionally`, otherwise parentJob will fail.
            // ListeningStatus.STOPPED

            // not stopping listening.
            ListeningStatus.LISTENING
        }
    }
}

/**
 * 这个事件类的监听器 list
 */
internal fun <E : Subscribable> KClass<out E>.listeners(): EventListeners<E> = EventListenerManger.get(this)

internal class EventListeners<E : Subscribable> : LockFreeLinkedList<Listener<E>>()

/**
 * 管理每个事件 class 的 [EventListeners].
 * [EventListeners] 是 lazy 的: 它们只会在被需要的时候才创建和存储.
 */
internal object EventListenerManger {
    private data class Registry<E : Subscribable>(val clazz: KClass<E>, val listeners: EventListeners<E>)

    private val registries = LockFreeLinkedList<Registry<*>>()

    @Suppress("UNCHECKED_CAST")
    internal fun <E : Subscribable> get(clazz: KClass<out E>): EventListeners<E> {
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
internal suspend inline fun Subscribable.broadcastInternal() {
    if (EventDisabled) return

    callAndRemoveIfRequired(this::class.listeners())

    var supertypes = this::class.supertypes
    while (true) {
        val superSubscribableType = supertypes.firstOrNull {
            it.classifier as? KClass<out Subscribable> != null
        }

        superSubscribableType?.let {
            callAndRemoveIfRequired((it.classifier as KClass<out Subscribable>).listeners())
        }

        supertypes = (superSubscribableType?.classifier as? KClass<*>)?.supertypes ?: return
    }
}

private suspend inline fun <E : Subscribable> E.callAndRemoveIfRequired(listeners: EventListeners<E>) {
    // atomic foreach
    listeners.forEach {
        if (it.onEvent(this) == ListeningStatus.STOPPED) {
            listeners.remove(it) // atomic remove
        }
    }
}