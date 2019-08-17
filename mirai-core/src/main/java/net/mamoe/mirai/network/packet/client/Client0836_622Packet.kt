package net.mamoe.mirai.network.packet.client

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.getCrc32
import net.mamoe.mirai.util.getRandomKey
import net.mamoe.mirai.util.hexToBytes
import java.io.IOException
import java.net.InetAddress

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId("08 36")
@ExperimentalUnsignedTypes
class Client0836_622Packet(private val qq: Int, private val password: String, private val loginTime: ByteArray, private val loginIP: ByteArray, private val tgtgtKey: ByteArray, private val seq: String, private val token0825: ByteArray) : ClientPacket() {
    @ExperimentalUnsignedTypes
    override fun encode() {
        this.writeHex(seq)
        this.writeQQ(qq)
        this.writeHex(Protocol._0836_622_fix1)
        this.writeHex(Protocol.publicKey)
        this.writeHex("00 00 00 10")
        this.writeHex(Protocol._0836key1)

        //TEA 加密
        this.write(TEACryptor.encrypt(object : ClientPacket() {
            @Throws(IOException::class)
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
                this.writeTLV0006(qq, password, loginTime, loginIP, tgtgtKey)
                //fix
                this.writeHex(Protocol._0836_622_fix2)
                this.writeHex("00 1A")//tag
                this.writeHex("00 40")//length
                this.write(TEACryptor.encrypt(Protocol._0836_622_fix2.hexToBytes(), tgtgtKey))
                this.writeHex(Protocol._0825data0)
                this.writeHex(Protocol._0825data2)
                this.writeQQ(qq)
                this.writeZero(4)

                this.writeHex("01 03")//tag
                this.writeHex("00 14")//length

                this.writeHex("00 01")//tag
                this.writeHex("00 10")//length
                this.writeHex("60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6")//key

                this.writeHex("03 12")//tag
                this.writeHex("00 05")//length
                this.writeHex("01 00 00 00 01")//value

                this.writeHex("05 08")//tag
                this.writeHex("00 05")//length
                this.writeHex("10 00 00 00 00")//value

                this.writeHex("03 13")//tag
                this.writeHex("00 19")//length
                this.writeHex("01")//value

                this.writeHex("01 02")//tag
                this.writeHex("00 10")//length
                this.writeHex("04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA")//key
                this.writeZero(3)
                this.writeByte(0)//maybe 00, 0F, 1F

                this.writeHex("01 02")//tag
                this.writeHex("00 62")//length
                this.writeHex("00 01")//word?
                this.writeHex("04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48")//key
                this.writeHex("00 38")//length
                //value
                this.writeHex("E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3")

                this.writeHex("00 14")//length

                getRandomKey(16).let {
                    write(it)//key
                    writeLong(getCrc32(it))//todo may be int? check that.
                }
            }
        }.encodeToByteArray(), Protocol.shareKey.hexToBytes()))
    }


}