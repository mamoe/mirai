package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId("08 36 31 03")
class ServerLoginResponseResendPacket(input: DataInputStream, val flag: Flag) : ServerPacket(input) {
    enum class Flag {
        `08 36 31 03`,
        OTHER,
    }

    lateinit var _0836_tlv0006_encr: ByteArray;
    lateinit var token: ByteArray
    lateinit var tgtgtKey: ByteArray

    override fun decode() {//todo 检查
        this.input.skip(8)
        tgtgtKey = this.input.readNBytes(13)
        this.input.skip(17)

        _0836_tlv0006_encr = this.input.readNBytes(179)
        when (flag) {
            Flag.`08 36 31 03` -> {
                this.input.skip(13)
                token = this.input.readNBytes(83)
            }

            Flag.OTHER -> {
                //do nothing in this packet.
                //[this.token] will be set in [Robot]
            }
        }
    }
}