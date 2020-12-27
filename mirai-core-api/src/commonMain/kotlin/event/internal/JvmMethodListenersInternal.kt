/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.EventListenerLikeJava
import net.mamoe.mirai.utils.castOrNull
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction


private fun Method.isKotlinFunction(): Boolean {

    if (getDeclaredAnnotation(EventListenerLikeJava::class.java) != null) return false
    if (declaringClass.getDeclaredAnnotation(EventListenerLikeJava::class.java) != null) return false

    @Suppress("RemoveRedundantQualifierName") // for strict
    return declaringClass.getDeclaredAnnotation(kotlin.Metadata::class.java) != null
}

@Suppress("UNCHECKED_CAST")
internal fun Method.registerEventHandler(
    owner: Any,
    eventChannel: EventChannel<*>,
    annotation: EventHandler,
    coroutineContext: CoroutineContext,
): Listener<Event> {
    this.isAccessible = true
    val kotlinFunction = kotlin.runCatching { this.kotlinFunction }.getOrNull()
    return if (kotlinFunction != null && isKotlinFunction()) {
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
            } catch (e: Throwable) {
                throw ExceptionInEventHandlerException(event, cause = e)
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
                eventChannel.subscribeAlways(
                    param[1].type.classifier as KClass<out Event>,
                    coroutineContext,
                    annotation.concurrency,
                    annotation.priority
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            callFunction(this)
                        }
                    } else callFunction(this)
                }.also { listener = it }
            }
            ListeningStatus::class -> {
                eventChannel.subscribe(
                    param[1].type.classifier as KClass<out Event>,
                    coroutineContext,
                    annotation.concurrency,
                    annotation.priority
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
            "Illegal method parameter. Required one exact Event subclass. found ${this.parameters.contentToString()}"
        }
        suspend fun callMethod(event: Event): Any? {
            fun Method.invokeWithErrorReport(self: Any?, vararg args: Any?): Any? = try {
                invoke(self, *args)
            } catch (exception: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Internal Error: $exception, method=${this}, this=$self, arguments=$args, please report to https://github.com/mamoe/mirai",
                    exception
                )
            } catch (e: Throwable) {
                throw ExceptionInEventHandlerException(event, cause = e)
            }


            return if (annotation.ignoreCancelled) {
                if (event.castOrNull<CancellableEvent>()?.isCancelled != true) {
                    withContext(Dispatchers.IO) {
                        this@registerEventHandler.invokeWithErrorReport(owner, event)
                    }
                } else ListeningStatus.LISTENING
            } else withContext(Dispatchers.IO) {
                this@registerEventHandler.invokeWithErrorReport(owner, event)
            }
        }

        when (this.returnType) {
            Void::class.java, Void.TYPE, Nothing::class.java -> {
                eventChannel.subscribeAlways(
                    paramType.kotlin as KClass<out Event>,
                    coroutineContext,
                    annotation.concurrency,
                    annotation.priority
                ) {
                    callMethod(this)
                }
            }
            ListeningStatus::class.java -> {
                eventChannel.subscribe(
                    paramType.kotlin as KClass<out Event>,
                    coroutineContext,
                    annotation.concurrency,
                    annotation.priority
                ) {
                    callMethod(this) as ListeningStatus?
                        ?: error("Java method EventHandler cannot return `null`: $this")
                }
            }
            else -> error("Illegal method return type. Required Void or ListeningStatus, but found ${this.returnType.canonicalName}")
        }
    }
}