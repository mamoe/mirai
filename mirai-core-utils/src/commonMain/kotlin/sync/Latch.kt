/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


public interface Latch<T> {
    /**
     * Suspends and waits to acquire the latch.
     * @throws Throwable if [resumeWith] is called with [Result.Failure]
     */
    public suspend fun acquire(): T

    /**
     * Release the latch, resuming the coroutines waiting for the latch.
     *
     * This function will return immediately unless a client is calling [acquire] concurrently.
     */
    public fun resumeWith(result: Result<T>)
}


public fun <T> Latch(parentCoroutineContext: CoroutineContext = EmptyCoroutineContext): Latch<T> =
    LatchImpl(parentCoroutineContext)

private class LatchImpl<T>(
    parentCoroutineContext: CoroutineContext
) : Latch<T> {
    private val deferred: CompletableDeferred<T> = CompletableDeferred(parentCoroutineContext[Job])


    override suspend fun acquire(): T = deferred.await()

    override fun resumeWith(result: Result<T>) {
        if (!deferred.completeWith(result)) {
            error("$this was already resumed")
        }
    }

    override fun toString(): String = "LatchImpl($deferred)"
}