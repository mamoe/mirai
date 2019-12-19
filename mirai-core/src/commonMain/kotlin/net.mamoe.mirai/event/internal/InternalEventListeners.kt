package net.mamoe.mirai.event.internal

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.io.logStacktrace
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmField
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * 设置为 `true` 以关闭事件.
 * 所有的 `subscribe` 都能正常添加到监听器列表, 但所有的广播都会直接返回.
 */
var EventDisabled = false

/**
 * 监听和广播实现.
 * 它会首先检查这个事件是否正在被广播
 *  - 如果是, 则将新的监听者放入缓存中. 在当前广播结束后转移到主列表 (通过一个协程完成)
 *  - 如果不是, 则直接将新的监听者放入主列表
 *
 * @author Him188moe
 */ // inline to avoid a Continuation creation
internal suspend inline fun <L : Listener<E>, E : Subscribable> KClass<E>.subscribeInternal(listener: L): L {
    this.listeners().addLast(listener)
    return listener
}

/**
 * 事件监听器.
 *
 * 它实现 [CompletableJob] 接口,
 * 可通过 [CompletableJob.complete] 来正常结束监听, 或 [CompletableJob.completeExceptionally] 来异常地结束监听.
 *
 * @author Him188moe
 */
sealed class Listener<in E : Subscribable> : CompletableJob {
    abstract suspend fun onEvent(event: E): ListeningStatus
}

@PublishedApi
@Suppress("FunctionName")
internal suspend inline fun <E : Subscribable> Handler(noinline handler: suspend (E) -> ListeningStatus): Handler<E> {
    return Handler(coroutineContext[Job], coroutineContext, handler)
}

/**
 * 事件处理器.
 */
@PublishedApi
internal class Handler<in E : Subscribable>
@PublishedApi internal constructor(parentJob: Job?, private val context: CoroutineContext, @JvmField val handler: suspend (E) -> ListeningStatus) :
    Listener<E>(), CompletableJob by Job(parentJob) {

    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            withContext(context) { handler.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            e.logStacktrace()
            //this.completeExceptionally(e)
            ListeningStatus.STOPPED
        }
    }
}

@PublishedApi
@Suppress("FunctionName")
internal suspend inline fun <E : Subscribable> HandlerWithSession(
    bot: Bot,
    noinline handler: suspend Bot.(E) -> ListeningStatus
): HandlerWithSession<E> {
    return HandlerWithSession(bot, coroutineContext[Job], coroutineContext, handler)
}

/**
 * 带有 bot 筛选的监听器.
 * 所有的非 [BotEvent] 的事件都不会被处理
 * 所有的 [BotEvent.bot] `!==` `bot` 的事件都不会被处理
 */
@PublishedApi
internal class HandlerWithSession<E : Subscribable> @PublishedApi internal constructor(
    bot: Bot,
    parentJob: Job?, private val context: CoroutineContext, @JvmField val handler: suspend Bot.(E) -> ListeningStatus
) : Listener<E>(), CompletableJob by Job(parentJob) {
    val bot: Bot by bot.unsafeWeakRef()

    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING

        if (event !is BotEvent || event.bot !== bot) return ListeningStatus.LISTENING

        return try {
            withContext(context) { bot.handler(event) }.also { if (it == ListeningStatus.STOPPED) complete() }
        } catch (e: Throwable) {
            e.logStacktrace()
            //completeExceptionally(e)
            complete()
            ListeningStatus.STOPPED
        }
    }
}

/**
 * 这个事件类的监听器 list
 */
internal suspend fun <E : Subscribable> KClass<out E>.listeners(): EventListeners<E> = EventListenerManger.get(this)

internal class EventListeners<E : Subscribable> : LockFreeLinkedList<Listener<E>>() {
    init {
        this::class.members.filterIsInstance<KFunction<*>>().forEach {
            if (it.name == "add") {
                it.isExternal
            }
        }
    }
}

/**
 * 管理每个事件 class 的 [EventListeners].
 * [EventListeners] 是 lazy 的: 它们只会在被需要的时候才创建和存储.
 */
internal object EventListenerManger {
    private val registries: MutableMap<KClass<out Subscribable>, EventListeners<*>> = mutableMapOf()
    private val registriesMutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <E : Subscribable> get(clazz: KClass<out E>): EventListeners<E> =
        if (registries.containsKey(clazz)) registries[clazz] as EventListeners<E>
        else registriesMutex.withLock {
            EventListeners<E>().let {
                registries[clazz] = it
                return it
            }
        }

}

// inline: NO extra Continuation
internal suspend inline fun <E : Subscribable> E.broadcastInternal(): E {
    if (EventDisabled) return this

    callAndRemoveIfRequired(this::class.listeners())

    this::class.supertypes.forEach { superType ->
        if (Subscribable::class.isInstance(superType)) {
            // the super type is a child of Subscribable, then we can cast.
            @Suppress("UNCHECKED_CAST")
            callAndRemoveIfRequired((superType.classifier as KClass<out Subscribable>).listeners())
        }
    }
    return this
}

private suspend inline fun <E : Subscribable> E.callAndRemoveIfRequired(listeners: EventListeners<E>) {
    listeners.forEach {
        if (it.onEvent(this) == ListeningStatus.STOPPED) {
            listeners.remove(it) // atomic remove
        }
    }
}