package net.mamoe.mirai.network.packet.client

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.TEAEncryption
import java.io.IOException

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId(0x00_58)
class ClientHeartbeatPacket : ClientPacket() {
    var qq: Int = 0
    var sessionKey: ByteArray? = null//登录后获得

    @Throws(IOException::class)
    override fun encode() {
        this.writeRandom(2)
        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.write(TEAEncryption.encrypt(byteArrayOf(0x00, 0x01, 0x00, 0x01), sessionKey))
    }
}