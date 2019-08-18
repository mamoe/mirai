package net.mamoe.mirai.network.packet.client.touch

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.ClientPacket
import net.mamoe.mirai.network.packet.client.writeHex
import net.mamoe.mirai.network.packet.client.writeQQ
import net.mamoe.mirai.network.packet.client.writeRandom
import net.mamoe.mirai.util.TEACryptor
import java.io.IOException

/**
 * @author Him188moe @ Mirai Project
 */
@ExperimentalUnsignedTypes
@PacketId("00 58")//todo check
class ClientHeartbeatPacket : ClientPacket() {
    var qq: Int = 0
    var sessionKey: ByteArray? = null//登录后获得

    @Throws(IOException::class)
    override fun encode() {
        this.writeRandom(2)
        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.write(TEACryptor.encrypt(byteArrayOf(0x00, 0x01, 0x00, 0x01), sessionKey))
    }
}