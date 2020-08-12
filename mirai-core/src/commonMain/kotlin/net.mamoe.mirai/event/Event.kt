/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.event.internal.broadcastInternal
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.internal.runBlocking
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Volatile

/**
 * 可被监听的类, 可以是任何 class 或 object.
 *
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * 所有 [Event] 都应继承 [AbstractEvent] 而不要直接实现 [Event]. 否则将无法广播也无法监听.
 *
 * ### 广播
 * 广播事件的唯一方式为 [broadcast].
 *
 * @see subscribeAlways
 * @see subscribeOnce
 *
 * @see subscribeMessages
 *
 * @see [broadcast] 广播事件
 * @see [CoroutineScope.subscribe] 监听事件
 *
 * @see CancellableEvent 可被取消的事件
 */
interface Event {
    /**
     * 事件是否已被拦截.
     *
     * 所有事件都可以被拦截, 拦截后低优先级的监听器将不会处理到这个事件.
     *
     * @see intercept 拦截事件
     */
    val isIntercepted: Boolean

    /**
     * 拦截这个事件
     *
     * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
     *
     * 优先级为 [Listener.EventPriority.MONITOR] 的监听器不应该调用这个函数.
     *
     * @see Listener.EventPriority 查看优先级相关信息
     */
    @SinceMirai("1.0.0")
    fun intercept()
}

/**
 * 所有实现了 [Event] 接口的类都应该继承的父类.
 *
 * 在使用事件时应使用类型 [Event]. 在实现自定义事件时应继承 [AbstractEvent].
 */
abstract class AbstractEvent : Event {
    /** 限制一个事件实例不能并行广播. (适用于 object 广播的情况) */
    @JvmField
    internal val broadCastLock = Mutex()

    @JvmField
    @Volatile
    internal var _intercepted = false

    @Volatile
    private var _cancelled = false

    // 实现 Event
    /**
     * @see Event.isIntercepted
     */
    @SinceMirai("1.0.0")
    override val isIntercepted: Boolean
        get() = _intercepted

    /**
     * @see Event.intercept
     */
    override fun intercept() {
        _intercepted = true
    }

    // 实现 CancellableEvent
    /**
     * @see CancellableEvent.isCancelled
     */
    val isCancelled: Boolean get() = _cancelled

    /**
     * @see CancellableEvent.cancel
     */
    fun cancel() {
        check(this is CancellableEvent) {
            "Event $this is not cancellable"
        }
        _cancelled = true
    }
}

/**
 * 可被取消的事件
 */
interface CancellableEvent : Event {
    /**
     * 事件是否已被取消.
     *
     * 事件需实现 [CancellableEvent] 接口才可以被取消,
     * 否则此属性固定返回 false.
     */
    val isCancelled: Boolean

    /**
     * 取消这个事件.
     * 事件需实现 [CancellableEvent] 接口才可以被取消
     *
     * @throws IllegalStateException 当事件未实现接口 [CancellableEvent] 时抛出
     */
    fun cancel()
}

/**
 * 广播一个事件的唯一途径.
 *
 * 当事件被实现为 Kotlin `object` 时, 同一时刻只能有一个 [广播][broadcast] 存在.
 * 较晚执行的 [广播][broadcast] 将会挂起协程并等待之前的广播任务结束.
 *
 * @see __broadcastJava Java 使用
 */
@JvmSynthetic
suspend fun <E : Event> E.broadcast(): E = apply {
    check(this is AbstractEvent) {
        "Events must extend AbstractEvent"
    }

    if (this is BroadcastControllable && !this.shouldBroadcast) {
        return@apply
    }
    this.broadCastLock.withLock {
        this._intercepted = false
        this.broadcastInternal() // inline, no extra cost
    }
}

/**
 * 在 Java 广播一个事件的唯一途径.
 *
 * 调用方法: `EventKt.broadcast(event)`
 */
@Suppress("FunctionName")
@JvmName("broadcast")
@JavaFriendlyAPI
fun <E : Event> E.__broadcastJava(): E = apply {
    if (this is BroadcastControllable && !this.shouldBroadcast) {
        return@apply
    }
    runBlocking { this@__broadcastJava.broadcast() }
}

/**
 * 设置为 `true` 以关闭事件.
 * 所有的 `subscribe` 都能正常添加到监听器列表, 但所有的广播都会直接返回.
 */
@MiraiExperimentalAPI
var EventDisabled = false

/**
 * 可控制是否需要广播这个事件
 */
interface BroadcastControllable : Event {
    /**
     * 返回 `false` 时将不会广播这个事件.
     */
    val shouldBroadcast: Boolean
        get() = true
}

