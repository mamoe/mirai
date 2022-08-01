/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")


@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import kotlin.reflect.KProperty

/**
 * WeakRef that `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 */
public class UnsafeWeakRef<T>(private val weakRef: WeakRef<T>) {
    public fun get(): T = weakRef.get() ?: error("WeakRef is released")
    public fun clear(): Unit = weakRef.clear()
}

/**
 * Provides delegate value.
 *
 * ```kotlin
 * val bot: Bot by param.unsafeWeakRef()
 * ```
 */
@JvmSynthetic
public inline operator fun <T> UnsafeWeakRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()

/**
 * Weak Reference.
 * On JVM, it is implemented as a typealias referring to `WeakReference` from JDK.
 *
 * Details:
 * On JVM, instances of objects are stored in the JVM Heap and are accessed via references.
 * GC(garbage collection) can automatically collect and release the memory used by objects that are not directly referred by any other.
 * [WeakRef] is not a direct reference, therefore it doesn't hinder GC.
 *
 * @see weakRef provides a WeakRef
 * @see unsafeWeakRef provides a UnsafeWeakRef
 */
public typealias WeakRef<T> = java.lang.ref.WeakReference<T>

/**
 * Indicates that the property is delegated by a [WeakRef]
 *
 * @see weakRef
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class WeakRefProperty

/**
 * Provides a weak reference to [this]
 * The `getValue` for delegation returns [this] when [this] is not released by GC
 */
@JvmSynthetic
public inline fun <T> T.weakRef(): WeakRef<T> = WeakRef(this)

/**
 * Constructs an unsafe inline delegate for [this]
 */
@JvmSynthetic
public inline fun <T> WeakRef<T>.unsafe(): UnsafeWeakRef<T> = UnsafeWeakRef(this)

/**
 * Provides a weak reference to [this].
 * The `getValue` for delegation throws an [IllegalStateException] if the referent is released by GC. Therefore it returns notnull value only
 *
 * **UNSTABLE API**: It is strongly suggested not to use this api
 */
@JvmSynthetic
public inline fun <T> T.unsafeWeakRef(): UnsafeWeakRef<T> = UnsafeWeakRef(this.weakRef())

/**
 * Provides delegate value.
 *
 * ```kotlin
 * val bot: Bot? by param.weakRef()
 * ```
 */
@JvmSynthetic
public inline operator fun <T> WeakRef<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = this.get()

/**
 * Call the block if the referent is absent
 */
@JvmSynthetic
public inline fun <T, R> WeakRef<T>.ifAbsent(block: (T) -> R): R? = this.get()?.let(block)