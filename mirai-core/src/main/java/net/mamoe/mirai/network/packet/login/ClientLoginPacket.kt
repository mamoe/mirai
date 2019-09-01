package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.util.TestedSuccessfully
import net.mamoe.mirai.utils.ByteArrayDataOutputStream
import net.mamoe.mirai.utils.TEACryptor
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
import java.io.DataOutputStream

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

@PacketId("08 36 31 04")
@ExperimentalUnsignedTypes
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


