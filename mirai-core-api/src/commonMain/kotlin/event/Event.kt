/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.sync.Mutex
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmField
import kotlin.jvm.Volatile

/**
 * 表示一个事件.
 *
 * 实现时应继承 [AbstractEvent] 而不要直接实现 [Event]. 否则将无法广播.
 *
 * ## 广播事件
 *
 * 使用 [Event.broadcast] 或 [IMirai.broadcastEvent].
 *
 * Kotlin:
 * ```
 * val event: Event = ...
 * event.broadcast()
 * ```
 *
 * Java:
 * ```
 * Event event = ...;
 * Mirai.getInstance().broadcastEvent(event);
 * ```
 *
 * ## 监听事件
 *
 * 参阅 [EventChannel].
 *
 * @see CancellableEvent 可被取消的事件
 */
public interface Event {
    /**
     * 事件是否已被拦截.
     *
     * 所有事件都可以被拦截, 拦截后低优先级的监听器将不会处理到这个事件.
     *
     * @see intercept 拦截事件
     */
    public val isIntercepted: Boolean

    /**
     * 拦截这个事件
     *
     * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
     *
     * 优先级为 [EventPriority.MONITOR] 的监听器不应该调用这个函数.
     *
     * @see EventPriority 查看优先级相关信息
     */
    public fun intercept()
}

/**
 * 所有实现了 [Event] 接口的类都应该继承的父类.
 *
 * 在使用事件时应使用类型 [Event]. 在实现自定义事件时应继承 [AbstractEvent].
 */
public abstract class AbstractEvent : Event {
    /** 限制一个事件实例不能并行广播. (适用于 object 广播的情况) */
    @JvmField
    @MiraiInternalApi
    public val broadCastLock: Mutex = Mutex()

    @Suppress("PropertyName")
    @JvmField
    @Volatile
    @MiraiInternalApi
    public var _intercepted: Boolean = false

    @Volatile
    private var _cancelled = false

    // 实现 Event
    /**
     * @see Event.isIntercepted
     */
    public override val isIntercepted: Boolean
        get() {
            @OptIn(MiraiInternalApi::class)
            return _intercepted
        }

    /**
     * @see Event.intercept
     */
    public override fun intercept() {
        @OptIn(MiraiInternalApi::class)
        _intercepted = true
    }

    // 实现 CancellableEvent
    /**
     * @see CancellableEvent.isCancelled
     */
    public val isCancelled: Boolean get() = _cancelled

    /**
     * @see CancellableEvent.cancel
     * @throws IllegalStateException 当事件未实现接口 [CancellableEvent] 时抛出
     */
    public fun cancel() {
        check(this is CancellableEvent) {
            "Event $this is not cancellable"
        }
        _cancelled = true
    }
}

/**
 * 可被取消的事件
 */
public interface CancellableEvent : Event {
    /**
     * 事件是否已被取消.
     *
     * 事件需实现 [CancellableEvent] 接口才可以被取消,
     * 否则此属性固定返回 false.
     */
    public val isCancelled: Boolean

    /**
     * 取消这个事件.
     * 事件需实现 [CancellableEvent] 接口才可以被取消
     */
    public fun cancel()
}

/**
 * 广播一个事件的唯一途径.
 *
 * 当事件被实现为 Kotlin `object` 时, 同一时刻只能有一个 [广播][broadcast] 存在.
 * 较晚执行的 [广播][broadcast] 将会挂起协程并等待之前的广播任务结束.
 *
 * ## 异常处理
 *
 * 作为广播方, 本函数不会抛出监听方产生的异常.
 *
 * [EventChannel.filter] 和 [Listener.onEvent] 时产生的异常只会由监听方处理.
 */
@JvmBlockingBridge
@Suppress("TOP_LEVEL_FUNCTIONS_NOT_SUPPORTED") // compiler bug
public suspend fun <E : Event> E.broadcast(): E {
    Mirai.broadcastEvent(this)
    return this
}

/**
 * 可控制是否需要广播这个事件
 */
public interface BroadcastControllable : Event {
    /**
     * 返回 `false` 时将不会广播这个事件.
     */
    public val shouldBroadcast: Boolean
        get() = true
}

