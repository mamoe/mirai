package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 */
class ServerLoginVerificationCodeResponsePacket(input: DataInputStream) : ServerPacket(input) {
    private var verifyCodeLength: Int = 0
    private lateinit var verifyCode: String
    private lateinit var token00BA: ByteArray


    override fun decode() {

        TODO()
    }
}