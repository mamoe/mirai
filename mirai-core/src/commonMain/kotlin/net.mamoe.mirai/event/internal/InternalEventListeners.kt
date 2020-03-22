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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventDisabled
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

val EventLogger: MiraiLoggerWithSwitch = DefaultLogger("Event").withSwitch(false)

@MiraiInternalAPI
fun <L : Listener<E>, E : Event> KClass<out E>.subscribeInternal(listener: L): L {
    with(this.listeners()) {
        addLast(listener)
        listener.invokeOnCompletion {
            this.remove(listener)
        }
    }
    return listener
}

@PublishedApi
@Suppress("FunctionName", "unused")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
internal fun <E : Event> CoroutineScope.Handler(
    coroutineContext: CoroutineContext,
    handler: suspend (E) -> ListeningStatus
): Handler<E> = Handler(coroutineContext, concurrencyKind = Listener.ConcurrencyKind.LOCKED, handler = handler)

@PublishedApi
@Suppress("FunctionName")
internal fun <E : Event> CoroutineScope.Handler(
    coroutineContext: CoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind,
    handler: suspend (E) -> ListeningStatus
): Handler<E> {
    @OptIn(ExperimentalCoroutinesApi::class) // don't remove
    val context = this.newCoroutineContext(coroutineContext)
    return Handler(context[Job], context, handler, concurrencyKind)
}

/**
 * 事件处理器.
 */
@PublishedApi
internal class Handler<in E : Event>
@PublishedApi internal constructor(
    parentJob: Job?,
    private val subscriberContext: CoroutineContext,
    @JvmField val handler: suspend (E) -> ListeningStatus,
    override val concurrencyKind: Listener.ConcurrencyKind
) :
    Listener<E>, CompletableJob by Job(parentJob) {

    @Suppress("unused")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @PublishedApi
    internal constructor(
        parentJob: Job?,
        subscriberContext: CoroutineContext,
        handler: suspend (E) -> ListeningStatus
    ) : this(parentJob, subscriberContext, handler, Listener.ConcurrencyKind.LOCKED)

    @MiraiInternalAPI
    val lock: Mutex? = when (concurrencyKind) {
        Listener.ConcurrencyKind.CONCURRENT -> null
        Listener.ConcurrencyKind.LOCKED -> Mutex()
    }

    @Suppress("unused")
    @OptIn(MiraiDebugAPI::class)
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

/**
 * 这个事件类的监听器 list
 */
internal fun <E : Event> KClass<out E>.listeners(): EventListeners<E> = EventListenerManager.get(this)

internal class EventListeners<E : Event>(clazz: KClass<E>) : LockFreeLinkedList<Listener<E>>() {
    @Suppress("UNCHECKED_CAST", "UNSUPPORTED", "NO_REFLECTION_IN_CLASS_PATH")
    val supertypes: Set<KClass<out Event>> by lazy {
        val supertypes = mutableSetOf<KClass<out Event>>()

        fun addSupertypes(clazz: KClass<out Event>) {
            clazz.supertypes.forEach {
                val classifier = it.classifier as? KClass<out Event>
                if (classifier != null) {
                    supertypes.add(classifier)
                    addSupertypes(classifier)
                }
            }
        }
        addSupertypes(clazz)

        supertypes
    }
}

internal expect class MiraiAtomicBoolean(initial: Boolean) {

    fun compareAndSet(expect: Boolean, update: Boolean): Boolean

    var value: Boolean
}

/**
 * 管理每个事件 class 的 [EventListeners].
 * [EventListeners] 是 lazy 的: 它们只会在被需要的时候才创建和存储.
 */
internal object EventListenerManager {
    private data class Registry<E : Event>(val clazz: KClass<E>, val listeners: EventListeners<E>)

    private val registries = LockFreeLinkedList<Registry<*>>()

    // 不要用 atomicfu. 在 publish 后会出现 VerifyError
    private val lock: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    @Suppress("UNCHECKED_CAST", "BooleanLiteralArgument")
    internal tailrec fun <E : Event> get(clazz: KClass<out E>): EventListeners<E> {
        registries.forEach {
            if (it.clazz == clazz) {
                return it.listeners as EventListeners<E>
            }
        }
        if (lock.compareAndSet(false, true)) {
            val registry = Registry(clazz as KClass<E>, EventListeners(clazz))
            registries.addLast(registry)
            lock.value = false
            return registry.listeners
        }
        return get(clazz)
    }
}

// inline: NO extra Continuation
@Suppress("UNCHECKED_CAST")
internal suspend inline fun Event.broadcastInternal() = coroutineScope {
    if (EventDisabled) return@coroutineScope

    EventLogger.info { "Event broadcast: $this" }

    val listeners = this@broadcastInternal::class.listeners()
    callAndRemoveIfRequired(this@broadcastInternal, listeners)
    listeners.supertypes.forEach {
        callAndRemoveIfRequired(this@broadcastInternal, it.listeners())
    }
}

@OptIn(MiraiInternalAPI::class)
private fun <E : Event> CoroutineScope.callAndRemoveIfRequired(event: E, listeners: EventListeners<E>) {
    // atomic foreach
    listeners.forEachNode { node ->
        launch {
            val listener = node.nodeValue
            if (listener.concurrencyKind == Listener.ConcurrencyKind.LOCKED) {
                (listener as Handler).lock!!.withLock {
                    if (!node.isRemoved() && listener.onEvent(event) == ListeningStatus.STOPPED) {
                        listeners.remove(listener) // atomic remove
                    }
                }
            } else {
                if (!node.isRemoved() && listener.onEvent(event) == ListeningStatus.STOPPED) {
                    listeners.remove(listener) // atomic remove
                }
            }
        }
    }
}