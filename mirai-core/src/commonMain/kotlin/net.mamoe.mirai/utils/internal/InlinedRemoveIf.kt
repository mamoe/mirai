@file:JvmName("IterableUtil")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmName

internal inline fun <T> MutableIterable<T>.inlinedRemoveIf(predicate: (T) -> Boolean) = iterator().inlinedRemoveIf(predicate)

internal inline fun <T> MutableIterator<T>.inlinedRemoveIf(predicate: (T) -> Boolean) {
    while (this.hasNext()) {
        if (predicate(this.next())) {
            this.remove()
        }
    }
}