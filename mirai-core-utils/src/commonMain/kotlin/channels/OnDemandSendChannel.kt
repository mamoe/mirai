/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.channels

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.utils.UtilsLogger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * 按需供给的 [Channel].
 */
public interface OnDemandSendChannel<T, V> {
    /**
     * 挂起协程, 直到 [OnDemandReceiveChannel] 期望接收一个 [V], 届时将 [value] 传递给 [OnDemandReceiveChannel.receiveOrNull], 成为其返回值.
     *
     * 若在调用 [emit] 时已经有 [OnDemandReceiveChannel] 正在等待, 则该 [OnDemandReceiveChannel] 协程会立即[恢复][Continuation.resumeWith].
     *
     * 若 [OnDemandReceiveChannel] 已经[完结][OnDemandReceiveChannel.finish], [OnDemandSendChannel.emit] 会抛出 [IllegalProducerStateException].
     */
    public suspend fun emit(value: V): T

    /**
     * 标记此 [OnDemandSendChannel] 在生产 [V] 的过程中出现错误.
     *
     * 这也会终止此 [OnDemandSendChannel], 随后 [OnDemandReceiveChannel.receiveOrNull] 将会抛出 [ProducerFailureException].
     */
    public fun finishExceptionally(exception: Throwable)

    /**
     * 标记此 [OnDemandSendChannel] 已经没有更多 [V] 可生产.
     *
     * 随后 [OnDemandReceiveChannel.receiveOrNull] 将会抛出 [IllegalStateException].
     */
    public fun finish()
}

/**
 * 按需消费者.
 *
 * 与 [ReceiveChannel] 不同, [OnDemandReceiveChannel] 只有在调用 [expectMore] 后才会让[生产者][OnDemandSendChannel] 开始生产下一个 [V].
 */
public interface OnDemandReceiveChannel<T, V> {
    /**
     * 挂起协程并等待从 [OnDemandSendChannel] [接收][OnDemandSendChannel.emit]一个 [V].
     *
     * 当此函数被多个线程 (协程) 同时调用时, 只有一个线程挂起并获得 [V], 其他线程将会
     *
     * @throws ProducerFailureException 当 [OnDemandSendChannel.finishExceptionally] 时抛出.
     * @throws CancellationException 当协程被取消时抛出
     * @throws IllegalProducerStateException 当状态异常, 如未调用 [expectMore] 时抛出
     */
    @Throws(ProducerFailureException::class, CancellationException::class)
    public suspend fun receiveOrNull(): V?

    /**
     * 期待 [OnDemandSendChannel] 再生产一个 [V]. 期望生产后必须在之后调用 [receiveOrNull] 或 [finish] 来消耗生产的 [V].
     *
     * 在成功发起期待后返回 `true`; 在 [OnDemandSendChannel] 已经[完结][OnDemandSendChannel.finish] 时返回 `false`.
     *
     * @throws IllegalProducerStateException 当 [expectMore] 被调用后, 没有调用 [receiveOrNull] 就又调用了 [expectMore] 时抛出
     */
    public fun expectMore(ticket: T): Boolean

    /**
     * 标记此 [OnDemandReceiveChannel] 已经完结.
     *
     * 如果 [OnDemandSendChannel] 仍在运行, 将会 (正常地) 取消 [OnDemandSendChannel].
     *
     * 随后 [OnDemandSendChannel.emit] 将会抛出 [IllegalStateException].
     */
    public fun finish()
}

public fun <T, V> OnDemandReceiveChannel(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    logger: UtilsLogger = UtilsLogger.noop(),
    producerCoroutine: suspend OnDemandSendChannel<T, V>.(initialTicket: T) -> Unit,
): OnDemandReceiveChannel<T, V> = CoroutineOnDemandReceiveChannel(parentCoroutineContext, logger, producerCoroutine)

