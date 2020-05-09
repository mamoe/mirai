/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

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
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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
enum class ListeningStatus {
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
 * 由 [subscribe] 等方法返回.
 *
 * 取消监听: [complete]
 */
interface Listener<in E : Event> : CompletableJob {

    enum class ConcurrencyKind {
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
    val concurrencyKind: ConcurrencyKind

    /**
     * 事件优先级.
     *
     * 在广播时, 事件监听器的调用顺序为 (从左到右):
     * `[HIGHEST]` -> `[HIGH]` -> `[NORMAL]` -> `[LOW]` -> `[LOWEST]` -> `[MONITOR]`
     *
     * - 使用 [MONITOR] 优先级的监听器将会被**并行**调用.
     * - 使用其他优先级的监听器都将会**按顺序**调用.
     *   因此一个监听器的挂起可以阻塞事件处理过程而导致低优先级的监听器较晚处理.
     */
    @SinceMirai("1.0.0")
    enum class EventPriority {

        HIGHEST, HIGH, NORMAL, LOW, LOWEST,

        /**
         * 最低的优先级.
         *
         * 只监听事件而不拦截事件的监听器应使用此监听器.
         */
        MONITOR;

        companion object {
            @JvmStatic
            internal val valuesExceptMonitor: Array<EventPriority> = arrayOf(HIGHEST, HIGH, NORMAL, LOW, LOWEST)
        }
    }

    /**
     * 事件优先级
     * @see [EventPriority]
     */
    val priority: EventPriority get() = NORMAL

    /**
     * 这个方法将会调用 [subscribe] 时提供的参数 `noinline handler: suspend E.(E) -> ListeningStatus`.
     *
     * 这个函数不会抛出任何异常, 详见 [subscribe]
     */
    suspend fun onEvent(event: E): ListeningStatus
}

// region 顶层方法 创建当前 coroutineContext 下的子 Job

/**
 * 在指定的 [协程作用域][CoroutineScope] 下创建一个事件监听器, 监听所有 [E] 及其子类事件.
 *
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行.
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
 * 当 [listener] 返回 [ListeningStatus.STOPPED] 时停止监听.
 * 或 [Listener.complete] 后结束.
 *
 * ### 子类监听
 * 监听父类事件, 也会同时监听其子类. 因此监听 [Event] 即可监听所有类型的事件.
 *
 * ### 异常处理
 * 事件处理时的 [CoroutineContext] 为调用本函数时的 [receiver][this] 的 [CoroutineScope.coroutineContext].
 * 因此:
 * - 当参数 [listener] 处理抛出异常时, 将会按如下顺序寻找 [CoroutineExceptionHandler] 处理异常:
 *   1. 参数 [coroutineContext]
 *   2. 接收者 [this] 的 [CoroutineScope.coroutineContext]
 *   3. [Event.broadcast] 调用者的 [coroutineContext]
 *   4. 若事件为 [BotEvent], 则从 [BotEvent.bot] 获取到 [Bot], 进而在 [Bot.coroutineContext] 中寻找
 *   5. 若以上四个步骤均无法获取 [CoroutineExceptionHandler], 则使用 [MiraiLogger.Companion] 通过日志记录. 但这种情况理论上不应发生.
 * - 事件处理时抛出异常不会停止监听器.
 * - 建议在事件处理中 (即 [listener] 里) 处理异常,
 *   或在参数 [coroutineContext] 中添加 [CoroutineExceptionHandler].
 *
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext].
 * @param concurrency 并发类型. 查看 [Listener.ConcurrencyKind]
 * @param priority  监听优先级，优先级越高越先执行
 * @param listener 事件处理器. 在接收到事件时会调用这个处理器. 其返回值意义参考 [ListeningStatus]. 其异常处理参考上文
 *
 * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [listener]
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
inline fun <reified E : Event> CoroutineScope.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority, listener)

/**
 * 与 [subscribe] 的区别是接受 [eventClass] 参数, 而不使用 `reified` 泛型
 *
 * @see CoroutineScope.subscribe
 *
 * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [listener]
 */
fun <E : Event> CoroutineScope.subscribe(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> ListeningStatus
): Listener<E> = eventClass.subscribeInternal(Handler(coroutineContext, concurrency, priority) { it.listener(it); })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param concurrency 并发类型默认为 [CONCURRENT]
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 * @param priority 处理优先级, 优先级高的先执行
 *
 * @return 监听器实例. 此监听器已经注册到指定事件上, 在事件广播时将会调用 [listener]
 *
 * @see CoroutineScope.subscribe 获取更多说明
 */
inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, listener)


/**
 * @see CoroutineScope.subscribe
 * @see CoroutineScope.subscribeAlways
 */
fun <E : Event> CoroutineScope.subscribeAlways(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, concurrency, priority) { it.listener(it); ListeningStatus.LISTENING }
)

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Event.broadcast] 时, [listener] 会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [CoroutineScope] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 * @param priority 处理优先级, 优先级高的先执行
 *
 * @see subscribe 获取更多说明
 */
@JvmSynthetic
inline fun <reified E : Event> CoroutineScope.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, listener)

/**
 * @see CoroutineScope.subscribeOnce
 */
fun <E : Event> CoroutineScope.subscribeOnce(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, LOCKED, priority) { it.listener(it); ListeningStatus.STOPPED }
)

//
// 以下为带筛选 Bot 的监听 (已启用)
//


/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see CoroutineScope.subscribe 获取更多说明
 */
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot")
@OptIn(MiraiInternalAPI::class)
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = this.subscribe(E::class, coroutineContext, concurrency, priority, handler)

/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see Bot.subscribe
 */
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribe(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = eventClass.subscribeInternal(
    Handler(
        coroutineContext,
        concurrency,
        priority
    ) { if (it.bot === this) it.handler(it) else ListeningStatus.LISTENING }
)

/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see CoroutineScope.subscribeAlways 获取更多说明
 */
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot1")
@kotlin.internal.LowPriorityInOverloadResolution
@OptIn(MiraiInternalAPI::class)
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, listener)

/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see Bot.subscribeAlways
 */
@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribeAlways(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, concurrency, priority) { if (it.bot === this) it.listener(it); ListeningStatus.LISTENING }
)

/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see subscribeOnce 获取更多说明
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@JvmSynthetic
@JvmName("subscribeOnceForBot2")
@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, listener)

/**
 * **注意:** 与 [CoroutineScope.subscribe] 不同的是,
 * [Bot.subscribe] 会筛选事件来源 [Bot], 只监听来自 [this] 的事件.
 *
 * @see Bot.subscribeOnce
 */
@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribeOnce(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> =
    eventClass.subscribeInternal(Handler(coroutineContext, LOCKED, priority) {
        if (it.bot === this) {
            it.listener(it)
            ListeningStatus.STOPPED
        } else ListeningStatus.LISTENING
    })

// endregion


// region 为了兼容旧版本的方法

@PlannedRemoval("1.2.0")
@JvmName("subscribe")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = handler
)

@PlannedRemoval("1.2.0")
@JvmName("subscribe")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = handler
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeAlways")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeAlwaysDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeAlways")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeAlwaysDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeOnce")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeOnceDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    coroutineContext = coroutineContext,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeOnce")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeOnceDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot")
@OptIn(MiraiInternalAPI::class)
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : BotEvent> Bot.subscribeDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = this.subscribe(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = handler
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribe")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = handler
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot1")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : BotEvent> Bot.subscribeAlwaysDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlways")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeAlwaysDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeOnceForBot2")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : BotEvent> Bot.subscribeOnceDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    coroutineContext = coroutineContext,
    priority = MONITOR,
    listener = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeOnce")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeOnceDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    priority = MONITOR,
    listener = listener
)
// endregion