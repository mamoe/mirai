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
import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.UtilsLogger
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.debug
import kotlin.coroutines.CoroutineContext


internal class CoroutineOnDemandReceiveChannel<T, V>(
    parentCoroutineContext: CoroutineContext,
    private val logger: UtilsLogger,
    private val producerCoroutine: suspend OnDemandSendChannel<T, V>.(initialTicket: T) -> Unit,
) : OnDemandReceiveChannel<T, V> {
    private val coroutineScope = parentCoroutineContext.childScope("CoroutineOnDemandReceiveChannel")

    @TestOnly
    internal fun getScope() = coroutineScope

    private val state: AtomicRef<ChannelState<T, V>> = atomic(ChannelState.JustInitialized())

    @TestOnly
    internal fun getState() = state.value


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
                } catch (e: Throwable) {
                    // close exceptionally
                    val r = emitImpl(Result.failure(e))
                    check(r == null) // assertion
                    return@launch
                }

                close()
            }
        }

        override suspend fun emit(value: V): T = emitImpl(Result.success(value))!!

        private suspend inline fun emitImpl(value: Result<V>): T? {
            state.loop { state ->
                when (state) {
                    is ChannelState.Finished -> {
                        if (value.isFailure) {
                            return null
                        } else {
                            throw state.createAlreadyFinishedException(null)
                        }
                    }

                    is ChannelState.Producing -> {
                        val deferred = state.deferred
                        val consumingState = ChannelState.Consuming(
                            state.producer,
                            state.deferred,
                            coroutineScope.coroutineContext
                        )
                        if (compareAndSetState(state, consumingState)) {
                            deferred.completeWith(value) // produce a value
                            return consumingState.producerLatch.await() // wait for producer to consume the previous value.
                        }
                        // failed race, try again
                    }

                    is ChannelState.ProducerReady -> {
                        // This implies another coroutine is running `expectMore`,
                        // and we are a bit faster than it!
                        setStateProducing(state)
                    }

                    else -> throw IllegalChannelStateException(
                        state,
                        if (value.isFailure)
                            "Producer threw an exception (see cause), so completing with the exception, but current state is not Producing"
                        else "Producer is emitting an value, but current state is not Producing",
                        value.exceptionOrNull()
                    )

                }
            }
        }
    }

    private fun setStateProducing(state: ChannelState.ProducerReady<T, V>): Boolean {
        return compareAndSetState(state, ChannelState.Producing(state.producer, coroutineScope.coroutineContext.job))
    }

    private fun setStateFinished(
        currState: ChannelState<T, V>,
        message: String,
        exception: ProducerFailureException?
    ): Boolean {
        if (compareAndSetState(currState, ChannelState.Finished(currState, exception))) {
            val cancellationException = CancellationException(message, exception)
            coroutineScope.cancel(cancellationException)
            return true
        }
        return false
    }

    private fun compareAndSetState(state: ChannelState<T, V>, newState: ChannelState<T, V>): Boolean {
        return this.state.compareAndSet(state, newState).also {
            logger.debug { "CAS: $state -> $newState: $it" }
        }
    }

    override val isClosed: Boolean
        get() = state.value is ChannelState.Finished

    override suspend fun receiveOrNull(): V? {
        // don't use atomicfu `.loop`:
        // java.lang.VerifyError: Bad type on operand stack	
        //     net/mamoe/mirai/utils/channels/CoroutineOnDemandReceiveChannel.receiveOrNull(Lkotlin/coroutines/Continuation;)Ljava/lang/Object; @103: getfield	

        while (true) {
            when (val state = state.value) {
                is ChannelState.Consuming -> {
                    // value is ready, now we try to consume the value

                    if (compareAndSetState(state, ChannelState.Consumed(state.producer, state.producerLatch))) {
                        // value is now reserved for us, no contention is possible, safe to retrieve

                        // This actually won't suspend (there are tests ensuring this point), 
                        // since the value is already completed.
                        // Just to be error-tolerating and re-throwing exceptions. 
                        // (Also because `Deferred.getCompleted()` is not stable yet (coroutines 1.6))
                        return awaitValueSafe(state.value)
                    }
                }

                // note: actually, this case should be the first case (for code consistency) in `when`, 
                // but atomicfu 1.8.10 fails on this.
                is ChannelState.Producing<T, V> -> {
                    // still producing value

                    // Wait for value and throw exception caused by the producer if there is one.
                    awaitValueSafe(state.deferred) // this may or may not suspend.

                    // Now deferred is complete, and we will be in the Consuming state, but we can't use the value here.
                    // We must ensure only one thread gets the value, and state should then be Consumed

                    // So we loop again and do this in the Consuming state.
                }

                is ChannelState.Finished -> {
                    // see public API docs for behavior
                    return null
                }

                else ->
                    // internal error 
                    throw IllegalChannelStateException(state)
            }
        }
    }

    private suspend inline fun awaitValueSafe(deferred: Deferred<V>) = try {
        deferred.await()
    } catch (e: Throwable) {
        // Producer failed to produce the previous value with exception
        val producerFailureException = ProducerFailureException(cause = e)
        setStateFinished(
            this.state.value,
            "OnDemandChannel is closed because producer failed to produce value, see cause",
            producerFailureException
        )
        throw producerFailureException
    }

    override fun expectMore(ticket: T): Boolean {
        state.loop { state ->
            when (state) {
                is ChannelState.JustInitialized -> {
                    // start producer atomically
                    val ready = ChannelState.ProducerReady { Producer(ticket) }
                    compareAndSetState(state, ready)
                    // loop again
                }

                is ChannelState.ProducerReady -> {
                    if (setStateProducing(state)) {
                        return true
                    }
                    // lost race, try again
                }

                is ChannelState.Producing,
                is ChannelState.Consuming -> throw IllegalChannelStateException(state) // a value is already ready

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

    override fun close() {
        state.loop { state ->
            when (state) {
                is ChannelState.Finished -> return
                else -> if (setStateFinished(state, "OnDemandChannel is closed normally", null)) return
            }
        }
    }
}