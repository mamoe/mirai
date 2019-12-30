@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.data.Gender
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.LoginResult
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.OutgoingPacket
import net.mamoe.mirai.timpc.network.packet.PacketFactory
import net.mamoe.mirai.timpc.network.packet.PacketId
import net.mamoe.mirai.timpc.network.packet.buildOutgoingPacket
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.cryptor.*
import net.mamoe.mirai.utils.io.*

internal object ShareKey : DecrypterByteArray,
    DecrypterType<ShareKey> {
    override val value: ByteArray = TIMProtocol.shareKey
}

internal inline class PrivateKey(override val value: ByteArray) : DecrypterByteArray {
    companion object Type : DecrypterType<PrivateKey>
}

internal inline class SubmitPasswordResponseDecrypter(private val privateKey: PrivateKey) : Decrypter {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket {
        var decrypted = ShareKey.decrypt(input)
        (decrypted.remaining).let {
            if (it.toInt() % 8 == 0 && it >= 16) {
                decrypted = try {
                    privateKey.decrypt(decrypted)
                } catch (e: Exception) {
                    // 某些情况不需要这次解密
                    decrypted
                }
            }
        }

        return decrypted
    }

    companion object Type : DecrypterType<SubmitPasswordResponseDecrypter>
}

/**
 * 提交密码
 */
internal object SubmitPasswordPacket : PacketFactory<SubmitPasswordPacket.LoginResponse, SubmitPasswordResponseDecrypter>(SubmitPasswordResponseDecrypter) {
    operator fun invoke(
        bot: Long,
        passwordMd5: ByteArray,
        loginTime: Int,
        loginIP: String,
        privateKey: PrivateKey,
        token0825: ByteArray,
        token00BA: ByteArray? = null,
        randomDeviceName: Boolean = false,
        tlv0006: IoBuffer? = null
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.passwordSubmissionTLV1)
        writeShort(25); writeFully(TIMProtocol.publicKey)//=25
        writeZero(2)
        writeShort(16); writeFully(TIMProtocol.key0836)//=16

        // shareKey 极大可能为 publicKey, key0836 计算得到
        encryptAndWrite(TIMProtocol.shareKey) {
            writePart1(bot, passwordMd5, loginTime, loginIP, privateKey, token0825, randomDeviceName, tlv0006)
            if (token00BA != null) {
                writeHex("01 10")
                writeHex("00 3C")
                writeHex("00 01")
                writeHex("00 38"); writeFully(token00BA)
            }
            writePart2()
        }
    }

    internal sealed class LoginResponse : Packet {
        class KeyExchange(
            val tlv0006: IoBuffer,//120bytes
            val tokenUnknown: ByteArray?,
            val privateKeyUpdate: PrivateKey//16bytes
        ) : LoginResponse() {
            override fun toString(): String = "LoginResponse.KeyExchange"
        }

        class CaptchaInit(
            val captchaPart1: IoBuffer,
            val token00BA: ByteArray
        ) : LoginResponse() {
            override fun toString(): String = "LoginResponse.CaptchaInit"
        }

        @Suppress("unused")
        class Success(
            val sessionResponseDecryptionKey: SessionResponseDecryptionKey,

            val token38: IoBuffer,//56
            val token88: IoBuffer,//136
            val encryptionKey: IoBuffer,//16

            val nickname: String,
            val age: Short,
            val gender: Gender
        ) : LoginResponse() {
            override fun toString(): String = "LoginResponse.Success"
        }

        data class Failed(val result: LoginResult) : LoginResponse()
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): LoginResponse {
        val size = remaining.toInt()
        return when {
            size == 229 || size == 271 || size == 207 || size == 165 /* TODO CHECK 165 */ -> {
                discardExact(5)//01 00 1E 00 10
                val privateKeyUpdate = PrivateKey(readBytes(0x10))
                discardExact(4)//00 06 00 78
                val tlv0006 = readIoBuffer(0x78)

                return try {
                    discardExact(8)//01 10 00 3C 00 01 00 38
                    LoginResponse.KeyExchange(tlv0006, readBytes(56), privateKeyUpdate)
                } catch (e: EOFException) {
                    //什么都不做. 因为有的包就是没有这个数据.
                    LoginResponse.KeyExchange(tlv0006, null, privateKeyUpdate)
                }
            }

            size == 844 || size == 871 || size == 852 -> {
                /*
                               FB 01 04 03 3B 00 01 00 BA 02 03 34 13
                               00 05 01 00 00 01 23
                               00 40 AA F6 23 CF 12 15 32 BE 21 5C 8D 43 7B BA 10 BD D8 8B 4B 23 54 7F C5 1C C2 34 51 84 B0 9E 86 8C 30 26 97 B3 26 A8 23 C2 15 72 26 C7 52 88 BA 56 C8 A3 C5 3E C4 DC B3 5A 96 DE 8F A8 72 AB 9D 00 02 BC 89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 00 82 00 00 00 35 08 03 00 00 00 BA 12 C3 02 00 00 00 04 67 41 4D 41 00 00 B1 8F 0B FC 61 05 00 00 00 01 73 52 47 42 00 AE CE 1C E9 00 00 00 45 50 4C 54 45 FD F7 ED D8 FF E9 EE F6 E7 74 9A 81 F8 F1 E7 EF FE EE 19 84 53 FF F2 EC F6 FC EF FF FE F3 E7 FF EF BE E7 CE 35 6D 4D 4A 92 6D 1A 73 48 54 77 5F EA EB DE E1 F5 E2 88 B2 97 B1 C7 B3 D3 EE D8 8D C5 A6 9E D8 B8 92 93 00 F8 00 00 09 FE 49 44 41 54 58 C3 B4 58 0B 97 AB BC 0D B4 2D 8C DF F8 81 CD FF FF A9 1D 99 6C 12 48 D2 9E EF B4 CD BD BB 09 2C C1 62 34 1A 8D 2C C4 7F F9 32 FC 7A FB 88 CF D6 5A 5D B5 B6 CB 82 73 38 BF F0 07 61 F0 B6 18 F1 BF 7F BD 45 70 1E 60 29 44 80 28 9C 58 F8 60 31 8E 5F 88 85 7F CF 60 FE 0F 31 F0 9B FD 3B 70 7C 50 0A 3F 3A 1F CC 25 97 E7 4B B8 5F B7 39 7F 96 7F F8 5B 3C 1F DF 3E 83 40 14 62 3E 35 7F 30 C8 09 80 C0 D9 F3 D4 C2 DF 30 E2 FA F3 1E C1 3F 7E FE F9 03 C8 ED F3 14 3E 62 C1 45 B9 13 84 A5 28 8D 10 9C B3 8A 5F C2 3D 50 B9 07 F1 F8 70 BE 99 E7 DF 7F FD BE 5E 83 55 17 AB FE A0 50 08 07 A9 5F B0 A8 98 FF B5 D6 33 FF 4C 0E D0 F3 EF 9B E6 13 86 DB C3 19 F1 BC E8 F3 B7 B8 9D 79 A0 F0 80 1D F1 20 DF AE 54 ED 6B F7 5C 19 96 09 A9 A5 94 DE FE 64 A3 B9 FD FB CF D8 5F AE 51 1C C1 5B 39 5A 65 C5 4E 39 C7 98 5A F0 7C 2C 5C C1 89 48 7B F9 B6 E0 CF 95 CC 83 74 E2 0E C9 3D 70 DC 5F B8 59 7D 78 03 E7 8A D6 4A ED 39 87 75 DD 56 0A B6 58 05 2A D0 9E 68 34 A6 C5 F2 E5 A1 C4 25 1A F3 B1 92 B8 A5 E1 1E 3D 6E EA 18 02 67 16 53 CA E2 90 73 D5 72 4A DB 8A 17 75 07 50 16 4B 14 8F 41 60 89 B3 E2 C2 A4 AF 95 F9 FA BB F9 75 D5 05 05 16 20 66 C2 AC 79 2C 62 AB D5 8D 41 C0 6B 8B A4 95 C5 C2 14 37 79 EC 8C AB 7B DD E5 4A C7 0B 05 C5 FD C0 FC 7D C5 DC 03 E1 8F 9C 86 99 02 DC 1D 49 51 4A EB 41 72 3D 63 C8 01 35 61 1C 6D EB 31 76 14 AA 5B BE B3 CA FC 40 E3 72 95 B9 9D 78 86 8A 1A 04 E9 3A CA AE 70 2D 2C 4E F9 01 14 90 88 0D AF E6 2B 28 DB B6 35 A5 9D E5 52 5D 51 B8 05 F4 F6 B8 D7 1A FC 08 E4 FD CB 78 30 94 7F 6D 84 20 0A 1E D8 38 EB 47 6C A9 8D 96 10 42 92 1A C8 0C A0 90 0E BE DA DE F3 F9 3B D7 5F 84 CA 5C C1 79 1C 2C 82 9F 5B E6 46 B9 39 00 61 0C 87 40 BB B4 75 27 E4 00 01 00 28 F4 99 45 7E B9 5C 79 76 6C EE E1 E3 F9 E9 CA C8 07 1A D1 93 40 BC 90 6A C3 05 E9 D6 B5 FF BD AE E9 33 01 0D 29 9D 6B 03 01 15 00 10 5F 09 4A F0 F5 43 58 F1 ED 1C C2 09 AB CB 1A FA
                                */

                /*
                FB 01 04 03 3B 00 01 00 BA 02 03 34 13
                 05 01 00 00 01 23

                 [00 40] E7 C7 8D 04 D4 37 E7 37 4E BD 68 6B CF DA EA FB 8B FD BB 95 90 FF 36 61 43 64 78 00 0D 07 EB F5 00 AC 1A 21 A9 5D 1F D1 3A 04 89 D0 18 49 CF D1 6B B6 F2 A2 A4 6B A2 3C 2C 8C 5E 7F 1A 94 37 D4 02 BC 89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 00 82 00 00 00 35 08 03 00 00 00 BA 12 C3 02 00 00 00 04 67 41 4D 41 00 00 B1 8F 0B FC 61 05 00 00 00 01 73 52 47 42 00 AE CE 1C E9 00 00 00 3C 50 4C 54 45 FF F7 ED 8D 70 44 D8 BD 96 9C 73 37 FD FA F7 FB F2 E4 FB EE DA FB F8 EC FD F5 F2 FF FA DF FF F5 CB FD E9 CB EA D4 B5 AB 93 6F 9C 80 59 80 60 2C F8 E2 BF 7F 66 42 C2 A6 7E B9 94 5F D7 79 28 98 00 00 0A D2 49 44 41 54 58 C3 8C 98 8D 82 AB A8 12 84 11 05 E4 47 44 F3 FE EF BA 5F B5 49 26 66 E6 DE 5D 33 67 E2 99 18 68 AA AB AB 0B 9C BB 5F C9 AE 75 59 97 45 6F 8B 6E F4 4B 2F 3E 08 D1 FD CB B5 DE 2E F7 5F AE 74 7F A5 7C 05 B1 24 4D AD 9B 85 91 74 BB A6 10 F8 F4 D7 37 6E 2F 42 70 F7 00 D2 BF BE 5E 71 BC 7E 3F 51 E0 62 C9 0A 23 84 F0 FA 6B FA 0C FC CF DF 16 C3 15 C4 F3 C1 F0 1A F7 FF FE B3 48 5E BF 9E 73 B1 70 46 62 84 C8 95 73 8E 35 3F 43 F9 79 D4 FD 75 AF 2F AD AF 9C 06 97 3F 9F FA FB FD B5 80 F7 FF C9 C4 CF DF 43 4E 9A BD 56 8B 22 06 71 E4 E7 9B 1F F3 3E 87 B8 62 48 17 6A D9 91 B7 90 93 FB 78 FA FD A0 FB FC F3 2B 1F 6F 44 5E 01 04 2D 7F 34 7F 9E DB E9 5B 25 88 10 60 C8 FA 43 21 E7 BE 62 49 EF 94 89 B9 C9 22 B8 CF F1 BF 50 70 BF FE 1F 9C 06 C8 D5 6F C7 64 57 DF 7C CD 49 30 7C 51 E9 FD F6 0A 4C 40 5D 20 08 86 94 3E 10 FA 8B 07 2E DD 91 78 DE 28 E9 21 E6 E8 FB 7E 6C 9B F7 FE DC 88 82 18 E0 E7 9A BE BE FD 99 47 BD AD E9 8A C1 C1 83 9C 7E 62 48 7F 54 C7 7B D9 77 62 2E 41 20 90 80 E6 FB B4 F9 12 03 29 18 7E EB C7 06 0E F9 4E 82 F4 89 A9 BE 1B AE 4A FE 11 89 DB F4 9F 85 E3 FE 22 EA 33 A0 B0 B8 48 04 CC D9 7D 29 4C 9B 44 8A E2 B7 69 1B F0 E1 0B 44 F7 83 34 91 8B 00 56 C9 D1 AA 51 82 F2 09 D8 5F B1 BB 5B 4C AF 34 C4 11 5B 1B DB B1 B5 52 0A 39 A5 18 A8 0A DF 0F CF 6D FA CC DE 57 10 FA B7 10 8A AE 48 0C 92 D8 F5 F7 A4 1F F0 A7 7B 1A AF 0F 48 44 D4 F7 FD 24 10 4A 69 44 A3 92 84 1A 53 6F 39 7F 32 EB 96 07 C7 8A B9 62 4A AA 61 2A 98 C5 90 88 F5 9B AF BF 2B DA DD 0B 86 2F 2E 71 01 D0 EA 59 73 3B 7B 9F 8E 7E 52 93 31 57 70 29 39 DD 34 F1 67 7C 42 07 7F A4 80 32 A2 82 7A AB 11 31 5B 17 77 A3 FD 17 27 BF 63 7A CA 42 72 8D 7A 08 D2 A4 76 6E F3 3C EF 3B B4 04 85 EC 8F 3E 4A FA 35 CA 00 01 00 28 83 4D 03 6C A3 41 E3 77 42 EC BD 53 16 A0 60 C8 32 CB 8C 39 2B 61 87 E3 50 79 E1 BD 5A 5D AF C0 A1 B7 27 83 FE CE CD 5B 01 15 00 10 B0 50 A7 C9 03 2F EC 2F 05 22 EC 27 32 93 FF 74
                 */

                /*
                FB 01 04 03 3B 00 01 00 BA 02 03 34 13
                00 05 01 00 00 01 23
                [00 40] 67 42 E8 E5 08 2D D2 87 83 DB A6 D3 56 51 6F 43 A5 DB 67 CD 31 24 DE 2D AF 5A D2 13 F6 5D 7B D1 26 55 61 DB 95 80 C6 B1 74 66 DB C2 8C EC 71 0E DA 74 D0 6D 80 BB 88 B5 12 6A 30 24 DB 65 95 1C
                [02 BC] 89 `50 4E 47` 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 00 82 00 00 00 35 08 03 00 00 00 BA 12 C3 02 00 00 00 04 67 41 4D 41 00 00 B1 8F 0B FC 61 05 00 00 00 01 73 52 47 42 00 AE CE 1C E9 00 00 00 3C 50 4C 54 45 FF F2 EC 55 96 73 F2 EF E4 79 AD 8F EC FD EC DC FF EB 23 70 4A FC FE F3 FD F7 ED F5 FA EC 30 84 5B AC E1 C3 12 78 49 CC E4 D0 99 C3 A9 12 87 53 E2 EF DE 49 7D 5F 3A 6F 51 BF F9 DA 26 D7 2D 0B 00 00 0B 58 49 44 41 54 58 C3 8C 98 8B 82 AC AA 0E 44 45 40 44 84 46 FD FF 7F BD AB E2 A3 9D D9 E7 EC 73 ED 99 7E 22 14 49 A5 92 30 CC AF 6B B8 AE 39 85 9E C6 79 98 E7 31 DB 47 FB 32 E7 FC 1A F3 EB E2 97 C4 08 EE D4 E0 39 C4 A0 DB E7 F3 E9 EF D7 60 8F FB 0D 57 D2 73 4E 29 0B C2 90 C2 9C C6 91 E9 13 83 5E 10 E6 5F 60 00 AB 41 F3 1C 42 18 46 20 84 CC ED FC 0F 86 E2 AF 7F 0F D4 E1 B1 03 33 64 4D C1 87 31 85 C0 BE 78 EA FA 35 7F 77 35 BC 6E 98 CF DF F4 48 73 C8 4C 30 84 70 CD 3A CC FF 17 88 97 1D EC 0F 13 84 64 D6 98 53 BC AE 30 0F BF 0C 9B ED 63 96 6D 6C F1 64 96 10 04 46 8E 32 82 59 E1 3F 11 7C 5F 6E 00 5A 7A 1E D9 85 AC 91 62 F1 CE 3B E7 5B B4 99 AF 89 CF C7 F5 74 5F 27 88 31 5F 86 31 F2 CC B7 8D 6E 9B FD D3 F3 30 BC B6 AF 5D C9 03 A2 A3 FD 34 7A 37 4D DB 3E AD CB F1 86 F0 45 C0 D2 E1 04 60 5F 25 BB 66 B1 67 30 42 9C 10 86 BF 81 F8 E9 02 99 37 40 44 D8 18 C4 C0 A1 EF D3 E2 4B F1 9F 4F 0B 27 84 6B F9 EF B3 AD 6F 2C D0 C2 63 EF A3 BE 91 25 C2 13 16 7F C7 F0 F5 C2 69 E2 1C 7A 6D AD 86 30 12 8A 29 6D 8B 3B 62 28 EB EA 44 F4 1C 6E 0E DC E3 BF 1F 65 BB D4 9B F7 BA D9 BC 19 60 50 7E B3 67 78 56 FB 15 08 F9 5C 1F 47 E6 34 A6 CA 14 B5 6D 25 E4 91 38 EC 7E 5A 4A 0A 71 59 97 13 42 1A 1F 07 26 D3 8D 71 B8 A2 93 7B C7 06 91 53 DD 62 24 A2 47 63 32 D3 88 5E 63 FA 97 BD 9B 12 E4 13 D2 29 2E 39 77 1C 98 FA E6 C5 47 AC 50 96 A9 85 1C DD BA 54 8D 65 C6 D1 08 7B 5E B8 FD 8E 1C 01 1A D9 7F 08 7D 8F 11 A4 A9 BA 69 F7 85 D0 32 5D 19 7F EE FD E7 4B 1E 6E 4D CA 29 57 D8 34 A6 E4 F6 68 1B 66 FF 93 47 1C CA 34 79 AC C5 00 91 CD E4 EB 21 BB 40 49 0A D2 5C 2B 91 D4 EB 04 04 6E F5 FB B4 EF DB 56 81 A0 9B E6 F9 A7 02 BD 22 22 DF 02 23 7D 1D 5B E8 62 63 DB 0B D3 A0 33 C1 4F AE A7 1C B7 69 8B A3 06 58
                00
                01
                [00 28] AD 53 81 65 DB 7D 7B 1E F4 AC 69 28 90 35 23 F3 0F DF AF 48 66 D9 06 13 0F AE 57 3C 5D AF CB 96 6C 5C CD 95 3F 2F 50 C9
                01
                15
                [00 10] A8 5D 2B 4F 33 AF 5D 99 B1 EF 92 DA C6 E5 A9 FB
                 */
                discardExact(20) // FB 01 04 03 3B 00 01 00 BA 02 03 34 13    00 05 01 00 00 01 23
                discardExact(readUShort()) // size=00 40, 64
                val captchaPart1 = readIoBuffer(readUShort().toInt()) // size=02 BC, 700

                discardExact(2)//00 01

                val token00BA = readBytes(readUShort().toInt()) // size=00 28, 40

                /*
                剩余
                 01
                15
                [00 10] A8 5D 2B 4F 33 AF 5D 99 B1 EF 92 DA C6 E5 A9 FB
                 */
                return LoginResponse.CaptchaInit(captchaPart1, token00BA)
            }
            size > 650 -> {

                /*
                00 01 09 00 70 00 01 C4 20 CB 84 35 17 3F 43 FC 06 63 D9 49 5B 3C AC 00 38 12 9E 18 DC 47 41 FC EF 0F EA FC AD 22 88 82 17 C0 52 84 63 9B 0C 1E E9 28 AE 78 CC 0A D3 FE BE 46 4A 59 CE 64 07 81 A6 9E AC E6 31 4C 23 A9 3E C2 20 84 54 05 92 8E E9 00 20 B5 9E 51 9C C4 FD 2F E1 00 8B F7 2B CE 1B C8 DA F0 7D 62 DC 5A CA FE AF 8C 54 92 A8 58 9E F5 91 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 1A F8 64 61 13 97 89 C1 64 E9 B9 97 A1 2F CE D6 91 5B D2 3A 60 D2 B7 F2 38 35 57 0C 24 51 18 FC 02 EA C6 E9 E8 B9 CB B3 35 97 8F 6E A1 CE 53 22 9E B5 2C 31 36 C6 3C C1 01 07 01 D3 00 01 00 16 00 00 00 01 00 00 00 64 00 00 0D FD 00 09 3A 80 00 00 00 3C 00 02 48 60 3F 44 54 39 70 44 24 62 2A 53 6E 71 72 34 00 88 00 04 5D E7 BE 55 AB 53 02 17 00 00 00 00 00 78 A9 44 3A 18 15 0F 3F 52 57 0B 6C C8 34 6B B6 B1 A6 B0 B5 9D 74 4D BD 52 88 DD E4 A1 F2 EC 3E 49 3B 05 B4 F5 46 2B 8A 2D 7D AE E6 91 66 DD A3 78 5C AF 7D 5A 65 AA AD 6C CD 65 55 49 4E 07 FE 3A AD 76 75 21 DC AF 92 48 AA 48 22 29 B4 D3 6A A5 D1 D5 EB 62 A8 17 6C E3 FA CB D6 BB BE CE 7F F4 4E 18 B4 BF 76 3D 9B AF CB A4 89 1A CC E8 B5 07 54 E2 6A 59 CE 0F 20 74 4B 60 6D 5A 49 24 5B 27 46 38 77 66 59 2B 46 7D 00 78 00 01 5D E7 BE 55 00 70 B9 A4 D6 DB CF AF C3 CA 04 98 22 60 B1 B5 9C 55 06 F1 B6 D8 CF 63 20 1E 81 90 DA 29 44 79 F0 13 65 3B 2B 83 B8 D7 93 D7 DF 05 71 19 5B 25 68 EA DD 9B 01 E0 F0 5F 7A 79 CF C6 35 A7 AC 14 D7 AF 1A 5D AF 72 D2 25 57 36 E0 DE 9B 0D A8 B1 62 78 3D 9F DE D6 0C 37 7F B7 AC 94 40 A7 0D A9 A2 71 AB E0 C2 EE 10 CA 67 59 C8 57 F4 36 2C 77 79 98 00 83 01 3D 77 5E 58 59 47 5F 3E 77 4B 45 4A 2A 2A 5A 6A 00 70 00 01 5D E7 BE 55 00 68 7A 22 3E 6F 09 F1 37 5F 95 62 FF 06 BB 6D C4 77 92 4C 16 23 65 8A FF 38 F2 7A A6 91 10 AB B6 3B 14 30 C6 AC 58 59 7B E8 3F B2 97 EA 63 99 B9 6E DC F5 2A D4 24 0B 38 6F 67 75 D5 BF FE 74 0B A0 E5 8A 64 10 41 EF 86 24 07 81 75 2E B3 BE EE A4 AD B1 91 37 BE 6B 80 43 AF D9 0F 73 1F B4 7B 82 CF 07 12 C6 41 39 B9 E8 53 70 42 51 5F 52 28 64 29 4E 4B 2D 77 32 29 52 01 08 00 23 00 01 00 1F 00 17 02 5B 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 01 13 80 02 00 06 00 04 00 00 00 01 01 15 00 10 04 19 C6 27 44 A7 B7 34 EF 1C 45 67 78 1F CD 18
                 */

                discardExact(7)//00 01 09 00 70 00 01
                //FB 01 04 03 33
                val encryptionKey = readIoBuffer(16)//C6 72 C7 73 70 01 46 A2 11 88 AC E4 92 7B BF 90

                discardExact(2)//00 38
                val token38 = readIoBuffer(56)

                discardExact(60)//00 20 01 60 C5 A1 39 7A 12 8E BC 34 C3 56 70 E3 1A ED 20 67 ED A9 DB 06 C1 70 81 3C 01 69 0D FF 63 DA 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6

                val flagFront = readUByte().toUInt()
                val flagBack = readUByte().toUInt()
                discardExact(
                    when (flagFront) {
                        0x00u -> when (flagBack) {
                            0x33u -> 28
                            else -> null
                        }
                        0x01u -> when (flagBack) {
                            0x07u -> 0
                            0x10u -> 64
                            else -> null
                        }
                        else -> null
                    }
                        ?: error("Unknown length flag: 0x" + flagFront.toUByte().toUHexString() + flagBack.toUByte().toUHexString() + ", remaining packet = ${readBytes().toUHexString()}")
                )

                discardExact(23 + 3)//01 D3 00 01 00 16 00 00 00 01 00 00 00 64 00 00 0D DE 00 09 3A 80 00

                discardExact(2)//00 02
                val sessionResponseDecryptionKey = SessionResponseDecryptionKey(readIoBuffer(16))
                discardExact(2)
                val token88 = readIoBuffer(136)

                discardExact(299)//2E 72 7A 50 41 54 5B 62 7D 47 5D 37 41 53 47 51 00 78 00 01 5D A2 DB 79 00 70 72 E7 D3 4E 6F D8 D1 DD F2 67 04 1D 23 4D E9 A7 AB 89 7A B7 E6 4B C0 79 60 3B 4F AA 31 C5 24 51 C1 4B 4F A4 32 74 BA FE 8E 06 DB 54 25 A2 56 91 E8 66 BB 23 29 EB F7 13 7B 94 1E AF B2 40 4E 69 5C 8C 35 04 D1 25 1F 60 93 F3 40 71 0B 61 60 F1 B6 A9 7A E8 B1 DA 0E 16 A2 F1 2D 69 5A 01 20 7A AB A7 37 68 D2 1A B0 4D 35 D1 E1 35 64 F6 90 2B 00 83 01 24 5B 4E 69 3D 45 54 6B 29 5E 73 23 2D 4E 42 3F 00 70 00 01 5D A2 DB 79 00 68 FD 10 8A 39 51 09 C6 69 CE 09 A4 52 8C 53 D3 B6 87 E1 7B 7E 4E 52 6D BA 9C C4 6E 6D DE 09 99 67 B4 BD 56 71 14 5A 54 01 68 1C 3C AA 0D 76 0B 86 5A C1 F1 BC 5E 0A ED E3 8C 57 86 35 D8 A5 F8 16 01 24 8B 57 56 8C A6 31 6F 65 73 03 DA ED 21 FA 6B 79 32 2B 09 01 E8 D2 D8 F0 7B F1 60 C2 7F 53 5D F6 53 50 8A 43 E2 23 2E 52 7B 60 39 56 67 2D 6A 23 43 4B 60 55 68 35 01 08 00 23 00 01 00 1F 00 17 02 5B
                val nickLength = readUByte().toInt()
                val nickname = readString(nickLength)

                //后文
                //00 05 00 04 00 00 00 01 01 15 00 10 49 83 5C D9 93 6C 8D FE 09 18 99 37 99 80 68 92

                discardExact(4)//02 13 80 02
                val age = readShort()//00 05

                discardExact(4)//00 04 00 00

                discardExact(2)//00 01
                val gender = if (readBoolean()) Gender.FEMALE else Gender.MALE

                return LoginResponse.Success(sessionResponseDecryptionKey, token38, token88, encryptionKey, nickname, age, gender)
            }

            else -> LoginResponse.Failed(when (size) {
                135 -> {//包数据错误. 目前怀疑是 tlv0006
                    this.readRemainingBytes().cutTail(1).decryptBy(TIMProtocol.shareKey).read {
                        discardExact(51)
                        MiraiLogger.error("Internal error: " + readUShortLVString())//抱歉，请重新输入密码。
                    }

                    LoginResult.INTERNAL_ERROR
                }

                246 -> LoginResult.PROTECTED
                240, 319, 320, 351 -> LoginResult.WRONG_PASSWORD
                //135 -> LoginState.RETYPE_PASSWORD
                63 -> LoginResult.BLOCKED
                263 -> LoginResult.UNKNOWN_QQ_NUMBER
                279, 495, 551, 487 -> LoginResult.DEVICE_LOCK
                343, 359 -> LoginResult.TAKEN_BACK

                // 246: 33 05 08 00 22 01 00 00 03 E8 00 1B 02 00 00 00 01 00 00 15 85 08 36 00 00 00 33 00 00 00 00 76 E7 50 ED 00 00 01 26 01 00 00 B7 00 01 08 36 00 00 01 26 00 AD E8 AF A5 E5 8F B7 E7 A0 81 E9 95 BF E6 9C 9F E6 9C AA E7 99 BB E5 BD 95 EF BC 8C E4 B8 BA E4 BA 86 E4 BF 9D E9 9A 9C E5 B8 90 E5 8F B7 E5 AE 89 E5 85 A8 EF BC 8C E5 B7 B2 E8 A2 AB E7 B3 BB E7 BB 9F E8 AE BE E7 BD AE E6 88 90 E4 BF 9D E6 8A A4 E7 8A B6 E6 80 81 EF BC 8C E8 AF B7 E7 94 A8 E6 89 8B E6 9C BA 51 51 E6 9C 80 E6 96 B0 E7 89 88 E6 9C AC E7 99 BB E5 BD 95 EF BC 8C E7 99 BB E5 BD 95 E6 88 90 E5 8A 9F E5 90 8E E5 8D B3 E5 8F AF E8 87 AA E5 8A A8 E8 A7 A3 E9 99 A4 E4 BF 9D E6 8A A4 E7 8A B6 E6 80 81 E3 80 82 01 15 00 10 26 F9 4C F4 F0 CA 6C 53 98 77 54 2B BD CD 40 66

                // 165: 01 00 1E 00 10 72 36 7B 6B 6D 78 3A 4B 63 7B 47 5B 68 3E 21 59 00 06 00 78 34 F6 F9 49 AA 13 F5 F5 01 36 13 E1 4C F7 0F 25 C1 2C 10 75 CA 69 E9 12 B3 6D F4 A7 59 60 FF 01 03 73 28 47 A3 2A B8 46 C3 92 24 D5 8A AE 8B C2 45 0C 31 27 B5 17 9E 22 13 59 AF B4 CC F6 E3 3A 91 60 13 21 11 3C 25 D9 50 F4 23 C6 06 1D F4 15 41 BA 5D 7B 66 26 96 EB 0E 04 14 8E 5B D4 33 6E B8 5D E7 10 3A 0E EF 96 B1 D4 22 E4 74 48 A7 1D 3A 46 7D E6 EF 1F 6B 69 01 15 00 10 6F 99 48 5E 98 AE D3 4B F8 35 63 1D 70 EE 6D 82

                else -> {
                    MiraiLogger.error("login response packet size = $size, data=${this.readRemainingBytes().toUHexString()}")
                    LoginResult.UNKNOWN
                }
            })
        }
    }
}

internal inline class SessionResponseDecryptionKey(private val delegate: IoBuffer) : Decrypter {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(delegate)

    override fun toString(): String = "SessionResponseDecryptionKey"

    companion object Type : DecrypterType<SessionResponseDecryptionKey>
}

private fun BytePacketBuilder.writePart1(
    qq: Long,
    password: ByteArray,
    loginTime: Int,
    loginIP: String,
    privateKey: PrivateKey,
    token0825: ByteArray,
    randomDeviceName: Boolean,
    tlv0006: IoBuffer? = null
) {
    //this.writeInt(System.currentTimeMillis().toInt())
    this.writeHex("01 12")//tag
    this.writeHex("00 38")//length
    this.writeFully(token0825)//length
    this.writeHex("03 0F")//tag
    this.writeDeviceName(randomDeviceName)

    this.writeHex("00 05 00 06 00 02")
    this.writeQQ(qq)
    this.writeHex("00 06")//tag
    this.writeHex("00 78")//length
    if (tlv0006 != null) {
        this.writeFully(tlv0006)
    } else {
        this.writeTLV0006(qq, password, loginTime, loginIP, privateKey)
    }
    //fix
    this.writeFully(TIMProtocol.passwordSubmissionTLV2)
    this.writeHex("00 1A")//tag
    this.writeHex("00 40")//length
    this.writeFully(TIMProtocol.passwordSubmissionTLV2.encryptBy(privateKey.value))
    this.writeFully(TIMProtocol.constantData1)
    this.writeFully(TIMProtocol.constantData2)
    this.writeQQ(qq)
    this.writeZero(4)

    this.writeHex("01 03")//tag
    this.writeHex("00 14")//length

    this.writeHex("00 01")//tag
    this.writeHex("00 10")//length
    this.writeHex("60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6")//key
}

private fun BytePacketBuilder.writeTLV0006(qq: Long, passwordMd5: ByteArray, loginTime: Int, loginIP: String, privateKey: PrivateKey) {
    val secondMD5 = md5(passwordMd5 + byteArrayOf(0, 0, 0, 0) + qq.toUInt().toByteArray())

    this.encryptAndWrite(secondMD5) {
        writeRandom(4)
        writeHex("00 02")
        writeQQ(qq)
        writeFully(TIMProtocol.constantData2)
        writeHex("00 00 01")

        writeFully(passwordMd5)
        writeInt(loginTime)
        writeByte(0)
        writeZero(4 * 3)
        writeIP(loginIP)
        writeZero(8)
        writeHex("00 10")//这两个hex是passwordSubmissionTLV2的末尾
        writeHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")//16
        writeFully(privateKey.value)
    }
}

private fun BytePacketBuilder.writePart2() {

    this.writeHex("03 12")//tag
    this.writeHex("00 05")//length
    this.writeHex("01 00 00 00 01")//value

    this.writeHex("05 08")//tag
    this.writeHex("00 05")//length
    this.writeHex("01 00 00 00 00")//value

    this.writeHex("03 13")//tag
    this.writeHex("00 19")//length
    this.writeHex("01")//value

    this.writeHex("01 02")//tag
    this.writeHex("00 10")//length
    this.writeHex("04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA")//key
    this.writeZero(3)
    this.writeByte(0)//maybe 00, 0F, 1F

    this.writeHex("01 02")//tag
    this.writeHex("00 62")//length
    this.writeHex("00 01")//word?
    this.writeHex("04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48")//key
    this.writeHex("00 38")//length
    //value
    this.writeHex("E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3")

    this.writeHex("00 14")
    this.writeCRC32()
}

internal fun BytePacketBuilder.writeCRC32() = writeCRC32(getRandomByteArray(16))

internal fun BytePacketBuilder.writeCRC32(key: ByteArray) {
    writeFully(key)//key
    writeInt(crc32(key))
}
