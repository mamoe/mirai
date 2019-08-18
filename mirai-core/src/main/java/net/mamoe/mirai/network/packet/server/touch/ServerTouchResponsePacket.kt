package net.mamoe.mirai.network.packet.server.touch

import lombok.ToString
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.readIP
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.getRandomKey
import java.io.DataInputStream

/**
 * A packet received when logging in, used to redirect server address
 *
 * @see net.mamoe.mirai.network.packet.client.login.ClientServerRedirectionPacket
 * @see net.mamoe.mirai.network.packet.client.login.ClientPasswordSubmissionPacket
 *
 * @author Him188moe
 */
@ToString
class ServerTouchResponsePacket(private val type: Type, inputStream: DataInputStream) : ServerPacket(inputStream) {
    var serverIP: String? = null;

    var loginTime: Int = 0
    lateinit var loginIP: String
    lateinit var token: ByteArray
    lateinit var tgtgtKey: ByteArray

    enum class Type {
        TYPE_08_25_31_01,
        TYPE_08_25_31_02,
    }

    @ExperimentalUnsignedTypes
    override fun decode() {
        when (input.readByte().toInt()) {
            0xFE -> {
                input.skip(94)
                serverIP = input.readIP()
            }
            0X00 -> {
                input.skip(4)
                token = input.readNBytes(56)
                input.skip(6)

                loginTime = input.readInt()
                loginIP = input.readIP()
                tgtgtKey = getRandomKey(16)
            }

            else -> {
                throw IllegalStateException()
            }
        }
    }
}

class ServerTouchResponsePacketEncrypted(private val type: ServerTouchResponsePacket.Type, inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    fun decrypt(): ServerTouchResponsePacket {
        input.skip(14)
        return ServerTouchResponsePacket(type, DataInputStream(TEACryptor.decrypt(input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }, when (type) {
            ServerTouchResponsePacket.Type.TYPE_08_25_31_01 -> Protocol.redirectionKey.toByteArray()
            ServerTouchResponsePacket.Type.TYPE_08_25_31_02 -> Protocol._0825key.toByteArray()
        }).inputStream()));
    }
}