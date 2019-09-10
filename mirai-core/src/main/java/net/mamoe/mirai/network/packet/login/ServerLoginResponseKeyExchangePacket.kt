package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.goto
import net.mamoe.mirai.utils.TestedSuccessfully
import java.io.DataInputStream

/**
 * 服务器进行加密后返回 tgtgtKey
 *
 * @author NaturalHG
 */
@PacketId("08 36 31 03")
class ServerLoginResponseKeyExchangePacket(input: DataInputStream, val flag: Flag) : ServerPacket(input) {
    enum class Flag {
        `08 36 31 03`,
        OTHER,
    }

    lateinit var tlv0006: ByteArray;//120bytes
    var tokenUnknown: ByteArray? = null
    lateinit var tgtgtKey: ByteArray//16bytes

    @TestedSuccessfully
    override fun decode() {
        this.input.skip(5)
        tgtgtKey = this.input.readNBytes(16)//22
        //this.input.skip(2)//25
        this.input.goto(25)
        tlv0006 = this.input.readNBytes(120)

        when (flag) {
            Flag.`08 36 31 03` -> {
                tokenUnknown = this.input.goto(153).readNBytes(56)
                //println(tokenUnknown!!.toUHexString())
            }

            Flag.OTHER -> {
                //do nothing in this packet.
                //[this.token] will be set in [BotNetworkHandler]
                //token
            }
        }
    }

    class Encrypted(input: DataInputStream, private val flag: Flag) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
        @TestedSuccessfully
        fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseKeyExchangePacket {
            return ServerLoginResponseKeyExchangePacket(this.decryptBy(Protocol.shareKey, tgtgtKey), flag).setId(this.idHex)
        }
    }
}
