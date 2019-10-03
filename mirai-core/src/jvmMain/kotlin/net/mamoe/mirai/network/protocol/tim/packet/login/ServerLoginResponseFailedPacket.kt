package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseFailedPacket(val loginState: LoginState, input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
    }
}