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

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass


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
    block: (count: Int, lastException: Throwable?) -> R,
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
    block: (count: Int, lastException: Throwable?) -> R,
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
    block: () -> R,
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
    block: () -> R,
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

public inline fun <R> Result<R>.mapFailure(
    block: (Throwable) -> Throwable,
): Result<R> = onFailure {
    return Result.failure(block(it))
}

public inline fun <R> Result<R>.onSuccessCatching(block: () -> Unit): Result<R> {
    if (isSuccess) {
        runCatching(block).onFailure {
            return@onSuccessCatching Result.failure(it)
        }
    }
    return this
}
