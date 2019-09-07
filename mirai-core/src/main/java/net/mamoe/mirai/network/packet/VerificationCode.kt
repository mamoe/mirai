package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.TestedSuccessfully
import net.mamoe.mirai.utils.getRandomByteArray
import net.mamoe.mirai.utils.hexToBytes
import java.io.DataInputStream

/**
 * 客户端请求验证码图片数据的第几部分
 */
@ExperimentalUnsignedTypes
@PacketId("00 BA 31")
class ClientVerificationCodeTransmissionRequestPacket(
        private val count: Int,
        private val qq: Long,
        private val token0825: ByteArray,
        private val verificationSequence: Int,
        private val token00BA: ByteArray
) : ClientPacket() {
    @TestedSuccessfully
    override fun encode() {
        this.writeByte(count)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer2)
        this.writeHex(Protocol.key00BA)
        this.encryptAndWrite(Protocol.key00BA) {
            it.writeHex("00 02 00 00 08 04 01 E0")
            it.writeHex(Protocol.constantData1)
            it.writeHex("00 00 38")
            it.write(token0825)
            it.writeHex("01 03 00 19")
            it.writeHex(Protocol.publicKey)
            it.writeHex("13 00 05 00 00 00 00")
            it.writeByte(verificationSequence)
            it.writeHex("00 28")
            it.write(token00BA)
            it.writeHex("00 10")
            it.writeHex(Protocol.key00BAFix)
        }
    }
}

/**
 * 服务器发送验证码图片文件一部分过来
 *
 * @author Him188moe
 */
@PacketId("00 BA 31")
class ServerVerificationCodeTransmissionPacket(input: DataInputStream, private val dataSize: Int, private val packetId: ByteArray) : ServerVerificationCodePacket(input) {

    lateinit var verificationCodePartN: ByteArray
    lateinit var verificationToken: ByteArray//56bytes
    var transmissionCompleted: Boolean = false//验证码是否已经传输完成
    lateinit var token00BA: ByteArray//40 bytes
    var count: Int = 0

    @ExperimentalUnsignedTypes
    override fun decode() {
        this.verificationToken = this.input.readNBytesAt(10, 56)

        val length = this.input.readShortAt(66)
        this.verificationCodePartN = this.input.readNBytes(length)

        this.input.skip(2)
        this.transmissionCompleted = this.input.readBoolean().not()

        this.token00BA = this.input.readNBytesAt(dataSize - 56, 40)
        this.count = packetId[3].toInt()
    }
}

/*
fun main() {
    val data = "13 00 05 01 00 00 01 23 00 38 66 6E 57 FB 30 91 BC 86 32 51 8A E2 2B 78 A1 60 9E 14 1F AD E5 C4 46 94 D0 DB 55 91 54 90 9A 24 38 63 61 9F A0 75 22 98 98 EF 72 D9 43 93 CC 28 63 14 25 3E 68 B0 1C 5B 02 BC AA E7 B5 A9 B6 96 8B 09 6B 2F 60 C3 86 2C 91 DE 75 88 D7 8D 53 68 E4 B5 0D D9 A5 42 7D 1A 3E 9F 0F DC 2F DE F9 79 FA F0 24 31 F2 30 DC 32 92 23 9B 11 F5 EB 1A 88 60 CE 6B 30 48 EA 55 D6 22 7A E4 2D 33 4A F3 9A D7 75 65 0C C5 E4 F8 01 E1 2B 53 0D 42 ED 66 87 19 66 65 C4 C9 30 2D B3 D8 4D 92 3D 4C B6 2D 68 05 08 B4 41 55 99 44 88 4C 6B 2A 2D CB 59 3A 81 7D 47 5B 39 98 C8 61 0F FC 0B 84 EB 09 A6 35 1A 86 A1 62 72 7E B1 48 0C F0 3E 1B 39 48 16 A6 9B 43 8F 38 B1 64 98 64 C1 32 59 61 47 7D CB DB 93 D4 D8 1E 97 CC 1C 38 A6 48 AF 97 0D C2 A3 C5 39 D2 13 76 60 96 18 0A 1C A7 E9 80 40 53 52 98 F8 C5 C2 1B 1D FB AA 42 72 24 36 B2 7A 60 B1 AE 72 4E 88 F7 F2 7B 06 BF B4 5D F2 1F C2 1B 6F 0D 14 82 9C 46 83 90 C2 09 A1 FA 1E D9 E2 AC 64 10 78 7C 0B F6 8A 25 A9 6B 9B 2F 5C C2 74 F3 61 01 7A 93 A9 30 E6 34 64 AD EE B6 2E 5E D8 7B B6 AD 1B D9 18 7F EE 10 86 D9 05 5F 26 54 E5 34 14 E6 33 9E 96 F8 E0 2C FD B9 2B D2 22 1A 84 00 4B EC 74 C1 67 74 48 D8 3D 6A 33 E7 E7 24 2F F4 5F 9B DC 76 6F DC 45 FF EA 11 F8 75 19 AC 28 AA 97 1D 42 F2 3D FE 89 77 14 93 52 17 92 C7 3B BA 01 49 A8 36 93 D8 56 AF 80 B0 F3 0D 0A 84 85 C9 F7 50 FC D2 2B D6 6D 9C 11 DF 63 D1 3F AF 0C C5 84 3A F0 33 8E C3 32 2D A9 A7 52 28 96 8E D2 A5 AA 42 42 AD C8 C2 DA 7A C5 38 D4 9E 82 23 3A 51 B3 07 55 62 D8 84 24 8B DE 2D 5D DE DF 89 15 F5 5D 17 92 E2 00 75 40 22 2C 09 3C A5 7D 85 83 EC 42 99 91 90 51 8D B5 CA 16 F2 4C AB 6C B9 D1 67 FA DC 42 77 B0 62 09 80 AA D5 03 42 D3 37 7D 8C 57 00 87 1B 7C 35 71 98 C6 05 E4 5C 49 D7 2E 02 00 52 AD 96 8A EA 27 23 01 40 F1 54 74 38 1A CE 31 BB 5A 4A B2 32 35 D7 6B 6A 20 F4 3D 3B D1 05 E5 4B 1F FF 93 15 FA 39 B0 CF A0 E9 01 02 B5 40 1E C0 D3 05 A6 40 D8 30 0C ED AE A0 99 54 66 99 D3 3C 9B 64 20 CA 2B 97 3E 50 76 A4 8F 69 02 02 41 D6 02 5F 26 D9 21 B4 A2 D7 48 C4 47 E7 16 6F 58 A8 D3 EC 56 60 02 80 3C 8C 41 B3 30 16 6A 37 05 34 55 A9 23 DE 27 A8 46 9D C1 C3 D2 6A C1 19 38 88 21 57 96 C9 4A 74 E9 88 84 90 E7 E3 B7 40 5F 16 0F 77 48 74 3B DD CC F2 13 93 AE 91 7A 35 62 20 38 AC 92 54 92 49 18 14 6D 9C 7D 99 E9 24 00 9F 4B 32 72 E2 A4 3D 84 A4 EC FC 68 E4 14 5E F2 F1 DA 64 B8 B8 09 AA 35 01 01 00 28 F7 92 C7 EF B1 C5 47 03 5A 45 25 07 95 7A DE 28 21 FA C0 F1 1A 11 71 C7 17 E0 D4 55 A7 30 B9 55 72 1A B4 C7 70 51 DE C2 00 10 E4 46 04 76 5D EB DF 86 1F 04 CB B4 3B B5 CD F8".hexToBytes()
    ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, "00 BA 31 01".hexToBytes()).let {
        it.decode()
        println(it)
    }

    ServerVerificationCodeTransmissionPacket{verificationCodePartN=AA E7 B5 A9 B6 96 8B 09 6B 2F 60 C3 86 2C 91 DE 75 88 D7 8D 53 68 E4 B5 0D D9 A5 42 7D 1A 3E 9F 0F DC 2F DE F9 79 FA F0 24 31 F2 30 DC 32 92 23 9B 11 F5 EB 1A 88 60 CE 6B 30 48 EA 55 D6 22 7A E4 2D 33 4A F3 9A D7 75 65 0C C5 E4 F8 01 E1 2B 53 0D 42 ED 66 87 19 66 65 C4 C9 30 2D B3 D8 4D 92 3D 4C B6 2D 68 05 08 B4 41 55 99 44 88 4C 6B 2A 2D CB 59 3A 81 7D 47 5B 39 98 C8 61 0F FC 0B 84 EB 09 A6 35 1A 86 A1 62 72 7E B1 48 0C F0 3E 1B 39 48 16 A6 9B 43 8F 38 B1 64 98 64 C1 32 59 61 47 7D CB DB 93 D4 D8 1E 97 CC 1C 38 A6 48 AF 97 0D C2 A3 C5 39 D2 13 76 60 96 18 0A 1C A7 E9 80 40 53 52 98 F8 C5 C2 1B 1D FB AA 42 72 24 36 B2 7A 60 B1 AE 72 4E 88 F7 F2 7B 06 BF B4 5D F2 1F C2 1B 6F 0D 14 82 9C 46 83 90 C2 09 A1 FA 1E D9 E2 AC 64 10 78 7C 0B F6 8A 25 A9 6B 9B 2F 5C C2 74 F3 61 01 7A 93 A9 30 E6 34 64 AD EE B6 2E 5E D8 7B B6 AD 1B D9 18 7F EE 10 86 D9 05 5F 26 54 E5 34 14 E6 33 9E 96 F8 E0 2C FD B9 2B D2 22 1A 84 00 4B EC 74 C1 67 74 48 D8 3D 6A 33 E7 E7 24 2F F4 5F 9B DC 76 6F DC 45 FF EA 11 F8 75 19 AC 28 AA 97 1D 42 F2 3D FE 89 77 14 93 52 17 92 C7 3B BA 01 49 A8 36 93 D8 56 AF 80 B0 F3 0D 0A 84 85 C9 F7 50 FC D2 2B D6 6D 9C 11 DF 63 D1 3F AF 0C C5 84 3A F0 33 8E C3 32 2D A9 A7 52 28 96 8E D2 A5 AA 42 42 AD C8 C2 DA 7A C5 38 D4 9E 82 23 3A 51 B3 07 55 62 D8 84 24 8B DE 2D 5D DE DF 89 15 F5 5D 17 92 E2 00 75 40 22 2C 09 3C A5 7D 85 83 EC 42 99 91 90 51 8D B5 CA 16 F2 4C AB 6C B9 D1 67 FA DC 42 77 B0 62 09 80 AA D5 03 42 D3 37 7D 8C 57 00 87 1B 7C 35 71 98 C6 05 E4 5C 49 D7 2E 02 00 52 AD 96 8A EA 27 23 01 40 F1 54 74 38 1A CE 31 BB 5A 4A B2 32 35 D7 6B 6A 20 F4 3D 3B D1 05 E5 4B 1F FF 93 15 FA 39 B0 CF A0 E9 01 02 B5 40 1E C0 D3 05 A6 40 D8 30 0C ED AE A0 99 54 66 99 D3 3C 9B 64 20 CA 2B 97 3E 50 76 A4 8F 69 02 02 41 D6 02 5F 26 D9 21 B4 A2 D7 48 C4 47 E7 16 6F 58 A8 D3 EC 56 60 02 80 3C 8C 41 B3 30 16 6A 37 05 34 55 A9 23 DE 27 A8 46 9D C1 C3 D2 6A C1 19 38 88 21 57 96 C9 4A 74 E9 88 84 90 E7 E3 B7 40 5F 16 0F 77 48 74 3B DD CC F2 13 93 AE 91 7A 35 62 20 38 AC 92 54 92 49 18 14 6D 9C 7D 99 E9 24 00 9F 4B 32 72 E2 A4 3D 84 A4 EC FC 68 E4 14 5E F2 F1 DA 64 B8 B8 09 AA 35, verificationToken=66 6E 57 FB 30 91 BC 86 32 51 8A E2 2B 78 A1 60 9E 14 1F AD E5 C4 46 94 D0 DB 55 91 54 90 9A 24 38 63 61 9F A0 75 22 98 98 EF 72 D9 43 93 CC 28 63 14 25 3E 68 B0 1C 5B, transmissionCompleted=true, token00BA=92 C7 EF B1 C5 47 03 5A 45 25 07 95 7A DE 28 21 FA C0 F1 1A 11 71 C7 17 E0 D4 55 A7 30 B9 55 72 1A B4 C7 70 51 DE C2 00, count=12545, dataSize=830, packetId=00 BA 31 01}

}*/

/**
 * 暂不了解意义
 *
 * @author Him188moe
 */
class ServerVerificationCodeRepeatPacket(input: DataInputStream) : ServerVerificationCodePacket(input) {

    lateinit var token00BA: ByteArray//56 bytes
    lateinit var tgtgtKeyUpdate: ByteArray

    @ExperimentalUnsignedTypes
    override fun decode() {
        token00BA = this.input.readNBytesAt(10, 56)
        tgtgtKeyUpdate = getRandomByteArray(16)
    }
}

abstract class ServerVerificationCodePacket(input: DataInputStream) : ServerPacket(input) {

    @PacketId("00 BA")
    class Encrypted(input: DataInputStream) : ServerPacket(input) {
        @ExperimentalUnsignedTypes
        fun decrypt(): ServerVerificationCodePacket {
            this.input goto 14
            val data = TEA.decrypt(this.input.readAllBytes().cutTail(1), Protocol.key00BA.hexToBytes())
            return if (data.size == 95) {
                ServerVerificationCodeRepeatPacket(data.dataInputStream())
            } else {
                ServerVerificationCodeTransmissionPacket(data.dataInputStream(), data.size, this.input.readNBytesAt(3, 4))
            }
        }
    }
}
