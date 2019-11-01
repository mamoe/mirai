package net.mamoe.mirai.event.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventLogger
import net.mamoe.mirai.event.EventScope
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.internal.inlinedRemoveIf
import kotlin.jvm.JvmField
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * 监听和广播实现.
 * 它会首先检查这个事件是否正在被广播
 *  - 如果是, 则将新的监听者放入缓存中. 在当前广播结束后转移到主列表 (通过一个协程完成)
 *  - 如果不是, 则直接将新的监听者放入主列表
 *
 * @author Him188moe
 */
internal suspend fun <E : Event> KClass<E>.subscribeInternal(listener: Listener<E>): Unit = with(this.listeners()) {
    if (mainMutex.tryLock(listener)) {//能锁则代表这个事件目前没有正在广播.
        try {
            add(listener)//直接修改主监听者列表
            EventLogger.debug("Added a listener to ${this@subscribeInternal.simpleName}")
        } finally {
            mainMutex.unlock(listener)
        }
        return
    }

    //不能锁住, 则这个事件正在广播, 那么要将新的监听者放入缓存
    cacheMutex.withLock {
        cache.add(listener)
        EventLogger.debug("Added a listener to cache of ${this@subscribeInternal.simpleName}")
    }

    EventScope.launch {
        //启动协程并等待正在进行的广播结束, 然后将缓存转移到主监听者列表
        //启动后的协程马上就会因为锁而被挂起
        mainMutex.withLock(listener) {
            cacheMutex.withLock {
                if (cache.size != 0) {
                    addAll(cache)
                    cache.clear()
                    EventLogger.debug("Cache of ${this@subscribeInternal.simpleName} is now transferred to main")
                }
            }
        }
    }
}

/**
 * 事件监听器
 *
 * @author Him188moe
 */
sealed class Listener<in E : Event> {
    internal val lock = Mutex()
    abstract suspend fun onEvent(event: E): ListeningStatus
}

@PublishedApi
internal class Handler<in E : Event>(@JvmField val handler: suspend (E) -> ListeningStatus) : Listener<E>() {
    override suspend fun onEvent(event: E): ListeningStatus = handler.invoke(event)
}

/**
 * 带有 bot 筛选的监听器.
 * 所有的非 [BotEvent] 的事件都不会被处理
 * 所有的 [BotEvent.bot] `!==` `bot` 的事件都不会被处理
 */
@PublishedApi
internal class HandlerWithBot<E : Event>(val bot: Bot, @JvmField val handler: suspend Bot.(E) -> ListeningStatus) :
    Listener<E>() {
    override suspend fun onEvent(event: E): ListeningStatus = with(bot) {
        if (event !is BotEvent || event.bot !== this) {
            return ListeningStatus.LISTENING
        }

        return if (bot !== this) {
            ListeningStatus.LISTENING
        } else {
            handler(event)
        }
    }
}

/**
 * 这个事件类的监听器 list
 */
internal suspend fun <E : Event> KClass<out E>.listeners(): EventListeners<E> = EventListenerManger.get(this)

internal class EventListeners<E : Event> : MutableList<Listener<E>> by mutableListOf() {
    /**
     * 主监听者列表.
     * 广播事件时使用这个锁.
     */
    val mainMutex = Mutex()
    /**
     * 缓存(监听)事件时使用的锁
     */
    val cacheMutex = Mutex()
    /**
     * 等待加入到主 list 的监听者. 务必使用 [cacheMutex]
     */
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
    private val registries: MutableMap<KClass<out Event>, EventListeners<*>> = mutableMapOf()
    private val registriesMutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <E : Event> get(clazz: KClass<out E>): EventListeners<E> = registriesMutex.withLock {
        if (registries.containsKey(clazz)) {
            return registries[clazz] as EventListeners<E>
        } else {
            EventListeners<E>().let {
                registries[clazz] = it
                return it
            }
        }
    }
}

internal suspend fun <E : Event> E.broadcastInternal(): E {
    suspend fun callListeners(listeners: EventListeners<in E>) {
        suspend fun callAndRemoveIfRequired() = listeners.inlinedRemoveIf {
            if (it.lock.tryLock()) {
                try {
                    it.onEvent(this) == ListeningStatus.STOPPED
                } finally {
                    it.lock.unlock()
                }
            } else false
        }

        //自己持有, 则是在一个事件中
        if (listeners.mainMutex.holdsLock(listeners)) {
            callAndRemoveIfRequired()
        } else {
            while (!listeners.mainMutex.tryLock(listeners)) {
                delay(10)
            }
            try {
                callAndRemoveIfRequired()
            } finally {
                listeners.mainMutex.unlock(listeners)
            }
        }
    }

    callListeners(this::class.listeners())

    applySuperListeners(this::class) { callListeners(it) }
    return this
}

/**
 * apply [block] to all the [EventListeners] in [clazz]'s superclasses
 */
private tailrec suspend fun <E : Event> applySuperListeners(
    clazz: KClass<out E>,
    block: suspend (EventListeners<in E>) -> Unit
) {
    val superEventClass =
        clazz.supertypes.map { it.classifier }.filterIsInstance<KClass<out Event>>().firstOrNull() ?: return
    @Suppress("UNCHECKED_CAST")
    block(superEventClass.listeners() as EventListeners<in E>)
    applySuperListeners(superEventClass, block)
}