package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.dataInputStream
import net.mamoe.mirai.network.packet.goto
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseVerificationCodePacket(input: DataInputStream, private val packetLength: Int) : ServerPacket(input) {

    lateinit var verifyCode: ByteArray
    lateinit var token00BA: ByteArray
    var unknownBoolean: Boolean? = null


    @ExperimentalUnsignedTypes
    override fun decode() {
        val verifyCodeLength = this.input.goto(78).readShort()//2bytes
        this.verifyCode = this.input.readNBytes(verifyCodeLength.toInt())

        this.input.skip(1)

        this.unknownBoolean = this.input.readByte().toInt() == 1

        this.token00BA = this.input.goto(packetLength - 60).readNBytes(40)
    }
}

class ServerLoginResponseVerificationCodePacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    fun decrypt(): ServerLoginResponseVerificationCodePacket {
        this.input goto 14
        val data = TEACryptor.CRYPTOR_SHARE_KEY.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) });
        return ServerLoginResponseVerificationCodePacket(data.dataInputStream(), data.size)
    }
}