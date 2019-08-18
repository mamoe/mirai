package net.mamoe.mirai.network.packet.client.touch

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.ClientPacket
import net.mamoe.mirai.network.packet.client.writeHex
import net.mamoe.mirai.network.packet.client.writeIP
import net.mamoe.mirai.network.packet.client.writeQQ
import net.mamoe.mirai.util.ByteArrayDataOutputStream
import net.mamoe.mirai.util.TEACryptor
import net.mamoe.mirai.util.toHexString
import java.io.IOException

/**
 * The packet to touch server.
 *
 * @see net.mamoe.mirai.network.packet.server.ServerTouchResponsePacket
 *
 * @author Him188moe @ Mirai Project
 */
@ExperimentalUnsignedTypes
@PacketId("08 25 31 01")
class ClientTouchPacket : ClientPacket() {
    //已经完成测试
    var qq: Int = 0

    @ExperimentalUnsignedTypes
    @Throws(IOException::class)
    override fun encode() {
        //println(this.toUByteArray().toHexString(" "))
        //exitProcess(1)

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.writeHex(Protocol._0825key)


        //TEA 加密
        this.write(TEACryptor.CRYPTOR_0825KEY.encrypt(object : ByteArrayDataOutputStream() {
            @Throws(IOException::class)
            override fun toByteArray(): ByteArray {
                this.writeHex(Protocol._0825data0)
                this.writeHex(Protocol._0825data2)
                this.writeQQ(qq)
                this.writeHex("00 00 00 00 03 09 00 08 00 01")
                //this.writeIP("192.168.1.1");
                this.writeIP(Protocol.SERVER_IP[2]);
                //this.writeIP("123456789")
                this.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
                this.writeHex(Protocol.publicKey)
                println(this.toUByteArray().toHexString(" "))
                return super.toByteArray()
            }
        }.toByteArray()))
    }

}

@ExperimentalUnsignedTypes
fun main() {
    val pk = ClientTouchPacket()
    pk.qq = 1994701021
    pk.encode()
    pk.writeHex(Protocol.tail)
    //println("pk.toByteArray() = " + pk.toUByteArray().contentToString())
    println(pk.toUByteArray().toHexString(" "))

    /*
    println(object : ByteArrayDataOutputStream() {
        @Throws(IOException::class)
        override fun toUByteArray(): UByteArray {
            this.writeInt(1994701021)
            return super.toUByteArray()
        }
    }.toUByteArray().toHexString())*/


/*
    println(object : ByteArrayDataOutputStream() {
        @Throws(IOException::class)
        override fun toUByteArray(): UByteArray {
            //this.writeIP("192.168.1.1")
            this.writeHex(Protocol._0825data0)
            this.writeHex(Protocol._0825data2)
            this.writeQQ(1994701021)
            this.writeHex("00 00 00 00 03 09 00 08 00 01")
            //this.writeIP(Protocol.SERVER_IP.get(2));
            this.writeIP("192.168.1.1")
            this.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
            this.writeHex(Protocol.publicKey)
            return super.toUByteArray()
        }
    }.toUByteArray().toHexString(" "))
*/

}


//
//mirai: 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 03 09 00 08 00 01 C0 A8 01 01 00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3
//epl  : 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 03 09 00 08 00 01 C0 A8 01 01 00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3

//encryption data
//mirai: 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 03 09 00 08 00 01 C0 A8 01 01 00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3
//epl  : 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 03 09 00 08 00 01 C0 A8 01 01 00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3

//mirai: 02 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D C3 47 F0 25 A1 8E 74 EF 1E 0B 32 5B 20 8A FA 3B 0B 52 8F 86 E6 04 F1 D6 F8 63 75 60 8C 0C 7D 06 D1 E0 22 F8 49 EF AF 61 EE 7E 69 72 EB 10 08 30 69 50 1C 84 A9 C2 16 D7 52 B9 1C 79 CA 5A CF FD BC AE D8 A6 BB DC 21 6E 79 26 E1 A2 23 11 AA B0 9A 49 39 72 ED 61 12 B6 88 4D A2 56 23 E9 92 11 92 27 4A 70 00 C9 01 7B 03
//epl  : 02 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D F0 1B 0A 8D 98 99 A9 78 B3 82 69 91 B1 8C FD 64 AC C1 DF B5 A1 A6 4C AC B6 CC A3 B2 11 51 15 00 A4 01 75 7C 61 83 C1 89 3E 93 42 A1 AF D4 1B B3 81 4E 52 67 C1 15 42 5D 28 00 3D 1E 40 28 B1 C9 CE 08 15 F3 2B B5 5A 88 59 4E F4 9A 15 CB 77 BE 56 86 16 CD 4F CD F6 14 D2 A6 B0 7B F1 22 B9 DD 64 98 5C 93 AE 6F 6C 43 03