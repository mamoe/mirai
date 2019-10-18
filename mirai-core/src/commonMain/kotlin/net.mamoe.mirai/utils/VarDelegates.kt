package net.mamoe.mirai.utils

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


fun <T : Any> Delegates.notNullBy(initializer: () -> T): ReadWriteProperty<Any?, T> = NotNullVarWithDefault(lazy(initializer = initializer))

class NotNullVarWithDefault<T : Any>(
        private val initializer: Lazy<T>
) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: initializer.value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
