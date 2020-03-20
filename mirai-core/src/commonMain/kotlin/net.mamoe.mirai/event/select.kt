/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

/**
 *
 */
@SinceMirai("0.29.0")
@Suppress("unused")
@MiraiExperimentalAPI
suspend inline fun <reified T : MessagePacket<*, *>> T.whileSelectMessages(
    timeoutMillis: Long = -1,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Boolean>.() -> Unit
) {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0 " }

    return coroutineScope {
        var deferred: CompletableDeferred<Boolean> = CompletableDeferred()

        MessageSelectBuilder<T, Boolean>(SELECT_MESSAGE_STUB) { filter: T.(String) -> Boolean, listener: MessageListener<T, Any?> ->
            subscribeAlways<T> {
                val toString = it.message.toString()
                if (filter.invoke(it, toString)) {
                    val value = listener.invoke(it, toString)
                    if (value !== SELECT_MESSAGE_STUB) {
                        while (deferred.isCompleted)
                            delay(1)

                        deferred.complete(value as Boolean)
                    }
                }
            }
        }.apply(selectBuilder)

        while (deferred.await()) {
            deferred = CompletableDeferred()
        }
        coroutineContext[Job]!!.cancelChildren()
    }
}

@OptIn(ExperimentalTypeInference::class)
@MiraiExperimentalAPI
@SinceMirai("0.29.0")
@JvmName("selectMessages1")
suspend inline fun <reified T : MessagePacket<*, *>> T.selectMessagesUnit(
    timeoutMillis: Long = -1,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Unit>.() -> Unit
) = selectMessages(timeoutMillis, selectBuilder)


/**
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 */
@MiraiExperimentalAPI
@SinceMirai("0.29.0")
@Suppress("unused") // false positive
@OptIn(ExperimentalTypeInference::class)
@BuilderInference
suspend inline fun <reified T : MessagePacket<*, *>, R> T.selectMessages(
    timeoutMillis: Long = -1,
    @BuilderInference
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, R>.() -> Unit
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0 " }

    return coroutineScope {
        val deferred = CompletableDeferred<R>()

        MessageSelectBuilder<T, R>(SELECT_MESSAGE_STUB) { filter: T.(String) -> Boolean, listener: MessageListener<T, Any?> ->
            subscribeAlways<T> {
                val toString = it.message.toString()
                if (filter.invoke(it, toString)) {
                    val value = listener.invoke(it, toString)
                    if (value !== SELECT_MESSAGE_STUB) {
                        @Suppress("UNCHECKED_CAST")
                        deferred.complete(value as R)
                    }
                }
            }
        }.apply(selectBuilder)

        deferred.await().also { coroutineContext[Job]!!.cancelChildren() }
    }
}

@PublishedApi
internal val SELECT_MESSAGE_STUB = Any()

@SinceMirai("0.29.0")
class MessageSelectBuilder<M : MessagePacket<*, *>, R>(
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : MessageSubscribersBuilder<M, Unit, R, Any?>(stub, subscriber)