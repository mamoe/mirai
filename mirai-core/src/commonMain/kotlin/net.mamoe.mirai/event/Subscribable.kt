@file:Suppress("unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.internal.broadcastInternal
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.withSwitch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * 可被监听的.
 *
 * 可以是任何 class 或 object.
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

    init {
        if (EventDebuggingFlag) {
            EventDebugLogger.debug(this::class.simpleName + " created")
        }
    }
}

internal object EventDebugLogger : MiraiLogger by DefaultLogger("Event").withSwitch(EventDebuggingFlag)

private val EventDebuggingFlag: Boolean by lazy {
    // avoid 'Condition is always true'
    false
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
 * 这个方法将会把处理挂起在 [context] 下运行. 默认为使用 [EventDispatcher] 调度事件协程.
 *
 * @param context 事件处理协程运行的 [CoroutineContext].
 */
@Suppress("UNCHECKED_CAST")
@JvmOverloads
suspend fun <E : Subscribable> E.broadcast(context: CoroutineContext = EmptyCoroutineContext): E {
    if (EventDebuggingFlag) {
        EventDebugLogger.debug(this::class.simpleName + " pre broadcast")
    }
    try {
        @Suppress("EXPERIMENTAL_API_USAGE")
        return withContext(EventScope.newCoroutineContext(context)) { this@broadcast.broadcastInternal() }
    } finally {
        if (EventDebuggingFlag) {
            EventDebugLogger.debug(this::class.simpleName + " after broadcast")
        }
    }
}

internal expect val EventDispatcher: CoroutineDispatcher

object EventScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        EventDispatcher + CoroutineExceptionHandler { _, e ->
            MiraiLogger.error("An exception is thrown in EventScope", e)
        }
}