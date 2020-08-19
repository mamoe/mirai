/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.*
import java.lang.reflect.Method
import java.util.function.Consumer
import java.util.function.Function
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

@Suppress("FunctionName")
internal fun <E : Event> Class<E>._subscribeEventForJaptOnly(
    scope: CoroutineScope,
    onEvent: Function<E, ListeningStatus>
): Listener<E> {
    return this.kotlin.subscribeInternal(
        scope.Handler(
            scope.coroutineContext,
            Listener.ConcurrencyKind.LOCKED
        ) { withContext(Dispatchers.IO) { onEvent.apply(it) } })
}

@Suppress("FunctionName")
internal fun <E : Event> Class<E>._subscribeEventForJaptOnly(scope: CoroutineScope, onEvent: Consumer<E>): Listener<E> {
    return this.kotlin.subscribeInternal(
        scope.Handler(
            EmptyCoroutineContext,
            Listener.ConcurrencyKind.LOCKED
        ) { withContext(Dispatchers.IO) { onEvent.accept(it) }; ListeningStatus.LISTENING; })
}



@Suppress("UNCHECKED_CAST")
internal fun Method.registerEvent(
    owner: Any,
    scope: CoroutineScope,
    annotation: EventHandler,
    coroutineContext: CoroutineContext
): Listener<Event> {
    this.isAccessible = true
    val kotlinFunction = kotlin.runCatching { this.kotlinFunction }.getOrNull()
    return if (kotlinFunction != null) {
        // kotlin functions

        val param = kotlinFunction.parameters
        when (param.size) {
            3 -> { // ownerClass, receiver, event
                check(param[1].type == param[2].type) { "Illegal kotlin function ${kotlinFunction.name}. Receiver and param must have same type" }
                check((param[1].type.classifier as? KClass<*>)?.isSubclassOf(Event::class) == true) {
                    "Illegal kotlin function ${kotlinFunction.name}. First param or receiver must be subclass of Event, but found ${param[1].type.classifier}"
                }
            }
            2 -> { // ownerClass, event
                check((param[1].type.classifier as? KClass<*>)?.isSubclassOf(Event::class) == true) {
                    "Illegal kotlin function ${kotlinFunction.name}. First param or receiver must be subclass of Event, but found ${param[1].type.classifier}"
                }
            }
            else -> error("function ${kotlinFunction.name} must have one Event param")
        }
        lateinit var listener: Listener<*>
        kotlin.runCatching {
            kotlinFunction.isAccessible = true
        }
        suspend fun callFunction(event: Event): Any? {
            try {
                return when (param.size) {
                    3 -> {
                        if (kotlinFunction.isSuspend) {
                            kotlinFunction.callSuspend(owner, event, event)
                        } else withContext(Dispatchers.IO) { // for safety
                            kotlinFunction.call(owner, event, event)
                        }

                    }
                    2 -> {
                        if (kotlinFunction.isSuspend) {
                            kotlinFunction.callSuspend(owner, event)
                        } else withContext(Dispatchers.IO) { // for safety
                            kotlinFunction.call(owner, event)
                        }
                    }
                    else -> error("stub")
                }
            } catch (e: IllegalCallableAccessException) {
                listener.completeExceptionally(e)
                return ListeningStatus.STOPPED
            }
        }
        require(!kotlinFunction.returnType.isMarkedNullable) {
            "Kotlin event handlers cannot have nullable return type."
        }
        require(kotlinFunction.parameters.none { it.type.isMarkedNullable }) {
            "Kotlin event handlers cannot have nullable parameter type."
        }
        when (kotlinFunction.returnType.classifier) {
            Unit::class, Nothing::class -> {
                scope.subscribeAlways(
                    param[1].type.classifier as KClass<out Event>,
                    priority = annotation.priority,
                    concurrency = annotation.concurrency,
                    coroutineContext = coroutineContext
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            callFunction(this)
                        }
                    } else callFunction(this)
                }.also { listener = it }
            }
            ListeningStatus::class -> {
                scope.subscribe(
                    param[1].type.classifier as KClass<out Event>,
                    priority = annotation.priority,
                    concurrency = annotation.concurrency,
                    coroutineContext = coroutineContext
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            callFunction(this) as ListeningStatus
                        } else ListeningStatus.LISTENING
                    } else callFunction(this) as ListeningStatus
                }.also { listener = it }
            }
            else -> error("Illegal method return type. Required Void, Nothing or ListeningStatus, found ${kotlinFunction.returnType.classifier}")
        }
    } else {
        // java methods

        val paramType = this.parameters[0].type
        check(this.parameterCount == 1 && Event::class.java.isAssignableFrom(paramType)) {
            "Illegal method parameter. Required one exact Event subclass. found $paramType"
        }
        when (this.returnType) {
            Void::class.java, Void.TYPE, Nothing::class.java -> {
                scope.subscribeAlways(
                    paramType.kotlin as KClass<out Event>,
                    priority = annotation.priority,
                    concurrency = annotation.concurrency,
                    coroutineContext = coroutineContext
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            withContext(Dispatchers.IO) {
                                this@registerEvent.invoke(owner, this@subscribeAlways)
                            }
                        }
                    } else withContext(Dispatchers.IO) {
                        this@registerEvent.invoke(owner, this@subscribeAlways)
                    }
                }
            }
            ListeningStatus::class.java -> {
                scope.subscribe(
                    paramType.kotlin as KClass<out Event>,
                    priority = annotation.priority,
                    concurrency = annotation.concurrency,
                    coroutineContext = coroutineContext
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            withContext(Dispatchers.IO) {
                                this@registerEvent.invoke(owner, this@subscribe) as ListeningStatus
                            }
                        } else ListeningStatus.LISTENING
                    } else withContext(Dispatchers.IO) {
                        this@registerEvent.invoke(owner, this@subscribe) as ListeningStatus
                    }

                }
            }
            else -> error("Illegal method return type. Required Void or ListeningStatus, but found ${this.returnType.canonicalName}")
        }
    }
}