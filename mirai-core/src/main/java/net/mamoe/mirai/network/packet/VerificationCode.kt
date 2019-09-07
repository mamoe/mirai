package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.TestedSuccessfully
import net.mamoe.mirai.utils.hexToBytes
import java.io.DataInputStream

/**
 * 客户端请求验证码图片数据的第几部分
 */
@ExperimentalUnsignedTypes
@PacketId("00 BA 31")
class ClientVerificationCodeTransmissionRequestPacket(
        private val verificationSessionId: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    @TestedSuccessfully
    override fun encode() {
        MiraiLogger debug "verificationSessionId=$verificationSessionId"
        MiraiLogger debug "verificationSequence=$verificationSequence"

        this.writeByte(verificationSessionId)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData2)
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
 * 提交验证码
 */
@PacketId("00 BA 32")
@ExperimentalUnsignedTypes
class ClientVerificationCodeSubmitPacket(
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSessionId: Int,
        private val verificationCode: String,
        private val verificationToken: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeByte(verificationSessionId)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData2)
            it.writeHex("01 00 38")
            it.write(token0825)
            it.writeHex("01 03 00 19")
            it.writeHex(Protocol.publicKey)
            it.writeHex("14 00 05 00 00 00 00 00 04")
            it.write(verificationCode.substring(0..3).toByteArray())
            it.writeByte(0x38)
            it.write(verificationToken)

            it.writeHex("00 10")
            it.writeHex(Protocol.key00BAFix)
        }
        this.writeHex("")
    }
}

/**
 * 刷新验证码
 */
@PacketId("00 BA 31")
@ExperimentalUnsignedTypes
class ClientVerificationCodeRefreshPacket(
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSessionId: Int
) : ClientPacket() {
    override fun encode() {
        this.writeByte(verificationSessionId)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData2)
            it.writeHex("00 00 38")
            it.write(token0825)
            it.writeHex("01 03 00 19")
            it.writeHex(Protocol.publicKey)
            it.writeHex("13 00 05 00 00 00 00 00 00 00 00 10")
            it.writeHex(Protocol.key00BAFix)
        }
        this.writeHex("")
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
    var verificationSessionId: Int = 0

    @ExperimentalUnsignedTypes
    override fun decode() {
        this.verificationToken = this.input.readNBytesAt(10, 56)

        val length = this.input.readShortAt(66)
        this.verificationCodePartN = this.input.readNBytes(length)

        this.input.skip(1)
        val byte = this.input.readByteAt(69 + length).toInt()
        this.transmissionCompleted = byte == 0

        this.token00BA = this.input.readNBytesAt(dataSize - 56 - 2, 40)
        this.verificationSessionId = packetId[3].toInt()
    }
}


fun main() {
    val data = "13 00 05 01 00 00 01 23 00 38 41 8A 4F 49 BF 68 90 72 9E F6 DB 2A 85 12 81 BF 95 0A 64 60 3C 1A 12 20 41 74 CA BC B8 C3 0D 48 28 7B F6 C1 D2 7D B3 3D E2 12 FA 64 58 9C 79 9A 8B 6A 58 14 6C 86 25 6B 02 BC F5 EE 11 59 90 F9 12 46 D1 B0 14 DF CE DB 34 CD 0E 4C 62 5B D7 7D D7 A2 F3 51 AB EB 8B E0 CE 5C 32 A6 8A D3 D6 1D 44 22 1D E8 38 E8 F5 03 E0 91 E9 56 FB 55 91 20 36 89 F7 10 B3 ED A9 D9 11 F7 2A AB 4D 0E 6D DE 8F A1 AE A5 6F D2 91 CE D3 58 D8 04 3E BF 2C D3 3E 13 12 08 18 A8 92 F2 0D 32 40 79 B6 07 40 83 F6 6A 28 BD 94 0C 31 EA E1 C4 A3 11 46 8D 48 C8 7A E2 4E 4F 20 24 EC 48 9A 2D 14 FE 66 90 ED 8E 8A 7F D7 6D 08 64 46 90 82 37 65 A2 0C 4D 64 DC 2F AC 1F D4 9F D7 54 61 34 34 35 2A B0 CE B1 64 62 24 6B A5 33 C4 B4 9C 26 10 B6 0E 0C DA EC 20 70 43 DC 3C FB A5 A9 E3 61 49 0D A2 0F C6 DC B1 20 A7 DE 42 83 9F 48 C2 C3 98 7D 6D 3C F3 53 86 58 D6 5A 4D 0A 52 A3 39 6A FE 41 A9 9C FB C8 7A 63 1A 40 06 58 EE D7 BA 62 29 CB 9A BC 72 6B D2 FD B9 3C 94 B4 42 BE F4 F8 BD F7 06 50 9E 64 E0 27 FD 62 53 5A AA 7A C9 5C DA B2 5E BC F2 40 05 98 D8 30 74 47 9A E3 BA 85 CE 5B CC DF 3F C7 1F 0D 9E B2 03 59 6B C1 4B 75 4E C5 F3 E9 E8 86 28 D6 FC A9 1C A3 25 DF 78 91 39 8B BB 66 B5 AA FF 53 D3 0B 7D 93 A8 EF 96 70 E9 3B D3 CB 66 99 B0 33 33 62 DB D3 44 02 77 3A 26 ED 2B ED 19 98 8A 25 39 7B 9B 0B D4 26 FA AC 77 83 76 DF 22 F3 EF 37 3C 16 A6 19 9F 73 65 C4 56 5F E6 D2 E3 A9 BC F5 1A 57 F5 D0 A0 09 CA 8A 70 F1 97 41 AD 36 C1 74 B2 9A F0 FE 6B C6 B5 86 1D C2 F6 7B BE 4B 2B A3 C0 B3 14 9C B3 9E D8 D9 C8 05 A3 55 02 92 34 2F C5 52 5C 44 E3 6B 31 B8 73 3F 34 21 5D 5D EF 7D 10 EF 19 F6 45 2E C2 EE 1F 47 81 7C 9B BA 99 5D 40 0F A3 05 12 00 B8 9E FC CA 7A 50 8F 90 1D F1 D4 27 6F F6 4B 41 D6 39 A1 3A 91 0C 7D 8B 17 0D D7 F6 42 EA D1 6D 55 F8 34 C2 A5 D2 F1 1E A5 C1 1A 95 A3 7C 95 E0 D4 E5 00 5F D3 D0 33 43 F6 FA 0E 26 D4 04 2B 9B 87 71 9E 77 4D 3F E7 DC 63 2A 72 47 38 94 58 57 DA 93 B4 A8 1D A0 9D 9F 35 68 54 A3 DE 50 74 87 38 CD 08 CE 35 BE E3 50 13 C1 8C F3 09 D7 BD 70 A1 A7 33 C6 A5 1E FC 17 9F 93 FA 77 2F EF EE 6D 89 B0 96 0F 49 30 53 81 BB 2A E7 5C CD BD 98 65 3D E1 75 E7 06 77 E3 F9 45 EF E0 52 48 75 2D 52 55 01 70 5D 11 C2 A7 1B CC 81 46 E9 B4 E1 E5 CB 3E 47 89 5F 3F 47 AA 3B 1D 7D 2A 30 FE 6F EC 03 59 BA F1 0E 09 7D 4F B5 9D AB FF DB F5 B4 26 1B 40 F5 BE FF 03 E7 64 A0 DE A1 0C 6B 62 39 1F 01 01 00 28 57 1B 29 6C A0 FD FB 82 34 7D 92 07 77 CF CE 7A C9 19 FA 46 9C 9B 0A 43 30 6F 1D EE D3 E0 46 A4 83 20 CD 0A 6C AF 1A 1E 00 10 E0 5A E1 E7 FE 45 53 0B 2B 70 68 40 CB C1 D6 7A".hexToBytes()
    ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.decode()
        println(it.toString())
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
