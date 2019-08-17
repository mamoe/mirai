package net.mamoe.mirai.network.packet.client

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.TEAEncryption
import net.mamoe.mirai.util.hexToBytes
import java.io.IOException

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId(0x08_25_31_02)
class Client0825ResponsePacket(private val serverIP: String, private val qq: Long) : ClientPacket() {
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol.redirectionKey)

        //TEA 加密
        this.write(TEAEncryption.encrypt(object : ClientPacket() {
            @Throws(IOException::class)
            override fun encode() {
                this.writeHex(Protocol._0825data0)
                this.writeHex(Protocol._0825data2)
                this.writeQQ(qq)
                this.writeHex("00 01 00 00 03 09 00 0C 00 01")
                this.writeIp(serverIP)
                this.writeHex("01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19")
                this.writeHex(Protocol.publicKey)
            }
        }.encodeToByteArray(), Protocol.redirectionKey.hexToBytes()))
    }
}