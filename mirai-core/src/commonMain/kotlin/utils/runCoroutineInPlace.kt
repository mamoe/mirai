/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn

/**
 * Runs the [coroutine] directly in current thread, **expecting no suspension**.
 */
internal fun <R> runCoroutineInPlace(coroutine: suspend () -> R): R {
    var lateResult: CompletableDeferred<R>? = null

    val result = coroutine.startCoroutineUninterceptedOrReturn(Continuation(EmptyCoroutineContext) { r ->
        val deferred: CompletableDeferred<R>? = lateResult
        @Suppress("KotlinConstantConditions")
        if (deferred != null) {
            r.fold(onSuccess = { deferred.complete(it) }, onFailure = { deferred.completeExceptionally(it) })
        } else {
            if (logger.isErrorEnabled) {
                logger.error(IllegalStateException("runCoroutineInPlace reached an unexpected state: coroutine did not finish. ()"))
            }
        }
    })

    if (result != COROUTINE_SUSPENDED) {
        @Suppress("UNCHECKED_CAST")
        return result as R
    }

    lateResult = CompletableDeferred()

    if (logger.isErrorEnabled) {
        logger.error(IllegalStateException("runCoroutineInPlace reached an unexpected state: coroutine did not finish."))
    }

    return runBlocking { lateResult.await() }
}

private val myStubFailure = Exception()

private class RunCoroutineInPlace

private val logger by lazy {
    MiraiLogger.Factory.create(RunCoroutineInPlace::class)
}