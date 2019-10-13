package net.mamoe.mirai.event.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.ListeningStatus
import kotlin.reflect.KClass

/**
 * 监听和广播实现
 *
 * @author Him188moe
 */
internal fun <E : Event> KClass<E>.subscribeInternal(listener: Listener<E>) = this.listeners.add(listener)

/**
 * 事件监听器
 *
 * @author Him188moe
 */
internal interface Listener<in E : Event> {

    suspend fun onEvent(event: E): ListeningStatus
}

/**
 * Lambda 监听器.
 * 不推荐直接使用该类
 */
class Handler<E : Event>(val handler: suspend (E) -> ListeningStatus) : Listener<E> {
    override suspend fun onEvent(event: E): ListeningStatus = handler.invoke(event)
}

internal val <E : Event> KClass<E>.listeners: EventListeners<E> get() = EventListenerManger.get(this)

internal class EventListeners<E : Event> : MutableList<Listener<E>> by mutableListOf() {
    val lock = Mutex()
}

internal object EventListenerManger {
    private val registries: MutableMap<KClass<out Event>, EventListeners<out Event>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    internal fun <E : Event> get(clazz: KClass<E>): EventListeners<E> {
        //synchronized(clazz) {
        if (registries.containsKey(clazz)) {
            return registries[clazz] as EventListeners<E>
        } else {
            EventListeners<E>().let {
                registries[clazz] = it
                return it
            }
        }
        //}
    }
}

@Suppress("UNCHECKED_CAST")
internal suspend fun <E : Event> E.broadcastInternal(): E {
    suspend fun callListeners(listeners: EventListeners<in E>) = listeners.lock.withLock {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().onEvent(this) == ListeningStatus.STOPPED) {
                iterator.remove()
            }
        }
    }

    callListeners(this::class.listeners as EventListeners<in E>)

    loopAllListeners(this::class) { callListeners(it as EventListeners<in E>) }
    return this
}

internal expect inline fun <E : Event> loopAllListeners(clazz: KClass<E>, consumer: (EventListeners<in E>) -> Unit)