/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.LockFreeLinkedListNode
import net.mamoe.mirai.utils.isRemoved
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass


internal actual class MiraiAtomicBoolean actual constructor(initial: Boolean) {
    private val delegate: AtomicBoolean = AtomicBoolean(initial)

    actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual var value: Boolean
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }
}

internal actual object GlobalEventListeners {
    private val map: Map<Listener.EventPriority, LockFreeLinkedList<ListenerNode>>

    init {
        val map = EnumMap<Listener.EventPriority, LockFreeLinkedList<ListenerNode>>(Listener.EventPriority::class.java)
        Listener.EventPriority.values().forEach {
            map[it] = LockFreeLinkedList()
        }
        this.map = map
    }

    actual operator fun get(priority: Listener.EventPriority): LockFreeLinkedList<ListenerNode> = map[priority]!!

}

/*
internal actual class EventListeners<E : Event> actual constructor(clazz: KClass<E>) :
    LockFreeLinkedList<Listener<E>>() {
    @Suppress("UNCHECKED_CAST", "UNSUPPORTED", "NO_REFLECTION_IN_CLASS_PATH")
    actual val supertypes: Set<KClass<out Event>> by lazy {
        val supertypes = mutableSetOf<KClass<out Event>>()

        fun addSupertypes(klass: KClass<out Event>) {
            klass.supertypes.forEach {
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
 */