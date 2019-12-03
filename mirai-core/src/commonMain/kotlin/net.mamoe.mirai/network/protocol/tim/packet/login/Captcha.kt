@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused", "FunctionName")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

object CaptchaKey : DecrypterByteArray, DecrypterType<CaptchaKey> {
    override val value: ByteArray = TIMProtocol.key00BA
}

@AnnotatedId(KnownPacketId.CAPTCHA)
object CaptchaPacket : PacketFactory<CaptchaPacket.CaptchaResponse, CaptchaKey>(CaptchaKey) {
    /**
     * 请求验证码传输
     */
    fun RequestTransmission(
        bot: UInt,
        token0825: ByteArray,
        captchaSequence: Int,
        token00BA: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        writeFully(TIMProtocol.key00BA)
        encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeFully(TIMProtocol.constantData2)
            writeHex("00 00 38")
            writeFully(token0825)
            writeHex("01 03 00 19")
            writeFully(TIMProtocol.publicKey)
            writeHex("13 00 05 00 00 00 00")
            writeUByte(captchaSequence.toUByte())
            writeHex("00 28")
            writeFully(token00BA)
            writeHex("00 10")
            writeFully(TIMProtocol.key00BAFix)
        }
    }

    /**
     * 刷新验证码
     */
    fun Refresh(
        bot: UInt,
        token0825: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        writeFully(TIMProtocol.key00BA)
        encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeFully(TIMProtocol.constantData2)
            writeHex("00 00 38")
            writeFully(token0825)
            writeHex("01 03 00 19")
            writeFully(TIMProtocol.publicKey)
            writeHex("13 00 05 00 00 00 00 00 00 00 00 10")
            writeFully(TIMProtocol.key00BAFix)
        }
    }

    /**
     * 提交验证码
     */
    fun Submit(
        bot: UInt,
        token0825: ByteArray,
        captcha: String,
        captchaToken: IoBuffer
    ): OutgoingPacket = buildOutgoingPacket {
        require(captcha.length == 4) { "captcha.length must == 4" }
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        writeFully(TIMProtocol.key00BA)
        encryptAndWrite(TIMProtocol.key00BA) {
            writeHex("00 02 00 00 08 04 01 E0")
            writeFully(TIMProtocol.constantData2)
            writeHex("01 00 38")
            writeFully(token0825)
            writeHex("01 03")

            writeShort(25)
            writeFully(TIMProtocol.publicKey)//25

            writeHex("14 00 05 00 00 00 00 00 04")
            writeStringUtf8(captcha.toUpperCase())
            writeHex("00 38")
            writeFully(captchaToken)

            writeShort(16)
            writeFully(TIMProtocol.key00BAFix)//16
        }
    }

    sealed class CaptchaResponse : Packet {
        lateinit var token00BA: ByteArray//56 bytes

        class Correct : CaptchaResponse() {
            override fun toString(): String = "CaptchaResponse.Correct"
        }

        class Transmission : CaptchaResponse() {
            lateinit var captchaSectionN: IoBuffer
            lateinit var captchaToken: IoBuffer//56bytes
            var transmissionCompleted: Boolean = false//验证码是否已经传输完成
            override fun toString(): String = "CaptchaResponse.Transmission"
        }
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): CaptchaResponse =
        when (val flag = readByte().toUInt()) {
            0x14u -> {//00 05 00 00 00 00 00 00 38
                CaptchaResponse.Correct().apply {
                    discardExact(9)
                    token00BA = readBytes(56)
                }
            }
            0x13u -> {
                CaptchaResponse.Transmission().apply {
                    with(debugPrint("验证码包")) {
                        discardExact(9)
                        captchaToken = readIoBuffer(56)

                        val length = readShort()
                        captchaSectionN = readIoBuffer(length)

                        discardExact(1)
                        val byte = readByte().toInt()
                        transmissionCompleted = byte == 0

                        discardExact(remaining - 56 - 2)
                        token00BA = readBytes(40)
                    }
                }
            }

            else -> error("Unable to analyze RequestCaptchaTransmissionPacket, unknown id: $flag")
        }
}
/*
fun main() {
    val data = "13 00 05 01 00 00 01 23 00 38 59 32 29 5A 3E 3D 2D FC F5 22 EB 9E 2D FB 9C 4F AA 06 C8 32 3D F0 3C 2C 2B BA 8D 05 C4 9B C1 74 3B 70 F1 99 90 BB 6E 3E 6F 74 48 97 D3 61 B7 04 C0 A3 F1 DF 40 A4 DC 2B 00 A2 01 2D BB BB E8 FE B8 AF B3 6F 39 7C EA E2 5B 91 BE DB 59 38 CF 58 BC F2 88 F1 09 CF 92 E9 F7 FB 13 76 C5 68 29 23 3F 8E 43 16 2E 50 D7 FA 4D C1 F7 67 EF 27 FB C6 F1 A7 25 A4 BC 45 39 3A EA B2 A5 38 02 FF 4B C9 FF EB BD 89 E5 5D B9 4A 2A BE 5F 52 F1 EB 09 29 CB 3E 66 CF EF 97 89 47 BB 6B E0 7B 4A 3E A1 BC 3F FB F2 0A 83 CB E3 EA B9 43 E1 26 88 03 0B A7 E0 B2 AD 7F 83 CC DA 74 85 83 72 08 EC D2 F9 95 05 15 05 96 F7 1C FF 00 82 C3 90 22 A4 BA 90 D5 00 00 00 00 49 45 4E 44 AE 42 60 82 03 00 00 28 EA 32 5A 85 C8 D2 73 B3 40 39 77 85 65 98 00 FE 03 A2 A5 95 B4 2F E6 79 7A DE 5A 03 10 C8 3D BF 6D 3D 8B 51 84 C2 6D 49 00 10 92 AA 69 FB C6 3D 60 5A 7A A4 AC 7A B0 71 00 36".hexToBytes()
    ServerCaptchaTransmissionResponsePacket(data.toReadPacket(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.dataDecode()
        println(it.toString())
    }
}*/