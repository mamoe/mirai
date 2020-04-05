/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("DuplicatedCode")

@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.qqandroid.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


@PublishedApi
internal expect fun Throwable.addSuppressedMirai(e: Throwable)

@OptIn(ExperimentalContracts::class)
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <R> retryCatching(n: Int, block: () -> R): Result<R> {
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
