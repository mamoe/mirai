package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.goto
import net.mamoe.mirai.network.packet.server.readVarString
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseVerificationCodePacket(input: DataInputStream, val packetLength: Int) : ServerPacket(input) {
    var verifyCodeLength: Short = 0
    lateinit var verifyCode: ByteArray
    lateinit var token00BA: ByteArray
    var unknownBoolean: Boolean? = null


    @ExperimentalUnsignedTypes
    override fun decode() {//todo decode 注释的内容
        /*
        verifyCodeLength = HexToDec(取文本中间(data, 235, 5))
        verifyCode = 取文本中间(data, 241, verifyCodeLength * 3 - 1)
        unknownBoolean = 取文本中间(data, 245 + verifyCodeLength * 3 - 1, 2) == "01"
        token00BA = 取文本中间(data, 取文本长度(data) - 178, 119)
        */
        this.verifyCodeLength = this.input.goto(78).readShort()
        this.verifyCode = this.input.readNBytes(this.verifyCodeLength.toInt())

        this.input.skip(1)

        val b = this.input.readByte()
        System.out.println(b.toHexString())

        this.token00BA = this.input.goto(packetLength - 60).readNBytes(40)
    }
}

/*
        data = 取文本中间(data, 43, 取文本长度(data) - 45)
        data = TeaDecrypt(data, Protocol.shareKey)
*/