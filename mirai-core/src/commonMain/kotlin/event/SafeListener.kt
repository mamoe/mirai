/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import kotlin.coroutines.CoroutineContext

/**
 * 包装用户的 [Listener], 增加异常处理, 实现 [ConcurrencyKind].
 */
internal class SafeListener<in E : Event> internal constructor(
    parentJob: Job?,
    subscriberContext: CoroutineContext,
    private val listenerBlock: suspend (E) -> ListeningStatus,
    override val concurrencyKind: ConcurrencyKind,
    override val priority: EventPriority,
    private val creationStacktrace: Exception? = if (traceEnabled) Exception() else null
) : Listener<E>, CompletableJob by SupervisorJob(parentJob) { // avoid being cancelled on handling event

    private val subscriberContext: CoroutineContext = subscriberContext + this // override Job.

    val lock: Mutex? = when (concurrencyKind) {
        ConcurrencyKind.LOCKED -> Mutex()
        else -> null
    }

    override fun toString(): String {
        return if (creationStacktrace != null) {
            "SafeListener(concurrency=${concurrencyKind}" +
                    ", priority=$priority" +
                    ", subscriberContext=${subscriberContext.minusKey(Job)}" +
                    ", trace=${creationStacktrace.stackTraceToString()})"
        } else {
            return "SafeListener(concurrency=${concurrencyKind}" +
                    ", priority=$priority" +
                    ", subscriberContext=${subscriberContext.minusKey(Job)})" // remove this
        }
    }

    @Suppress("unused")
    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            // Inherit context.
            withContext(subscriberContext) { listenerBlock.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            // 若监听方使用了 EventChannel.exceptionHandler, 那么它就能处理异常, 否则将只记录异常.
            val subscriberExceptionHandler = subscriberContext[CoroutineExceptionHandler]
            if (subscriberExceptionHandler == null) {
                val logger = if (event is BotEvent) event.bot.logger else logger
                val subscriberName =
                    subscriberContext[CoroutineName]?.name
                        ?: "<unnamed>" // Bot 协程域有 CoroutineName, mirai-console 也会给插件域加入.
                val broadcasterName = currentCoroutineContext()[CoroutineName]?.name ?: "<unnamed>"
                val message =
                    "An exception occurred when processing event. " +
                            "Subscriber scope: '$subscriberName'. " +
                            "Broadcaster scope: '$broadcasterName'"
                logger.warning(message, e)

            } else {
                subscriberExceptionHandler.handleException(subscriberContext, e)
            }


            ListeningStatus.LISTENING
        }
    }

    companion object {
        private val logger by lazy {
            MiraiLogger.Factory.create(SafeListener::class)
        }
    }
}

private val traceEnabled by lazy { systemProp("mirai.event.trace", true) }