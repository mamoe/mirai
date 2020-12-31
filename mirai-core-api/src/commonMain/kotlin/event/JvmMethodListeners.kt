/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("Events")
@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.castOrNull
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
 * MyEvents.registerTo(eventChannel)
 * // 或 eventChannel.registerListenerHost(MyEvents)
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
 * eventChannel.registerListenerHost(new MyEventHandlers())
 * ```
 *
 * //@sample net.mamoe.mirai.event.JvmMethodEventsTest
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class EventHandler(
    /**
     * 监听器优先级
     * @see Listener.EventPriority 查看优先级相关信息
     * @see Event.intercept 拦截事件
     */
    public val priority: Listener.EventPriority = EventPriority.NORMAL,
    /**
     * 是否自动忽略被 [取消][CancellableEvent.isCancelled]
     * @see CancellableEvent
     */
    public val ignoreCancelled: Boolean = true,
    /**
     * 并发类型
     * @see Listener.ConcurrencyKind
     */
    public val concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT
)

/**
 * 实现这个接口的对象可以通过 [EventHandler] 标注事件监听函数, 并通过 [registerEvents] 注册.
 *
 * @see SimpleListenerHost 简单的实现
 * @see EventHandler 查看更多信息
 */
public interface ListenerHost

/**
 * 携带一个异常处理器的 [ListenerHost].
 *
 * @see registerTo
 * @see ListenerHost 查看更多信息
 * @see EventHandler 查看更多信息
 */
public abstract class SimpleListenerHost
@JvmOverloads constructor(coroutineContext: CoroutineContext = EmptyCoroutineContext) : ListenerHost, CoroutineScope {

    public final override val coroutineContext: CoroutineContext =
        CoroutineExceptionHandler(::handleException) + coroutineContext + SupervisorJob(coroutineContext[Job])

    /**
     * 处理事件处理中未捕获的异常. 在构造器中的 [coroutineContext] 未提供 [CoroutineExceptionHandler] 情况下必须继承此函数.
     *
     * [exception] 通常是 [ExceptionInEventHandlerException]. 可以获取事件: [ExceptionInEventHandlerException.event]
     */
    public open fun handleException(context: CoroutineContext, exception: Throwable) {
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
    public fun cancelAll() {
        this.cancel()
    }

    protected companion object {
        /**
         * 获取 [ExceptionInEventHandlerException.event]
         */
        @JvmStatic
        protected val Throwable.event: Event?
            get() = this.castOrNull<ExceptionInEventHandlerException>()?.event

        /**
         * 递归获取 [Throwable.cause], 无 `cause` 时返回 `this`
         */
        @JvmStatic
        protected val Throwable.rootCause: Throwable
            get() = generateSequence(this) { it.cause }.last()
    }
}

/**
 * [EventHandler] 标记的函数在处理事件时产生异常时包装异常并重新抛出
 */
public class ExceptionInEventHandlerException(
    /**
     * 当时正在处理的事件
     */
    public val event: Event,
    override val message: String = "Exception in EventHandler",
    /**
     * 原异常
     */
    override val cause: Throwable
) : IllegalStateException()


/**
 * 反射得到所有标注了 [EventHandler] 的函数 (Java 为方法), 并注册为事件监听器
 *
 * @see EventHandler 获取更多信息
 */
@JvmSynthetic
// T 通常可以是 SimpleListenerHost
public inline fun <T> T.registerTo(eventChannel: EventChannel<*>): Unit
        where T : CoroutineScope, T : ListenerHost = eventChannel.parentScope(this).registerListenerHost(this)


@Deprecated(
    "Use EventChannel.registerListenerHost",
    ReplaceWith(
        "this.globalEventChannel(coroutineContext).registerListenerHost(this)",
        "net.mamoe.mirai.event.*"
    ),
    DeprecationLevel.ERROR
)
@PlannedRemoval("2.0-RC")
@JvmOverloads
public fun <T> T.registerEvents(coroutineContext: CoroutineContext = EmptyCoroutineContext): Unit
        where T : CoroutineScope, T : ListenerHost =
    this.globalEventChannel(coroutineContext).registerListenerHost(this)

@Deprecated(
    "Use EventChannel.registerListenerHost",
    ReplaceWith(
        "this.globalEventChannel(coroutineContext).registerListenerHost(host)",
        "net.mamoe.mirai.event.*"
    ),
    DeprecationLevel.ERROR
)
@PlannedRemoval("2.0-RC")
@JvmOverloads
public fun CoroutineScope.registerEvents(
    host: ListenerHost,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Unit = globalEventChannel(coroutineContext).registerListenerHost(host)