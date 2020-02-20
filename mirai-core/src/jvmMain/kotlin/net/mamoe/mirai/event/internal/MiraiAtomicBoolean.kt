package net.mamoe.mirai.event.internal

import java.util.concurrent.atomic.AtomicBoolean


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