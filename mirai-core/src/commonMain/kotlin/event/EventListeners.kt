/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.*
import net.mamoe.mirai.internal.network.components.EVENT_LAUNCH_UNDISPATCHED
import net.mamoe.mirai.utils.*
import kotlin.reflect.KClass


internal class ListenerRegistry(
    val listener: Listener<Event>,
    val type: KClass<out Event>,
)


internal class EventListeners {
    companion object {
        private val logger by lazy {
            MiraiLogger.Factory.create(EventListeners::class).withSwitch(systemProp("mirai.event.trace", false))
        }
    }

    private val map: Map<EventPriority, MutableCollection<ListenerRegistry>>

    init {
        val map =
            EnumMap<EventPriority, MutableCollection<ListenerRegistry>>(EventPriority::class)
        EventPriority.values().forEach {
            map[it] = ConcurrentLinkedDeque()
        }
        this.map = map
    }


    fun clear() {
        map.forEach { (_, u) ->
            u.clear()
        }
    }

    operator fun get(priority: EventPriority): MutableCollection<ListenerRegistry> =
        map[priority] ?: error("Internal error: map[$priority] == null")


    private val prioritiesExcludedMonitor: Array<EventPriority> = run {
        EventPriority.values().filter { it != EventPriority.MONITOR }.toTypedArray()
    }

    internal suspend fun <E : AbstractEvent> callListeners(event: E) {
        for (p in prioritiesExcludedMonitor) {
            val container = get(p)
            for (registry in container) {
                if (event.isIntercepted) return
                if (!registry.type.isInstance(event)) continue
                val listener = registry.listener
                process(container, registry, listener, event)
            }
        }

        if (event.isIntercepted) return
        val container = get(EventPriority.MONITOR)
        when (container.size) {
            0 -> return
            1 -> {
                val registry = container.firstOrNull() ?: return
                if (!registry.type.isInstance(event)) return
                process(container, registry, registry.listener, event)
            }
            else -> supervisorScope {
                for (registry in get(EventPriority.MONITOR)) {
                    if (!registry.type.isInstance(event)) continue
                    launch(start = if (EVENT_LAUNCH_UNDISPATCHED) CoroutineStart.UNDISPATCHED else CoroutineStart.DEFAULT) {
                        process(container, registry, registry.listener, event)
                    }
                }
            }
        }
    }

    internal fun <E : Event> addListener(eventClass: KClass<E>, listener: Listener<E>) {
        logger.info { "Add listener: $listener for $eventClass" }
        val listeners = get(listener.priority)

        @Suppress("UNCHECKED_CAST")
        val node = ListenerRegistry(listener as Listener<Event>, eventClass)
        listeners.add(node)
        listener.invokeOnCompletion {
            listeners.remove(node)
        }
    }

    private suspend fun <E : AbstractEvent> process(
        container: MutableCollection<ListenerRegistry>,
        registry: ListenerRegistry,
        listener: Listener<Event>,
        event: E,
    ) {
        logger.info { "Invoke listener: $listener" }
        when (listener.concurrencyKind) {
            ConcurrencyKind.LOCKED -> {
                (listener as SafeListener).lock!!.withLock {
                    if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                        container.remove(registry)
                    }
                }
            }
            ConcurrencyKind.CONCURRENT -> {
                if (listener.onEvent(event) == ListeningStatus.STOPPED) {
                    container.remove(registry)
                }
            }
        }
        logger.info { "Finished listener: $listener" }
    }
}