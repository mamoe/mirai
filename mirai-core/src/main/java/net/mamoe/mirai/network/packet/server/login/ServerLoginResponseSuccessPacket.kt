package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.readIP
import net.mamoe.mirai.network.packet.server.readVarString
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.hexToShort
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 * @author NaturalHG @ Mirai Project
 */
class ServerLoginResponseSuccessPacket(input: DataInputStream, val packetDataLength: Int) : ServerPacket(input) {
    lateinit var _0828_rec_decr_key: ByteArray//16 bytes|
    var age: Short = 0
    var gender: Int = 0//from 1byte
    lateinit var nick: String
    lateinit var clientKey: ByteArray

    lateinit var token38: ByteArray
    lateinit var token88: ByteArray
    lateinit var encryptionKey: ByteArray


    @ExperimentalUnsignedTypes
    override fun decode() {

        this.input.skip(7)//8

        encryptionKey = this.input.readNBytes(16)//24

        this.input.skip(1)//25

        token38 = this.input.readNBytes(56)//81

        this.input.skip(61L)//142

        val msgLength = when (this.input.readShort()) {
                "01 07".hexToShort() -> 0
                "00 33".hexToShort() -> 28 * 3
                "01 10".hexToShort() -> 64 * 3
                else -> throw IllegalStateException()
            }//144

        this.input.skip(17L +  msgLength)//161+msgLength

        this.input.mark(113)//161+msgLength

        clientKey = this.input.readNBytes(112)//273+msgLength

        this.input.reset()//161+msgLength

        this.input.skip(10)//171+msgLength

        this._0828_rec_decr_key = this.input.readNBytes(16)//187+msgLength

        this.input.skip(2)//189+msgLength

        token88 = this.input.readNBytes(136)//325+msgLength

        this.input.skip(299L)//624+msgLength

        //varString (nickLength bytes)
        val nickLength = this.input.readByte().toUByte().toInt()//625+msgLength

        nick = this.input.readVarString(nickLength)//625+msgLength+nickLength

        val dataIndex = packetDataLength - 31

        this.input.skip((dataIndex - (625 + msgLength + nickLength)) + 0L)//-31

        gender = this.input.readByte().toUByte().toInt()//-30

        this.input.skip(9)//-27

        age = this.input.readShort()//-25

        /*
        age = HexToDec(取文本中间(data, 取文本长度(data) - 82, 5))
        gender = 取文本中间(data, 取文本长度(data) - 94, 2)
        */
    }
}

class ServerLoginResponseSucceedPacketEncrypted(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }

    @ExperimentalUnsignedTypes
    fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseSuccessPacket {//todo test
        this.input.skip(14)
        return ServerLoginResponseSuccessPacket(TEACryptor.decrypt(TEACryptor.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }, Protocol.shareKey.hexToBytes()), tgtgtKey).dataInputStream());
        //TeaDecrypt(取文本中间(data, 43, 取文本长度(data) － 45), m_0828_rec_decr_key)
    }
}
