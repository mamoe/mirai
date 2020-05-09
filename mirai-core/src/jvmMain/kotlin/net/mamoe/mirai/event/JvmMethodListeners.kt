/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Events")
@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

/**
 * 标注一个函数为事件监听器.
 *
 * ### Kotlin 函数
 * Kotlin 函数要求:
 * - 接收者和函数参数: 所标注的 Kotlin 函数必须至少拥有一个接收者或一个函数参数, 或二者都具有. 接收者和函数参数的类型必须相同 (如果二者都有)
 *   接收者或函数参数的类型都必须为 [Event] 或其子类.
 * - 返回值: 为 [Unit] 或不指定返回值时将注册为 [CoroutineScope.subscribeAlways], 为 [ListeningStatus] 时将注册为 [CoroutineScope.subscribe]
 *
 * 所有 Kotlin 非 `suspend` 的函数都将会在 [Dispatchers.IO] 中调用
 *
 * 所有支持的函数类型:
 * ```
 * suspend fun T.onEvent(T)
 * suspend fun T.onEvent(T): ListeningStatus
 * suspend fun onEvent(T)
 * suspend fun onEvent(T): ListeningStatus
 * suspend fun T.onEvent()
 * suspend fun T.onEvent(): ListeningStatus
 * fun T.onEvent(T)
 * fun T.onEvent(T): ListeningStatus
 * fun onEvent(T)
 * fun onEvent(T): ListeningStatus
 * fun T.onEvent()
 * fun T.onEvent(): ListeningStatus
 * ```
 *
 * ### Java 方法
 * 所有 Java 方法都会在 [Dispatchers.IO] 中调用.
 *
 * 支持的方法类型
 * ```
 * void onEvent(T)
 * ListeningStatus onEvent(T)
 * ```
 *
 * @sample net.mamoe.mirai.event.JvmMethodEventsTest
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    /**
     * 监听器优先级
     * @see Listener.EventPriority
     * @see Event.intercept
     */
    val priority: Listener.EventPriority = EventPriority.NORMAL,
    /**
     * 是否自动忽略被 [取消][CancellableEvent.isCancelled]
     * @see CancellableEvent
     */
    val ignoreCancelled: Boolean = true,
    /**
     * 并发类型
     * @see Listener.ConcurrencyKind
     */
    val concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT
)

/**
 * 实现这个接口的对象可以通过 [EventHandler] 标注事件监听函数, 并通过 [registerEvents] 注册.
 */
interface ListenerHost

/**
 * 反射得到所有标注了 [EventHandler] 的函数 (Java 为方法), 并注册为事件监听器
 * @see EventHandler 获取更多信息
 */
@JvmOverloads
fun <T> T.registerEvents(coroutineContext: CoroutineContext = EmptyCoroutineContext)
        where T : CoroutineScope, T : ListenerHost = this.registerEvents(this, coroutineContext)

/**
 * 反射得到所有标注了 [EventHandler] 的函数 (Java 为方法), 并注册为事件监听器
 * @see EventHandler 获取更多信息
 */
@JvmOverloads
fun CoroutineScope.registerEvents(host: ListenerHost, coroutineContext: CoroutineContext = EmptyCoroutineContext) {
    for (method in host.javaClass.declaredMethods) {
        method.getAnnotation(EventHandler::class.java)?.let {
            method.registerEvent(host, this, it, coroutineContext)
        }
    }
}



@Suppress("UNCHECKED_CAST")
private fun Method.registerEvent(
    owner: Any,
    scope: CoroutineScope,
    annotation: EventHandler,
    coroutineContext: CoroutineContext
): Listener<Event> {
    this.isAccessible = true
    val kotlinFunction = this.kotlinFunction
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
        kotlinFunction.isAccessible = true
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
        when (kotlinFunction.returnType.classifier) {
            Unit::class -> {
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
            else -> error("Illegal method return type. Required Void or ListeningStatus, found ${kotlinFunction.returnType.classifier}")
        }
    } else {
        // java methods

        val paramType = this.parameters[0].type
        check(this.parameterCount == 1 && Event::class.java.isAssignableFrom(paramType)) {
            "Illegal method parameter. Required one exact Event subclass. found $paramType"
        }
        when (this.returnType) {
            Void::class.java -> {
                scope.subscribeAlways(
                    paramType.kotlin as KClass<out Event>,
                    priority = annotation.priority,
                    concurrency = annotation.concurrency,
                    coroutineContext = coroutineContext
                ) {
                    if (annotation.ignoreCancelled) {
                        if ((this as? CancellableEvent)?.isCancelled != true) {
                            withContext(Dispatchers.IO) {
                                this@registerEvent.invoke(owner, this)
                            }
                        }
                    } else withContext(Dispatchers.IO) {
                        this@registerEvent.invoke(owner, this)
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
                                this@registerEvent.invoke(owner, this) as ListeningStatus
                            }
                        } else ListeningStatus.LISTENING
                    } else withContext(Dispatchers.IO) {
                        this@registerEvent.invoke(owner, this) as ListeningStatus
                    }

                }
            }
            else -> error("Illegal method return type. Required Void or ListeningStatus, but found ${this.returnType.canonicalName}")
        }
    }
}