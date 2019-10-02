package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.goto
import net.mamoe.mirai.utils.Tested
import java.io.DataInputStream

/**
 * 服务器进行加密后返回 privateKey
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
    lateinit var privateKey: ByteArray//16bytes

    @Tested
    override fun decode() {
        this.input.skip(5)
        privateKey = this.input.readNBytes(16)//22
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

        @Tested
        fun decrypt(privateKey: ByteArray): ServerLoginResponseKeyExchangePacket {
            return ServerLoginResponseKeyExchangePacket(this.decryptBy(TIMProtocol.shareKey, privateKey), flag).setId(this.idHex)
        }
    }
}
