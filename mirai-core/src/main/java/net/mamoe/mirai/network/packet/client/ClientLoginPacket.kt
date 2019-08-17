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
    var qq: Long = 0

    @Throws(IOException::class)
    override fun encode() {
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
                //this.writeIp(Protocol.SERVER_IP.get(2));
                this.writeIp("123456789")
                this.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
                this.writeHex(Protocol.publicKey)
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
}


//mirai: 02 37 13 08 25 31 01 00 00 00 00 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D 94 06 D9 3B 40 B5 E2 39 58 F5 E1 71 46 63 FF 6C CE 1E F1 BF CB F5 04 67 96 81 01 7C EF 47 10 15 45 8A 59 F7 B4 39 48 A3 E1 9C 74 3C DC 8E 7E 2F CF B6 C1 0C 2C C6 D6 7F DC 98 12 9C 88 35 29 33 C6 98 A9 81 C7 7B 2D 76 00 67 A1 DD 82 1E 12 04 DF DF 48 18 E0 C3 C8 54 B5 C2 16 A8 C4 CD BD 7D FD 5E 2A A9 74 68 82 44 F7 0D 7D 0E 6C 4F C8 05 03
//epl  : 02 37 13 08 25 31 02 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A8 F2 14 5F 58 12 60 AF 07 63 97 D6 76 B2 1A 3B D9 9E E9 58 5A E5 46 0D 40 D0 A5 A2 DF 48 8D 23 FB 25 C1 1A 4B D1 27 BA AB B2 69 AB DE 91 C0 63 65 2B 3A 0F 06 0C 3F EC 5C 48 A7 AE 25 06 3F 3C 7A A2 46 91 22 8E B2 A0 41 3F 5D C8 A5 C6 64 64 62 11 A1 9E 14 51 28 39 41 01 07 B5 8B 98 33 AB 50 AD 2F 05 8E F1 17 D7 1D 67 61 1B CD E9 B8 C6 A5 A7 F9 48 F7 BE 05 BC 03
