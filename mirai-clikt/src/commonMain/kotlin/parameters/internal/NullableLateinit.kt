package net.mamoe.mirai.clikt.parameters.internal

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A container for a value that is initialized after the container is created.
 *
 * Similar to a lateinit variable, but allows nullable types. If the value is not set before
 * being read, it will return null if T is nullable, or throw an IllegalStateException otherwise.
 */
internal class NullableLateinit<T>(private val errorMessage: String) : ReadWriteProperty<Any, T> {
    private object UNINITIALIZED

    private var value: Any? = UNINITIALIZED

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value === UNINITIALIZED) throw IllegalStateException(errorMessage)

        try {
            @Suppress("UNCHECKED_CAST")
            return value as T
        } catch (e: ClassCastException) {
            throw IllegalStateException(errorMessage)
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}