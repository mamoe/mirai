package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.*
import java.io.DataInputStream

/**
 * 客户端请求验证码图片数据的第几部分
 */
@ExperimentalUnsignedTypes
@PacketId("00 BA 31")
class ClientVerificationCodeTransmissionRequestPacket(
        private val count: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    @TestedSuccessfully
    override fun encode() {
        this.writeByte(count)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer2)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData1)
            it.writeHex("00 00 38")
            it.write(token0825)
            it.writeHex("01 03 00 19")
            it.writeHex(Protocol.publicKey)
            it.writeHex("13 00 05 00 00 00 00")
            it.writeByte(verificationSequence)
            it.writeHex("00 28")
            it.write(token00BA)
            it.writeHex("00 10")
            it.writeHex(Protocol.key00BAFix)
        }
    }
}

/**
 * 验证码输入错误
 */
class ServerVerificationCodeWrongPacket(input: DataInputStream, dataSize: Int, packetId: ByteArray) : ServerVerificationCodeTransmissionPacket(input, dataSize, packetId) {

}

/**
 * 服务器发送验证码图片文件一部分过来
 *
 * @author Him188moe
 */
@PacketId("00 BA 31")
open class ServerVerificationCodeTransmissionPacket(input: DataInputStream, private val dataSize: Int, private val packetId: ByteArray) : ServerVerificationCodePacket(input) {

    lateinit var verificationCodePartN: ByteArray
    lateinit var verificationToken: ByteArray//56bytes
    var transmissionCompleted: Boolean = false//验证码是否已经传输完成
    lateinit var token00BA: ByteArray//40 bytes
    var count: Int = 0

    @ExperimentalUnsignedTypes
    override fun decode() {
        this.verificationToken = this.input.readNBytesAt(10, 56)

        val length = this.input.readShortAt(66)
        this.input.skip(2)
        this.verificationCodePartN = this.input.readNBytes(length)

        this.input.skip(1)
        //val byte = this.input.readByte().toInt()
        val byte = this.input.readByteAt(70 + length).toInt()
        MiraiLogger.debug("transmissionCompleted=$byte")
        MiraiLogger.debug("verificationCodePartN=" + this.verificationCodePartN.toUHexString())
        this.transmissionCompleted = byte == 0

        this.token00BA = this.input.readNBytesAt(dataSize - 56, 40)
        this.count = packetId[3].toInt()
    }
}


fun main() {
    val data = "FC 40 C0 57 0F 15 A4 1F 09 32 39 C9 52 05 44 D5 BA C4 78 B8 70 D7 C0 74 91 A4 7E 44 A5 A7 FD D2 E3 A7 10 3E E4 73 D8 13 E2 A2 0B A4 38 9F AB D3 4A D1 01 0E AB 37 11 84 52 08 DC 85 53 7E 75 08 D1 BA 2A 05 76 0F 84 7C A0 70 25 A4 4E E6 C1 9A C9 71 E7 10 48 F0 9D AA 27 87 3C 99 38 5A AE AE C1 58 17 FC A4 C6 9E 25 68 C0 F7 20 04 CA 98 91 1D 88 83 A7 74 D0 05 DD E9 28 57 46 CA 93 A1 F4 C0 83 4E 18 CE 57 0C 4F 1F 96 20 8F 62 4D E5 90 D2 6A AA E5 45 8B A1 B1 97 32 B5 38 97 9D 43 E9 28 65 5D B4 09 73 44 52 DE 2B C3 5B 18 F1 4A 0C 36 CC DE 31 B2 24 19 C2 19 A4 30 A2 8C 87 B2 12 E2 78 9A 52 9C 40 7F 47 0A 40 90 84 69 84 84 86 8B F8 FE 30 8E C3 30 C0 7D 3F 73 38 89 D4 6F 56 91 9B 04 7D 94 25 5E C4 8D EB E2 18 02 CC 8D 98 07 28 0E CE 05 4E 11 25 B9 27 2C E9 3E 49 71 76 E7 BC C2 02 8D D3 85 49 66 BA F0 87 31 C2 93 0D 88 F8 39 04 37 2F 2C 63 F2 55 96 8F 32 D1 CE 51 F0 D4 0A 0C F0 23 3B 63 06 28 80 41 E9 9E E1 CC AE 00 9E 20 6F CB 3C B3 50 D7 02 CC 5A F0 D1 97 C8 DC 3D F8 1B C6 6D A3 1B C3 B6 55 7A B2 44 D5 47 A7 F0 96 46 4C 3B AC 9C 2E E6 58 D1 FF 48 5C A2 30 35 B2 97 89 62 19 42 6A 81 60 C4 DC B6 6D 03 47 75 AD 26 B0 30 67 57 C6 C3 05 3F FB 3A B6 51 C1 4C 24 AC FC AC 94 C7 A7 B8 82 BC E0 64 4C A5 E9 8F 86 85 CA B0 52 F5 13 33 55 D9 18 DA 70 C3 FE 78 D7 68 8D 96 0D A3 76 0F 70 61 46 94 86 61 B4 9F EA 72 0A 72 96 66 F9 B0 DE 32 A2 80 66 8C 6A 5C 4D 13 25 06 94 80 52 A9 00 29 95 05 B0 FB A6 32 60 41 1D 06 9A 1A 36 B8 C0 4C CD BE 82 7D F5 8C 83 6B 2E F0 C4 38 40 33 45 7F B4 AF 57 8E 90 B4 B0 0F 7D A0 F2 A1 DA 6A 5E 2A 14 A7 35 07 0B CB 17 3A 43 A2 71 CE 77 A4 0B A8 6E 50 6A 46 A5 39 40 14 C0 11 BA F0 D7 EF 0A E0 F6 BB 40 3E 89 D1 2A 0E D8 86 22 C8 C0 52 A1 72 40 7A 08 A9 B8 42 39 27 8A 66 5E 2C F3 CB D0 1E 3E CC 42 82 C2 39 A6 E3 EE 02 A8 40 6D E8 98 C0 23 50 5A 1F B3 FE 68 59 84 E4 26 AD A0 64 B2 56 D4 08 56 0A BC AF 15 DD 67 51 CA 20 D5 0F C2 BD 22 E9 BB 0B A3 CB B8 00 98 66 26 C0 6E 73 18 67 F2 78 27 E7 38 F8 F4 51 9E 5B 15 BE E8 13 F3 CC D9 80 B6 E2 D7 F2 DE 91 55 05 0C 58 93 2D 50 56 34 C5 14 4F 7F B8 80 F6 D5 0A 2B 4F 0C 67 20 66 4D 57 17 96 4B CB 25 29 FD 00 42 B6 BA 0F DF".hexToBytes()
    ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.decode()
        println(it)
    }
}

/**
 * 验证码正确
 *
 * @author Him188moe
 */
class ServerVerificationCodeCorrectPacket(input: DataInputStream) : ServerVerificationCodePacket(input) {

    lateinit var token00BA: ByteArray//56 bytes

    @ExperimentalUnsignedTypes
    override fun decode() {
        token00BA = this.input.readNBytesAt(10, 56)
    }
}

abstract class ServerVerificationCodePacket(input: DataInputStream) : ServerPacket(input) {

    @PacketId("00 BA")
    class Encrypted(input: DataInputStream, val idHex: String) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
        fun decrypt(): ServerVerificationCodePacket {
            this.input goto 14
            val data = TEA.decrypt(this.input.readAllBytes().cutTail(1), Protocol.key00BA.hexToBytes())
            if (idHex.startsWith("00 BA 32")) {
                if (data.size == 95) {
                    ServerVerificationCodeCorrectPacket(data.dataInputStream())
                } else {
                    return ServerVerificationCodeWrongPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4))
                }
            }

            return ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4))
        }
    }
}
