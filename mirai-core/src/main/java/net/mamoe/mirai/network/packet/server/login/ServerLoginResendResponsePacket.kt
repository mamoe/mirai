package net.mamoe.mirai.network.packet.server.login

import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId("08 36 31 03")
class ServerLoginResendResponsePacket(input: DataInputStream, private val flag: Flag) : ServerPacket(input) {
    enum class Flag {
        `08 36 31 03`,
        OTHER,
    }

    lateinit var _0836_tlv0006_encr: ByteArray;
    lateinit var token: ByteArray
    lateinit var tgtgtKey: ByteArray

    override fun decode() {
        _0836_tlv0006_encr = 取文本中间(data, 76, 359)
        when (flag) {
            Flag.`08 36 31 03` -> {
                token = 取文本中间(data, 460, 167)
            }

            Flag.OTHER -> {
                //do nothing in this packet.
                //[this.token] will be set in [Robot]
            }
        }
        m_tgtgtKey = 取文本中间(data, 16, 47)
    }
}