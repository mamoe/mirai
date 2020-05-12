package net.mamoe.mirai.console.utils

import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * A Value
 * the input type of this Value is T while the output is V
 */
@MiraiExperimentalAPI
abstract class Value<T, V> {
    operator fun invoke(): V = get()

    abstract fun get(): V

    abstract fun set(t: T)
}


/**
 * This value can be used as a Config Value
 */
@MiraiExperimentalAPI
interface ConfigValue


/**
 * A simple value
 * the input type is same as output value
 */

@MiraiExperimentalAPI
open class SimpleValue<T>(
    var value: T
) : Value<T, T>() {
    override fun get() = this.value

    override fun set(t: T) {
        this.value = t
    }
}

@MiraiExperimentalAPI
open class NullableSimpleValue<T>(
    value: T? = null
) : SimpleValue<T?>(
    value
) {
    fun isNull() = value == null
}

