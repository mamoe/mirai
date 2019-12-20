@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.handler

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import net.mamoe.mirai.data.Packet
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

internal class TemporaryPacketHandler<P : Packet, R>(
    private val expectationClass: KClass<P>,
    private val deferred: CompletableDeferred<R>,
    private val checkSequence: UShort? = null,
    /**
     * 调用者的 [CoroutineContext]. 包处理过程将会在这个 context 下运行
     */
    private val callerContext: CoroutineContext,
    private val handler: suspend (P) -> R
) {
    internal fun filter(packet: Packet, sequenceId: UShort): Boolean =
        expectationClass.isInstance(packet) && if (checkSequence != null) sequenceId == checkSequence else true

    internal suspend inline fun doReceivePassingExceptionsToDeferred(packet: Packet) {
        @Suppress("UNCHECKED_CAST")
        val ret = try {
            withContext(callerContext) {
                handler(packet as P)
            }
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
            return
        }
        deferred.complete(ret)
    }
}

