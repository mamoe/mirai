/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmName("SubscriberKt")
@file:JvmMultifileClass

package net.mamoe.mirai.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.Listener.ConcurrencyKind.LOCKED
import net.mamoe.mirai.event.Listener.EventPriority.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KClass

/*
 * 该文件为所有的订阅事件的方法.
 */

/**
 * 订阅者的状态
 */
public enum class ListeningStatus {
    /**
     * 表示继续监听
     */
    LISTENING,

    /**
     * 表示已停止.
     *
     * - 若监听器使用 [Listener.ConcurrencyKind.LOCKED],
     * 在这之后监听器将会被从监听器列表中删除, 因此不再能接收到事件.
     * - 若使用 [Listener.ConcurrencyKind.CONCURRENT],
     * 在这之后无法保证立即停止监听.
     */
    STOPPED
}

/**
 * 事件监听器.
 * 由 [CoroutineScope.subscribe] 等方法返回.
 *
 * 取消监听: [complete]
 */
public interface Listener<in E : Event> : CompletableJob {

    public enum class ConcurrencyKind {
        /**
         * 并发地同时处理多个事件, 但无法保证 [onEvent] 返回 [ListeningStatus.STOPPED] 后立即停止事件监听.
         */
        CONCURRENT,

        /**
         * 使用 [Mutex] 保证同一时刻只处理一个事件.
         */
        LOCKED
    }

    /**
     * 并发类型
     */
    public val concurrencyKind: ConcurrencyKind

    /**
     * 事件优先级.
     *
     * 在广播时, 事件监听器的调用顺序为 (从左到右):
     * `[HIGHEST]` -> `[HIGH]` -> `[NORMAL]` -> `[LOW]` -> `[LOWEST]` -> `[MONITOR]`
     *
     * - 使用 [MONITOR] 优先级的监听器将会被**并行**调用.
     * - 使用其他优先级的监听器都将会**按顺序**调用.
     *   因此一个监听器的挂起可以阻塞事件处理过程而导致低优先级的监听器较晚处理.
     *
     * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
     */
    public enum class EventPriority {

        HIGHEST, HIGH, NORMAL, LOW, LOWEST,

        /**
         * 最低的优先级.
         *
         * 使用此优先级的监听器应遵循约束:
         * - 不 [拦截事件][Event.intercept]
         */
        MONITOR;

        internal companion object {
            @JvmStatic
            internal val prioritiesExcludedMonitor: Array<EventPriority> = run {
                values().filter { it != MONITOR }.toTypedArray()
            }
        }
    }

    /**
     * 事件优先级
     * @see [EventPriority]
     */
    public val priority: EventPriority get() = NORMAL

    /**
     * 这个方法将会调用 [CoroutineScope.subscribe] 时提供的参数 `noinline handler: suspend E.(E) -> ListeningStatus`.
     *
     * 这个函数不会抛出任何异常, 详见 [CoroutineScope.subscribe]
     */
    public suspend fun onEvent(event: E): ListeningStatus
}

public typealias EventPriority = Listener.EventPriority

// region subscribe / subscribeAlways / subscribeOnce

/**
 * 在指定的 [协程作用域][CoroutineScope] 下创建一个事件监听器, 监听所有 [E] 及其子类事件.
 *
 * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
 *
 *
 * ### 创建监听
 * 调用本函数:
 * ```
 * coroutineScope.subscribe<Event> { /* 会收到来自全部 Bot 的事件和与 Bot 不相关的事件 */ }
 * ```
 *
 * ### 生命周期
 *
 * #### 通过协程作用域管理监听器
 * 本函数将会创建一个 [Job], 成为 [this] 中的子任务. 可创建一个 [CoroutineScope] 来管理所有的监听器:
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
 * 事件处理时的 [CoroutineContext] 为调用本函数时的 [receiver][this] 的 [CoroutineScope.coroutineContext].
 * 因此:
 * - 当参数 [handler] 处理抛出异常时, 将会按如下顺序寻找 [CoroutineExceptionHandler] 处理异常:
 *   1. 参数 [coroutineContext]
 *   2. 接收者 [this] 的 [CoroutineScope.coroutineContext]
 *   3. [Event.broadcast] 调用者的 [coroutineContext]
 *   4. 若事件为 [BotEvent], 则从 [BotEvent.bot] 获取到 [Bot], 进而在 [Bot.coroutineContext] 中寻找
 *   5. 若以上四个步骤均无法获取 [CoroutineExceptionHandler], 则使用 [MiraiLogger.Companion] 通过日志记录. 但这种情况理论上不应发生.
 * - 事件处理时抛出异常不会停止监听器.
 * - 建议在事件处理中 (即 [handler] 里) 处理异常,
 *   或在参数 [coroutineContext] 中添加 [CoroutineExceptionHandler].
 *
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext].
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
public inline fun <reified E : Event> CoroutineScope.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority, handler)

/**
 * 与 [CoroutineScope.subscribe] 的区别是接受 [eventClass] 参数, 而不使用 `reified` 泛型
 *
 * @see CoroutineScope.subscribe
 *
 * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
 */
public fun <E : Event> CoroutineScope.subscribe(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = eventClass.subscribeInternal(Handler(coroutineContext, concurrency, priority) { it.handler(it); })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param concurrency 并发类型默认为 [CONCURRENT]
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 * @param priority 处理优先级, 优先级高的先执行
 *
 * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [handler]
 *
 * @see CoroutineScope.subscribe 获取更多说明
 */
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    noinline handler: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, handler)


/**
 * @see CoroutineScope.subscribe
 * @see CoroutineScope.subscribeAlways
 */
public fun <E : Event> CoroutineScope.subscribeAlways(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    handler: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, concurrency, priority) { it.handler(it); ListeningStatus.LISTENING }
)

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Event.broadcast] 时, [handler] 会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 * @param priority 处理优先级, 优先级高的先执行
 *
 * @see CoroutineScope.subscribe 获取更多说明
 */
@JvmSynthetic
public inline fun <reified E : Event> CoroutineScope.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    noinline handler: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, handler)

/**
 * @see CoroutineScope.subscribeOnce
 */
public fun <E : Event> CoroutineScope.subscribeOnce(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    handler: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, LOCKED, priority) { it.handler(it); ListeningStatus.STOPPED }
)


// endregion


// region subscribe for Kotlin functional reference


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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: (E) -> ListeningStatus,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe2")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: E.(E) -> ListeningStatus,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: suspend (E) -> ListeningStatus,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe3")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: suspend E.(E) -> ListeningStatus,
    priority: Listener.EventPriority = NORMAL,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }


// endregion


// region subscribeAlways for Kotlin functional references


/**
 * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
 * ```
 * fun onMessage(event: GroupMessageEvent) {
 *
 * }
 * scope.subscribeAlways(::onMessage)
 * ```
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribeAlways1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: (E) -> Unit,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribeAlways1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: E.(E) -> Unit,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe4")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: suspend (E) -> Unit,
    priority: Listener.EventPriority = NORMAL,
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
 */
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: suspend E.(E) -> Unit,
    priority: Listener.EventPriority = NORMAL,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

// endregion