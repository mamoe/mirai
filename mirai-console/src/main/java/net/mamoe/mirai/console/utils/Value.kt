package net.mamoe.mirai.console.utils

/**
 * A Value
 * the input type of this Value is T while the output is V
 */
abstract class Value<T,V>{
    operator fun invoke():V = get()

    abstract fun get():V

    abstract fun set(t:T)
}



/**
 * This value can be used as a Config Value
 */
interface ConfigValue


/**
 * A simple value
 * the input type is same as output value
 */

open class SimpleValue<T>(
    var value:T
):Value<T,T>() {
    override fun get() = this.value

    override fun set(t: T) {
        this.value = t
    }
}

open class NullableSimpleValue<T>(
    value:T? = null
):SimpleValue<T?>(
    value
){
   fun isNull() = value == null
}

