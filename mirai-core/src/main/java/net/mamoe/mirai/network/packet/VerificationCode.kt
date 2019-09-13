package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.Tested
import net.mamoe.mirai.utils.hexToBytes
import java.io.DataInputStream

/**
 * 客户端请求验证码图片数据的第几部分
 */

@PacketId("00 BA 31")
class ClientVerificationCodeTransmissionRequestPacket(
        private val packetId: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    @Tested
    override fun encode() {
        this.writeByte(packetId)//part of packet id

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

class ClientVerificationCodeSubmitPacket(
        private val packetIdLast: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationCode: String,
        private val verificationToken: ByteArray
) : ClientPacket() {
    init {
        require(verificationCode.length == 4) { "verificationCode.length must == 4" }
    }

    override fun encode() {
        this.writeByte(packetIdLast)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData2)
            it.writeHex("01 00 38")
            it.write(token0825)
            it.writeHex("01 03")

            it.writeShort(25)
            it.writeHex(Protocol.publicKey)

            it.writeHex("14 00 05 00 00 00 00 00 04")
            it.write(verificationCode.toUpperCase().toByteArray())
            it.writeHex("00 38")
            it.write(verificationToken)

            it.writeHex("00 10")
            it.writeHex(Protocol.key00BAFix)
        }
    }

    override fun getFixedId(): String {
        return this.idHex + " " + packetIdLast
    }
}

/**
 * 刷新验证码
 */
@PacketId("00 BA 31")

class ClientVerificationCodeRefreshPacket(
        private val packetIdLast: Int,
        private val qq: Long,
        private val token0825: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeByte(packetIdLast)//part of packet id

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
    }

    override fun getFixedId(): String {
        return this.idHex + " " + packetIdLast
    }
}

/**
 * 验证码输入错误, 同时也会给一部分验证码
 */
@PacketId("00 BA 32")
class ServerVerificationCodeWrongPacket(input: DataInputStream, dataSize: Int, packetId: ByteArray) : ServerVerificationCodeTransmissionPacket(input, dataSize, packetId)

/**
 * 服务器发送验证码图片文件一部分过来
 *
 * @author Him188moe
 */
@PacketId("00 BA 31")
open class ServerVerificationCodeTransmissionPacket(input: DataInputStream, private val dataSize: Int, private val packetId: ByteArray) : ServerVerificationCodePacket(input) {

    lateinit var captchaSectionN: ByteArray
    lateinit var verificationToken: ByteArray//56bytes
    var transmissionCompleted: Boolean = false//验证码是否已经传输完成
    lateinit var token00BA: ByteArray//40 bytes
    var packetIdLast: Int = 0


    override fun decode() {
        this.verificationToken = this.input.readNBytesAt(10, 56)

        val length = this.input.readShortAt(66)
        this.captchaSectionN = this.input.readNBytes(length)

        this.input.skip(1)
        val byte = this.input.readByteAt(69 + length).toInt()
        this.transmissionCompleted = byte == 0

        this.token00BA = this.input.readNBytesAt(dataSize - 56 - 2, 40)
        this.packetIdLast = packetId[3].toInt()
    }

    override fun getFixedId(): String {
        return this.idHex + " " + packetIdLast
    }
}

/*
fun main() {
    val data = "13 00 05 01 00 00 01 23 00 38 59 32 29 5A 3E 3D 2D FC F5 22 EB 9E 2D FB 9C 4F AA 06 C8 32 3D F0 3C 2C 2B BA 8D 05 C4 9B C1 74 3B 70 F1 99 90 BB 6E 3E 6F 74 48 97 D3 61 B7 04 C0 A3 F1 DF 40 A4 DC 2B 00 A2 01 2D BB BB E8 FE B8 AF B3 6F 39 7C EA E2 5B 91 BE DB 59 38 CF 58 BC F2 88 F1 09 CF 92 E9 F7 FB 13 76 C5 68 29 23 3F 8E 43 16 2E 50 D7 FA 4D C1 F7 67 EF 27 FB C6 F1 A7 25 A4 BC 45 39 3A EA B2 A5 38 02 FF 4B C9 FF EB BD 89 E5 5D B9 4A 2A BE 5F 52 F1 EB 09 29 CB 3E 66 CF EF 97 89 47 BB 6B E0 7B 4A 3E A1 BC 3F FB F2 0A 83 CB E3 EA B9 43 E1 26 88 03 0B A7 E0 B2 AD 7F 83 CC DA 74 85 83 72 08 EC D2 F9 95 05 15 05 96 F7 1C FF 00 82 C3 90 22 A4 BA 90 D5 00 00 00 00 49 45 4E 44 AE 42 60 82 03 00 00 28 EA 32 5A 85 C8 D2 73 B3 40 39 77 85 65 98 00 FE 03 A2 A5 95 B4 2F E6 79 7A DE 5A 03 10 C8 3D BF 6D 3D 8B 51 84 C2 6D 49 00 10 92 AA 69 FB C6 3D 60 5A 7A A4 AC 7A B0 71 00 36".hexToBytes()
    ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.decode()
        println(it.toString())
    }
}*/

/**
 * 验证码正确
 *
 * @author Him188moe
 */
@PacketId("00 BA 32")
class ServerVerificationCodeCorrectPacket(input: DataInputStream) : ServerVerificationCodePacket(input) {

    lateinit var token00BA: ByteArray//56 bytes


    override fun decode() {
        token00BA = this.input.readNBytesAt(10, 56)
    }
}

abstract class ServerVerificationCodePacket(input: DataInputStream) : ServerPacket(input) {

    @PacketId("00 BA")
    class Encrypted(input: DataInputStream, private val id: String) : ServerPacket(input) {

        fun decrypt(): ServerVerificationCodePacket {
            this.input goto 14
            val data = TEA.decrypt(this.input.readAllBytes().cutTail(1), Protocol.key00BA.hexToBytes())
            if (id.startsWith("00 BA 32")) {
                return when (data.size) {
                    66,
                    95 -> ServerVerificationCodeCorrectPacket(data.dataInputStream())
                    //66 -> ServerVerificationCodeUnknownPacket(data.dataInputStream())
                    else -> return ServerVerificationCodeWrongPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4))
                }.setId(this.idHex)
            }

            return ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4)).setId(this.idHex)
        }

        override fun getFixedId(): String = this.getFixedId(id)
    }
}
