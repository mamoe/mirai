package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.server.ServerPacket
import net.mamoe.mirai.network.packet.server.dataInputStream
import net.mamoe.mirai.network.packet.server.readVarString
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.hexToShort
import net.mamoe.mirai.util.toHexString
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

    lateinit var token38: ByteArray
    lateinit var token88: ByteArray
    lateinit var encryptionKey: ByteArray


    @ExperimentalUnsignedTypes
    override fun decode() {

        this.input.skip(7)//8

        encryptionKey = this.input.readNBytes(16)//24

        this.input.skip(2)//25->26

        token38 = this.input.readNBytes(56)//81->82

        this.input.skip(60L)//142

        //??
        var b = this.input.readNBytes(2)
        val msgLength = when (b.toUByteArray().toHexString()) {
                "01 07" -> 0
                "00 33" -> 28
                "01 10" -> 65
                else -> throw IllegalStateException()
            }//144


        System.out.println(msgLength)

        this.input.skip(17L +  msgLength)//161+msgLength

        this.input.skip(10)//171+msgLength

        _0828_rec_decr_key = this.input.readNBytes(16)//187+msgLength


        this.input.skip(2)

        token88 = this.input.readNBytes(136)//325+ // msgLength

        this.input.skip(299L)//624+msgLength

        //varString (nickLength bytes)
        val nickLength = this.input.readByte().toInt()//625+msgLength

        System.out.println(nickLength)

        nick = this.input.readVarString(nickLength)//625+msgLength+nickLength

        val dataIndex = packetDataLength - 31

        /*
        this.input.skip((dataIndex - (625 + msgLength + nickLength)) + 0L)//-31

        gender = this.input.readByte().toUByte().toInt()//-30

        this.input.skip(9)//-27

        age = this.input.readShort()//-25
        */
        age = 0
        gender = 0

        /*
        age = HexToDec(取文本中间(data, 取文本长度(data) - 82, 5))
        gender = 取文本中间(data, 取文本长度(data) - 94, 2)
        */
    }
}

class ServerLoginResponseSucceedPacketEncrypted(input: DataInputStream, val length: Int) : ServerPacket(input) {
    override fun decode() {

    }

    @ExperimentalUnsignedTypes
    fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseSuccessPacket {//todo test
        this.input.skip(14)
        return ServerLoginResponseSuccessPacket(TEACryptor.decrypt(TEACryptor.decrypt(this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }, Protocol.shareKey.hexToBytes()), tgtgtKey).dataInputStream(), length);
        //TeaDecrypt(取文本中间(data, 43, 取文本长度(data) － 45), m_0828_rec_decr_key)
    }
}
