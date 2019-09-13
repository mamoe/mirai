package net.mamoe.mirai.network.handler

import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.ServerPacket
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * 临时数据包处理器
 *
 * @see LoginSession.expectPacket
 */
open class TemporaryPacketHandler<P : ServerPacket>(
        private val expectationClass: KClass<P>,
        private val future: CompletableFuture<Unit>,
        private val fromSession: LoginSession
) {
    private lateinit var toSend: ClientPacket

    private lateinit var expect: (P) -> Unit


    lateinit var session: LoginSession//无需覆盖

    fun toSend(packet: () -> ClientPacket) {
        this.toSend = packet()
    }


    fun expect(handler: (P) -> Unit) {
        this.expect = handler
    }

    fun send(session: LoginSession) {
        this.session = session
        session.socket.sendPacket(toSend)
    }

    fun onPacketReceived(session: LoginSession, packet: ServerPacket): Boolean {
        if (expectationClass.isInstance(packet) && session === this.fromSession) {
            @Suppress("UNCHECKED_CAST")
            expect(packet as P)
            future.complete(Unit)
            return true
        }
        return false
    }
}