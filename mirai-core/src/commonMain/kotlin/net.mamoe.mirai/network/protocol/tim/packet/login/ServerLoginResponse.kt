@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.Tested
import net.mamoe.mirai.utils.io.readBoolean
import net.mamoe.mirai.utils.io.readIoBuffer
import net.mamoe.mirai.utils.io.readString
import net.mamoe.mirai.utils.io.toReadPacket
import kotlin.properties.Delegates

@PacketId(0x08_36u)
sealed class ServerLoginResponsePacket(input: ByteReadPacket) : ServerPacket(input)

@PacketId(0x08_36u)
class LoginResponseFailedPacket(val loginResult: LoginResult, input: ByteReadPacket) : ServerLoginResponsePacket(input)

/**
 * 服务器进行加密后返回 privateKey
 */
@PacketId(0x08_36u)
class LoginResponseKeyExchangeResponsePacket(input: ByteReadPacket) : ServerLoginResponsePacket(input) {
    lateinit var tlv0006: IoBuffer//120bytes
    var tokenUnknown: ByteArray? = null

    lateinit var privateKeyUpdate: ByteArray//16bytes

    @Tested
    override fun decode() {
        this.input.discardExact(5)//01 00 1E 00 10
        privateKeyUpdate = this.input.readBytes(0x10)
        this.input.discardExact(4)//00 06 00 78
        tlv0006 = this.input.readIoBuffer(0x78)

        //todo 这边原本会判断是否 `08 36 31 03`, 是才会进行下列2行读取.
        try {
            this.input.discardExact(8)//01 10 00 3C 00 01 00 38
            tokenUnknown = this.input.readBytes(56)
        } catch (e: EOFException) {
            //什么都不做. 因为有的包就是没有这个数据.
        }
    }

    @PacketId(0x08_36u)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        @Tested
        fun decrypt(privateKey: ByteArray): LoginResponseKeyExchangeResponsePacket =
            LoginResponseKeyExchangeResponsePacket(this.decryptBy(TIMProtocol.shareKey, privateKey)).applySequence(sequenceId)
    }
}

enum class Gender(val id: Boolean) {
    MALE(false),
    FEMALE(true);
}

/**
 * @author NaturalHG
 */
@PacketId(0x08_36u)
class LoginResponseSuccessPacket(input: ByteReadPacket) : ServerLoginResponsePacket(input) {
    lateinit var sessionResponseDecryptionKey: IoBuffer//16 bytes|

    lateinit var token38: IoBuffer//56
    lateinit var token88: IoBuffer//136
    lateinit var encryptionKey: IoBuffer//16

    lateinit var nickname: String
    var age: Short by Delegates.notNull()
    lateinit var gender: Gender

    @Tested
    override fun decode() = with(input) {
        discardExact(7)//00 01 09 00 70 00 01
        encryptionKey = readIoBuffer(16)//C6 72 C7 73 70 01 46 A2 11 88 AC E4 92 7B BF 90

        discardExact(2)//00 38
        token38 = readIoBuffer(56)

        discardExact(60)//00 20 01 60 C5 A1 39 7A 12 8E BC 34 C3 56 70 E3 1A ED 20 67 ED A9 DB 06 C1 70 81 3C 01 69 0D FF 63 DA 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6

        discardExact(when (readUByte().toUInt()) {
            0x00u -> if (readUByte().toUInt() == 0x33u) 28 else null
            0x01u -> when (readUByte().toUInt()) {
                0x07u -> 0
                0x10u -> 64
                else -> null
            }
            else -> null
        } ?: error("Unknown length flag"))

        discardExact(23 + 3)//01 D3 00 01 00 16 00 00 00 01 00 00 00 64 00 00 0D DE 00 09 3A 80 00

        discardExact(2)//00 02
        sessionResponseDecryptionKey = readIoBuffer(16)
        discardExact(2)
        token88 = readIoBuffer(136)

        discardExact(299)//2E 72 7A 50 41 54 5B 62 7D 47 5D 37 41 53 47 51 00 78 00 01 5D A2 DB 79 00 70 72 E7 D3 4E 6F D8 D1 DD F2 67 04 1D 23 4D E9 A7 AB 89 7A B7 E6 4B C0 79 60 3B 4F AA 31 C5 24 51 C1 4B 4F A4 32 74 BA FE 8E 06 DB 54 25 A2 56 91 E8 66 BB 23 29 EB F7 13 7B 94 1E AF B2 40 4E 69 5C 8C 35 04 D1 25 1F 60 93 F3 40 71 0B 61 60 F1 B6 A9 7A E8 B1 DA 0E 16 A2 F1 2D 69 5A 01 20 7A AB A7 37 68 D2 1A B0 4D 35 D1 E1 35 64 F6 90 2B 00 83 01 24 5B 4E 69 3D 45 54 6B 29 5E 73 23 2D 4E 42 3F 00 70 00 01 5D A2 DB 79 00 68 FD 10 8A 39 51 09 C6 69 CE 09 A4 52 8C 53 D3 B6 87 E1 7B 7E 4E 52 6D BA 9C C4 6E 6D DE 09 99 67 B4 BD 56 71 14 5A 54 01 68 1C 3C AA 0D 76 0B 86 5A C1 F1 BC 5E 0A ED E3 8C 57 86 35 D8 A5 F8 16 01 24 8B 57 56 8C A6 31 6F 65 73 03 DA ED 21 FA 6B 79 32 2B 09 01 E8 D2 D8 F0 7B F1 60 C2 7F 53 5D F6 53 50 8A 43 E2 23 2E 52 7B 60 39 56 67 2D 6A 23 43 4B 60 55 68 35 01 08 00 23 00 01 00 1F 00 17 02 5B
        val nickLength = readUByte().toInt()
        nickname = readString(nickLength)

        //后文
        //00 05 00 04 00 00 00 01 01 15 00 10 49 83 5C D9 93 6C 8D FE 09 18 99 37 99 80 68 92

        discardExact(4)//02 13 80 02
        age = readShort()//00 05

        discardExact(4)//00 04 00 00

        discardExact(2)//00 01
        gender = if (readBoolean()) Gender.FEMALE else Gender.MALE
    }

    @PacketId(0x08_36u)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(privateKey: ByteArray): LoginResponseSuccessPacket = LoginResponseSuccessPacket(this.decryptBy(TIMProtocol.shareKey, privateKey)).applySequence(sequenceId)
    }

}

/**
 * 收到这个包意味着需要验证码登录, 并且能得到验证码图片文件的一部分
 *
 * @author Him188moe
 */
@PacketId(0x08_36u)
class LoginResponseCaptchaInitPacket(input: ByteReadPacket) : ServerLoginResponsePacket(input) {

    lateinit var captchaPart1: IoBuffer
    lateinit var token00BA: ByteArray
    var unknownBoolean: Boolean by Delegates.notNull()


    @Tested
    override fun decode() {
        this.input.discardExact(78)
        //println(this.input.readRemainingBytes().toUHexString())
        val captchaLength = this.input.readShort()//2bytes
        this.captchaPart1 = this.input.readIoBuffer(captchaLength)

        this.input.discardExact(1)

        this.unknownBoolean = this.input.readByte().toInt() == 1

        this.input.discardExact(this.input.remaining - 60)
        this.token00BA = this.input.readBytes(40)
    }


    @PacketId(0x08_36u)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(): LoginResponseCaptchaInitPacket = LoginResponseCaptchaInitPacket(decryptAsByteArray(TIMProtocol.shareKey).toReadPacket()).applySequence(sequenceId)
    }
}