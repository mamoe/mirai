/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE", "unused")
@file:JvmMultifileClass
@file:JvmName("ConsoleUtils")

package net.mamoe.mirai.console.util

import org.jetbrains.annotations.Range
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.InlineOnly

/**
 * 执行 [n] 次 [block], 在第一次成功时返回执行结果, 在捕获到异常时返回异常.
 */
@InlineOnly
public inline fun <R> retryCatching(n: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int, block: () -> R): Result<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    require(n >= 1) { "param n for retryCatching must not be negative" }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            exception?.addSuppressed(e)
            exception = e
        }
    }
    return Result.failure(exception!!)
}