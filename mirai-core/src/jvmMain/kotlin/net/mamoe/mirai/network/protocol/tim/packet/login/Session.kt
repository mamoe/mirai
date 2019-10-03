package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.lazyEncode
import java.io.DataInputStream
import java.net.InetAddress

/**
 * @author Him188moe
 */

@PacketId("08 28 04 34")
class ClientSessionRequestPacket(
        private val qq: Long,
        private val serverIp: String,
        private val token38: ByteArray,
        private val token88: ByteArray,
        private val encryptionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex("02 00 00 00 01 2E 01 00 00 68 52 00 30 00 3A")
        this.writeHex("00 38")
        this.write(token38)
        this.encryptAndWrite(encryptionKey) {
            writeHex("00 07 00 88")
            write(token88)
            writeHex("00 0C 00 16 00 02 00 00 00 00 00 00 00 00 00 00")
            writeIP(serverIp)
            writeHex("1F 40 00 00 00 00 00 15 00 30 00 01")//fix1
            writeHex("01 92 A5 D2 59 00 10 54 2D CF 9B 60 BF BB EC 0D D4 81 CE 36 87 DE 35 02 AE 6D ED DC 00 10 ")
            writeHex("06 A9 12 97 B7 F8 76 25 AF AF D3 EA B4 C8 BC E7")//fix0836
            writeHex("00 36 00 12 00 02 00 01 00 00 00 05 00 00 00 00 00 00 00 00 00 00")
            writeHex(TIMProtocol.constantData1)
            writeHex(TIMProtocol.constantData2)
            writeQQ(qq)
            writeHex("00 00 00 00 00 1F 00 22 00 01")
            writeHex("1A 68 73 66 E4 BA 79 92 CC C2 D4 EC 14 7C 8B AF 43 B0 62 FB 65 58 A9 EB 37 55 1D 26 13 A8 E5 3D")//device ID

            //tlv0106
            writeHex("01 05 00 30")
            writeHex("00 01 01 02 00 14 01 01 00 10")
            writeRandom(16)
            writeHex("00 14 01 02 00 10")
            writeRandom(16)

            writeHex("01 0B 00 85 00 02")
            writeHex("B9 ED EF D7 CD E5 47 96 7A B5 28 34 CA 93 6B 5C")//fix2
            writeRandom(1)
            writeHex("10 00 00 00 00 00 00 00 02")

            //fix3
            writeHex("00 63 3E 00 63 02 04 03 06 02 00 04 00 52 D9 00 00 00 00 A9 58 3E 6D 6D 49 AA F6 A6 D9 33 0A E7 7E 36 84 03 01 00 00 68 20 15 8B 00 00 01 02 00 00 03 00 07 DF 00 0A 00 0C 00 01 00 04 00 03 00 04 20 5C 00")
            writeRandom(32)//md5 32
            writeHex("68")

            writeHex("00 00 00 00 00 2D 00 06 00 01")
            writeIP(InetAddress.getLocalHost().hostAddress)//todo  random to avoid being banned?
        }
    }
}

/**
 * @author Him188moe
 */
@PacketId("08 28 04 34")
class ServerSessionKeyResponsePacket(inputStream: DataInputStream, private val dataLength: Int) : ServerPacket(inputStream) {
    lateinit var sessionKey: ByteArray
    lateinit var tlv0105: ByteArray


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

    class Encrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
        fun decrypt(sessionResponseDecryptionKey: ByteArray): ServerSessionKeyResponsePacket {
            this.input goto 14
            val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
            return ServerSessionKeyResponsePacket(TEA.decrypt(data, sessionResponseDecryptionKey).dataInputStream(), data.size).setId(this.idHex)
        }
    }
}