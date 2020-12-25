/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.Listener.ConcurrencyKind.LOCKED
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.GlobalEventListeners
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.ListenerRegistry
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass

/**
 * 包装 [EventChannel.filter] 的 `filter` lambda 抛出的异常并重新抛出.
 */
public class ExceptionInEventChannelFilterException(
    /**
     * 当时正在处理的事件
     */
    public val event: Event,
    public val eventChannel: EventChannel<*>,
    override val message: String = "Exception in EventHandler",
    /**
     * 原异常
     */
    override val cause: Throwable
) : IllegalStateException()

/**
 * 在此 [CoroutineScope] 下创建一个监听所有事件的 [EventChannel]. 相当于 `GlobalEventChannel.parentScope(this).context(coroutineContext)`.
 *
 * 在返回的 [EventChannel] 中的事件监听器都会以 [this] 作为父协程作用域. 即会 使用 [this]
 *
 * @param coroutineContext 额外的 [CoroutineContext]
 *
 * @throws IllegalStateException 当 [this] 和 [coroutineContext] 均不包含 [CoroutineContext]
 */
@JvmSynthetic
public fun CoroutineScope.globalEventChannel(coroutineContext: CoroutineContext = EmptyCoroutineContext): EventChannel<Event> {
    return if (coroutineContext === EmptyCoroutineContext) GlobalEventChannel.parentScope(this)
    else GlobalEventChannel.parentScope(this).context(coroutineContext)
}

/**
 * 全局事件通道. 此通道包含来自所有 [Bot] 的所有类型的事件. 可通过 [EventChannel.filter] 过滤得到范围更小的 [EventChannel].
 *
 * @see EventChannel
 */
public object GlobalEventChannel : EventChannel<Event>(Event::class, EmptyCoroutineContext)

/**
 * 事件通道. 事件通道是监听事件的入口. **在不同的事件通道中可以监听到不同类型的事件**.
 *
 * [GlobalEventChannel] 是最大的通道: 所有的事件都可以在 [GlobalEventChannel] 监听到.
 *
 * ### 对通道的操作
 * - "缩窄" 通道: 通过 [EventChannel.filter]. 例如 `filter { it is BotEvent }` 得到一个只能监听到 [BotEvent] 的事件通道.
 * - 转换为 Kotlin 协程 [Channel]: [EventChannel.asChannel]
 * - 添加 [CoroutineContext]: [context], [parentJob], [parentScope], [exceptionHandler]
 *
 * ### 创建事件监听
 * - [EventChannel.subscribe] 创建带条件的一个事件监听器.
 * - [EventChannel.subscribeAlways] 创建一个总是监听事件的事件监听器.
 * - [EventChannel.subscribeOnce] 创建一个只监听单次的事件监听器.
 *
 * ### 获取事件通道
 * - [GlobalEventChannel]
 * - [Bot.eventChannel]
 *
 * @see EventChannel.subscribe
 */
public open class EventChannel<out BaseEvent : Event> @JvmOverloads constructor(
    public val baseEventClass: KClass<out BaseEvent>,
    /**
     * 此事件通道的默认 [CoroutineScope.coroutineContext]. 将会被添加给所有注册的事件监听器.
     */
    public val defaultCoroutineContext: CoroutineContext = EmptyCoroutineContext
) {

    /**
     * 创建事件监听并将监听结果发送在 [Channel]. 将返回值 [Channel] [关闭][Channel.close] 时将会同时关闭事件监听.
     *
     * 标注 [ExperimentalCoroutinesApi] 是因为使用了 [Channel.invokeOnClose]
     *
     * @param capacity Channel 容量. 详见 [Channel] 构造.
     *
     * @see subscribeAlways
     * @see Channel
     */
    @MiraiExperimentalApi
    @ExperimentalCoroutinesApi
    public fun asChannel(
        capacity: Int = Channel.RENDEZVOUS,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
    ): Channel<out BaseEvent> {
        val channel = Channel<BaseEvent>(capacity)
        val listener = subscribeAlways(baseEventClass, coroutineContext, concurrency, priority) { channel.send(it) }
        channel.invokeOnClose {
            if (it != null) listener.completeExceptionally(it)
            else listener.complete()
        }

        return channel
    }

    // region transforming operations

    /**
     * 添加一个过滤器. 过滤器将在收到任何事件之后, 传递给通过 [subscribe] 注册的监听器之前调用.
     *
     * 若 [filter] 返回 `true`, 该事件将会被传给监听器. 否则将会被忽略, **监听器继续监听**.
     *
     * ### 线性顺序
     * 多个 [filter] 的处理是线性且有顺序的. 若一个 [filter] 已经返回了 `false` (代表忽略这个事件), 则会立即忽略, 而不会传递给后续过滤器.
     *
     * 示例:
     * ```
     * GlobalEventChannel // GlobalEventChannel 会收到全局所有事件, 事件类型是 Event
     *     .filterIsInstance<BotEvent>() // 过滤, 只接受 BotEvent
     *     .filter { event: BotEvent ->
     *         // 此时的 event 一定是 BotEvent
     *         event.bot.id == 123456 // 再过滤 event 的 bot.id
     *     }
     *     .subscribeAlways { event: BotEvent ->
     *         // 现在 event 是 BotEvent, 且 bot.id == 123456
     *     }
     * ```
     *
     * ### 过滤器挂起
     * [filter] 允许挂起协程. **过滤器的挂起将被认为是事件监听器的挂起**.
     *
     * 过滤器挂起是否会影响事件处理,
     * 取决于 [subscribe] 时的 [Listener.ConcurrencyKind] 和 [Listener.EventPriority].
     *
     * ### 过滤器异常处理
     * 若 [filter] 抛出异常, 将被包装为 [ExceptionInEventChannelFilterException] 并重新抛出.
     *
     * @see filterIsInstance 过滤指定类型的事件
     */
    public fun filter(filter: suspend (event: @UnsafeVariance BaseEvent) -> Boolean): EventChannel<BaseEvent> {
        return object : EventChannel<BaseEvent>(baseEventClass, defaultCoroutineContext) {
            private inline val innerThis get() = this

            override fun <E : Event> (suspend (E) -> ListeningStatus).intercepted(): suspend (E) -> ListeningStatus {
                return { ev ->
                    val filterResult = try {
                        @Suppress("UNCHECKED_CAST")
                        baseEventClass.isInstance(ev) && filter(ev as BaseEvent)
                    } catch (e: Throwable) {
                        if (e is ExceptionInEventChannelFilterException) throw e // wrapped by another filter
                        throw ExceptionInEventChannelFilterException(ev, innerThis, cause = e)
                    }
                    if (filterResult) this.invoke(ev)
                    else ListeningStatus.LISTENING
                }
            }
        }
    }

    /**
     * 过滤事件的类型. 返回一个只包含 [E] 类型事件的 [EventChannel]
     * @see filter 获取更多信息
     */
    public inline fun <reified E : Event> filterIsInstance(): EventChannel<E> =
        filterIsInstance(E::class)

    /**
     * 过滤事件的类型. 返回一个只包含 [E] 类型事件的 [EventChannel]
     * @see filter 获取更多信息
     */
    public fun <E : Event> filterIsInstance(kClass: KClass<out E>): EventChannel<E> {
        return object : EventChannel<E>(kClass, defaultCoroutineContext) {
            private inline val innerThis get() = this

            override fun <E1 : Event> (suspend (E1) -> ListeningStatus).intercepted(): suspend (E1) -> ListeningStatus {
                return { ev ->
                    if (kClass.isInstance(ev)) this.invoke(ev)
                    else ListeningStatus.LISTENING
                }
            }
        }
    }

    /**
     * 过滤事件的类型. 返回一个只包含 [E] 类型事件的 [EventChannel]
     * @see filter 获取更多信息
     */
    public fun <E : Event> filterIsInstance(clazz: Class<out E>): EventChannel<E> =
        filterIsInstance(clazz.kotlin)


    /**
     * 创建一个新的 [EventChannel], 该 [EventChannel] 包含 [`this.coroutineContext`][defaultCoroutineContext] 和添加的 [coroutineContexts].
     * [coroutineContexts] 会覆盖 [defaultCoroutineContext] 中的重复元素.
     *
     * 此操作不会修改 [`this.coroutineContext`][defaultCoroutineContext], 只会创建一个新的 [EventChannel].
     */
    public fun context(vararg coroutineContexts: CoroutineContext): EventChannel<BaseEvent> =
        EventChannel(
            baseEventClass,
            coroutineContexts.fold(this.defaultCoroutineContext) { acc, element -> acc + element }
        )

    /**
     * 创建一个新的 [EventChannel], 该 [EventChannel] 包含 [this.coroutineContext][defaultCoroutineContext] 和添加的 [coroutineExceptionHandler]
     * @see context
     */
    @LowPriorityInOverloadResolution
    public fun exceptionHandler(coroutineExceptionHandler: CoroutineExceptionHandler): EventChannel<BaseEvent> {
        return context(coroutineExceptionHandler)
    }

    /**
     * 创建一个新的 [EventChannel], 该 [EventChannel] 包含 [`this.coroutineContext`][defaultCoroutineContext] 和添加的 [coroutineExceptionHandler]
     * @see context
     */
    public fun exceptionHandler(coroutineExceptionHandler: (exception: Throwable) -> Unit): EventChannel<BaseEvent> {
        return context(CoroutineExceptionHandler { _, throwable ->
            coroutineExceptionHandler(throwable)
        })
    }

    /**
     * 将 [coroutineScope] 作为这个 [EventChannel] 的父作用域.
     *
     * 实际作用为创建一个新的 [EventChannel],
     * 该 [EventChannel] 包含 [`this.coroutineContext`][defaultCoroutineContext] 和添加的 [CoroutineScope.coroutineContext],
     * 并以 [CoroutineScope] 中 [Job] (如果有) [作为父 Job][parentJob]
     *
     * @see parentJob
     * @see context
     *
     * @see CoroutineScope.globalEventChannel `GlobalEventChannel.parentScope()` 的扩展
     */
    public fun parentScope(coroutineScope: CoroutineScope): EventChannel<BaseEvent> {
        return context(coroutineScope.coroutineContext).apply {
            val job = coroutineScope.coroutineContext[Job]
            if (job != null) parentJob(job)
        }
    }

    /**
     * 指定协程父 [Job]. 之后在此 [EventChannel] 下创建的事件监听器都会成为 [job] 的子任务, 当 [job] 被取消时, 所有的事件监听器都会被取消.
     *
     * 注意: 监听器不会失败 ([Job.cancel]). 监听器处理过程的异常都会被捕获然后交由 [CoroutineExceptionHandler] 处理, 因此 [job] 不会因为子任务监听器的失败而被取消.
     *
     * @see parentScope
     * @see context
     */
    public fun parentJob(job: Job): EventChannel<BaseEvent> {
        return context(job)
    }

    // endregion

    // region subscribe

    /**
     * 在指定的 [协程作用域][CoroutineScope] 下创建一个事件监听器, 监听所有 [E] 及其子类事件.
     *
     * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
     *
     *
     * ### 创建监听
     * 调用本函数:
     * ```
     * subscribe<Event> { /* 会收到来自全部 Bot 的事件和与 Bot 不相关的事件 */ }
     * ```
     *
     * ### 生命周期
     *
     * #### 通过协程作用域管理监听器
     * 本函数将会创建一个 [Job], 成为 [coroutineContext] 中的子任务. 可创建一个 [CoroutineScope] 来管理所有的监听器:
     * ```
     * val scope = CoroutineScope(SupervisorJob())
     *
     * scope.subscribeAlways<MemberJoinEvent> { /* ... */ }
     * scope.subscribeAlways<MemberMuteEvent> { /* ... */ }
     *
     * scope.cancel() // 停止上文两个监听
     * ```
     *
     * **注意**, 这个函数返回 [Listener], 它是一个 [CompletableJob]. 它会成为 [CoroutineScope] 的一个 [子任务][Job]
     * ```
     * runBlocking { // this: CoroutineScope
     *   subscribe<Event> { /* 一些处理 */ } // 返回 Listener, 即 CompletableJob
     * }
     * // runBlocking 不会完结, 直到监听时创建的 `Listener` 被停止.
     * // 它可能通过 Listener.cancel() 停止, 也可能自行返回 ListeningStatus.Stopped 停止.
     * ```
     *
     * #### 在监听器内部停止后续监听
     * 当 [handler] 返回 [ListeningStatus.STOPPED] 时停止监听.
     * 或 [Listener.complete] 后结束.
     *
     * ### 子类监听
     * 监听父类事件, 也会同时监听其子类. 因此监听 [Event] 即可监听所有类型的事件.
     *
     * ### 异常处理
     * - 当参数 [handler] 处理抛出异常时, 将会按如下顺序寻找 [CoroutineExceptionHandler] 处理异常:
     *   1. 参数 [coroutineContext]
     *   2. [EventChannel.defaultCoroutineContext]
     *   3. [Event.broadcast] 调用者的 [coroutineContext]
     *   4. 若事件为 [BotEvent], 则从 [BotEvent.bot] 获取到 [Bot], 进而在 [Bot.coroutineContext] 中寻找
     *   5. 若以上四个步骤均无法获取 [CoroutineExceptionHandler], 则使用 [MiraiLogger.Companion] 通过日志记录. 但这种情况理论上不应发生.
     * - 事件处理时抛出异常不会停止监听器.
     * - 建议在事件处理中 (即 [handler] 里) 处理异常,
     *   或在参数 [coroutineContext] 中添加 [CoroutineExceptionHandler].
     *
     *
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext].
     * @param concurrency 并发类型. 查看 [Listener.ConcurrencyKind]
     * @param priority  监听优先级，优先级越高越先执行
     * @param handler 事件处理器. 在接收到事件时会调用这个处理器. 其返回值意义参考 [ListeningStatus]. 其异常处理参考上文
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     *
     * @see syncFromEvent 挂起当前协程, 监听一个事件, 并尝试从这个事件中**同步**一个值
     * @see asyncFromEvent 异步监听一个事件, 并尝试从这个事件中获取一个值.
     *
     * @see nextEvent 挂起当前协程, 直到监听到事件 [E] 的广播, 返回这个事件实例.
     *
     * @see selectMessages 以 `when` 的语法 '选择' 即将到来的一条消息.
     * @see whileSelectMessages 以 `when` 的语法 '选择' 即将到来的所有消息, 直到不满足筛选结果.
     *
     * @see subscribeAlways 一直监听
     * @see subscribeOnce   只监听一次
     *
     * @see subscribeMessages       监听消息 DSL
     * @see subscribeGroupMessages  监听群消息 DSL
     * @see subscribeFriendMessages 监听好友消息 DSL
     * @see subscribeTempMessages   监听临时会话消息 DSL
     */
    public inline fun <reified E : Event> subscribe(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: Listener.ConcurrencyKind = LOCKED,
        priority: EventPriority = EventPriority.NORMAL,
        noinline handler: suspend E.(E) -> ListeningStatus
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority, handler)

    /**
     * 与 [subscribe] 的区别是接受 [eventClass] 参数, 而不使用 `reified` 泛型
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     * @see subscribe
     */
    public fun <E : Event> subscribe(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: Listener.ConcurrencyKind = LOCKED,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> ListeningStatus
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener(coroutineContext, concurrency, priority) { it.handler(it); }
    )

    /**
     * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
     * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
     * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
     *
     * @param concurrency 并发类型默认为 [CONCURRENT]
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     * @param priority 处理优先级, 优先级高的先执行
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     *
     * @see subscribe 获取更多说明
     */
    public inline fun <reified E : Event> subscribeAlways(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        noinline handler: suspend E.(E) -> Unit
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, handler)


    /**
     * @see subscribe
     * @see subscribeAlways
     */
    public fun <E : Event> subscribeAlways(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> Unit
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener(coroutineContext, concurrency, priority) { it.handler(it); ListeningStatus.LISTENING }
    )

    /**
     * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
     * 仅在第一次 [事件广播][Event.broadcast] 时, [handler] 会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
     * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
     *
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     * @param priority 处理优先级, 优先级高的先执行
     *
     * @see subscribe 获取更多说明
     */
    @JvmSynthetic
    public inline fun <reified E : Event> subscribeOnce(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        noinline handler: suspend E.(E) -> Unit
    ): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, handler)

    /**
     * @see subscribeOnce
     */
    public fun <E : Event> subscribeOnce(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> Unit
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener(coroutineContext, LOCKED, priority) { it.handler(it); ListeningStatus.STOPPED }
    )

    // endregion

    // region subscribe with Kotlin function reference


    /**
     * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
     *
     * ```
     * fun onMessage(event: GroupMessageEvent): ListeningStatus {
     *     return ListeningStatus.LISTENING
     * }
     *
     * scope.subscribe(::onMessage)
     * ```
     * @see subscribe
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe1")
    public inline fun <reified E : Event> subscribe(
        crossinline handler: (E) -> ListeningStatus,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 带接收者的函数的函数引用的监听方式.
     *
     * ```
     * fun GroupMessageEvent.onMessage(event: GroupMessageEvent): ListeningStatus {
     *     return ListeningStatus.LISTENING
     * }
     *
     * scope.subscribe(GroupMessageEvent::onMessage)
     * ```
     * @see subscribe
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe2")
    public inline fun <reified E : Event> subscribe(
        crossinline handler: E.(E) -> ListeningStatus,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 挂起函数的函数引用的监听方式.
     *
     * ```
     * suspend fun onMessage(event: GroupMessageEvent): ListeningStatus {
     *     return ListeningStatus.LISTENING
     * }
     *
     * scope.subscribe(::onMessage)
     * ```
     * @see subscribe
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe1")
    public inline fun <reified E : Event> subscribe(
        crossinline handler: suspend (E) -> ListeningStatus,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
     *
     * ```
     * suspend fun GroupMessageEvent.onMessage(event: GroupMessageEvent): ListeningStatus {
     *     return ListeningStatus.LISTENING
     * }
     *
     * scope.subscribe(GroupMessageEvent::onMessage)
     * ```
     * @see subscribe
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe3")
    public inline fun <reified E : Event> subscribe(
        crossinline handler: suspend E.(E) -> ListeningStatus,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }


    // endregion

    // region subscribeAlways with Kotlin function references


    /**
     * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
     * ```
     * fun onMessage(event: GroupMessageEvent) {
     *
     * }
     * scope.subscribeAlways(::onMessage)
     * ```
     * @see subscribeAlways
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribeAlways1")
    public inline fun <reified E : Event> subscribeAlways(
        crossinline handler: (E) -> Unit,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 带接收者的函数的函数引用的监听方式.
     * ```
     * fun GroupMessageEvent.onMessage(event: GroupMessageEvent) {
     *
     * }
     * scope.subscribeAlways(GroupMessageEvent::onMessage)
     * ```
     * @see subscribeAlways
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribeAlways1")
    public inline fun <reified E : Event> subscribeAlways(
        crossinline handler: E.(E) -> Unit,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 挂起函数的函数引用的监听方式.
     * ```
     * suspend fun onMessage(event: GroupMessageEvent) {
     *
     * }
     * scope.subscribeAlways(::onMessage)
     * ```
     * @see subscribeAlways
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe4")
    public inline fun <reified E : Event> subscribeAlways(
        crossinline handler: suspend (E) -> Unit,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

    /**
     * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
     * ```
     * suspend fun GroupMessageEvent.onMessage(event: GroupMessageEvent) {
     *
     * }
     * scope.subscribeAlways(GroupMessageEvent::onMessage)
     * ```
     * @see subscribeAlways
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    @JvmName("subscribe1")
    public inline fun <reified E : Event> subscribeAlways(
        crossinline handler: suspend E.(E) -> Unit,
        priority: EventPriority = EventPriority.NORMAL,
        concurrency: Listener.ConcurrencyKind = CONCURRENT,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

    // endregion

    // region impl

    /**
     * 由子类实现，可以为 handler 包装一个过滤器等. 每个 handler 都会经过此函数处理.
     */
    protected open fun <E : Event> (suspend (E) -> ListeningStatus).intercepted(): (suspend (E) -> ListeningStatus) {
        return this
    }

    internal fun <L : Listener<E>, E : Event> subscribeInternal(eventClass: KClass<out E>, listener: L): L {
        with(GlobalEventListeners[listener.priority]) {
            @Suppress("UNCHECKED_CAST")
            val node = ListenerRegistry(listener as Listener<Event>, eventClass)
            add(node)
            listener.invokeOnCompletion {
                this.remove(node)
            }
        }
        return listener
    }


    @Suppress("FunctionName")
    private fun <E : Event> createListener(
        coroutineContext: CoroutineContext,
        concurrencyKind: Listener.ConcurrencyKind,
        priority: Listener.EventPriority = EventPriority.NORMAL,
        handler: suspend (E) -> ListeningStatus
    ): Listener<E> {
        val context = this.defaultCoroutineContext + coroutineContext
        return Handler(
            parentJob = context[Job],
            subscriberContext = context,
            handler = handler.intercepted(),
            concurrencyKind = concurrencyKind,
            priority = priority
        )
    }

    // endregion
}