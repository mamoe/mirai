@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.*

fun main() {
    "1A".hexToBytes().read {
        println(readUnsignedVarInt())
    }
}

/**
 * 获取 Image Id 和上传用的一个 uKey
 */
@PacketId(0x0388u)
class ClientGroupImageIdRequestPacket(
        private val bot: UInt,
        private val group: UInt,
        private val image: PlatformImage,
        private val imageData: ByteArray,
        private val sessionKey: ByteArray
) : ClientPacket() {

    @PacketVersion(date = "2019.10.20", timVersion = "2.3.2.21173")
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        //未知图片A
        // 00 00 00 07 00 00 00
        // 53 08 =后文长度-6
        // 01 12 03 98 01 02 10 02 22 4F 08 F3 DB F3 E3 01 10 A2 FF 8C F0 03 18 B1 C7 B1 BB 0A 22 10 77 FB 3D 6F 97 BD 7B F0 C4 1F DC 60 1F 22 D2 7C 28 04 30 02 38 20 40 FF 01 48 00 50 01 5A 05 32 36 39 33 33 60 00 68 00 70 00 78 00 80 01 A4 05 88 01 D8 03 90 01 EB 07 A0 01 01

        //小图B
        // 00 00 00 07 00 00 00
        // 5B 08 =后文长度-6
        // 01 12 03 98 01 01 10 01 1A
        // 57长度
        // 08 FB D2 D8 94 02
        // 10 A2 FF 8C F0 03
        // 18 00
        // 22 [10] 7A A4 B3 AA 8C 3C 0F 45 2D 9B 7F 30 2A 0A CE AA
        // 28 F3 06//size
        // 32 1A
        // 29 00
        // 37 00
        // 42 00
        // 53 00
        // 4B 00
        // 48 00
        // 32 00
        // 44 00
        // 35 00
        // 54 00
        // 51 00
        // 28 00
        // 5A 00
        // 38 01
        // 48 01
        // 50 41 //宽度
        // 58 34 //高度
        // 60 04
        // 6A [05] 32 36 39 33 33
        // 70 00
        // 78 03
        // 80 01 00

        //大图C
        // 00 00 00 07 00 00 00
        // 5E 08 =后文长度-6
        // 01 12 03 98 01 01 10 01 1A
        // 5A长度
        // 08 A0 89 F7 B6 03
        // 10 A2 FF 8C F0 03
        // 18 00
        // 22 [10] F1 DD 65 4D A1 AB 66 B4 0F B5 27 B5 14 8E 73 B5
        // 28 96 83 08//size
        // 32 1A
        // 31 00
        // 35 00
        // 4C 00
        // 24 00
        // 40 00
        // 5B 00
        // 4D 00
        // 5B 00
        // 39 00
        // 39 00
        // 40 00
        // 57 00
        // 5D 00
        // 38 01
        // 48 01
        // 50 80 14 //宽度
        // 58 A0 0B //高度
        // 60 02
        // 6A [05] 32 36 39 33 33
        // 70 00
        // 78 03
        // 80 01 00

        writeQQ(bot)
        writeHex(TIMProtocol.version0x04)

        encryptAndWrite(sessionKey) {
            writeHex("00 00 00 07 00 00 00")

            writeUVarintLVPacket(lengthOffset = { it - 6 }) {
                writeHex("01 12 03 98 01 01 10 01 1A")

                writeUVarintLVPacket(lengthOffset = { it + 1 }) {
                    writeUVarInt(group)
                    writeUVarInt(bot)

                    writeTV(0x1800u)
                    writeTLV(0x22u, md5(imageData))
                    writeTUVarint(0x28u, imageData.size.toUInt())
                    writeUVarintLVPacket(tag = 0x32u) {
                        writeTV(0x31_00u)
                        writeTV(0x35_00u)
                        writeTV(0x4C_00u)
                        writeTV(0x24_00u)
                        writeTV(0x40_00u)
                        writeTV(0x5B_00u)
                        writeTV(0x4D_00u)
                        writeTV(0x5B_00u)
                        writeTV(0x39_00u)
                        writeTV(0x39_00u)
                        writeTV(0x40_00u)
                        writeTV(0x57_00u)
                        writeTV(0x5D_00u)
                    }
                    writeTV(0x38_01u)
                    writeTV(0x48_01u)
                    writeTUVarint(0x50u, image.imageWidth.toUInt())
                    writeTUVarint(0x58u, image.imageHeight.toUInt())
                    writeTV(0x60_02u)
                    writeTLV(0x6Au, value0x6A)
                    writeTV(0x70_00u)
                    writeTV(0x78_03u)
                    writeTV(0x80_01u)
                    writeUByte(0u)
                }
            }
        }
    }

    companion object {
        private val value0x6A: UByteArray = ubyteArrayOf(32u, 36u, 39u, 33u, 33u)
    }
}

