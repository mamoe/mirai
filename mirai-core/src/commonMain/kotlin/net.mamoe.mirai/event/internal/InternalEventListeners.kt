package net.mamoe.mirai.event.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventDebugLogger
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.session
import net.mamoe.mirai.utils.internal.inlinedRemoveIf
import net.mamoe.mirai.utils.io.logStacktrace
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

// TODO: 2019/11/29 修改监听为 lock-free 模式

/**
 * 监听和广播实现.
 * 它会首先检查这个事件是否正在被广播
 *  - 如果是, 则将新的监听者放入缓存中. 在当前广播结束后转移到主列表 (通过一个协程完成)
 *  - 如果不是, 则直接将新的监听者放入主列表
 *
 * @author Him188moe
 */ // inline to avoid a Continuation creation
internal suspend inline fun <L : Listener<E>, E : Subscribable> KClass<E>.subscribeInternal(listener: L): L = with(this.listeners()) {
    if (mainMutex.tryLock(listener)) {//能锁则代表这个事件目前没有正在广播.
        try {
            add(listener)//直接修改主监听者列表
            EventDebugLogger.debug("Added a listener to ${this@subscribeInternal.simpleName}")
        } finally {
            mainMutex.unlock(listener)
        }
        return listener
    }

    //不能锁住, 则这个事件正在广播, 那么要将新的监听者放入缓存
    cacheMutex.withLock {
        cache.add(listener)
        EventDebugLogger.debug("Added a listener to cache of ${this@subscribeInternal.simpleName}")
    }

    GlobalScope.launch {
        //启动协程并等待正在进行的广播结束, 然后将缓存转移到主监听者列表
        //启动后的协程马上就会因为锁而被挂起
        mainMutex.withLock(listener) {
            cacheMutex.withLock {
                if (cache.size != 0) {
                    addAll(cache)
                    cache.clear()
                    EventDebugLogger.debug("Cache of ${this@subscribeInternal.simpleName} is now transferred to main")
                }
            }
        }
    }

    return@with listener
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
    @JvmField
    internal val lock = Mutex()
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
    noinline handler: suspend BotSession.(E) -> ListeningStatus
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
    @JvmField val bot: Bot,
    parentJob: Job?, private val context: CoroutineContext, @JvmField val handler: suspend BotSession.(E) -> ListeningStatus
) :
    Listener<E>(), CompletableJob by Job(parentJob) {

    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING

        if (event !is BotEvent || event.bot !== bot) return ListeningStatus.LISTENING

        return try {
            withContext(context) { bot.session.handler(event) }.also { if (it == ListeningStatus.STOPPED) complete() }
        } catch (e: Throwable) {
            e.logStacktrace()
            //completeExceptionally(e)
            ListeningStatus.STOPPED
        }
    }
}

/**
 * 这个事件类的监听器 list
 */
internal suspend fun <E : Subscribable> KClass<out E>.listeners(): EventListeners<E> = EventListenerManger.get(this)

internal class EventListeners<E : Subscribable> : MutableList<Listener<E>> by mutableListOf() {
    /**
     * 主监听者列表.
     * 广播事件时使用这个锁.
     */
    @JvmField
    val mainMutex = Mutex()
    /**
     * 缓存(监听)事件时使用的锁
     */
    @JvmField
    val cacheMutex = Mutex()
    /**
     * 等待加入到主 list 的监听者. 务必使用 [cacheMutex]
     */
    @JvmField
    val cache: MutableList<Listener<E>> = mutableListOf()

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

internal suspend fun <E : Subscribable> E.broadcastInternal(): E {
    if (EventDisabled) return this

    callListeners(this::class.listeners())

    applySuperListeners(this::class) { callListeners(it) }
    return this
}

private suspend inline fun <E : Subscribable> E.callListeners(listeners: EventListeners<in E>) {
    //自己持有, 则是在一个事件中
    if (listeners.mainMutex.holdsLock(listeners)) {
        callAndRemoveIfRequired(listeners)
    } else {
        while (!listeners.mainMutex.tryLock(listeners)) {
            delay(10)
        }
        try {
            callAndRemoveIfRequired(listeners)
        } finally {
            listeners.mainMutex.unlock(listeners)
        }
    }
}

private suspend inline fun <E : Subscribable> E.callAndRemoveIfRequired(listeners: EventListeners<in E>) = listeners.inlinedRemoveIf {
    if (it.lock.tryLock()) {
        try {
            it.onEvent(this) == ListeningStatus.STOPPED
        } finally {
            it.lock.unlock()
        }
    } else false
}

/**
 * apply [block] to all the [EventListeners] in [clazz]'s superclasses
 */
private tailrec suspend fun <E : Subscribable> applySuperListeners(
    clazz: KClass<out E>,
    block: suspend (EventListeners<in E>) -> Unit
) {
    val superEventClass =
        clazz.supertypes.map { it.classifier }.filterIsInstance<KClass<out Subscribable>>().firstOrNull() ?: return
    @Suppress("UNCHECKED_CAST")
    block(superEventClass.listeners() as EventListeners<in E>)
    applySuperListeners(superEventClass, block)
}