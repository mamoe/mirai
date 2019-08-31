package net.mamoe.mirai.network.packet.client.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.*
import net.mamoe.mirai.util.*
import java.io.DataOutputStream
import java.net.InetAddress
import kotlin.system.exitProcess

/**
 * Password submission (0836_622)
 *
 * @author Him188moe
 */
@PacketId("08 36 31 03")
@ExperimentalUnsignedTypes
@TestedSuccessfully
class ClientPasswordSubmissionPacket(
        private val qq: Int,
        private val password: String,
        private val loginTime: Int,
        private val loginIP: String,
        private val tgtgtKey: ByteArray,
        private val token0825: ByteArray
) : ClientPacket() {
    @ExperimentalUnsignedTypes
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(Protocol._0836_622_fix1)
        this.writeHex(Protocol.publicKey)
        this.writeHex("00 00 00 10")
        this.writeHex(Protocol._0836key1)

        this.encryptAndWrite(Protocol.shareKey.hexToBytes()) {
            it.writePart1(qq, password, loginTime, loginIP, tgtgtKey, token0825)
            it.writePart2()
            println(it.toByteArray().toUHexString())
        }
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun main() {
    println(InetAddress.getLocalHost().hostName)
    exitProcess(0)

    val loginTime = "5D 60 F6 33".hexToInt()
    println(loginTime)

    val loginIP = "AB 70 E2 96".let { it.split(" ").map { it.hexToByte() }.joinToString(".") { it.toString() } }


    val tgtgtKey = "68 25 55 61 52 66 4A 54 71 6A 71 5A 24 50 27 6D".hexToBytes()

    val token0825 = "56 3A E4 8B B4 64 D2 72 60 FE 01 54 FC B1 5F 88 E0 BA 64 1A 55 F2 84 FC 97 D0 BF 5F 47 A8 D9 76 BB FB 4A 7A F3 5E 0E A4 8E CA 8F 27 C2 02 6E 5D E7 68 9F 7C CF 91 83 F4".hexToBytes()
    val token00ba = "57 3A 37 C3 FB A0 C3 E5 AE F3 0E B6 03 DE BF 9E E2 B5 C5 FE A0 F0 03 4F F7 8A 5C 29 5C E0 5A A2 89 D5 3F 60 E2 B2 81 FE D4 16 04 D4 E3 C6 4A D7 A9 D9 E6 FC 2E 7E 0C F3".hexToBytes()
    val tlv_0006_encr = "0D DF 92 9C 5A 08 D1 67 FD 7D D6 DE CE D0 92 39 79 17 53 57 41 9B D6 D3 F9 F8 9A 3B E1 C2 3A E7 CF 02 6E 5E 36 B7 6D CF 33 66 77 FE AC 58 93 A3 85 E7 AF 6F 2D A2 74 E2 60 28 4B 29 17 04 79 95 39 D4 BF 4D C1 ED 61 49 13 23 9D 71 62 29 AF 87 D7 E3 42 49 88 3F D8 5C DB 9F 9E 5A 2A EA 02 F6 4F 2B D3 5B AF BE 0C B2 54 46 AE 99 1B 07 0B BE 6A C2 29 18 25 6A 95 0A".hexToBytes()


    ClientLoginResendPacket3104(
            1994701021,
            "xiaoqqq",
            loginTime,
            loginIP,
            tgtgtKey,
            token0825,
            token00ba,
            tlv_0006_encr
    ).let { it.encode();println(it.toUByteArray().toUHexString()) }
}

@PacketId("08 36 31 04")
@ExperimentalUnsignedTypes//todo 测试出来这个包长度有问题
class ClientLoginResendPacket3104(qq: Int, password: String, loginTime: Int, loginIP: String, tgtgtKey: ByteArray, token0825: ByteArray, token00BA: ByteArray, tlv_0006_encr: ByteArray? = null)
    : ClientLoginResendPacket(qq, password, loginTime, loginIP, tgtgtKey, token0825, token00BA, tlv_0006_encr)

@PacketId("08 36 31 06")
@ExperimentalUnsignedTypes
class ClientLoginResendPacket3106(qq: Int, password: String, loginTime: Int, loginIP: String, tgtgtKey: ByteArray, token0825: ByteArray, token00BA: ByteArray, tlv_0006_encr: ByteArray? = null)
    : ClientLoginResendPacket(qq, password, loginTime, loginIP, tgtgtKey, token0825, token00BA, tlv_0006_encr)

@ExperimentalUnsignedTypes
open class ClientLoginResendPacket internal constructor(
        val qq: Int,
        val password: String,
        val loginTime: Int,
        val loginIP: String,
        val tgtgtKey: ByteArray,
        val token0825: ByteArray,
        val token00BA: ByteArray,
        val tlv_0006_encr: ByteArray? = null
) : ClientPacket() {
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(Protocol._0836_622_fix1)
        this.writeHex(Protocol.publicKey)
        this.writeHex("00 00 00 10")
        this.writeHex(Protocol._0836key1)

        this.write(TEACryptor.encrypt(object : ByteArrayDataOutputStream() {
            override fun toByteArray(): ByteArray {
                this.writePart1(qq, password, loginTime, loginIP, tgtgtKey, token0825, tlv_0006_encr)

                this.writeHex("01 10") //tag
                this.writeHex("00 3C")//length
                this.writeHex("00 01")//tag
                this.writeHex("00 38")//length
                this.write(token00BA)//value

                this.writePart2()
                return super.toByteArray()
            }
        }.toByteArray(), Protocol.shareKey.hexToBytes()))
    }
}


@ExperimentalUnsignedTypes
@PacketId("08 28 04 34")
class ClientSessionRequestPacket(
        private val qq: Int,
        private val serverIp: String,
        private val loginIP: String,
        private val md5_32: ByteArray,
        private val token38: ByteArray,
        private val token88: ByteArray,
        private val encryptionKey: ByteArray,
        private val tlv0105: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex("02 00 00 00 01 2E 01 00 00 68 52 00 30 00 3A")
        this.writeHex("00 38")
        this.write(token38)
        this.write(TEACryptor.encrypt(object : ByteArrayDataOutputStream() {
            override fun toByteArray(): ByteArray {
                this.writeHex("00 07 00 88")
                this.write(token88)
                this.writeHex("00 0C 00 16 00 02 00 00 00 00 00 00 00 00 00 00")
                this.writeIP(serverIp)
                this.writeHex("1F 40 00 00 00 00 00 15 00 30 00 01")//fix1
                this.writeHex("01 92 A5 D2 59 00 10 54 2D CF 9B 60 BF BB EC 0D D4 81 CE 36 87 DE 35 02 AE 6D ED DC 00 10 ")
                this.writeHex(Protocol._0836fix)
                this.writeHex("00 36 00 12 00 02 00 01 00 00 00 05 00 00 00 00 00 00 00 00 00 00")
                this.writeHex(Protocol._0825data0)
                this.writeHex(Protocol._0825data2)
                this.writeQQ(qq)
                this.writeHex("00 00 00 00 00 1F 00 22 00 01")
                this.writeHex("1A 68 73 66 E4 BA 79 92 CC C2 D4 EC 14 7C 8B AF 43 B0 62 FB 65 58 A9 EB 37 55 1D 26 13 A8 E5 3D")//device ID
                this.write(tlv0105)
                this.writeHex("01 0B 00 85 00 02")
                this.writeHex("B9 ED EF D7 CD E5 47 96 7A B5 28 34 CA 93 6B 5C")//fix2
                this.write(getRandomKey(1))
                this.writeHex("10 00 00 00 00 00 00 00 02")

                //fix3
                this.writeHex("00 63 3E 00 63 02 04 03 06 02 00 04 00 52 D9 00 00 00 00 A9 58 3E 6D 6D 49 AA F6 A6 D9 33 0A E7 7E 36 84 03 01 00 00 68 20 15 8B 00 00 01 02 00 00 03 00 07 DF 00 0A 00 0C 00 01 00 04 00 03 00 04 20 5C 00")
                this.write(md5_32)
                this.writeHex("68")

                this.writeHex("00 00 00 00 00 2D 00 06 00 01")
                this.writeIP(InetAddress.getLocalHost().hostAddress)

                return super.toByteArray()
            }
        }.toByteArray(), encryptionKey))
    }
}

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
private fun DataOutputStream.writePart1(qq: Int, password: String, loginTime: Int, loginIP: String, tgtgtKey: ByteArray, token0825: ByteArray, tlv_0006_encr: ByteArray? = null) {

    //this.writeInt(System.currentTimeMillis().toInt())
    this.writeHex("01 12")//tag
    this.writeHex("00 38")//length
    this.write(token0825)//length
    this.writeHex("03 0F")//tag
    this.writeDeviceName()

    this.writeHex("00 05 00 06 00 02")
    this.writeQQ(qq)
    this.writeHex("00 06")//tag
    this.writeHex("00 78")//length
    if (tlv_0006_encr != null) {
        this.write(tlv_0006_encr)
    } else {
        this.writeTLV0006(qq, password, loginTime, loginIP, tgtgtKey)
    }
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
}

@ExperimentalUnsignedTypes
private fun DataOutputStream.writePart2() {

    this.writeHex("03 12")//tag
    this.writeHex("00 05")//length
    this.writeHex("01 00 00 00 01")//value

    this.writeHex("05 08")//tag
    this.writeHex("00 05")//length
    this.writeHex("01 00 00 00 00")//value

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
    writeCRC32()

}


