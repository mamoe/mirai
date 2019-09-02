package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.dataInputStream
import net.mamoe.mirai.network.packet.goto
import net.mamoe.mirai.utils.TEACryptor
import java.io.DataInputStream

/**
 * 收到这个包意味着需要验证码登录, 并且能得到验证码图片文件的一半
 *
 * @author Him188moe
 */
class ServerLoginResponseVerificationCodeInitPacket(input: DataInputStream, private val packetLength: Int) : ServerPacket(input) {

    lateinit var verifyCodePart1: ByteArray
    lateinit var token00BA: ByteArray
    var unknownBoolean: Boolean? = null
//todo 也有可能这个包有问题, 也有可能 ClientVerificationCodeTransmissionRequestPacket. 检查


    @ExperimentalUnsignedTypes
    override fun decode() {
        val verifyCodeLength = this.input.goto(78).readShort()//2bytes
        this.verifyCodePart1 = this.input.readNBytes(verifyCodeLength.toInt())

        this.input.skip(1)

        this.unknownBoolean = this.input.readByte().toInt() == 1

        this.token00BA = this.input.goto(packetLength - 60).readNBytes(40)
    }
}

class ServerLoginResponseVerificationCodePacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    fun decrypt(): ServerLoginResponseVerificationCodeInitPacket {
        this.input goto 14
        val data = TEACryptor.CRYPTOR_SHARE_KEY.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) });
        return ServerLoginResponseVerificationCodeInitPacket(data.dataInputStream(), data.size)
    }
}