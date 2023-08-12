/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("EventChannelKt")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.event.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.ConcurrencyKind.LOCKED
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.event.JvmMethodListenersInternal
import net.mamoe.mirai.utils.*
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 * 事件通道.
 *
 * 事件通道是监听事件的入口, 但不负责广播事件. 要广播事件, 使用 [Event.broadcast] 或 [IMirai.broadcastEvent].
 *
 * ## 获取事件通道
 *
 * [EventChannel] 不可自行构造, 只能通过 [GlobalEventChannel], [BotEvent], 或基于一个通道的过滤等操作获得.
 *
 * ### 全局事件通道
 *
 * [GlobalEventChannel] 是单例对象, 表示全局事件通道, 可以获取到在其中广播的所有事件.
 *
 * ### [BotEvent] 事件通道
 *
 * 若只需要监听某个 [Bot] 的事件, 可通过 [Bot.eventChannel] 获取到这样的 [EventChannel].
 *
 * ## 通道操作
 *
 * ### 对通道的操作
 * - 过滤通道: 通过 [EventChannel.filter]. 例如 `filter { it is BotEvent }` 得到一个只能监听到 [BotEvent] 的事件通道.
 * - 转换为 Kotlin 协程 [Channel]: [EventChannel.forwardToChannel]
 * - 添加 [CoroutineContext]: [context], [parentJob], [parentScope], [exceptionHandler]
 *
 * ### 创建事件监听
 * - [EventChannel.subscribe] 创建带条件的一个事件监听器.
 * - [EventChannel.subscribeAlways] 创建一个总是监听事件的事件监听器.
 * - [EventChannel.subscribeOnce] 创建一个只监听单次的事件监听器.
 *
 * ### 监听器生命周期
 *
 * 阅读 [EventChannel.subscribe] 以获取监听器生命周期相关信息.
 *
 * ## 与 kotlinx-coroutines 交互
 *
 * mirai [EventChannel] 设计比 kotlinx-coroutines 的 [Flow] 稳定版更早.
 * [EventChannel] 的功能与 [Flow] 类似, 不过 [EventChannel] 在 [subscribe] (类似 [Flow.collect]) 时有优先级判定, 也允许[拦截][Event.intercept].
 *
 * ### 通过 [Flow] 接收事件
 *
 * 使用 [EventChannel.asFlow] 获得 [Flow], 然后可使用 [Flow.collect] 等操作.
 *
 * ### 转发事件到 [SendChannel]
 *
 * 使用 [EventChannel.forwardToChannel] 可将事件转发到指定 [SendChannel].
 */
@NotStableForInheritance // since 2.12, before it was `final class`.
public abstract class EventChannel<out BaseEvent : Event> @MiraiInternalApi public constructor(
    public val baseEventClass: KClass<out BaseEvent>,
    /**
     * 此事件通道的默认 [CoroutineScope.coroutineContext]. 将会被添加给所有注册的事件监听器.
     */
    public val defaultCoroutineContext: CoroutineContext,
) {
    /**
     * 创建事件监听并将监听结果转发到 [channel]. 当 [Channel.send] 抛出 [ClosedSendChannelException] 时停止 [Listener] 监听和转发.
     *
     * 返回创建的会转发监听到的所有事件到 [channel] 的[事件监听器][Listener]. [停止][Listener.complete] 该监听器会停止转发, 不会影响目标 [channel].
     *
     * 若 [Channel.send] 挂起, 则监听器也会挂起, 也就可能会导致事件广播过程挂起.
     *
     * 示例:
     *
     * ```
     * val eventChannel: EventChannel<BotEvent> = ...
     * val channel = Channel<BotEvent>() // kotlinx.coroutines.channels.Channel
     * eventChannel.forwardToChannel(channel, priority = ...)
     *
     * // 其他地方
     * val event: BotEvent = channel.receive() // 挂起并接收一个事件
     * ```
     *
     * @see subscribeAlways
     * @see Channel
     * @since 2.10
     */
    public fun forwardToChannel(
        channel: SendChannel<@UnsafeVariance BaseEvent>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.MONITOR,
    ): Listener<@UnsafeVariance BaseEvent> {
        return subscribe(baseEventClass, coroutineContext, priority = priority) {
            try {
                channel.send(it)
                ListeningStatus.LISTENING
            } catch (_: ClosedSendChannelException) {
                ListeningStatus.STOPPED
            }
        }
    }

    /**
     * 通过 [Flow] 接收此通道内的所有事件.
     *
     * ```
     * val eventChannel: EventChannel<BotEvent> = ...
     * val flow: Flow<BotEvent> = eventChannel.asFlow()
     *
     * flow.collect { // it
     *   //
     * }
     *
     * flow.filterIsInstance<GroupMessageEvent>.collect { // it: GroupMessageEvent
     *   // 处理事件 ...
     * }
     *
     * flow.filterIsInstance<FriendMessageEvent>.collect { // it: FriendMessageEvent
     *   // 处理事件 ...
     * }
     * ```
     *
     * 类似于 [SharedFlow], [EventChannel.asFlow] 返回的 [Flow] 永远都不会停止. 因此上述示例 [Flow.collect] 永远都不会正常 (以抛出异常之外的) 结束.
     *
     * 通过 [asFlow] 接收事件相当于通过 [subscribeAlways] 以 [EventPriority.MONITOR] 监听事件.
     *
     * **注意**: [context], [parentJob] 等控制 [EventChannel.defaultCoroutineContext] 的操作对 [asFlow] 无效. 因为 [asFlow] 并不创建协程.
     *
     * @see Flow
     * @since 2.12
     */
    public abstract fun asFlow(): Flow<BaseEvent>

    // region transforming operations

    /**
     * 添加一个过滤器. 过滤器将在收到任何事件之后, 传递给通过 [EventChannel.subscribe] 注册的监听器之前调用.
     *
     * 若 [filter] 返回 `true`, 该事件将会被传给监听器. 否则将会被忽略, **监听器继续监听**.
     *
     * ## 线性顺序
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
     * ## 过滤器挂起
     * [filter] 允许挂起协程. **过滤器的挂起将被认为是事件监听器的挂起**.
     *
     * 过滤器挂起是否会影响事件处理,
     * 取决于 [subscribe] 时的 [ConcurrencyKind] 和 [EventPriority].
     *
     * ## 过滤器异常处理
     * 若 [filter] 抛出异常, 将被包装为 [ExceptionInEventChannelFilterException] 并重新抛出.
     *
     * @see filterIsInstance 过滤指定类型的事件
     */
    @JvmSynthetic
    public fun filter(filter: suspend (event: BaseEvent) -> Boolean): EventChannel<BaseEvent> {
        return FilterEventChannel(this, filter)
    }

    /**
     * [EventChannel.filter] 的 Java 版本.
     *
     * 添加一个过滤器. 过滤器将在收到任何事件之后, 传递给通过 [EventChannel.subscribe] 注册的监听器之前调用.
     *
     * 若 [filter] 返回 `true`, 该事件将会被传给监听器. 否则将会被忽略, **监听器继续监听**.
     *
     * ## 线性顺序
     * 多个 [filter] 的处理是线性且有顺序的. 若一个 [filter] 已经返回了 `false` (代表忽略这个事件), 则会立即忽略, 而不会传递给后续过滤器.
     *
     * 示例:
     * ```
     * GlobalEventChannel // GlobalEventChannel 会收到全局所有事件, 事件类型是 Event
     *     .filterIsInstance(BotEvent.class) // 过滤, 只接受 BotEvent
     *     .filter(event ->
     *         // 此时的 event 一定是 BotEvent
     *         event.bot.id == 123456 // 再过滤 event 的 bot.id
     *     )
     *     .subscribeAlways(event -> {
     *         // 现在 event 是 BotEvent, 且 bot.id == 123456
     *     })
     * ```
     *
     * ## 过滤器阻塞
     * [filter] 允许阻塞线程. **过滤器的阻塞将被认为是事件监听器的阻塞**.
     *
     * 过滤器阻塞是否会影响事件处理,
     * 取决于 [subscribe] 时的 [ConcurrencyKind] 和 [EventPriority].
     *
     * ## 过滤器异常处理
     * 若 [filter] 抛出异常, 将被包装为 [ExceptionInEventChannelFilterException] 并重新抛出.
     *
     * @see filterIsInstance 过滤指定类型的事件
     *
     * @since 2.2
     */
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    public fun filter(filter: (event: BaseEvent) -> Boolean): EventChannel<BaseEvent> {
        return filter { runBIO { filter(it) } }
    }

    /**
     * 过滤事件的类型. 返回一个只包含 [E] 类型事件的 [EventChannel]
     * @see filter 获取更多信息
     */
    @JvmSynthetic
    public inline fun <reified E : Event> filterIsInstance(): EventChannel<E> =
        filterIsInstance(E::class)

    /**
     * 过滤事件的类型. 返回一个只包含 [E] 类型事件的 [EventChannel]
     * @see filter 获取更多信息
     */
    public fun <E : Event> filterIsInstance(kClass: KClass<out E>): EventChannel<E> {
        return filter { kClass.isInstance(it) }.cast()
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
    public abstract fun context(vararg coroutineContexts: CoroutineContext): EventChannel<BaseEvent>

    /**
     * 创建一个新的 [EventChannel], 该 [EventChannel] 包含 [this.coroutineContext][defaultCoroutineContext] 和添加的 [coroutineExceptionHandler]
     * @see context
     */
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
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
     * 创建一个新的 [EventChannel], 该 [EventChannel] 包含 [`this.coroutineContext`][defaultCoroutineContext] 和添加的 [coroutineExceptionHandler]
     * @see context
     * @since 2.12
     */
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    public fun exceptionHandler(coroutineExceptionHandler: Consumer<Throwable>): EventChannel<BaseEvent> {
        return exceptionHandler { coroutineExceptionHandler.accept(it) }
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
        return context(coroutineScope.coroutineContext)
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
     * 创建一个事件监听器, 监听事件通道中所有 [E] 及其子类事件.
     *
     * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
     *
     *
     * ## 创建监听
     * 调用本函数:
     * ```
     * eventChannel.subscribe<E> { /* 会收到此通道中的所有是 E 的事件 */ }
     * ```
     *
     * ## 生命周期
     *
     * ### 通过协程作用域管理监听器
     * 本函数将会创建一个 [Job], 成为 [parentJob] 中的子任务. 可创建一个 [CoroutineScope] 来管理所有的监听器:
     * ```
     * val scope = CoroutineScope(SupervisorJob())
     *
     * val scopedChannel = eventChannel.parentScope(scope) // 将协程作用域 scope 附加到这个 EventChannel
     *
     * scopedChannel.subscribeAlways<MemberJoinEvent> { /* ... */ } // 启动监听, 监听器协程会作为 scope 的子任务
     * scopedChannel.subscribeAlways<MemberMuteEvent> { /* ... */ } // 启动监听, 监听器协程会作为 scope 的子任务
     *
     * scope.cancel() // 停止了协程作用域, 也就取消了两个监听器
     * ```
     *
     * 这个函数返回 [Listener], 它是一个 [CompletableJob]. 它会成为 [parentJob] 或 [parentScope] 的一个 [子任务][Job]
     *
     * ### 停止监听
     * 如果 [handler] 返回 [ListeningStatus.STOPPED] 监听器将被停止.
     *
     * 也可以通过 [subscribe] 返回值 [Listener] 的 [Listener.complete]
     *
     * ## 监听器调度
     * 监听器会被创建一个协程任务, 语义上在 [parentScope] 下运行.
     * 通过 Kotlin [默认协程调度器][Dispatchers.Default] 在固定的全局共享线程池里执行, 除非有 [coroutineContext] 指定.
     *
     * 默认在 [handler] 中不能处理阻塞任务. 阻塞任务将会阻塞一个 Kotlin 全局协程调度线程并可能导致严重问题.
     * 请通过 `withContext(Dispatchers.IO) { }` 等方法执行阻塞工作.
     *
     * ## 异常处理
     *
     * **监听过程抛出的异常是需要尽可能避免的, 因为这将产生不确定性.**
     *
     * 当参数 [handler] 处理事件抛出异常时, 只会从监听方协程上下文 ([CoroutineContext]) 寻找 [CoroutineExceptionHandler] 处理异常, 即如下顺序:
     *   1. 本函数参数 [coroutineContext]
     *   2. [EventChannel.defaultCoroutineContext]
     *   3. 若以上步骤无法获取 [CoroutineExceptionHandler], 则只会在日志记录异常.
     *   因此建议先指定 [CoroutineExceptionHandler] (可通过 [EventChannel.exceptionHandler]) 再监听事件, 或者在监听事件中捕获异常.
     *
     * 因此, 广播方 ([Event.broadcast]) 不会知晓监听方产生的异常, 其 [Event.broadcast] 过程也不会因监听方产生异常而提前结束.
     *
     * ***备注***: 在 2.11 以前, 发生上述异常时还会从广播方和有关 [Bot] 协程域获取 [CoroutineExceptionHandler]. 因此行为不稳定而在 2.11 变更为上述过程.
     *
     * 事件处理时抛出异常不会停止监听器.
     *
     * 建议在事件处理中 (即 [handler] 里) 处理异常,
     * 或在参数 [coroutineContext] 中添加 [CoroutineExceptionHandler], 或通过 [EventChannel.exceptionHandler].
     *
     * ## 并发安全性
     * 基于 [concurrency] 参数, 事件监听器可以被允许并行执行.
     *
     * - 若 [concurrency] 为 [ConcurrencyKind.CONCURRENT], [handler] 可能被并行调用, 需要保证并发安全.
     * - 若 [concurrency] 为 [ConcurrencyKind.LOCKED], [handler] 会被 [Mutex] 限制, 串行异步执行.
     *
     * ## 衍生监听方法
     *
     * 这些方法仅 Kotlin 可用.
     *
     * - [syncFromEvent]: 挂起当前协程, 监听一个事件, 并尝试从这个事件中**获取**一个值
     * - [nextEvent]: 挂起当前协程, 直到监听到特定类型事件的广播并通过过滤器, 返回这个事件实例.
     *
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext].
     * @param concurrency 并发类型. 查看 [ConcurrencyKind]
     * @param priority  监听优先级，优先级越高越先执行
     * @param handler 事件处理器. 在接收到事件时会调用这个处理器. 其返回值意义参考 [ListeningStatus]. 其异常处理参考上文
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     *
     *
     * @see selectMessages 以 `when` 的语法 '选择' 即将到来的一条消息.
     * @see whileSelectMessages 以 `when` 的语法 '选择' 即将到来的所有消息, 直到不满足筛选结果.
     *
     * @see subscribeAlways 一直监听
     * @see subscribeOnce   只监听一次
     *
     * @see subscribeMessages       监听消息 DSL
     */
    @JvmSynthetic
    public inline fun <reified E : Event> subscribe(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = LOCKED,
        priority: EventPriority = EventPriority.NORMAL,
        noinline handler: suspend E.(E) -> ListeningStatus,
    ): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority, handler)

    /**
     * 与 [subscribe] 的区别是接受 [eventClass] 参数, 而不使用 `reified` 泛型. 通常推荐使用具体化类型参数.
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     * @see subscribe
     */
    @JvmSynthetic
    public fun <E : Event> subscribe(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = LOCKED,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> ListeningStatus,
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener0(coroutineContext, concurrency, priority) { it.handler(it); }
    )

    /**
     * 创建一个事件监听器, 监听事件通道中所有 [E] 及其子类事件.
     * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
     *
     * @param concurrency 并发类型默认为 [CONCURRENT]
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     * @param priority 处理优先级, 优先级高的先执行
     *
     * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
     *
     * @see subscribe 获取更多说明
     */
    @JvmSynthetic
    public inline fun <reified E : Event> subscribeAlways(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        noinline handler: suspend E.(E) -> Unit,
    ): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, handler)


    /**
     * @see subscribe
     * @see subscribeAlways
     */
    @JvmSynthetic
    public fun <E : Event> subscribeAlways(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> Unit,
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener0(coroutineContext, concurrency, priority) { it.handler(it); ListeningStatus.LISTENING }
    )

    /**
     * 创建一个事件监听器, 监听事件通道中所有 [E] 及其子类事件, 只监听一次.
     * 当 [事件广播][Event.broadcast] 时, [handler] 会被执行.
     *
     * 可在任意时候通过 [Listener.complete] 来主动停止监听.
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
        noinline handler: suspend E.(E) -> Unit,
    ): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, handler)

    /**
     * @see subscribeOnce
     */
    public fun <E : Event> subscribeOnce(
        eventClass: KClass<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        priority: EventPriority = EventPriority.NORMAL,
        handler: suspend E.(E) -> Unit,
    ): Listener<E> = subscribeInternal(
        eventClass,
        createListener0(coroutineContext, ConcurrencyKind.LOCKED, priority) { it.handler(it); ListeningStatus.STOPPED }
    )

    // endregion

    /**
     * 注册 [ListenerHost] 中的所有 [EventHandler] 标注的方法到这个 [EventChannel]. 查看 [EventHandler].
     *
     * @param coroutineContext 在 [defaultCoroutineContext] 的基础上, 给事件监听协程的额外的 [CoroutineContext]
     *
     * @see subscribe
     * @see EventHandler
     * @see ListenerHost
     */
    @JvmOverloads
    public fun registerListenerHost(
        host: ListenerHost,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
    ) {
        val jobOfListenerHost: Job?
        val coroutineContext0 = if (host is SimpleListenerHost) {
            val listenerCoroutineContext = host.coroutineContext
            val listenerJob = listenerCoroutineContext[Job]

            val rsp = listenerCoroutineContext.minusKey(Job) +
                    coroutineContext +
                    (listenerCoroutineContext[CoroutineExceptionHandler] ?: EmptyCoroutineContext)

            val registerCancelHook = when {
                listenerJob === null -> false

                // Registering cancellation hook is needless
                // if [Job] of [EventChannel] is same as [Job] of [SimpleListenerHost]
                (rsp[Job] ?: this.defaultCoroutineContext[Job]) === listenerJob -> false

                else -> true
            }

            jobOfListenerHost = if (registerCancelHook) {
                listenerCoroutineContext[Job]
            } else {
                null
            }
            rsp
        } else {
            jobOfListenerHost = null
            coroutineContext
        }
        for (method in host.javaClass.declaredMethods) {
            method.getAnnotation(EventHandler::class.java)?.let {
                val listener =
                    JvmMethodListenersInternal.registerEventHandler(method, host, this, it, coroutineContext0)
                // For [SimpleListenerHost.cancelAll]
                jobOfListenerHost?.invokeOnCompletion { exception ->
                    listener.cancel(
                        when (exception) {
                            is CancellationException -> exception
                            is Throwable -> CancellationException(null, exception)
                            else -> null
                        }
                    )
                }
            }
        }
    }

    // region Java API

    /**
     * Java API. 查看 [subscribeAlways] 获取更多信息.
     *
     * ```java
     * eventChannel.subscribeAlways(GroupMessageEvent.class, (event) -> { });
     * ```
     *
     * @see subscribe
     * @see subscribeAlways
     */
    @JvmOverloads
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    public fun <E : Event> subscribeAlways(
        eventClass: Class<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        handler: Consumer<E>,
    ): Listener<E> = subscribeInternal(
        eventClass.kotlin,
        createListener0(coroutineContext, concurrency, priority) { event ->
            runInterruptible(Dispatchers.IO) { handler.accept(event) }
            ListeningStatus.LISTENING
        }
    )

    /**
     * Java API. 查看 [subscribe] 获取更多信息.
     *
     * ```java
     * eventChannel.subscribe(GroupMessageEvent.class, (event) -> {
     *     return ListeningStatus.LISTENING;
     * });
     * ```
     *
     * @see subscribe
     */
    @JvmOverloads
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    public fun <E : Event> subscribe(
        eventClass: Class<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        handler: java.util.function.Function<E, ListeningStatus>,
    ): Listener<E> = subscribeInternal(
        eventClass.kotlin,
        createListener0(coroutineContext, concurrency, priority) { event ->
            runInterruptible(Dispatchers.IO) { handler.apply(event) }
        }
    )

    /**
     * Java API. 查看 [subscribeOnce] 获取更多信息.
     *
     * ```java
     * eventChannel.subscribeOnce(GroupMessageEvent.class, (event) -> { });
     * ```
     *
     * @see subscribe
     * @see subscribeOnce
     */
    @JvmOverloads
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    public fun <E : Event> subscribeOnce(
        eventClass: Class<out E>,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
        handler: Consumer<E>,
    ): Listener<E> = subscribeInternal(
        eventClass.kotlin,
        createListener0(coroutineContext, concurrency, priority) { event ->
            runInterruptible(Dispatchers.IO) { handler.accept(event) }
            ListeningStatus.STOPPED
        }
    )

    // endregion

    // region deprecated

    /**
     * 创建事件监听并将监听结果发送在 [Channel]. 将返回值 [Channel] [关闭][Channel.close] 时将会同时关闭事件监听.
     *
     * ## 已弃用
     *
     * 请使用 [forwardToChannel] 替代.
     *
     * @param capacity Channel 容量. 详见 [Channel] 构造.
     *
     * @see subscribeAlways
     * @see Channel
     */
    @Deprecated(
        "Please use forwardToChannel instead.",
        replaceWith = ReplaceWith(
            "Channel<BaseEvent>(capacity).apply { forwardToChannel(this, coroutineContext, priority) }",
            "kotlinx.coroutines.channels.Channel"
        ),
        level = DeprecationLevel.ERROR,
    )
    @DeprecatedSinceMirai(warningSince = "2.10", errorSince = "2.14")
    @MiraiExperimentalApi
    public fun asChannel(
        capacity: Int = Channel.RENDEZVOUS,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        @Suppress("UNUSED_PARAMETER") concurrency: ConcurrencyKind = CONCURRENT,
        priority: EventPriority = EventPriority.NORMAL,
    ): Channel<out BaseEvent> =
        Channel<BaseEvent>(capacity).apply { forwardToChannel(this, coroutineContext, priority) }

    // endregion

    // region impl


    // protected, to hide from users
    @MiraiInternalApi
    protected abstract fun <E : Event> registerListener(eventClass: KClass<out E>, listener: Listener<E>)

    // to overcome visibility issue
    @OptIn(MiraiInternalApi::class)
    internal fun <E : Event> registerListener0(eventClass: KClass<out E>, listener: Listener<E>) {
        return registerListener(eventClass, listener)
    }

    @OptIn(MiraiInternalApi::class)
    private fun <L : Listener<E>, E : Event> subscribeInternal(eventClass: KClass<out E>, listener: L): L {
        registerListener(eventClass, listener)
        return listener
    }

    /**
     * Creates [Listener] instance using the [listenerBlock] action.
     */
//    @Contract("_ -> new") // always creates new instance
    @MiraiInternalApi
    protected abstract fun <E : Event> createListener(
        coroutineContext: CoroutineContext,
        concurrencyKind: ConcurrencyKind,
        priority: EventPriority,
        listenerBlock: suspend (E) -> ListeningStatus,
    ): Listener<E>

    // to overcome visibility issue
    @OptIn(MiraiInternalApi::class)
    internal fun <E : Event> createListener0(
        coroutineContext: CoroutineContext,
        concurrencyKind: ConcurrencyKind,
        priority: EventPriority,
        listenerBlock: suspend (E) -> ListeningStatus,
    ): Listener<E> = createListener(coroutineContext, concurrencyKind, priority, listenerBlock)

    // endregion
}


// used by mirai-core
@OptIn(MiraiInternalApi::class)
internal open class FilterEventChannel<BaseEvent : Event>(
    private val delegate: EventChannel<BaseEvent>,
    private val filter: suspend (event: BaseEvent) -> Boolean,
) : EventChannel<BaseEvent>(delegate.baseEventClass, delegate.defaultCoroutineContext) {
    private fun <E : Event> intercept(block: suspend (E) -> ListeningStatus): suspend (E) -> ListeningStatus {
        return { ev ->
            val filterResult = try {
                @Suppress("UNCHECKED_CAST")
                baseEventClass.isInstance(ev) && filter(ev as BaseEvent)
            } catch (e: Throwable) {
                if (e is ExceptionInEventChannelFilterException) throw e // wrapped by another filter
                throw ExceptionInEventChannelFilterException(ev, this, cause = e)
            }
            if (filterResult) block.invoke(ev)
            else ListeningStatus.LISTENING
        }
    }

    override fun asFlow(): Flow<BaseEvent> = delegate.asFlow().filter(filter)

    override fun <E : Event> registerListener(eventClass: KClass<out E>, listener: Listener<E>) {
        delegate.registerListener0(eventClass, listener)
    }

    override fun <E : Event> createListener(
        coroutineContext: CoroutineContext,
        concurrencyKind: ConcurrencyKind,
        priority: EventPriority,
        listenerBlock: suspend (E) -> ListeningStatus
    ): Listener<E> = delegate.createListener0(coroutineContext, concurrencyKind, priority, intercept(listenerBlock))

    override fun context(vararg coroutineContexts: CoroutineContext): EventChannel<BaseEvent> {
        return delegate.context(*coroutineContexts)
    }
}