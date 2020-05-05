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

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.event.internal.broadcastInternal
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmSynthetic
import kotlin.jvm.Volatile

/**
 * 可被监听的类, 可以是任何 class 或 object.
 *
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * 所有 [Event] 都应继承 [AbstractEvent] 而不要直接实现 [Event]. 否则将无法广播也无法监听.
 *
 * @see subscribeAlways
 * @see subscribeOnce
 *
 * @see subscribeMessages
 *
 * @see [broadcast] 广播事件
 * @see [subscribe] 监听事件
 */
interface Event {
    /**
     * 事件是否已被拦截.
     *
     * 所有事件都可以被拦截, 拦截后低优先级的监听器将不会处理到这个事件.
     */
    @SinceMirai("1.0.0")
    val isIntercepted: Boolean

    /**
     * 拦截这个事件
     */
    @SinceMirai("1.0.0")
    fun intercept()


    @Deprecated(
        """
        Don't implement Event but extend AbstractEvent instead.
    """, level = DeprecationLevel.HIDDEN
    ) // so Kotlin class won't be compiled.
    @Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "PropertyName")
    @get:JvmSynthetic // so Java user won't see it
    internal val DoNotImplementThisClassButExtendAbstractEvent: Nothing
}

/**
 * 所有实现了 [Event] 接口的类都应该继承的父类.
 *
 * 在使用事件时应使用类型 [Event]. 在实现自定义事件时应继承 [AbstractEvent].
 */
@SinceMirai("1.0.0")
abstract class AbstractEvent : Event {
    @Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "PropertyName")
    @get:JvmSynthetic // so Java user won't see it
    @Deprecated("prohibit illegal overrides", level = DeprecationLevel.HIDDEN)
    final override val DoNotImplementThisClassButExtendAbstractEvent: Nothing
        get() = throw Error("Shouldn't be reached")

    @Volatile
    private var _intercepted = false
    private val _cancelled = atomic(false)

    // 实现 Event
    override val isIntercepted: Boolean get() = _intercepted

    @SinceMirai("1.0.0")
    override fun intercept() {
        _intercepted = true
    }

    // 实现 CancellableEvent
    val isCancelled: Boolean get() = _cancelled.value
    fun cancel() {
        check(this is CancellableEvent) {
            "Event $this is not cancellable"
        }
        _cancelled.value = true
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
 */
@OptIn(MiraiInternalAPI::class)
suspend fun <E : Event> E.broadcast(): E = apply {
    if (this is BroadcastControllable && !this.shouldBroadcast) {
        return@apply
    }
    this@broadcast.broadcastInternal() // inline, no extra cost
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


@PlannedRemoval("1.1.0")
@Deprecated(
    "use AbstractEvent and implement CancellableEvent",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("AbstractEvent", "net.mamoe.mirai.event.AbstractEvent")
)
abstract class AbstractCancellableEvent : AbstractEvent(), CancellableEvent
