package net.mamoe.mirai.event.internal

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventScope
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.internal.inlinedRemoveIf
import kotlin.reflect.KClass

/**
 * 监听和广播实现.
 * 它会首先检查这个事件是否正在被广播
 *  - 如果是, 则将新的监听者放入缓存中. 在当前广播结束后转移到主列表 (通过一个协程完成)
 *  - 如果不是, 则直接将新的监听者放入主列表
 *
 * @author Him188moe
 */
internal suspend fun <E : Event> KClass<E>.subscribeInternal(listener: Listener<E>): Unit = with(this.listeners()) {
    if (mainMutex.tryLock()) {//能锁则代表这个事件目前没有正在广播.
        try {
            add(listener)//直接修改主监听者列表
        } finally {
            mainMutex.unlock()
        }
        return
    }

    //不能锁住, 则这个事件正在广播, 那么要将新的监听者放入缓存
    cacheMutex.withLock {
        cache.add(listener)
    }

    EventScope.launch {
        //启动协程并等待正在进行的广播结束, 然后将缓存转移到主监听者列表
        //启动后的协程马上就会因为锁而被挂起
        mainMutex.withLock {
            if (cache.size != 0) {
                addAll(cache)
                cache.clear()
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
    abstract suspend fun onEvent(event: E): ListeningStatus
}

/**
 * Lambda 监听器.
 * 不推荐直接使用该类
 */
class Handler<E : Event>(val handler: suspend (E) -> ListeningStatus) : Listener<E>() {
    override suspend fun onEvent(event: E): ListeningStatus = handler.invoke(event)
}

/**
 * 带有 bot 筛选的监听器.
 * 所有的非 [BotEvent] 的事件都不会被处理
 * 所有的 [BotEvent.bot] `!==` `bot` 的事件都不会被处理
 */
class HandlerWithBot<E : Event>(val bot: Bot, val handler: suspend Bot.(E) -> ListeningStatus) : Listener<E>() {
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
internal suspend fun <E : Event> KClass<E>.listeners(): EventListeners<E> = EventListenerManger.get(this)

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
}

/**
 * 管理每个事件 class 的 [EventListeners].
 * [EventListeners] 是 lazy 的: 它们只会在被需要的时候才创建和存储.
 */
internal object EventListenerManger {
    private val registries: MutableMap<KClass<out Event>, EventListeners<out Event>> = mutableMapOf()
    private val registriesMutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <E : Event> get(clazz: KClass<E>): EventListeners<E> = registriesMutex.withLock {
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

@Suppress("UNCHECKED_CAST")
internal suspend fun <E : Event> E.broadcastInternal(): E {
    suspend fun callListeners(listeners: EventListeners<in E>) = listeners.mainMutex.withLock {
        listeners.inlinedRemoveIf { it.onEvent(this) == ListeningStatus.STOPPED }
    }

    callListeners(this::class.listeners() as EventListeners<in E>)

    //FIXME 这可能不支持所有的平台. 可能需要修改.
    loopAllListeners(this::class) { callListeners(it as EventListeners<in E>) }
    return this
}

internal expect suspend inline fun <E : Event> loopAllListeners(
    clazz: KClass<E>,
    consumer: (EventListeners<in E>) -> Unit
)