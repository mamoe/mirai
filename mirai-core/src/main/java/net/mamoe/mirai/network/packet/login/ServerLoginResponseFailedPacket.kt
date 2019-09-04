package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseFailedPacket(val loginState: LoginState, input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
    }
}