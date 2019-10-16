@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.*

/**
 * 客户端请求验证码图片数据的第几部分
 */
@PacketId(0x00_BAu)
class ClientCaptchaTransmissionRequestPacket(
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    @Tested
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.key00BA)
        this.encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeHex(TIMProtocol.constantData2)
            writeHex("00 00 38")
            writeFully(token0825)
            writeHex("01 03 00 19")
            writeHex(TIMProtocol.publicKey)
            writeHex("13 00 05 00 00 00 00")
            writeUByte(verificationSequence.toUByte())
            writeHex("00 28")
            writeFully(token00BA)
            writeHex("00 10")
            writeHex(TIMProtocol.key00BAFix)
        }
    }
}

/**
 * 提交验证码
 */
@PacketId(0x00_BAu)
class ClientCaptchaSubmitPacket(
        private val qq: Long,
        private val token0825: ByteArray,
        private val captcha: String,
        private val verificationToken: IoBuffer
) : ClientPacket() {
    init {
        require(captcha.length == 4) { "captcha.length must == 4" }
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.key00BA)
        this.encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeHex(TIMProtocol.constantData2)
            writeHex("01 00 38")
            writeFully(token0825)
            writeHex("01 03")

            writeShort(25)
            writeHex(TIMProtocol.publicKey)//25

            writeHex("14 00 05 00 00 00 00 00 04")
            writeStringUtf8(captcha.toUpperCase())
            writeHex("00 38")
            writeFully(verificationToken)

            writeShort(16)
            writeHex(TIMProtocol.key00BAFix)//16
        }
    }
}

/**
 * 刷新验证码
 */
@PacketId(0x00_BAu)
class ClientCaptchaRefreshPacket(
        private val qq: Long,
        private val token0825: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.key00BA)
        this.encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeHex(TIMProtocol.constantData2)
            writeHex("00 00 38")
            writeFully(token0825)
            writeHex("01 03 00 19")
            writeHex(TIMProtocol.publicKey)
            writeHex("13 00 05 00 00 00 00 00 00 00 00 10")
            writeHex(TIMProtocol.key00BAFix)
        }
    }
}

/**
 * 服务器发送验证码图片文件一部分过来. 当验证码输入错误时, 服务器的返回也会是这个包.
 *
 * @author Him188moe
 */
@PacketId(0x00_BAu)
open class ServerCaptchaTransmissionPacket(input: ByteReadPacket) : ServerCaptchaPacket(input) {

    lateinit var captchaSectionN: IoBuffer
    lateinit var verificationToken: IoBuffer//56bytes
    var transmissionCompleted: Boolean = false//验证码是否已经传输完成
    lateinit var token00BA: ByteArray//40 bytes


    override fun decode() = with(input) {
        input.discardExact(10)//13 00 05 01 00 00 01 23 00 38
        verificationToken = readIoBuffer(56)

        val length = readShort()
        captchaSectionN = readIoBuffer(length)

        discardExact(1)
        val byte = readByte().toInt()
        transmissionCompleted = byte == 0

        discardExact(remaining - 56 - 2)
        token00BA = readBytes(40)
    }
}

/*
fun main() {
    val data = "13 00 05 01 00 00 01 23 00 38 59 32 29 5A 3E 3D 2D FC F5 22 EB 9E 2D FB 9C 4F AA 06 C8 32 3D F0 3C 2C 2B BA 8D 05 C4 9B C1 74 3B 70 F1 99 90 BB 6E 3E 6F 74 48 97 D3 61 B7 04 C0 A3 F1 DF 40 A4 DC 2B 00 A2 01 2D BB BB E8 FE B8 AF B3 6F 39 7C EA E2 5B 91 BE DB 59 38 CF 58 BC F2 88 F1 09 CF 92 E9 F7 FB 13 76 C5 68 29 23 3F 8E 43 16 2E 50 D7 FA 4D C1 F7 67 EF 27 FB C6 F1 A7 25 A4 BC 45 39 3A EA B2 A5 38 02 FF 4B C9 FF EB BD 89 E5 5D B9 4A 2A BE 5F 52 F1 EB 09 29 CB 3E 66 CF EF 97 89 47 BB 6B E0 7B 4A 3E A1 BC 3F FB F2 0A 83 CB E3 EA B9 43 E1 26 88 03 0B A7 E0 B2 AD 7F 83 CC DA 74 85 83 72 08 EC D2 F9 95 05 15 05 96 F7 1C FF 00 82 C3 90 22 A4 BA 90 D5 00 00 00 00 49 45 4E 44 AE 42 60 82 03 00 00 28 EA 32 5A 85 C8 D2 73 B3 40 39 77 85 65 98 00 FE 03 A2 A5 95 B4 2F E6 79 7A DE 5A 03 10 C8 3D BF 6D 3D 8B 51 84 C2 6D 49 00 10 92 AA 69 FB C6 3D 60 5A 7A A4 AC 7A B0 71 00 36".hexToBytes()
    ServerCaptchaTransmissionPacket(data.toReadPacket(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.dataDecode()
        println(it.toString())
    }
}*/

/**
 * 验证码正确
 *
 * @author Him188moe
 */
@PacketId(0x00_BAu)
class ServerCaptchaCorrectPacket(input: ByteReadPacket) : ServerCaptchaPacket(input) {
    lateinit var token00BA: ByteArray//56 bytes

    override fun decode() = with(input) {
        discardExact(10)//14 00 05 00 00 00 00 00 00 38
        token00BA = readBytes(56)
    }
}

@PacketId(0x00_BAu)
abstract class ServerCaptchaPacket(input: ByteReadPacket) : ServerPacket(input) {

    @PacketId(0x00_BAu)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(): ServerCaptchaPacket {
            val data = this.decryptAsByteArray(TIMProtocol.key00BA)

            return when (data.size) {
                66,
                95 -> ServerCaptchaCorrectPacket(data.toReadPacket())
                //66 -> ServerCaptchaUnknownPacket(data.toReadPacket())
                else -> ServerCaptchaTransmissionPacket(data.toReadPacket())
            }.applySequence(sequenceId)
        }
    }
}
