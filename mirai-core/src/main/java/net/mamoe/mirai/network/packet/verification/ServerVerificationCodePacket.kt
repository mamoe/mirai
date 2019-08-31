package net.mamoe.mirai.network.packet.verification

import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.dataInputStream
import net.mamoe.mirai.network.packet.goto
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerVerificationCodePacket(input: DataInputStream) : ServerPacket(input) {

    lateinit var verifyCode: ByteArray
    lateinit var verifyToken: ByteArray
    var unknownBoolean: Boolean? = null
    lateinit var token00BA: ByteArray
    var count: Int = 0

    @ExperimentalUnsignedTypes
    override fun decode() {
        TODO()
        val verifyCodeLength = this.input.goto(78).readShort()//2bytes
        this.verifyCode = this.input.readNBytes(verifyCodeLength.toInt())

        this.input.skip(1)

        this.unknownBoolean = this.input.readByte().toInt() == 1

        //this.token00BA = this.input.goto(packetLength - 60).readNBytes(40)
    }
}

class ServerVerificationCodePacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    fun decrypt(token00BA: ByteArray): ServerVerificationCodePacket {
        this.input goto 14
        val data = TEACryptor.decrypt(token00BA, this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) });
        return ServerVerificationCodePacket(data.dataInputStream())
    }
}