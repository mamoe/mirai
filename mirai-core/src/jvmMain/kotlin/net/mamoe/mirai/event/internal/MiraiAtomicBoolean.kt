/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.InsertLink
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.isRemoved
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
                    classifier.listeners().children.add(clazz)
                    addSupertypes(classifier)
                }
            }
        }
        addSupertypes(clazz)

        supertypes
    }

    @Suppress("UNCHECKED_CAST", "UNSUPPORTED", "NO_REFLECTION_IN_CLASS_PATH")
    actual val children: MutableSet<KClass<out Event>> = mutableSetOf()
    private var handlers0: Iterable<ListenerNode<E>?>? = null
    actual val handlers: Iterable<ListenerNode<E>?>
        get() {
            val h = handlers0
            if (h != null) {
                return h
            }
            val link = InsertLink<ListenerNode<E>>()
            val head = link.head
            fun EventListeners<*>.register(l: Listener<E>) {
                val p = l.priority
                val node0 = ListenerNode(l, this)
                var scanning = head
                while (true) {
                    val next = scanning.next
                    if (next == null || next == link.tail) {
                        scanning.insertAfter(node0)
                        return@register
                    }
                    val np = next.value!!.listener.priority
                    if (np > p) {
                        scanning.insertAfter(node0)
                        return@register
                    }
                    scanning = next
                }
            }
            forEachNode { if (!it.isRemoved()) register(it.nodeValue) }
            supertypes.forEach {
                it.listeners().apply {
                    forEachNode {
                        if (!it.isRemoved())
                            this@apply.register(it.nodeValue)
                    }
                }
            }
            handlers0 = link
            return link
        }

    override fun remove(element: Listener<E>): Boolean {
        val result = super.remove(element)
        if (result) postReset()
        return result
    }

    actual fun postReset() {
        if (handlers0 != null) {
            handlers0 = null
            children.forEach { it.listeners().postReset() }
            supertypes.forEach { it.listeners().postReset() }
        }
    }
}