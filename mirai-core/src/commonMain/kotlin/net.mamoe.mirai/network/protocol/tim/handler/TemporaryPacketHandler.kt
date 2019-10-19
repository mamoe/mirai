package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import kotlin.reflect.KClass

/**
 * 临时数据包处理器
 * ```kotlin
 * session.addHandler<ClientTouchResponsePacket>{
 *   toSend { ClientTouchPacket() }
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
        private val fromSession: BotSession
) {
    private lateinit var toSend: ClientPacket

    private lateinit var expect: suspend (P) -> R


    lateinit var session: BotSession//无需覆盖

    fun toSend(packet: () -> ClientPacket) {
        this.toSend = packet()
    }

    fun toSend(packet: ClientPacket) {
        this.toSend = packet
    }


    fun onExpect(handler: suspend (P) -> R) {
        this.expect = handler
    }

    suspend fun send(session: BotSession) {
        this.session = session
        session.socket.sendPacket(toSend)
    }

    suspend fun shouldRemove(session: BotSession, packet: ServerPacket): Boolean {
        if (expectationClass.isInstance(packet) && session === this.fromSession) {

            @Suppress("UNCHECKED_CAST")
            val ret = try {
                expect(packet as P)
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
                return true
            }
            deferred.complete(ret)
            return true
        }
        return false
    }
}