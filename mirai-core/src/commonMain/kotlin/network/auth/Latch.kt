/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Volatile


internal interface Latch<T> {
    /**
     * Suspends and waits to acquire the latch.
     * @throws Throwable if [resumeWith] is called with [Result.Failure]
     */
    suspend fun acquire(): T

    /**
     * Release the latch, resuming the coroutines waiting for the latch.
     *
     * This function will return immediately unless a client is calling [acquire] concurrently.
     */
    fun resumeWith(result: Result<T>)
}


internal fun <T> Latch(parentCoroutineContext: CoroutineContext): Latch<T> = LatchImpl(parentCoroutineContext)

private class LatchImpl<T>(
    parentCoroutineContext: CoroutineContext
) : Latch<T> {
    @Volatile
    private var deferred: CompletableDeferred<T>? = CompletableDeferred(parentCoroutineContext[Job])

    private val lock = reentrantLock()

    override suspend fun acquire(): T = lock.withLock {
        val deferred = this.deferred!!
        return deferred.await().also {
            this.deferred = null
        }
    }

    override fun resumeWith(result: Result<T>): Unit = lock.withLock {
        val deferred = this.deferred ?: CompletableDeferred<T>().also { this.deferred = it }
        deferred.completeWith(result)
    }

    override fun toString(): String = "LatchImpl($deferred)"
}