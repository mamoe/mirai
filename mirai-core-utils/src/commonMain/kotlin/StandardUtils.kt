/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import java.util.*
import kotlin.reflect.KClass

public inline fun <reified T> Any?.cast(): T = this as T

public inline fun <reified T> Any?.safeCast(): T? = this as? T

public inline fun <reified T> Any?.castOrNull(): T? = this as? T

public inline fun <reified R> Iterable<*>.firstIsInstanceOrNull(): R? {
    for (it in this) {
        if (it is R) return it
    }
    return null
}


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <R, T : R> Result<T>.recoverCatchingSuppressed(transform: (exception: Throwable) -> R): Result<R> {
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> {
            try {
                Result.success(transform(exception))
            } catch (e: Throwable) {
                e.addSuppressed(exception)
                Result.failure(e)
            }
        }
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <R> retryCatching(
    n: Int,
    except: KClass<out Throwable>? = null,
    block: (count: Int, lastException: Throwable?) -> R
): Result<R> {
    require(n >= 0) {
        "param n for retryCatching must not be negative"
    }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block(it, exception))
        } catch (e: Throwable) {
            if (except?.isInstance(e) == true) {
                return Result.failure(e)
            }
            exception?.addSuppressed(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
@kotlin.internal.LowPriorityInOverloadResolution
public inline fun <R> retryCatchingExceptions(
    n: Int,
    except: KClass<out Exception>? = null,
    block: (count: Int, lastException: Throwable?) -> R
): Result<R> {
    require(n >= 0) {
        "param n for retryCatching must not be negative"
    }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block(it, exception))
        } catch (e: Exception) {
            if (except?.isInstance(e) == true) {
                return Result.failure(e)
            }
            exception?.addSuppressed(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
public inline fun <R> retryCatching(
    n: Int,
    except: KClass<out Throwable>? = null,
    block: () -> R
): Result<R> {
    require(n >= 0) {
        "param n for retryCatching must not be negative"
    }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            if (except?.isInstance(e) == true) {
                return Result.failure(e)
            }
            exception?.addSuppressed(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
public inline fun <R> retryCatchingExceptions(
    n: Int,
    except: KClass<out Exception>? = null,
    block: () -> R
): Result<R> {
    require(n >= 0) {
        "param n for retryCatching must not be negative"
    }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block())
        } catch (e: Exception) {
            if (except?.isInstance(e) == true) {
                return Result.failure(e)
            }
            exception?.addSuppressed(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
public inline fun <R> runCatchingExceptions(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

public inline fun <E> MutableList<E>.replaceAllKotlin(operator: (E) -> E) {
    val li: MutableListIterator<E> = this.listIterator()
    while (li.hasNext()) {
        li.set(operator(li.next()))
    }
}

public fun systemProp(name: String, default: String): String =
    System.getProperty(name, default) ?: default

public fun systemProp(name: String, default: Boolean): Boolean =
    System.getProperty(name, default.toString())?.toBoolean() ?: default


public fun systemProp(name: String, default: Long): Long =
    System.getProperty(name, default.toString())?.toLongOrNull() ?: default


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
    var rootCause: Throwable? = this
    while (true) {
        if (rootCause?.cause === rootCause) return rootCause
        val current = rootCause?.cause ?: return null
        if (filter(current)) return current
        rootCause = rootCause.cause
        if (depth++ >= maxDepth) return null
    }
}

public inline fun Throwable.findCauseOrSelf(maxDepth: Int = 20, filter: (Throwable) -> Boolean): Throwable =
    findCause(maxDepth, filter) ?: this

public fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}