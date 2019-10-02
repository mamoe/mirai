package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.ListeningStatus
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

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

internal class EventListeners<E : Event> : MutableList<Listener<E>> by mutableListOf()

internal object EventListenerManger {
    private val REGISTRIES: MutableMap<KClass<out Event>, EventListeners<out Event>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    internal fun <E : Event> get(clazz: KClass<E>): EventListeners<E> {
        synchronized(clazz) {
            if (REGISTRIES.containsKey(clazz)) {
                return REGISTRIES[clazz] as EventListeners<E>
            } else {
                EventListeners<E>().let {
                    REGISTRIES[clazz] = it
                    return it
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal suspend fun <E : Event> E.broadcastInternal(): E {
    suspend fun callListeners(listeners: EventListeners<in E>) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().onEvent(this) == ListeningStatus.STOPPED) {
                iterator.remove()
            }
        }
    }

    callListeners(this::class.listeners as EventListeners<E>)
    this::class.allSuperclasses.forEach {
        //println("super: " + it.simpleName)
        if (Event::class.java.isAssignableFrom(it.java)) {
            callListeners((it as KClass<out Event>).listeners as EventListeners<in E>)
        }
    }

    return this
}