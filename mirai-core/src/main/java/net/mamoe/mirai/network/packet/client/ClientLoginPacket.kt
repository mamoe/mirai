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


//mirai: 02 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D 64 CB 62 9D 7D FE 2A 20 B6 14 B8 25 68 8A 85 D6 34 12 A6 5F C5 7D 94 EE 6F 47 CB E2 45 BF 46 7B 90 A2 F7 C0 E7 9A 73 FF 03 51 B5 2C C2 1A 66 A6 A1 DE 2D FE E2 6F 68 58 F0 C1 92 AF 00 51 60 9D 32 17 73 3E 94 EE 6C F8 CB FF 46 66 E6 9D 8D 51 8D B3 44 EE 52 5F 67 6C 23 EE 0F 04 9C 13 E5 A4 82 DE E7 80 7B 16 4B 9C 03
//epl  : 02 37 13 08 25 31 01 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D 37 B1 B1 62 C2 09 06 C5 FA 7B 4A 1A 77 DA BE 69 FE 03 61 3B 20 3E 99 72 33 65 D1 FA 16 03 33 DC E0 91 43 10 6D DE B7 E1 6C F8 21 A6 FA F4 A7 16 7A 7C 78 2F C1 7C 1A 1F 2A 38 68 AF 61 CE F4 0A A5 E8 BC AA 8E 4E AC FA 31 8C 70 33 DD DC FD FC 72 69 B8 FB 80 29 05 F4 61 97 E9 AD DB 89 51 D2 B1 44 A3 B2 E2 B8 89 63 03