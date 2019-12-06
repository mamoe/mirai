@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.sendPacket
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * 临时数据包处理器
 * ```kotlin
 * session.addHandler<ClientTouchResponsePacket>{
 *   toSend { TouchPacket() }
 *   onExpect {//it: ClientTouchResponsePacket
 *      //do sth.
 *   }
 * }
 * ```
 *
 * @see BotSession.sendAndExpectAsync
 */
internal class TemporaryPacketHandler<P : Packet, R>(
    private val expectationClass: KClass<P>,
    private val deferred: CompletableDeferred<R>,
    private val fromSession: BotSession,
    private val checkSequence: Boolean,
    /**
     * 调用者的 [CoroutineContext]. 包处理过程将会在这个 context 下运行
     */
    private val callerContext: CoroutineContext
) {
    private lateinit var toSend: OutgoingPacket

    private lateinit var handler: suspend (P) -> R

    lateinit var session: BotSession//无需覆盖


    @Suppress("NOTHING_TO_INLINE")
    inline fun toSend(packet: OutgoingPacket) {
        this.toSend = packet
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun onExpect(noinline handler: suspend (P) -> R) {
        this.handler = handler
    }

    internal suspend inline fun send(session: BotSession) {
        this.session = session
        session.sendPacket(toSend)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun filter(session: BotSession, packet: Packet, sequenceId: UShort): Boolean =
        expectationClass.isInstance(packet) && session === this.fromSession && if (checkSequence) sequenceId == toSend.sequenceId else true

    internal suspend inline fun doReceiveWithoutExceptions(packet: Packet) {
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