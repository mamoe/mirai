@file:Suppress("unused")

package net.mamoe.mirai.utils

import kotlin.reflect.KProperty

/**
 * WeakRef that `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 */
inline class UnsafeWeakRef<T>(private val weakRef: WeakRef<T>) {
    fun get(): T = weakRef.get() ?: error("WeakRef is released")
    fun clear() = weakRef.clear()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = this.get()
}

/**
 * Weak Reference.
 * In JVM, this is implemented as a typealias to `WeakReference` from JDK.
 *
 * Reference details:
 * In JVM, instances of objects are stored in the Heap and are accessed via references.
 * GC can automatically collect and release the memory used by objects that are not directly referred by any other.
 * WeakReference is not direct reference, therefore it does no influence on garbage collection.
 * Using weak reference can help GC with that.
 *
 * @see weakRef provides a WeakRef
 * @see unsafeWeakRef provides a UnsafeWeakRef
 */
expect class WeakRef<T>(referent: T) {
    fun get(): T?
    fun clear()
}

/**
 * Provides a weak reference to [this]
 * The `getValue` for delegation returns [this] when [this] is not released by GC
 */
fun <T> T.weakRef(): WeakRef<T> = WeakRef(this)

/**
 * Provides a weak reference to [this].
 * The `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 */
fun <T> T.unsafeWeakRef(): UnsafeWeakRef<T> = UnsafeWeakRef(this.weakRef())

/**
 * Provides delegate value.
 *
 * ```kotlin
 * val bot: Bot? by param.weakRef()
 * ```
 */
operator fun <T> WeakRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = this.get()

/**
 * Call the block if the referent is absent
 */
inline fun <T, R> WeakRef<T>.ifAbsent(block: (T) -> R): R? = this.get()?.let(block)