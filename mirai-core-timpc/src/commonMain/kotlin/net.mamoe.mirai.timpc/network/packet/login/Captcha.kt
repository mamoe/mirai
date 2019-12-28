@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused", "FunctionName")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.OutgoingPacket
import net.mamoe.mirai.timpc.network.packet.PacketFactory
import net.mamoe.mirai.timpc.network.packet.PacketId
import net.mamoe.mirai.timpc.network.packet.buildOutgoingPacket
import net.mamoe.mirai.utils.cryptor.DecrypterByteArray
import net.mamoe.mirai.utils.cryptor.DecrypterType
import net.mamoe.mirai.utils.io.*

internal object CaptchaKey : DecrypterByteArray,
    DecrypterType<CaptchaKey> {
    override val value: ByteArray = TIMProtocol.key00BA
}

internal object CaptchaPacket : PacketFactory<CaptchaPacket.CaptchaResponse, CaptchaKey>(CaptchaKey) {
    /**
     * 请求验证码传输
     */
    fun RequestTransmission(
        bot: Long,
        token0825: ByteArray,
        captchaSequence: Int,
        token00BA: ByteArray
    ): OutgoingPacket = buildOutgoingPacket(name = "CaptchaPacket.RequestTransmission") {
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
        bot: Long,
        token0825: ByteArray
    ): OutgoingPacket = buildOutgoingPacket(name = "CaptchaPacket.Refresh") {
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
        bot: Long,
        token0825: ByteArray,
        captcha: String,
        captchaToken: IoBuffer
    ): OutgoingPacket = buildOutgoingPacket(name = "CaptchaPacket.Submit") {
        require(captcha.length == 4) { "captcha.length must == 4" }
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        writeFully(TIMProtocol.key00BA)
        encryptAndWrite(TIMProtocol.key00BA) {

            //00 02 00 00 08 04 01 E0
            // 00 00 04 56 00 00 00 01 00 00 15 E3 01
            // 00 38 58 CE A0 12 81 31 5C 5E 36 23 5B E4 0E 05 A6 47 BF 7C 1A 7A 35 37 59 90 17 50 66 0C 07 03 77 E4 48 DB 28 0A CF C3 A9 B7 C0 95 D3 9D 00 AA A5 EB FB D6 85 8D 10 61 5A D0
            // 01 03
            // 00 19 02 CA 53 7E F0 7B 32 82 EC 9F DE CF 51 8B A4 93 26 76 EC 42 1C 02 00 74 58
            // 14 00 05 00 00 00 00 00
            // 04
            // 6C 73 64 61
            //
            // 00 40 CE 99 84 E8 F1 59 31 B0 3F 6C 4D 44 09 E4 82 77 96 67 03 A7 3A EA 8F 36 B9 20 79 7E C9 0F 75 3C 2A C3 E1 E5 C6 00 B3 5E 91 5B 47 63 EF AF 30 C0 48 2F 58 23 96 CF 65 2F 4C 75 95 A6 CA 5A 2C 5C
            //
            // 00 10 E1 50 C9 F4 F6 F4 2F D1 7F E9 8C AB B6 1C 38 7B

            writeHex("00 02 00 00 08 04 01 E0")
            writeFully(TIMProtocol.constantData2) //00 00 04 53 00 00 00 01 00 00 15 85
            writeHex("01 00 38")
            writeFully(token0825)
            writeHex("01 03")

            writeShort(25)
            writeFully(TIMProtocol.publicKey)//25

            writeHex("14 00 05 00 00 00 00 00 04")
            writeStringUtf8(captcha.toUpperCase())
            writeHex("00 40")
            writeFully(captchaToken)

            writeShort(16)
            writeFully(TIMProtocol.key00BAFix)//16
        }
    }

    internal sealed class CaptchaResponse : Packet {
        lateinit var token00BA: ByteArray//56 bytes

        class Correct : CaptchaResponse() {
            override fun toString(): String = "CaptchaResponse.Correct"
        }

        class Transmission : CaptchaResponse() {
            lateinit var captchaSectionN: IoBuffer
            lateinit var captchaToken: IoBuffer//0x40=64bytes
            var transmissionCompleted: Boolean = false//验证码是否已经传输完成
            override fun toString(): String = "CaptchaResponse.Transmission"
        }
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): CaptchaResponse =
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
                        /*
                         * 00 05 01 00 00 01 23
                         * 00 40 A0 E9 2F 12 1D 2E B1 15 26 89 EB C3 F0 9A 0C 03 00 03 A0 F2 74 57 58 57 E9 7A 2B C7 52 5D BC D8 7B D5 A4 7C AD 33 85 85 39 88 D2 CE AD 68 36 2E F0 AE 19 E8 25 3A F7 3A AD BE 19 A9 E7 C4 B5 4C
                         * 02 BC 28 01 38 40 C6 4E 85 A6 32 24 0C 3C B3 19 46 5D AD 56 AC 3D 3A E1 ED AD 8C 60 05 47 37 10 DC AD E5 72 F9 F9 18 B8 0C 13 10 D2 4E C9 3C 02 BE 57 E4 02 E0 6C 6C 6E E9 3C 57 28 66 BD 0C D3 FF CC 5A 47 B4 F1 7C 87 85 24 B0 60 44 20 1C 1E AD 95 7B CB 45 AF 43 95 10 0F 1D 0B 33 CB 09 7E BE F8 35 B0 D4 5C AB 9E 5A BE 34 E8 B9 2E 65 C7 DA F5 E1 EB 71 43 31 A1 2E 40 4D 84 22 EF 8F CD 05 13 33 E5 CF E3 AA 09 C7 71 15 30 A4 83 A7 36 84 90 4D 4C A7 67 66 4B A5 D7 C5 FB 5E D7 26 ED 9C 92 AD 7C 8F 09 36 A3 60 84 16 07 45 B4 6E EA CD 05 EC C7 0B BA A2 BE 71 24 E6 49 C1 FC 05 3E 26 C9 E6 F7 EA B3 25 8D BA 1F 15 3D DC BC FD CE A6 79 FF 8B 28 B6 12 78 F3 8F EB A6 A9 B6 A5 5F 65 58 CC CD FC F6 BC A6 46 21 68 70 64 82 C7 8F 79 1A C0 B3 48 B6 CD C8 7C 7E 90 61 43 F7 A6 D7 B1 39 F1 72 C7 78 7E 37 49 50 6A B6 9F 5B 8D A9 C0 B0 BB F0 EF 9D CD 6E F7 E7 5A 3C BA E1 02 2E A0 2D 00 04 07 25 B3 B2 34 FA CD 6E C3 A4 ED 87 88 59 D8 63 0C 1D 27 D1 04 4D B5 5B 6E 43 07 17 79 FA EB C2 A3 11 77 72 9D C5 55 90 80 EF 01 47 1A 10 02 E7 02 F6 8A 76 E9 E5 C1 A0 F6 E4 B4 65 36 7F 41 36 37 E8 CE 99 7F 49 66 2A 61 7D A8 D2 57 D9 18 E9 FA 85 CB 3A 1E 7A DE 8C 07 F5 2A CA 33 25 D4 E0 86 08 75 50 B6 1C EE 99 BA 56 F8 4F E9 EF CD E6 27 EE 81 D9 CC 5E 7F 4A 33 54 CB F9 A5 92 DE 76 0B F4 57 29 65 77 BC BD 3D CC E5 1C C4 2E 2E 02 0E 41 A0 09 29 ED DB F2 53 6B 19 6A ED EC FA D5 0B 76 E6 87 CC 99 9E 80 75 28 A6 92 6D 63 DB BF D7 09 B1 DA DD EC CB D6 7F 5E 60 14 83 C7 B8 19 85 97 37 BA 64 0C AA B7 E9 D5 E0 C2 0F 7A 86 DA 56 96 D1 07 FD DA F0 F1 83 9E 8B 49 F3 DF 3C 2F FD 35 33 55 D2 D4 FA D0 3B 52 BE CD 22 60 22 9E 4C 03 EA 1A 3A 23 46 29 C0 A2 12 51 BC 81 EF E6 FF E8 E9 19 8D 66 F4 F4 A5 FE CD 33 8F 77 67 DC 38 F9 E4 1F D4 63 0D CF 24 AA F5 E1 89 7D F3 79 3D B6 47 02 E9 F8 C9 D0 5A DF 84 00 08 B6 E2 95 3F 3D B3 4E 83 CE EC 91 52 ED 61 63 74 7B 6E CC CC EE A3 5D 3F 7B 91 2E EA F7 3C 0C 3A 4C BC 08 86 A0 6A 63 D0 2D 30 EF 28 BC B3 85 57 85 C1 39 D8 AC FC ED 64 C7 C4 A9 EA F2 5A C5 7F 96 9B 1B CF 97 1E 16 8B EB E4 D7 23 7B 7B D9 E4 09 C9 32 BD 35 B6 AF FE 92 C5 78 BF E1 1A D8 A1 0A 09 5E DE 22 8A F7 7A 9F 4E A2 FD 7E
                         * 01 //第几个包
                         * 01 //是否还有更多
                         * 00 28 39 24 31 73 77 6E 55 E7 99 4D 9E 56 AF 6D 38 77 10 60 3B 68 45 41 35 70 1D B4 FE 7E CE 78 65 5A D7 C8 95 AF F2 6B 6D C8
                         * 00 10 CC A9 FA 63 A8 34 C7 3C E6 F7 2E 15 B7 EF 3E 07
                         */
                        discardExact(7)
                        captchaToken = readIoBuffer(readUShort().toInt()) // 0x40=64,

                        /*
                         *00 05 01 00 00 01 23
                         * 00 40 0B 84 40 B1 59 9C FE B8 EC E4 E8 36 2B 4B 03 C7 9F 5D FA A3 7B 43 BD 50 19 55 EA 4C A8 DE 49 FF 5F 45 89 7F 2E B2 6D C9 D6 B7 08 3B 60 31 74 4C FA DA 5F 5F A6 80 ED A1 19 48 F9 C9 4A 6A AD F6
                         * 00 48 39 46 4F 92 0F 70 C5 55 81 3E 0B E7 96 18 4F 31 93 FE 1B D6 A9 50 97 97 E0 83 76 03 6C 50 80 2B 65 13 63 44 A3 9E 0B D9 C0 10 60 70 5B A1 53 2C FD 3B 84 DF A6 E6 F6 1F 71 B5 A6 54 00 00 00 00 49 45 4E 44 AE 42 60 82
                         * 04 //第几个包
                         * 00 //是否还有更多
                         * 00 28 3C 40 BD A5 8B F9 63 97 7E 62 34 E3 F9 49 49 9E 21 01 3C 64 21 AE 8D 87 21 9F 44 4A 0C 6F 85 32 B4 13 4C 59 66 E7 EE 17
                         * 00 10 AF 66 92 E2 B4 39 6B 9A BA 29 EF AA 8D 98 79 55
                         */
                        captchaSectionN = readIoBuffer(readUShort().toInt()) // <=700

                        discardExact(1) // 第几个包
                        transmissionCompleted = readByte().toInt() == 0

                        token00BA = readBytes(readUShort().toInt())

                        println(token00BA.toUHexString())
                        // 剩余
                        // 00 10 AF 66 92 E2 B4 39 6B 9A BA 29 EF AA 8D 98 79 55
                    }
                }
            }

            /*

发出包ID = CaptchaPacket(00 BA)
sequence = 71 6B
  fixVer2=03 00 01 00 01 2E 01 00 00 69 35 00 00 00 00
  密文=65 F7 F3 14 E3 94 10 1F DD 95 84 A3 F5 9F AD 94 8D 4F 6A 70 F8 4A DE 43 AF 75 D1 3F 3A 3F F2 E0 A8 16 1A 46 13 CD B0 51 45 00 29 52 57 75 6D 4A 4C D9 B7 98 8C B0 96 EC 57 4E 67 FB 8D C5 F1 BF 72 38 40 42 19 54 C2 28 F4 72 C8 AE 24 EB 66 B5 D0 45 0B 72 44 81 E2 F6 2B EE C3 85 93 BA CB B7 72 F4 1A 30 F9 5B 3D B0 79 3E F4 0B F2 1A A7 49 60 3B 37 02 60 0C 5D D5 76 76 47 4F B5 B3 F5 CA 58 6C FC D2 41 3E 24 D1 FB 0A 18 53 D8 E5 A5 85 A8 BC 51 54 3B 66 5B 21 C6 7B AF C9 62 F0 AA 9C CF 2E 84 0F CC 15 5B 35 93 49 5C E4 28 49 A7 8A D3 30 A9 6E 36 4E 7A 49 28 69 4D C3 25 39 6E 45 6E 40 F2 86 1E F4 4F 00 A6 9D E6 9B 84 19 69 C1 31 6A 17 BA F0 0D 8A 22 09 86 24 92 F7 22 C3 47 7F F2 BF 94 8A 8A B5 29
  解密body=
  00 02 00 00 08 04 01 E0 00 00 04 56 00 00 00 01 00 00 15 E3 01 00 38 58 CE A0 12 81 31 5C 5E 36 23 5B E4 0E 05 A6 47 BF 7C 1A 7A 35 37 59 90 17 50 66 0C 07 03 77 E4 48 DB 28 0A CF C3 A9 B7 C0 95 D3 9D 00 AA A5 EB FB D6 85 8D 10 61 5A D0 01 03 00 19 02 CA 53 7E F0 7B 32 82 EC 9F DE CF 51 8B A4 93 26 76 EC 42 1C 02 00 74 58 14 00 05 00 00 00 00 00 04 6C 73 64 61 00 40 CE 99 84 E8 F1 59 31 B0 3F 6C 4D 44 09 E4 82 77 96 67 03 A7 3A EA 8F 36 B9 20 79 7E C9 0F 75 3C 2A C3 E1 E5 C6 00 B3 5E 91 5B 47 63 EF AF 30 C0 48 2F 58 23 96 CF 65 2F 4C 75 95 A6 CA 5A 2C 5C 00 10 E1 50 C9 F4 F6 F4 2F D1 7F E9 8C AB B6 1C 38 7B
--------------
接收包id=CaptchaPacket(00 BA),
sequence=71 6B
  密文body=92 74 E1 41 8D DE AA 26 5B 3E 8F 91 E0 DC 41 DD 39 EF 1E 2F FD 0E 19 41 B2 AD 8F A1 8D 5D C7 D4 D5 36 2B 51 E5 05 91 9F EE 89 82 B0 C6 B7 EC BE 40 8A 7E 06 FC 82 FA 39 AC 54 94 27 26 8D 71 46 1A F2 BB 23 A8 7E 15 69 8A C7 00 D1 52 06 2C 8B
  解密body=解密失败

             */

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