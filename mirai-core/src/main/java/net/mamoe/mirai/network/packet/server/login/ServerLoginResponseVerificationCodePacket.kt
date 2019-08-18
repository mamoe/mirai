package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.goto
import net.mamoe.mirai.util.TEACryptor
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerLoginResponseVerificationCodePacket(input: DataInputStream) : ServerPacket(input) {
    var verifyCodeLength: Int = 0
    lateinit var verifyCode: String
    lateinit var token00BA: ByteArray
    var unknownBoolean: Boolean? = null


    override fun decode() {//todo decode 注释的内容
        /*
        data = 取文本中间(data, 43, 取文本长度(data) - 45)
        data = TeaDecrypt(data, Protocol.shareKey)

        verifyCodeLength = HexToDec(取文本中间(data, 235, 5))
        verifyCode = 取文本中间(data, 241, verifyCodeLength * 3 - 1)
        unknownBoolean = 取文本中间(data, 245 + verifyCodeLength * 3 - 1, 2) == "01"
        token00BA = 取文本中间(data, 取文本长度(data) - 178, 119)*/
    }
}

class ServerLoginResponseVerificationCodePacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    fun decrypt(): ServerLoginResponseVerificationCodePacket {
        this.input goto 14
        return ServerLoginResponseVerificationCodePacket(TEACryptor.CRYPTOR_SHARE_KEY.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }).dataInputStream())
    }
}