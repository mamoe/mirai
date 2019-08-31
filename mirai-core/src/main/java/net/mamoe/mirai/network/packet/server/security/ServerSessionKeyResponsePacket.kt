package net.mamoe.mirai.network.packet.server.security

import net.mamoe.mirai.network.packet.client.writeHex
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.goto
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.lazyEncode
import java.io.DataInputStream

/**
 * Dispose_0828
 *
 * @author Him188moe
 */
class ServerSessionKeyResponsePacket(inputStream: DataInputStream, val dataLength: Int) : ServerPacket(inputStream) {
    lateinit var sessionKey: ByteArray
    lateinit var tlv0105: ByteArray

    @ExperimentalUnsignedTypes
    override fun decode() {
        when (dataLength) {
            407 -> {
                input goto 25
                sessionKey = input.readNBytes(16)
            }

            439 -> {
                input.goto(63)
                sessionKey = input.readNBytes(16)
            }

            512,
            527 -> {
                input.goto(63)
                sessionKey = input.readNBytes(16)
                tlv0105 = lazyEncode {
                    it.writeHex("01 05 00 88 00 01 01 02 00 40 02 01 03 3C 01 03 00 00")
                    input.goto(dataLength - 122)
                    it.write(input.readNBytes(56))
                    it.writeHex("00 40 02 02 03 3C 01 03 00 00")
                    input.goto(dataLength - 55)
                    it.write(input.readNBytes(56))
                } //todo 这个 tlv0105似乎可以保存起来然后下次登录时使用.
            }

            else -> throw IllegalArgumentException(dataLength.toString())
        }


        //tlv0105 = "01 05 00 88 00 01 01 02 00 40 02 01 03 3C 01 03 00 00" + 取文本中间(data, 取文本长度(data) － 367, 167) ＋ “00 40 02 02 03 3C 01 03 00 00 ” ＋ 取文本中间 (data, 取文本长度 (data) － 166, 167)

    }
}

/**
 * Encrypted using [0828_rec_decr_key], decrypting in RobotNetworkHandler
 *
 * @author Him188moe
 */
class ServerSessionKeyResponsePacketEncrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    fun decrypt(_0828_rec_decr_key: ByteArray): ServerSessionKeyResponsePacket {
        this.input goto 14
        val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
        return ServerSessionKeyResponsePacket(TEACryptor.decrypt(data, _0828_rec_decr_key).dataInputStream(), data.size);
    }
}