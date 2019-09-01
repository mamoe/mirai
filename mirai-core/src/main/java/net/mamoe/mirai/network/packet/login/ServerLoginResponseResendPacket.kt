package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.dataInputStream
import net.mamoe.mirai.network.packet.goto
import net.mamoe.mirai.util.TestedSuccessfully
import net.mamoe.mirai.utils.TEACryptor
import net.mamoe.mirai.utils.hexToUBytes
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author NaturalHG
 */
@PacketId("08 36 31 03")
class ServerLoginResponseResendPacket(input: DataInputStream, val flag: Flag) : ServerPacket(input) {
    enum class Flag {
        `08 36 31 03`,
        OTHER,
    }

    lateinit var _0836_tlv0006_encr: ByteArray;//120bytes
    var tokenUnknown: ByteArray? = null
    lateinit var tgtgtKey: ByteArray//16bytes

    @TestedSuccessfully
    override fun decode() {
        this.input.skip(5)
        tgtgtKey = this.input.readNBytes(16)//22
        //this.input.skip(2)//25
        this.input.goto(25)
        _0836_tlv0006_encr = this.input.readNBytes(120)

        when (flag) {
            Flag.`08 36 31 03` -> {
                tokenUnknown = this.input.goto(153).readNBytes(56)
                //println(tokenUnknown!!.toUHexString())
            }

            Flag.OTHER -> {
                //do nothing in this packet.
                //[this.token] will be set in [RobotNetworkHandler]
                //token
            }
        }
    }
}

class ServerLoginResponseResendPacketEncrypted(input: DataInputStream, private val flag: ServerLoginResponseResendPacket.Flag) : ServerPacket(input) {
    override fun decode() {

    }

    @TestedSuccessfully
    fun decrypt(tgtgtKey: ByteArray): ServerLoginResponseResendPacket {
        //this.input.skip(7)
        this.input goto 14
        var data: ByteArray = this.input.readAllBytes()
        data = TEACryptor.CRYPTOR_SHARE_KEY.decrypt(data.let { it.copyOfRange(0, it.size - 1) });
        data = TEACryptor.decrypt(data, tgtgtKey)
        return ServerLoginResponseResendPacket(data.dataInputStream(), flag)
    }
}

fun main() {
    val tgtgtkey = "9E 83 61 FF 18 61 4B 77 34 FE 1C 9C E2 03 B4 F2".hexToUBytes()

    ServerLoginResponseResendPacketEncrypted("02 37 13 08 36 31 03 76 E4 B8 DD 00 00 00 94 9B 87 00 87 7F 9E D0 E5 6A F6 17 41 02 0C AA F3 AC C8 CF 4E C6 9D EC FA 6C BD F8 7C 4B A5 28 80 CC DE B5 0A 41 8E 63 CE 5E 30 D8 A6 83 92 0E 2E 5C 35 E5 6E 62 3D FE 17 DD 7C 47 9A AD EF F0 F7 2A 6F 21 32 99 1B 6D E1 DA BE 68 2F 26 A9 93 DE 1B 4F 11 F0 AF A1 06 7B 85 53 46 D2 A3 DD A6 BE F2 76 8A 61 BF 15 FD 17 C4 45 DB EC 05 51 56 46 63 48 87 49 79 0D 40 DF 9D D9 99 93 EC D0 44 7B 4A 79 EB BD 08 10 18 29 0E 85 EE 26 A0 CD 40 00 2F 3E ED F4 A4 C3 01 5E 82 F5 A8 02 FA 70 EB F2 07 AD FF 0E DA 08 7A 3A FE B6 F4 5D 98 18 F7 58 C2 19 21 AF 29 D2 95 16 CE C4 A3 5F B0 E6 23 C2 B2 C6 5F 03 42 C2 44 C2 B0 A0 3F 95 8E 89 EF FC EC E4 BF 03 CB DA 9C D3 84 3F 9B A0 F1 B4 14 6E 23 D5 74 79 6F 89 DA B8 33 DB EF 0B 21 E1 27 27 57 8B 56 CB D9 BF C2 A8 25 6E 48 23 EB 31 9D 03".hexToUBytes().toByteArray().dataInputStream(), ServerLoginResponseResendPacket.Flag.`08 36 31 03`).decrypt(tgtgtkey.toByteArray()).let { it.decode();println(it._0836_tlv0006_encr.toUHexString()) }

    val data = "94 9B 87 00 87 7F 9E D0 E5 6A F6 17 41 02 0C AA F3 AC C8 CF 4E C6 9D EC FA 6C BD F8 7C 4B A5 28 80 CC DE B5 0A 41 8E 63 CE 5E 30 D8 A6 83 92 0E 2E 5C 35 E5 6E 62 3D FE 17 DD 7C 47 9A AD EF F0 F7 2A 6F 21 32 99 1B 6D E1 DA BE 68 2F 26 A9 93 DE 1B 4F 11 F0 AF A1 06 7B 85 53 46 D2 A3 DD A6 BE F2 76 8A 61 BF 15 FD 17 C4 45 DB EC 05 51 56 46 63 48 87 49 79 0D 40 DF 9D D9 99 93 EC D0 44 7B 4A 79 EB BD 08 10 18 29 0E 85 EE 26 A0 CD 40 00 2F 3E ED F4 A4 C3 01 5E 82 F5 A8 02 FA 70 EB F2 07 AD FF 0E DA 08 7A 3A FE B6 F4 5D 98 18 F7 58 C2 19 21 AF 29 D2 95 16 CE C4 A3 5F B0 E6 23 C2 B2 C6 5F 03 42 C2 44 C2 B0 A0 3F 95 8E 89 EF FC EC E4 BF 03 CB DA 9C D3 84 3F 9B A0 F1 B4 14 6E 23 D5 74 79 6F 89 DA B8 33 DB EF 0B 21 E1 27 27 57 8B 56 CB D9 BF C2 A8 25 6E 48 23 EB 31 9D".hexToUBytes()

    val d1 = TEACryptor.CRYPTOR_SHARE_KEY.decrypt(data.toByteArray())

    ServerLoginResponseResendPacket(TEACryptor.decrypt(d1, tgtgtkey.toByteArray()).dataInputStream(), ServerLoginResponseResendPacket.Flag.`08 36 31 03`).let { it.decode();println(it._0836_tlv0006_encr.toUHexString()) }

}