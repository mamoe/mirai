/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

/*

/**
 * SoftRef that `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 */
class UnsafeSoftRef<T>(private val softRef: SoftRef<T>) {
    fun get(): T = softRef.get() ?: error("SoftRef is released")
    fun clear() = softRef.clear()
}

/**
 * Provides delegate value.
 *
 * ```kotlin
 * val bot: Bot by param.unsafeSoftRef()
 * ```
 */
@JvmSynthetic
inline operator fun <T> UnsafeSoftRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()

/**
 * Soft Reference.
 * On JVM, it is implemented as a typealias referring to `SoftReference` from JDK.
 *
 * Details:
 * On JVM, instances of objects are stored in the JVM Heap and are accessed via references.
 * GC(garbage collection) can automatically collect and release the memory used by objects that are not directly referred by any other.
 * [SoftRef] will keep the reference until JVM run out of memory.
 *
 * @see softRef provides a SoftRef
 * @see unsafeSoftRef provides a UnsafeSoftRef
 */
expect class SoftRef<T>(referent: T) {
    fun get(): T?
    fun clear()
}

/**
 * Indicates that the property is delegated by a [SoftRef]
 *
 * @see softRef
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SoftRefProperty

/**
 * Provides a soft reference to [this]
 * The `getValue` for delegation returns [this] when [this] is not released by GC
 */
@JvmSynthetic
inline fun <T> T.softRef(): SoftRef<T> = SoftRef(this)

/**
 * Constructs an unsafe inline delegate for [this]
 */
@JvmSynthetic
inline fun <T> SoftRef<T>.unsafe(): UnsafeSoftRef<T> = UnsafeSoftRef(this)

/**
 * Provides a soft reference to [this].
 * The `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 *
 * **UNSTABLE API**: It is strongly suggested not to use this api
 */
@JvmSynthetic
inline fun <T> T.unsafeSoftRef(): UnsafeSoftRef<T> = UnsafeSoftRef(this.softRef())

/**
 * Provides delegate value.
 *
 * ```kotlin
 * val bot: Bot? by param.softRef()
 * ```
 */
@JvmSynthetic
inline operator fun <T> SoftRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = this.get()

/**
 * Call the block if the referent is absent
 */
@JvmSynthetic
inline fun <T, R> SoftRef<T>.ifAbsent(block: (T) -> R): R? = this.get()?.let(block)

 */