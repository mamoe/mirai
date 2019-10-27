package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
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
 * @see BotSession.sendAndExpect
 */
class TemporaryPacketHandler<P : ServerPacket, R>(
    private val expectationClass: KClass<P>,
    private val deferred: CompletableDeferred<R>,
    private val fromSession: BotSession,
    private val checkSequence: Boolean
) {
    private lateinit var toSend: OutgoingPacket

    private lateinit var handler: suspend (P) -> R

    lateinit var session: BotSession//无需覆盖


    fun toSend(packet: OutgoingPacket) {
        this.toSend = packet
    }

    fun onExpect(handler: suspend (P) -> R) {
        this.handler = handler
    }

    suspend fun send(session: BotSession) {
        require(::handler.isInitialized) { "handler is not initialized" }
        this.session = session
        session.socket.sendPacket(toSend)
    }

    @ExperimentalUnsignedTypes
    fun filter(session: BotSession, packet: ServerPacket): Boolean =
        expectationClass.isInstance(packet) && session === this.fromSession && if (checkSequence) packet.sequenceId == toSend.sequenceId else true

    suspend fun doReceive(packet: ServerPacket) {
        @Suppress("UNCHECKED_CAST")
        val ret = try {
            handler(packet as P)
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            return
        }
        deferred.complete(ret)
        return
    }
}