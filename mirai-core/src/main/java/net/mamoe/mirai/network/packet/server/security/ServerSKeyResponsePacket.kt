package net.mamoe.mirai.network.packet.server.security

import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.goto
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerSKeyResponsePacket(input: DataInputStream) : ServerPacket(input) {
    lateinit var sKey: String

    override fun decode() {
        this.sKey = String(this.input.goto(4).readNBytes(10))
    }
}

/**
 * Encrypted using [0828_rec_decr_key], decrypting in RobotNetworkHandler
 *
 * @author Him188moe
 */
class ServerSKeyResponsePacketEncrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    fun decrypt(sessionKey: ByteArray): ServerSKeyResponsePacket {
        this.input goto 14
        val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
        return ServerSKeyResponsePacket(TEACryptor.decrypt(data, sessionKey).dataInputStream());
    }
}