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
) : Listener<E>, CompletableJob by SupervisorJob(parentJob) { // avoid being cancelled on handling event

    private val subscriberContext: CoroutineContext = subscriberContext + this // override Job.

    val lock: Mutex? = when (concurrencyKind) {
        ConcurrencyKind.LOCKED -> Mutex()
        else -> null
    }

    @Suppress("unused")
    override suspend fun onEvent(event: E): ListeningStatus {
        if (isCompleted || isCancelled) return ListeningStatus.STOPPED
        if (!isActive) return ListeningStatus.LISTENING
        return try {
            // Inherit context.
            withContext(subscriberContext) { listenerBlock.invoke(event) }.also { if (it == ListeningStatus.STOPPED) this.complete() }
        } catch (e: Throwable) {
            subscriberContext[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: currentCoroutineContext()[CoroutineExceptionHandler]?.handleException(subscriberContext, e)
                ?: kotlin.run {
                    val logger = if (event is BotEvent) event.bot.logger else logger
                    val subscriberName = subscriberContext[CoroutineName]?.name ?: "<unnamed>"
                    val broadcasterName = currentCoroutineContext()[CoroutineName]?.name ?: "<unnamed>"
                    val message =
                        "An exception occurred when processing event. " +
                                "Subscriber scope: '$subscriberName'. " +
                                "Broadcaster scope: '$broadcasterName'"
                    logger.warning(message, e)
                }
            // this.complete() // do not `completeExceptionally`, otherwise parentJob will fai`l.
            // ListeningStatus.STOPPED

            // not stopping listening.
            ListeningStatus.LISTENING
        }
    }

    companion object {
        private val logger by lazy {
            MiraiLogger.Factory.create(SafeListener::class)
        }
    }
}