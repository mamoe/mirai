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
import kotlinx.coroutines.*
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

    private val state: AtomicRef<ChannelState<T, V>> = atomic(ChannelState.JustInitialized())


    inner class Producer(
        private val initialTicket: T,
    ) : OnDemandSendChannel<T, V> {
        init {
            // `UNDISPATCHED` with `yield()`: start the coroutine immediately in current thread,
            // attaching Job to the coroutineScope, then `yield` the thread back, to complete `launch`.
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                yield()
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
                    is ChannelState.Finished -> throw state.createAlreadyFinishedException(null)
                    is ChannelState.Producing -> {
                        val deferred = state.deferred
                        val consumingState = ChannelState.Consuming(
                            state.producer,
                            state.deferred,
                            coroutineScope.coroutineContext
                        )
                        if (compareAndSetState(state, consumingState)) {
                            deferred.complete(value) // produce a value
                            return consumingState.producerLatch.await() // wait for producer to consume the previous value.
                        }
                    }

                    is ChannelState.ProducerReady -> {
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
                    is ChannelState.Finished -> throw state.createAlreadyFinishedException(null)
                    else -> {
                        if (compareAndSetState(state, ChannelState.Finished(state, null))) {
                            return
                        }
                    }
                }
            }
        }
    }

    private fun setStateProducing(state: ChannelState.ProducerReady<T, V>) {
        compareAndSetState(state, ChannelState.Producing(state.producer, coroutineScope.coroutineContext.job))
    }

    private fun finishImpl(exception: Throwable?) {
        state.loop { state ->
            when (state) {
                is ChannelState.Finished -> {} // ignore 
                else -> {
                    if (compareAndSetState(state, ChannelState.Finished(state, exception))) {
                        val cancellationException = kotlinx.coroutines.CancellationException("Finished", exception)
                        coroutineScope.cancel(cancellationException)
                        return
                    }
                }
            }
        }
    }

    private fun compareAndSetState(state: ChannelState<T, V>, newState: ChannelState<T, V>): Boolean {
        return this.state.compareAndSet(state, newState).also {
            logger.debug { "CAS: $state -> $newState: $it" }
        }
    }

    override suspend fun receiveOrNull(): V? {
        // don't use `.loop`:
        // java.lang.VerifyError: Bad type on operand stack	
        //     net/mamoe/mirai/utils/channels/CoroutineOnDemandReceiveChannel.receiveOrNull(Lkotlin/coroutines/Continuation;)Ljava/lang/Object; @103: getfield	

        while (true) {
            when (val state = state.value) {
                is ChannelState.Consuming -> {
                    // value is ready, now we consume the value

                    if (compareAndSetState(state, ChannelState.Consumed(state.producer, state.producerLatch))) {
                        // value is consumed, no contention, safe to retrieve 

                        return try {
                            // This actually won't suspend, since the value is already completed
                            // Just to be error-tolerating and re-throwing exceptions.
                            state.value.await()
                        } catch (e: Throwable) {
                            // Producer failed to produce the previous value with exception
                            throw ProducerFailureException(cause = e)
                        }
                    }
                }

                // note: actually, this case should be the first case (for code consistency) in `when`, 
                // but atomicfu 1.8.10 fails on this.
                is ChannelState.Producing<T, V> -> {
                    // still producing value

                    state.deferred.await() // just wait for value, but does not return it.

                    // The value will be completed in ProducerState.Consuming state,
                    // but you cannot thread-safely assume current state is Consuming.

                    // Here we will loop again, to atomically switch to Consumed state.
                }

                is ChannelState.Finished -> {
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
                is ChannelState.JustInitialized -> {
                    // start producer atomically
                    val ready = ChannelState.ProducerReady { Producer(ticket) }
                    if (compareAndSetState(state, ready)) {
                        ready.startProducerIfNotYet()
                    }
                    // loop again
                }

                is ChannelState.ProducerReady -> {
                    setStateProducing(state)
                }

                is ChannelState.Producing -> return true // ok

                is ChannelState.Consuming -> throw IllegalProducerStateException(state) // a value is already ready

                is ChannelState.Consumed -> {
                    if (compareAndSetState(state, ChannelState.ProducerReady { state.producer })) {
                        // wake up producer async.
                        state.producerLatch.complete(ticket)
                        // loop again to switch state atomically to Producing. 
                        // Do not do switch state directly here — async producer may race with you! 
                    }
                }

                is ChannelState.Finished -> return false
            }
        }
    }

    override fun finish() {
        finishImpl(null)
    }
}