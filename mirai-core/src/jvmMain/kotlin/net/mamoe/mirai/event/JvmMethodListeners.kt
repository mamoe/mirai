/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("Events")
@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
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
 * - 接收者 (英 receiver) 和函数参数: 所标注的 Kotlin 函数必须至少拥有一个接收者或一个函数参数, 或二者都具有. 接收者和函数参数的类型必须相同 (如果二者都存在)
 *   接收者或函数参数的类型都必须为 [Event] 或其子类.
 * - 返回值: 为 [Unit] 或不指定返回值时将注册为 [CoroutineScope.subscribeAlways], 为 [ListeningStatus] 时将注册为 [CoroutineScope.subscribe].
 *   任何其他类型的返回值将会在注册时抛出异常.
 *
 * 所有 Kotlin 非 `suspend` 的函数都将会在 [Dispatchers.IO] 中调用
 *
 * 支持的函数类型:
 * ```
 * // 所有函数参数, 函数返回值都不允许标记为可空 (带有 '?' 符号)
 * // T 表示任何 Event 类型.
 * suspend fun T.onEvent(T)
 * suspend fun T.onEvent(T): ListeningStatus
 * suspend fun T.onEvent(T): Nothing
 * suspend fun onEvent(T)
 * suspend fun onEvent(T): ListeningStatus
 * suspend fun onEvent(T): Nothing
 * suspend fun T.onEvent()
 * suspend fun T.onEvent(): ListeningStatus
 * suspend fun T.onEvent(): Nothing
 * fun T.onEvent(T)
 * fun T.onEvent(T): ListeningStatus
 * fun T.onEvent(T): Nothing
 * fun onEvent(T)
 * fun onEvent(T): ListeningStatus
 * fun onEvent(T): Nothing
 * fun T.onEvent()
 * fun T.onEvent(): ListeningStatus
 * fun T.onEvent(): Nothing
 * ```
 *
 * Kotlin 使用示例:
 * - 独立 [CoroutineScope] 和 [ListenerHost]
 * ```
 * object MyEvents : ListenerHost {
 *     override val coroutineContext = SupervisorJob()
 *
 *
 *     // 可以抛出任何异常, 将在 this.coroutineContext 或 registerEvents 时提供的 CoroutineScope.coroutineContext 中的 CoroutineExceptionHandler 处理.
 *     @EventHandler
 *     suspend fun MessageEvent.onMessage() {
 *         reply("received")
 *     }
 * }
 *
 * myCoroutineScope.registerEvents(MyEvents)
 * ```
 * `onMessage` 抛出的异常将会交给 `myCoroutineScope` 处理
 *
 *
 * - 合并 [CoroutineScope] 和 [ListenerHost]: 使用 [SimpleListenerHost]
 * ```
 * object MyEvents : SimpleListenerHost( /* override coroutineContext here */ ) {
 *     override fun handleException(context: CoroutineContext, exception: Throwable) {
 *         // 处理 onMessage 中未捕获的异常
 *     }
 *
 *     @EventHandler
 *     suspend fun MessageEvent.onMessage() { // 可以抛出任何异常, 将在 handleException 处理
 *         reply("received")
 *         // 无返回值 (或者返回 Unit), 表示一直监听事件.
 *     }
 *
 *     @EventHandler
 *     suspend fun MessageEvent.onMessage(): ListeningStatus { // 可以抛出任何异常, 将在 handleException 处理
 *         reply("received")
 *
 *         return ListeningStatus.LISTENING // 表示继续监听事件
 *         // return ListeningStatus.STOPPED // 表示停止监听事件
 *     }
 * }
 *
 * MyEvents.registerEvents()
 * ```
 *
 *
 * ### Java 方法
 * 所有 Java 方法都会在 [Dispatchers.IO] 中调用.
 *
 * 支持的方法类型
 * ```
 * // T 表示任何 Event 类型.
 * void onEvent(T)
 * Void onEvent(T)
 * @NotNull ListeningStatus onEvent(T) // 返回 null 时将抛出异常
 * ```
 *
 *
 * Java 使用示例:
 * ```
 * public class MyEventHandlers extends SimpleListenerHost {
 *     @Override
 *     public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
 *         // 处理事件处理时抛出的异常
 *     }
 *
 *     @EventHandler
 *     public void onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
 *         event.subject.sendMessage("received");
 *         // 无返回值, 表示一直监听事件.
 *     }
 *
 *     @NotNull
 *     @EventHandler
 *     public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
 *         event.subject.sendMessage("received");
 *
 *         return ListeningStatus.LISTENING; // 表示继续监听事件
 *         // return ListeningStatus.STOPPED; // 表示停止监听事件
 *     }
 * }
 *
 * Events.registerEvents(new MyEventHandlers())
 * ```
 *
 * //@sample net.mamoe.mirai.event.JvmMethodEventsTest
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    /**
     * 监听器优先级
     * @see Listener.EventPriority 查看优先级相关信息
     * @see Event.intercept 拦截事件
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
 *
 * @see SimpleListenerHost 简单的实现
 * @see EventHandler 查看更多信息
 */
interface ListenerHost

/**
 * 携带一个异常处理器的 [ListenerHost].
 * @see ListenerHost 查看更多信息
 * @see EventHandler 查看更多信息
 */
abstract class SimpleListenerHost
@JvmOverloads constructor(coroutineContext: CoroutineContext = EmptyCoroutineContext) : ListenerHost, CoroutineScope {

    final override val coroutineContext: CoroutineContext =
        CoroutineExceptionHandler(::handleException) + coroutineContext + SupervisorJob(coroutineContext[Job])

    /**
     * 处理事件处理中未捕获的异常. 在构造器中的 [coroutineContext] 未提供 [CoroutineExceptionHandler] 情况下必须继承此函数.
     */
    open fun handleException(context: CoroutineContext, exception: Throwable) {
        throw IllegalStateException(
            """
            未找到异常处理器. 请继承 SimpleListenerHost 中的 handleException 方法, 或在构造 SimpleListenerHost 时提供 CoroutineExceptionHandler
            ------------
            Cannot find exception handler from coroutineContext. 
            Please extend SimpleListenerHost.handleException or provide a CoroutineExceptionHandler to the constructor of SimpleListenerHost
        """.trimIndent(), exception
        )
    }

    /**
     * 停止所有事件监听
     */
    fun cancelAll() {
        this.cancel()
    }
}

/**
 * 反射得到所有标注了 [EventHandler] 的函数 (Java 为方法), 并注册为事件监听器
 *
 * Java 使用: `Events.registerEvents(listenerHost)`
 *
 * @see EventHandler 获取更多信息
 */
@JvmOverloads
fun <T> T.registerEvents(coroutineContext: CoroutineContext = EmptyCoroutineContext)
        where T : CoroutineScope, T : ListenerHost = this.registerEvents(this, coroutineContext)

/**
 * 反射得到所有标注了 [EventHandler] 的函数 (Java 为方法), 并注册为事件监听器
 *
 * Java 使用: `Events.registerEvents(coroutineScope, host)`
 *
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
    val kotlinFunction = kotlin.runCatching { this.kotlinFunction }.getOrNull()
    return if (kotlinFunction != null) {
        // kotlin functions

        val param = kotlinFunction.parameters
        when (param.size) {
            3 -> { // dispatch receiver, extension receiver, param #0 event
                check(param[1].type == param[2].type) { "Illegal kotlin function ${kotlinFunction.name}. Receiver and param must have same type" }
                check((param[1].type.classifier as? KClass<*>)?.isSubclassOf(Event::class) == true) {
                    "Illegal kotlin function ${kotlinFunction.name}. First param or extension receiver must be subclass of Event, but found ${param[1].type.classifier}"
                }
            }
            2 -> { // dispatch receiver, param #0 event
                check((param[1].type.classifier as? KClass<*>)?.isSubclassOf(Event::class) == true) {
                    "Illegal kotlin function ${kotlinFunction.name}. First param or extension receiver must be subclass of Event, but found ${param[1].type.classifier}"
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
        check(this.parameterCount == 1) {
            "Illegal method parameter. Only one parameter is required."
        }
        val paramType = this.parameters[0].type
        check(Event::class.java.isAssignableFrom(paramType)) {
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