package net.mamoe.mirai.network.packet.client

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.ByteArrayDataOutputStream
import net.mamoe.mirai.util.TEAEncryption
import net.mamoe.mirai.util.toHexString
import java.io.IOException

/**
 * @author Him188moe @ Mirai Project
 */
@PacketId(0x08_25_31_01)
class ClientLoginPacket : ClientPacket() {
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
        this.write(TEAEncryption.encrypt(object : ByteArrayDataOutputStream() {
            @Throws(IOException::class)
            override fun toByteArray(): ByteArray {
                this.writeHex(Protocol._0825data0)
                this.writeHex(Protocol._0825data2)
                this.writeQQ(qq)
                this.writeHex("00 00 00 00 03 09 00 08 00 01")
                this.writeIp("192.168.1.1");
                //this.writeIp(Protocol.SERVER_IP[2]);
                //this.writeIp("123456789")
                this.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
                this.writeHex(Protocol.publicKey)
                println(this.toUByteArray().toHexString(" "))
                return super.toByteArray()
            }
        }.toByteArray(), Protocol.hexToBytes(Protocol._0825key)))
    }

}

@ExperimentalUnsignedTypes
fun main() {
    val pk = ClientLoginPacket()
    pk.qq = 1994701021
    pk.encode()
    pk.writeHex(Protocol.tail)
    println("pk.toByteArray() = " + pk.toUByteArray().contentToString())
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
            //this.writeIp("192.168.1.1")
            this.writeHex(Protocol._0825data0)
            this.writeHex(Protocol._0825data2)
            this.writeQQ(1994701021)
            this.writeHex("00 00 00 00 03 09 00 08 00 01")
            //this.writeIp(Protocol.SERVER_IP.get(2));
            this.writeIp("192.168.1.1")
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

//whole package
//mirai: 02 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D 91 DE 6C 86 08 E0 5C ED C7 78 97 E4 A2 3E A6 F9 59 89 91 0A A3 5B 17 35 8B 1A 1F 6A 6C EA 26 C0 CE A1 FE 70 47 A2 FC 6C FA 17 0B 0E 64 A3 8E 59 21 AA E3 EF 5E 3D 4A C3 03 2B 66 FB 44 B8 C4 3F AC BB 6E 12 D0 0D 55 CD 1D 9E 96 6C B5 AE 46 DC 28 B6 81 30 04 10 B0 A4 04 7C 51 E8 EF FE F5 D3 19 9C 77 F6 FD 7B A8 02 03
//epl  : 02 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D E6 3D D4 31 C7 80 74 0A EE 0F 6C 8F 4B 13 77 40 CE C9 C3 96 AB 05 13 3F C7 D0 1C 11 18 A9 03 32 FF 1F EB D9 6A 00 4E D4 AC 86 03 A4 30 2F 62 A0 77 6D 47 F3 4F EF AB 01 80 3D EB 47 65 8A A4 DB 63 8E 38 5A 4B 59 D0 D8 AF 42 6C 6D B3 F7 5B A4 2A 42 FD CA 8C 11 85 92 4A 0F 28 FB F3 3C A1 50 79 66 C4 21 09 E0 51 9E 03