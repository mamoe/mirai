package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.LockFreeLinkedList
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