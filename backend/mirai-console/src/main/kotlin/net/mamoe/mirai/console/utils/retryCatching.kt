@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")

package net.mamoe.mirai.console.utils

import org.jetbrains.annotations.Range
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 执行 [n] 次 [block], 在第一次成功时返回执行结果, 在捕获到异常时返回异常.
 */
@kotlin.internal.InlineOnly
inline fun <R> retryCatching(n: @Range(from = 1, to = Long.MAX_VALUE) Int, block: () -> R): Result<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    require(n >= 0) { "param n for retryCatching must not be negative" }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            exception?.addSuppressedMirai(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}

@PublishedApi
internal fun Throwable.addSuppressedMirai(e: Throwable) {
    if (e === this) {
        return
    }
    kotlin.runCatching {
        this.addSuppressed(e)
    }
}