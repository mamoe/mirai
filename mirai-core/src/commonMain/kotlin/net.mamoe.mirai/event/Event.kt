@file:Suppress("unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.internal.broadcastInternal
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.log
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * 所有事件的基类.
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * @see [broadcast] 广播事件
 * @see [subscribe] 监听事件
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
    fun cancel() {
        cancelled = true
    }
}

/**
 * 实现这个接口的事件可以被取消.
 */
interface Cancellable {
    val cancelled: Boolean

    fun cancel()
}

/**
 * 广播一个事件的唯一途径.
 * 若 [context] 不包含 [CoroutineExceptionHandler], 将会使用默认的异常捕获, 即 [log]
 * 也就是说, 这个方法不会抛出异常, 只会把异常交由 [context] 捕获
 */
@Suppress("UNCHECKED_CAST")
@JvmOverloads
suspend fun <E : Event> E.broadcast(context: CoroutineContext = EmptyCoroutineContext): E {
    return withContext(EventScope.newCoroutineContext(context)) { this@broadcast.broadcastInternal() }
}

/**
 * 事件协程作用域.
 * 所有的事件 [broadcast] 过程均在此作用域下运行.
 *
 * 然而, 若在事件处理过程中使用到 [Contact.sendMessage] 等会 [发送数据包][BotNetworkHandler.sendPacket] 的方法,
 * 发送过程将会通过 [withContext] 将协程切换到 [BotNetworkHandler.NetworkScope]
 */
object EventScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
}