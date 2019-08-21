package net.mamoe.mirai.network.packet.server.touch

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.readIP
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.getRandomKey
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

/**
 * A packet received when logging in, used to redirect server address
 *
 * @see net.mamoe.mirai.network.packet.client.login.ClientServerRedirectionPacket
 * @see net.mamoe.mirai.network.packet.client.login.ClientPasswordSubmissionPacket
 *
 * @author Him188moe
 */
class ServerTouchResponsePacket(inputStream: DataInputStream) : ServerPacket(inputStream) {
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
        when (val id = input.readByte().toUByte().toInt()) {
            0xFE -> {
                input.skip(94)
                serverIP = input.readIP()
            }
            0x00 -> {
                input.skip(4)
                token = input.readNBytes(56)
                input.skip(6)

                loginTime = input.readInt()
                loginIP = input.readIP()
                tgtgtKey = getRandomKey(16)
            }

            else -> {
                throw IllegalStateException(arrayOf(id.toUByte()).toUByteArray().toHexString())
            }
        }
    }
}

class ServerTouchResponsePacketEncrypted(private val type: ServerTouchResponsePacket.Type, inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    @ExperimentalUnsignedTypes
    fun decrypt(): ServerTouchResponsePacket {
        input.skip(7)
        var bytes = input.readAllBytes();
        bytes = bytes.copyOfRange(0, bytes.size - 1);
        println(bytes.toUByteArray().toHexString())

        return ServerTouchResponsePacket(DataInputStream(TEACryptor.decrypt(bytes, when (type) {
            ServerTouchResponsePacket.Type.TYPE_08_25_31_02 -> Protocol.redirectionKey.hexToBytes()
            ServerTouchResponsePacket.Type.TYPE_08_25_31_01 -> Protocol._0825key.hexToBytes()
        }).inputStream()));
    }
}