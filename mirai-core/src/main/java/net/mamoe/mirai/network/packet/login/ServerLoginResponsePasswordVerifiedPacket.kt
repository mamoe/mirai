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
    lateinit var sessionResponseDecryptionKey: ByteArray//16 bytes|
    lateinit var nickname: String

    lateinit var token38: ByteArray
    lateinit var token88: ByteArray
    lateinit var encryptionKey: ByteArray


    @TestedSuccessfully
    @ExperimentalUnsignedTypes
    override fun decode() {
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

        this.sessionResponseDecryptionKey = this.input.readNBytesAt(171 + msgLength, 16)

        this.token88 = this.input.readNBytesAt(189 + msgLength, 136)

        val nickLength = this.input.goto(624 + msgLength).readByte().toInt()
        this.nickname = this.input.readVarString(nickLength)

        //this.age = this.input.goto(packetDataLength - 28).readShortAt()

        //this.gender = this.input.goto(packetDataLength - 32).readByteAt().toInt()
    }


    class Encrypted(input: DataInputStream) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
        fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseSuccessPacket {
            input goto 14
            return ServerLoginResponseSuccessPacket(TEA.decrypt(TEA.decrypt(input.readAllBytes().cutTail(1), Protocol.shareKey), tgtgtKey).dataInputStream()).setId(this.idHex)
        }
    }

}