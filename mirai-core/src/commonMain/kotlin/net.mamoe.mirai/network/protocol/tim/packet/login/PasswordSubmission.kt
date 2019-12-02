@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.contact.data.Gender
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.decryptBy
import net.mamoe.mirai.utils.encryptBy
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.writeCRC32

object ShareKey : DecrypterByteArray, DecrypterType<ShareKey> {
    override val value: ByteArray = TIMProtocol.shareKey
}

inline class PrivateKey(override val value: ByteArray) : DecrypterByteArray {
    companion object Type : DecrypterType<PrivateKey>
}

inline class SubmitPasswordResponseDecrypter(private val privateKey: PrivateKey) : Decrypter {
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
@AnnotatedId(KnownPacketId.LOGIN)
object SubmitPasswordPacket : PacketFactory<SubmitPasswordPacket.LoginResponse, SubmitPasswordResponseDecrypter>(SubmitPasswordResponseDecrypter) {
    operator fun invoke(
        bot: UInt,
        password: String,
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
            writePart1(bot, password, loginTime, loginIP, privateKey, token0825, randomDeviceName, tlv0006)
            if (token00BA != null) {
                writeHex("01 10")
                writeHex("00 3C")
                writeHex("00 01")
                writeHex("00 38"); writeFully(token00BA)
            }
            writePart2()
        }
    }

    sealed class LoginResponse : Packet {
        class KeyExchange(
            val tlv0006: IoBuffer,//120bytes
            val tokenUnknown: ByteArray?,
            val privateKeyUpdate: PrivateKey//16bytes
        ) : LoginResponse() {
            override fun toString(): String = "LoginResponse.KeyExchange"
        }

        class CaptchaInit(
            val captchaPart1: IoBuffer,
            val token00BA: ByteArray,
            val unknownBoolean: Boolean
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

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): LoginResponse {
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

            size == 844 || size == 871 -> {
                discardExact(78)
                //println(readRemainingBytes().toUHexString())
                val captchaLength = readShort()//2bytes
                val captchaPart1 = readIoBuffer(captchaLength)

                discardExact(1)

                val unknownBoolean = readByte().toInt() == 1

                discardExact(remaining - 60)

                return LoginResponse.CaptchaInit(captchaPart1, readBytes(40), unknownBoolean)
            }
            size > 650 -> {
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
                    } ?: error("Unknown length flag: " + flagFront.toUByte().toUHexString() + flagBack.toUByte().toUHexString())
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

                240, 319, 320, 351 -> LoginResult.WRONG_PASSWORD
                //135 -> LoginState.RETYPE_PASSWORD
                63 -> LoginResult.BLOCKED
                263 -> LoginResult.UNKNOWN_QQ_NUMBER
                279, 495, 551, 487 -> LoginResult.DEVICE_LOCK
                343, 359 -> LoginResult.TAKEN_BACK

                // 165: 01 00 1E 00 10 72 36 7B 6B 6D 78 3A 4B 63 7B 47 5B 68 3E 21 59 00 06 00 78 34 F6 F9 49 AA 13 F5 F5 01 36 13 E1 4C F7 0F 25 C1 2C 10 75 CA 69 E9 12 B3 6D F4 A7 59 60 FF 01 03 73 28 47 A3 2A B8 46 C3 92 24 D5 8A AE 8B C2 45 0C 31 27 B5 17 9E 22 13 59 AF B4 CC F6 E3 3A 91 60 13 21 11 3C 25 D9 50 F4 23 C6 06 1D F4 15 41 BA 5D 7B 66 26 96 EB 0E 04 14 8E 5B D4 33 6E B8 5D E7 10 3A 0E EF 96 B1 D4 22 E4 74 48 A7 1D 3A 46 7D E6 EF 1F 6B 69 01 15 00 10 6F 99 48 5E 98 AE D3 4B F8 35 63 1D 70 EE 6D 82

                else -> {
                    MiraiLogger.error("login response packet size = $size, data=${this.readRemainingBytes().toUHexString()}")
                    LoginResult.UNKNOWN
                }
            })
        }
    }
}

inline class SessionResponseDecryptionKey(private val delegate: IoBuffer) : Decrypter {
    override fun decrypt(input: ByteReadPacket): ByteReadPacket = input.decryptBy(delegate)

    override fun toString(): String = "SessionResponseDecryptionKey"

    companion object Type : DecrypterType<SessionResponseDecryptionKey>
}

private fun BytePacketBuilder.writePart1(
    qq: UInt,
    password: String,
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
    this.writeFully(TIMProtocol.passwordSubmissionTLV2.encryptBy(privateKey))
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


