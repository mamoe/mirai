package net.mamoe.mirai.utils


inline fun <T> MutableIterable<T>.removeIfInlined(predicate: (T) -> Boolean) = iterator().removeIfInlined(predicate)

inline fun <T> MutableIterator<T>.removeIfInlined(predicate: (T) -> Boolean) {
    while (this.hasNext()) {
        if (predicate(this.next())) {
            this.remove()
        }
    }
}