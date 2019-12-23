@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.broadcastInternal

/**
 * 可被监听的.
 *
 * 可以是任何 class 或 object.
 *
 * @see subscribeAlways
 * @see subscribeWhile
 *
 * @see subscribeMessages
 */
interface Subscribable

/**
 * 所有事件的基类.
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * @see [broadcast] 广播事件
 * @see [subscribe] 监听事件
 */
abstract class Event : Subscribable {

    /**
     * 事件是否已取消. 事件需实现 [Cancellable] 才可以被取消, 否则这个字段为常量值 false
     */
    var cancelled: Boolean = false
        get() = field.takeIf { this is Cancellable } ?: false
        private set(value) = if (this is Cancellable) {
            check(!field); field = value
        } else throw UnsupportedOperationException()

    /**
     * 取消事件. 事件需实现 [Cancellable] 才可以被取消
     *
     * @throws UnsupportedOperationException 如果事件没有实现 [Cancellable]
     */
    fun cancel() {
        cancelled = true
    }
}

/**
 * 实现这个接口的事件可以被取消. 在广播中取消不会影响广播过程.
 */
interface Cancellable : Subscribable {
    val cancelled: Boolean

    fun cancel()
}

/**
 * 广播一个事件的唯一途径.
 */
suspend fun <E : Subscribable> E.broadcast(): E {
    @Suppress("EXPERIMENTAL_API_USAGE")
    this@broadcast.broadcastInternal() // inline, no extra cost
    return this
}

/**
 * 可控制是否需要广播这个事件包
 */
interface BroadcastControllable : Subscribable {
    val shouldBroadcast: Boolean
        get() = true
}