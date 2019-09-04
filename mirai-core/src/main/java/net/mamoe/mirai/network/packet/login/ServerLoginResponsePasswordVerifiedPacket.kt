package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.TestedSuccessfully
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author NaturalHG
 */
class ServerLoginResponseSuccessPacket(input: DataInputStream) : ServerPacket(input) {
    lateinit var _0828_rec_decr_key: ByteArray//16 bytes|
    lateinit var nick: String

    lateinit var token38: ByteArray
    lateinit var token88: ByteArray
    lateinit var encryptionKey: ByteArray


    @TestedSuccessfully
    @ExperimentalUnsignedTypes
    override fun decode() {
        //测试完成 @NaturalHG
        /**
         * Version 1  @Deprecated
        this.input.skip(7)//8

        encryptionKey = this.input.readNBytesAt(16)//24

        this.input.skip(2)//25->26

        token38 = this.input.readNBytesAt(56)//81->82

        this.input.skip(60L)//142

        //??
        var b = this.input.readNBytesAt(2)
        val msgLength = when (b.toUByteArray().toUHexString()) {
        "01 07" -> 0
        "00 33" -> 28
        "01 10" -> 65
        else -> throw IllegalStateException()
        }//144


        System.out.println(msgLength)

        this.input.skip(17L +  msgLength)//161+msgLength

        this.input.skip(10)//171+msgLength

        _0828_rec_decr_key = this.input.readNBytesAt(16)//187+msgLength


        this.input.skip(2)

        token88 = this.input.readNBytesAt(136)//325+ // msgLength

        this.input.skip(299L)//624+msgLength

        //varString (nickLength bytes)
        val nickLength = this.input.readByteAt().toInt()//625+msgLength

        System.out.println(nickLength)

        nick = this.input.readVarString(nickLength)//625+msgLength+nickLength

        val dataIndex = packetDataLength - 31

        /*
        this.input.skip((dataIndex - (625 + msgLength + nickLength)) + 0L)//-31

        gender = this.input.readByteAt().toUByte().toInt()//-30

        this.input.skip(9)//-27

        age = this.input.readShortAt()//-25
        */
        age = 0
        gender = 0

        /*
        age = HexToDec(取文本中间(data, 取文本长度(data) - 82, 5))
        gender = 取文本中间(data, 取文本长度(data) - 94, 2)
        */
         * **/
        /** version 2 */

        this.input.skip(7)//8
        this.encryptionKey = this.input.readNBytes(16)//24

        this.input.skip(2)//26
        this.token38 = this.input.readNBytes(56)//82

        this.input.skip(60L)//142
        val msgLength = when (val id = this.input.readNBytes(2).toUByteArray().toUHexString()) {
            "01 07" -> 0
            "00 33" -> 28
            "01 10" -> 64
            else -> throw IllegalStateException(id)
        }

        this._0828_rec_decr_key = this.input.readNBytesAt(171 + msgLength, 16)

        this.token88 = this.input.readNBytesAt(189 + msgLength, 136)

        val nickLength = this.input.goto(624 + msgLength).readByte().toInt()
        this.nick = this.input.readVarString(nickLength)

        //this.age = this.input.goto(packetDataLength - 28).readShortAt()

        //this.gender = this.input.goto(packetDataLength - 32).readByteAt().toInt()
    }


    class Encrypted(input: DataInputStream) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
        fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseSuccessPacket {
            input goto 14
            return ServerLoginResponseSuccessPacket(TEA.decrypt(TEA.decrypt(input.readAllBytes().cutTail(1), Protocol.shareKey), tgtgtKey).dataInputStream());
        }
    }

}