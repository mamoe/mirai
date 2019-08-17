package net.mamoe.mirai.network.packet.client

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.hexToBytes
import java.io.IOException
import java.net.InetAddress

/**
 * @author Him188moe @ Mirai Project
 */
@ExperimentalUnsignedTypes
class Client0836_622Packet(private val qq: Int, private val seq: String, private val token0825: ByteArray) : ClientPacket() {
    @ExperimentalUnsignedTypes
    override fun encode() {
        val hostName: String = InetAddress.getLocalHost().hostName.let { it.substring(0, it.length - 3) };

        this.writeQQ(System.currentTimeMillis().toInt())//that's correct
        this.writeHex("01 12");//tag
        this.writeHex("00 38");//length
        this.write(token0825);//length
        this.writeHex("03 0F");//tag
        this.writeShort(hostName.length / 2);//todo check that
        this.writeShort(hostName.length);
        this.writeBytes(hostName)
        this.writeHex("00 05 00 06 00 02")
        this.writeQQ(qq)
        this.writeHex("00 06")//tag
        this.writeHex("00 78")//length


        //TEA 加密
        this.write(TEACryptor.encrypt(object : ClientPacket() {
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