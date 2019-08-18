package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.hexToShort
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 * @author NaturalHG @ Mirai Project
 */
class ServerLoginResponseSucceedPacket(input: DataInputStream) : ServerPacket(input) {
    lateinit var _0828_rec_decr_key: ByteArray
    var age: Int = 0
    var gender: Boolean = false//from 1byte
    lateinit var nick: String
    lateinit var clientKey: String

    lateinit var token38: ByteArray
    lateinit var token88: ByteArray
    lateinit var encryptionKey: ByteArray


    @ExperimentalUnsignedTypes
    override fun decode() {

        this.input.skip(141)//取文本中间 (data, 141 * 3 + 1, 5)
        val msgLength = when (this.input.readShort()) {
                "01 07".hexToShort() -> 0
                "00 33".hexToShort() -> 28 * 3
                "01 10".hexToShort() -> 64 * 3
                else -> throw IllegalStateException()
            }

        _0828_rec_decr_key = 取文本中间(data, 514 + msgLength, 47)
        val nickLength = HexToDec(取文本中间(data, 1873 + msgLength, 2))
        nick = 转_Ansi文本(取文本中间(data, 1876 + msgLength, 3 * nickLength - 1))
        age = HexToDec(取文本中间(data, 取文本长度(data) - 82, 5))
        gender = 取文本中间(data, 取文本长度(data) - 94, 2)
        clientKey = 删全部空(取文本中间(data, 484 * 3 + msgLength + 1, 112 * 3 - 1))

        token38 = 取文本中间(data, 76, 167)
        token88 = 取文本中间(data, 568 + msgLength, 407)
        encryptionKey = 取文本中间(data, 22, 47)
    }
}

class ServerLoginResponseSucceedPacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    @ExperimentalUnsignedTypes
    fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseSucceedPacket {//todo test
        this.input.skip(14)
        return ServerLoginResponseSucceedPacket(TEACryptor.decrypt(TEACryptor.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }, Protocol.shareKey.hexToBytes()), tgtgtKey).dataInputStream());
        //TeaDecrypt(取文本中间(data, 43, 取文本长度(data) － 45), m_0828_rec_decr_key)
    }
}
