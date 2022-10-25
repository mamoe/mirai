package net.mamoe.mirai.clikt.parameters.options

/** A container for a value that can have a default value and can be manually set */
public data class ValueWithDefault<out T>(val explicit: T?, val default: T) {
    val value: T get() = explicit ?: default
}

/** Create a copy with a new [default] value */
public fun <T> ValueWithDefault<T>.withDefault(default: T): ValueWithDefault<T> = copy(default = default)

/** Create a copy with a new [explicit] value */
public fun <T> ValueWithDefault<T>.withExplicit(explicit: T): ValueWithDefault<T> = copy(explicit = explicit)