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
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import java.util.Spliterators.AbstractSpliterator
import java.util.concurrent.ArrayBlockingQueue
import java.util.function.Consumer
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.coroutines.*
import kotlin.streams.asStream

@JvmSynthetic
public inline fun <T> stream(@BuilderInference noinline block: suspend SequenceScope<T>.() -> Unit): Stream<T> =
    sequence(block).asStream()

@Suppress("RemoveExplicitTypeArguments")
public object JdkStreamSupport {
    private class CompleteToken(val error: Throwable?) {
        override fun toString(): String {
            return "CompleteToken[$error]"
        }
    }

    private val NULL_PLACEHOLDER = Symbol("null")!!

    /*
    Implementation:

    Spliterator.tryAdvance():
        - Resume coroutine
        (*Wait for collector.emit*)
        - Re-Suspend flow
        - Put response to queue

        - Fire response to jdk consumer

    Completion & Exception caught:
        (* Spliterator.tryAdvance(): Resume coroutine *)
        (* No more values or exception thrown. *)
        (* completion called *)
        - Put the exception or the completion token to queue

        - Throw exception in Spliterator.tryAdvance() if possible
        - Return false in Spliterator.tryAdvance()

     */
    public fun <T> Flow<T>.toStream(
        context: CoroutineContext = EmptyCoroutineContext,
    ): Stream<T> {
        val spliterator = FlowSpliterator(
            flow = this,
            coroutineContext = context,
        )

        return StreamSupport.stream(spliterator, false).onClose {
            spliterator.cancelled = true
            spliterator.nextStep?.let { nextStep ->
                if (nextStep is CancellableContinuation<*>) {
                    nextStep.cancel()
                } else {
                    nextStep.resumeWithException(CancellationException())
                }
            }
            spliterator.nextStep = null
        }
    }

    private class FlowSpliterator<T>(
        private val flow: Flow<T>,
        private val coroutineContext: CoroutineContext,
    ) : AbstractSpliterator<T>(
        Long.MAX_VALUE, Spliterator.ORDERED or Spliterator.IMMUTABLE
    ) {

        private val queue = ArrayBlockingQueue<Any?>(1)
        private var completed = false

        @JvmField
        var cancelled = false

        @JvmField
        var nextStep: Continuation<Unit>? = run {
            val completion = object : Continuation<Unit> {
                override val context: CoroutineContext get() = coroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    nextStep = null
                    completed = true
                    queue.put(CompleteToken(result.exceptionOrNull()))
                }

            }
            return@run (suspend {
                flow.collect { item ->
                    suspendCancellableCoroutine<Unit> { cont ->
                        nextStep = cont
                        queue.put(boxValue(item))
                    }
                }
            }).createCoroutine(completion)
        }

        private inline fun boxValue(value: Any?): Any {
            return value ?: NULL_PLACEHOLDER
        }

        private fun unboxResponse(value: Any?, action: Consumer<in T>): Boolean {
            if (value is CompleteToken) { // completion & exception caught
                value.error?.let { throw boxError(it) }
                completed = true
                return false // no more value available
            }

            if (value === NULL_PLACEHOLDER) { // null
                @Suppress("UNCHECKED_CAST")
                action.accept(null as T)
            } else {
                @Suppress("UNCHECKED_CAST")
                action.accept(value as T)
            }
            return true
        }

        override fun tryAdvance(action: Consumer<in T>): Boolean {
            if (completed) return false

            if (queue.isNotEmpty()) {
                return unboxResponse(queue.take(), action)
            }
            if (cancelled) return false

            val step = nextStep!!
            nextStep = null
            step.resume(Unit)

            return unboxResponse(queue.take(), action)
        }

    }

    private fun boxError(error: Throwable): Throwable {
        return ExceptionInFlowException(error)
    }

    // @PublishedApi
    public open class ExceptionInFlowException : RuntimeException {
        public constructor() : super()
        public constructor(msg: String?) : super(msg)
        public constructor(cause: Throwable?) : super(cause)
        public constructor(msg: String?, cause: Throwable?) : super(msg, cause)
    }
}

