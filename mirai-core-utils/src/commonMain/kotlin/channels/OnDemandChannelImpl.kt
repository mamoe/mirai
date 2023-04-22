/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.channels

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.UtilsLogger
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.debug
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException


internal class CoroutineOnDemandReceiveChannel<T, V>(
    parentCoroutineContext: CoroutineContext,
    private val logger: UtilsLogger,
    private val producerCoroutine: suspend OnDemandSendChannel<T, V>.(initialTicket: T) -> Unit,
) : OnDemandReceiveChannel<T, V> {
    private val coroutineScope = parentCoroutineContext.childScope("CoroutineOnDemandReceiveChannel")

    private val state: AtomicRef<ProducerState<T, V>> = atomic(ProducerState.JustInitialized())


    inner class Producer(
        private val initialTicket: T,
    ) : OnDemandSendChannel<T, V> {
        init {
            coroutineScope.launch {
                try {
                    producerCoroutine(initialTicket)
                } catch (_: CancellationException) {
                    // ignored
                } catch (e: Exception) {
                    finishExceptionally(e)
                }
            }
        }

        override suspend fun emit(value: V): T {
            state.loop { state ->
                when (state) {
                    is ProducerState.Finished -> throw state.createAlreadyFinishedException(null)
                    is ProducerState.Producing -> {
                        val deferred = state.deferred
                        val consumingState = ProducerState.Consuming(
                            state.producer,
                            state.deferred,
                            coroutineScope.coroutineContext
                        )
                        if (compareAndSetState(state, consumingState)) {
                            deferred.complete(value) // produce a value
                            return consumingState.producerLatch.acquire() // wait for producer to consume the previous value.
                        }
                    }

                    is ProducerState.ProducerReady -> {
                        setStateProducing(state)
                    }

                    else -> throw IllegalProducerStateException(state)
                }
            }
        }

        override fun finishExceptionally(exception: Throwable) {
            finishImpl(exception)
        }

        override fun finish() {
            state.loop { state ->
                when (state) {
                    is ProducerState.Finished -> throw state.createAlreadyFinishedException(null)
                    else -> {
                        if (compareAndSetState(state, ProducerState.Finished(state, null))) {
                            return
                        }
                    }
                }
            }
        }
    }

    private fun setStateProducing(state: ProducerState.ProducerReady<T, V>) {
        val deferred = CompletableDeferred<V>(coroutineScope.coroutineContext.job)
        if (!compareAndSetState(state, ProducerState.Producing(state.producer, deferred))) {
            deferred.cancel() // avoid leak
        }
        // loop again
    }

    private fun finishImpl(exception: Throwable?) {
        state.loop { state ->
            when (state) {
                is ProducerState.Finished -> {} // ignore 
                else -> {
                    if (compareAndSetState(state, ProducerState.Finished(state, exception))) {
                        val cancellationException = kotlinx.coroutines.CancellationException("Finished", exception)
                        coroutineScope.cancel(cancellationException)
                        return
                    }
                }
            }
        }
    }

    private fun compareAndSetState(state: ProducerState<T, V>, newState: ProducerState<T, V>): Boolean {
        return this.state.compareAndSet(state, newState).also {
            logger.debug { "CAS: $state -> $newState: $it" }
        }
    }

    override suspend fun receiveOrNull(): V? {
        state.loop { state ->
            when (state) {
                is ProducerState.Consuming -> {
                    // value is ready, switch state to Consumed

                    if (compareAndSetState(state, ProducerState.Consumed(state.producer, state.producerLatch))) {
                        return try {
                            // This actually won't suspend, since the value is already completed
                            // Just to be error-tolerating
                            state.value.await()
                        } catch (e: Exception) {
                            throw ProducerFailureException(cause = e)
                        }
                    }
                }

                // note: actually, this case should be the first case (for code consistency) in `when`, 
                // but atomicfu 1.8.10 fails on this.
                is ProducerState.Producing<T, V> -> {
                    // still producing value

                    state.deferred.await() // just wait for value, but does not return it.

                    // The value will be completed in ProducerState.Consuming state,
                    // but you cannot thread-safely assume current state is Consuming.

                    // Here we will loop again, to atomically switch to Consumed state.
                }

                is ProducerState.Finished -> {
                    state.exception?.let { err ->
                        throw ProducerFailureException(cause = err)
                    }
                    return null
                }

                else -> throw IllegalProducerStateException(state)
            }
        }
    }

    override fun expectMore(ticket: T): Boolean {
        state.loop { state ->
            when (state) {
                is ProducerState.JustInitialized -> {
                    val ready = ProducerState.ProducerReady { Producer(ticket) }
                    if (compareAndSetState(state, ready)) {
                        ready.startProducerIfNotYet()
                    }
                    // loop again
                }

                is ProducerState.ProducerReady -> {
                    setStateProducing(state)
                }

                is ProducerState.Producing -> return true // ok

                is ProducerState.Consuming -> throw IllegalProducerStateException(state) // a value is already ready

                is ProducerState.Consumed -> {
                    if (compareAndSetState(state, ProducerState.ProducerReady { state.producer })) {
                        // wake up producer async.
                        state.producerLatch.resumeWith(Result.success(ticket))
                        // loop again to switch state atomically to Producing. 
                        // Do not do switch state directly here — async producer may race with you! 
                    }
                }

                is ProducerState.Finished -> return false
            }
        }
    }

    override fun finish() {
        finishImpl(null)
    }
}