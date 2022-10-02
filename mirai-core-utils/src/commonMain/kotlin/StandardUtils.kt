/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("StandardUtilsKt_common")

package net.mamoe.mirai.utils

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

public inline fun <reified T> Any?.cast(): T {
    contract { returns() implies (this@cast is T) }
    return this as T
}

/**
 * Casts T to U where U : T. Safer than [cast] -- [castUp] only allow casting to upper types.
 */
public inline fun <reified U : T, T> T.castUp(): U {
    contract { returns() implies (this@castUp is U) }
    return this as U
}

public inline fun <reified T> Any?.safeCast(): T? {
    contract { returnsNotNull() implies (this@safeCast is T) }
    return this as? T
}

public inline fun <reified T> Any?.castOrNull(): T? {
    contract { returnsNotNull() implies (this@castOrNull is T) }
    return this as? T
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
public inline fun <T> Any?.uncheckedCast(): T = this as T


public inline fun <reified R> Iterable<*>.firstIsInstanceOrNull(): R? {
    for (it in this) {
        if (it is R) return it
    }
    return null
}


public inline fun <E> MutableList<E>.replaceAllKotlin(operator: (E) -> E) {
    val li: MutableListIterator<E> = this.listIterator()
    while (li.hasNext()) {
        li.set(operator(li.next()))
    }
}

public fun Throwable.getRootCause(maxDepth: Int = 20): Throwable {
    var depth = 0
    var rootCause: Throwable? = this
    while (rootCause?.cause != null) {
        rootCause = rootCause.cause
        if (depth++ >= maxDepth) break
    }
    return rootCause ?: this
}

/**
 * Use [findCause] instead for better performance.
 */
@TestOnly
public fun Throwable.causes(maxDepth: Int = 20): Sequence<Throwable> = sequence {
    var depth = 0
    var rootCause: Throwable? = this@causes
    while (rootCause?.cause != null) {
        yield(rootCause.cause!!)
        rootCause = rootCause.cause
        if (depth++ >= maxDepth) break
    }
}

public inline fun Throwable.findCause(maxDepth: Int = 20, filter: (Throwable) -> Boolean): Throwable? {
    var depth = 0
    var curr: Throwable? = this
    while (true) {
        if (curr == null) return null
        val cause = curr.cause ?: return null
        if (filter(cause)) return cause

        if (curr.cause === curr) return null // circular reference
        curr = curr.cause

        if (depth++ >= maxDepth) return null
    }
}

/**
 * Run [block] and do [finally], catching exception thrown in [finally] and add it to the exception from [block].
 */
public inline fun <R> trySafely(
    block: () -> R,
    finally: () -> Unit,
): R {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//        callsInPlace(finally, InvocationKind.EXACTLY_ONCE)
//    }
    var eInBlock: Throwable? = null
    try {
        return block()
    } catch (e: Throwable) {
        eInBlock = e
    } finally {
        try {
            finally()
        } catch (eInFinally: Throwable) {
            if (eInBlock != null) {
                eInBlock.addSuppressed(eInFinally)
                throw eInBlock
            } else throw eInFinally
        }
        if (eInBlock != null) throw eInBlock
    }
    throw AssertionError()
}

public inline fun Throwable.findCauseOrSelf(maxDepth: Int = 20, filter: (Throwable) -> Boolean): Throwable =
    findCause(maxDepth, filter) ?: this

public fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

public fun String.truncated(length: Int, truncated: String = "..."): String {
    return if (this.length > length) {
        this.take(10) + truncated
    } else this
}

/**
 * Similar to [run] bot with [Unit] return type.
 *
 * You should not reference to [T] directly in the [block].
 */
// can convert to contextual receiver in the future, or there might be a stdlib function which we can delegate to.
public inline fun <T> T.context(block: T.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block()
}

public fun assertUnreachable(hint: String? = null): Nothing =
    error("This clause should not be reached. " + hint.orEmpty())

public fun isSameClass(object1: Any?, object2: Any?): Boolean {
    if (object1 == null || object2 == null) {
        return object1 == null && object2 == null
    }
    return isSameClassPlatform(object1, object2)
}

internal expect fun isSameClassPlatform(object1: Any, object2: Any): Boolean

public inline fun <reified T> isSameType(thisObject: T, other: Any?): Boolean {
    contract {
        returns(true) implies (other is T)
    }
    if (other == null) return false
    if (other !is T) return false
    return isSameClass(thisObject, other)
}

public expect fun availableProcessors(): Int


/**
 * Localhost 解析
 */
public expect fun localIpAddress(): String
