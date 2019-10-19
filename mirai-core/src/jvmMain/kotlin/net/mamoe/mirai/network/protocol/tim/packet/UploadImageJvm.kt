@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.*
import net.mamoe.mirai.utils.*
import java.io.File
import javax.imageio.ImageIO

actual typealias ClientTryGetImageIDPacket = ClientTryGetImageIDPacketJvm

fun main() {
    val packet = ClientTryGetImageIDPacketJvm(1040400290u,
            "99 82 67 D4 62 20 CA 5D 81 F8 6F 83 EE 8A F7 68".hexToBytes(),
            2978594313u,
            ImageIO.read(File(("C:\\Users\\Him18\\Desktop\\哈哈哈操.jpg"))))
    println(packet.packet.readBytes().toUHexString())



    "89 FC A6 8C 0B".hexToBytes().read {
        println(readUnsignedVarInt())
    }
}

/**
 * 请求上传图片. 将发送图片的 md5, size, width, height.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片 [ServerTryGetImageIDFailedPacket]
 * - 服务器未存有, 返回一个 key 用于客户端上传 [ServerTryGetImageIDSuccessPacket]
 *
 * @author Him188moe
 */
@PacketId(0x03_52u)
class ClientTryGetImageIDPacketJvm(
        private val botNumber: UInt,
        private val sessionKey: ByteArray,
        private val target: UInt,
        private val image: PlatformImage
) : ClientPacket() {

    //00 00 00 07 00 00 00 4B 08 01 12 03 98 01 01 08 01 12 47 08 A2 FF 8C F0 03 10 89 FC A6 8C 0B 18 00 22 10 2B 23 D7 05 CA D1 F2 CF 37 10 FE 58 26 92 FC C4 28 FD 08 32 1A 7B 00 47 00 47 00 42 00 7E 00 49 00 31 00 5A 00 4D 00 43 00 28 00 25 00 49 00 38 01 48 00 70 42 78 42

    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2.21173")
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(botNumber)
        //04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00
        writeHex("04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00")

        val imageData = image.toByteArray()
        encryptAndWrite(sessionKey) {
            //好友图片
            // 00 00 00
            // 07 00
            // 00 00

            // proto

            // [4D 08]后文长度
            // 01 12
            // 03 98
            // 01 01
            // 08 01
            // 12 49
            // 08 [A2 FF 8C F0 03](1040400290 varint)
            // 10 [DD F1 92 B7 07](1994701021 varint)
            // 18 00
            // 22 [10](=16) [E9 BA 47 2E 36 ED D4 BF 8C 4F E5 6A CB A0 2D 5E](md5)
            // 28 [CE 0E](1870 varint)
            // 32 1A
            // 39 00
            // 51 00
            // 24 00
            // 32 00
            // 4A 00
            // 53 00
            // 25 00
            // 4C 00
            // 56 00
            // 42 00
            // 33 00
            // 44 00
            // 44 00
            // 38 01
            // 48 00
            // 70 [92 03](402 varint)
            // 78 [E3 01](227 varint)

            //好友图片
            /*
             * 00 00 00 07 00 00 00
             * [4E 08]后文长度
             * 01 12
             * 03 98
             * 01 01
             * 08 01
             * 12 4A
             * 08 [A2 FF 8C F0 03](varint)
             * 10 [DD F1 92 B7 07](varint)
             * 18 00//24
             * 22 10 72 02 57 44 84 1D 83 FC C0 85 A1 E9 10 AA 9C 2C
             * 28 [BD D9 19](421053 varint)
             * 32 1A//48
             * 49 00
             * 49 00
             * 25 00
             * 45 00
             * 5D 00
             * 50 00
             * 41 00
             * 7D 00
             * 4F 00
             * 56 00
             * 46 00
             * 4B 00
             * 5D 00
             * 38 01
             * 48 00//78
             *
             *
             * 70 [80 14]
             * 78 [A0 0B]//84
             */

            writeZero(3)
            writeUShort(0x07_00u)
            writeZero(1)

            //proto
            val packet = buildPacket {
                writeUByte(0x08u)
                writeUShort(0x01_12u)
                writeUShort(0x03_98u)
                writeUShort(0x01_01u)
                writeUShort(0x08_01u)


                writeUShort(0x12_47u)//?似乎会变

                writeUByte(0x08u)
                writeUVarInt(target)//todo 这两qq号反过来放也tm可以成功

                writeUByte(0x10u)
                writeUVarInt(botNumber)

                writeUShort(0x18_00u)

                writeUByte(0x22u)
                writeUByte(0x10u)
                writeFully(md5(imageData))

                writeUByte(0x28u)
                writeUVarInt(imageData.size.toUInt())

                writeUByte(0x32u)
                //长度应为1A
                writeUVarintLVPacket {
                    writeUShort(0x28_00u)
                    writeUShort(0x46_00u)
                    writeUShort(0x51_00u)
                    writeUShort(0x56_00u)
                    writeUShort(0x4B_00u)
                    writeUShort(0x41_00u)
                    writeUShort(0x49_00u)
                    writeUShort(0x25_00u)
                    writeUShort(0x4B_00u)
                    writeUShort(0x24_00u)
                    writeUShort(0x55_00u)
                    writeUShort(0x30_00u)
                    writeUShort(0x24_00u)
                }

                writeUShort(0x38_01u)
                writeUShort(0x48_00u)

                writeUByte(0x70u)
                writeUVarInt(image.width.toUInt())
                writeUByte(0x78u)
                writeUVarInt(image.height.toUInt())
            }
            writeShort((packet.remaining - 7).toShort())//why?
            writePacket(packet)

            //println(this.build().readBytes().toUHexString())
        }
    }
}