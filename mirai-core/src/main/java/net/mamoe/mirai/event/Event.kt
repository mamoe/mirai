@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.broadcastInternal

/**
 * 所有事件的基类.
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * @see [broadcast] 广播事件
 * @see [subscribe] 监听事件
 * @author Him188moe
 */
abstract class Event {

    /**
     * 事件是否已取消. 事件需实现 [Cancellable] 才可以被取消, 否则这个字段为常量值 false
     */
    var cancelled: Boolean = false
        get() = field.takeIf { this is Cancellable } ?: false
        private set(value) = if (this is Cancellable) {
            check(!field); field = value
        } else throw UnsupportedOperationException()

    /**
     * 取消事件. 事件需实现 [Cancellable] 才可以被取消, 否则调用这个方法将会得到 [UnsupportedOperationException]
     *
     * @throws UnsupportedOperationException 如果事件没有实现 [Cancellable]
     */
    @Throws(UnsupportedOperationException::class)
    fun cancel() {
        cancelled = true
    }
}

/**
 * 实现这个接口的事件可以被取消.
 *
 * @author Him188moe
 */
interface Cancellable {
    val cancelled: Boolean

    fun cancel()
}

/**
 * 广播一个事件的唯一途径
 */
@Synchronized
@Suppress("UNCHECKED_CAST")
suspend fun <E : Event> E.broadcast(): E = this.broadcastInternal()