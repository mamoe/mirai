@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.withSession
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.network.protocol.tim.packet.ResponsePacket
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.httpPostGroupImage
import net.mamoe.mirai.utils.io.*


/**
 * 图片文件过大
 */
class OverFileSizeMaxException : IllegalStateException()

/**
 * 上传群图片
 * 挂起直到上传完成或失败
 * 失败后抛出 [OverFileSizeMaxException]
 */
suspend fun Group.uploadImage(
    image: ExternalImage
) = withSession {
    GroupImageIdRequestPacket(bot.qqAccount, internalId, image, sessionKey)
        .sendAndExpect<GroupImageIdRequestPacket.Response, Unit> {
            when (it.state) {
                GroupImageIdRequestPacket.Response.State.REQUIRE_UPLOAD -> {
                    httpPostGroupImage(
                        botAccount = bot.qqAccount,
                        groupId = GroupId(id),
                        imageInput = image.input,
                        inputSize = image.inputSize,
                        uKeyHex = it.uKey!!.toUHexString("")
                    )
                }

                GroupImageIdRequestPacket.Response.State.ALREADY_EXISTS -> {

                }

                GroupImageIdRequestPacket.Response.State.OVER_FILE_SIZE_MAX -> throw OverFileSizeMaxException()
            }
        }.join()
}

/**
 * 获取 Image Id 和上传用的一个 uKey
 */
@PacketId(0x0388u)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
class GroupImageIdRequestPacket(
    private val bot: UInt,
    private val groupInternalId: GroupInternalId,
    private val image: ExternalImage,
    private val sessionKey: ByteArray
) : OutgoingPacket() {

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        //未知图片A
        // 00 00 00 07 00 00 00
        // 53 08 =后文长度-6
        // 01 12 03 98 01 02 10 02 22 4F 08 F3 DB F3 E3 01 10 A2 FF 8C F0 03 18 B1 C7 B1 BB 0A 22 10 77 FB 3D 6F 97 BD 7B F0 C4 1F DC 60 1F 22 D2 7C 28 04 30 02 38 20 40 FF 01 48 00 50 01 5A 05 32 36 39 33 33 60 00 68 00 70 00 78 00 80 01 A4 05 88 01 D8 03 90 01 EB 07 A0 01 01

        //小图B
        // 00 00 00 07 00 00 00
        // 5B =后文长度-7
        // 08 01 12 03 98 01 01 10 01 1A
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

        //450*298
        //00 00 00 07 00 00 00
        // 5D=后文-7 varint
        // 08 01 12 03 98 01 01 10 01 1A
        // 59 =后文长度 varint
        // 08 A0 89 F7 B6 03
        // 10 A2 FF 8C F0 03
        // 18 00
        // 22 10  01 FC 9D 6B E9 B2 D9 CD AC 25 66 73 F9 AF 6A 67
        // 28 [C9 10] varint size
        // 32 1A
        // 58 00 51 00 56 00 51 00 58 00 47 00 55 00 47 00 38 00 57 00 5F 00 4A 00 43 00
        // 38 01 48 01
        // 50 [C2 03]
        // 58 [AA 02]
        // 60 02
        // 6A 05 32 36 39 33 33
        // 70 00
        // 78 03
        // 80 01
        // 00

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


        //00 00 00 07 00 00 00
        // 5B 08 01 12 03 98 01 01 10 01 1A
        // 57
        // 08 A0 89 F7 B6 03
        // 10 A2 FF 8C F0 03
        // 18 00
        // 22 10 39 F7 65 32 E1 AB 5C A7 86 D7 A5 13 89 22 53 85
        // 28 90 23
        // 32 1A
        // 28 00 52 00 49 00 5F 00 36 00 31 00 28 00 32 00 52 00 59 00 4B 00 59 00 43 00
        // 38 01
        // 48 01
        // 50 2D
        // 58 2D
        // 60 03
        // 6A 05 32 36 39 33 33
        // 70 00
        // 78 03
        // 80 01 00
        writeQQ(bot)
        writeHex("04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00")
        //writeHex(TIMProtocol.version0x02)

        encryptAndWrite(sessionKey) {
            writeHex("00 00 00 07 00 00 00")

            writeUVarintLVPacket(lengthOffset = { it - 7 }) {
                writeByte(0x08)
                writeHex("01 12 03 98 01 01 10 01 1A")

                writeUVarintLVPacket(lengthOffset = { it }) {
                    writeTUVarint(0x08u, groupInternalId.value)
                    writeTUVarint(0x10u, bot)
                    writeTV(0x1800u)

                    writeUByte(0x22u)
                    writeUByte(0x10u)
                    writeFully(image.md5)

                    writeTUVarint(0x28u, image.inputSize.toUInt())
                    writeUVarintLVPacket(tag = 0x32u) {
                        writeTV(0x5B_00u)
                        writeTV(0x40_00u)
                        writeTV(0x33_00u)
                        writeTV(0x48_00u)
                        writeTV(0x5F_00u)
                        writeTV(0x58_00u)
                        writeTV(0x46_00u)
                        writeTV(0x51_00u)
                        writeTV(0x45_00u)
                        writeTV(0x51_00u)
                        writeTV(0x40_00u)
                        writeTV(0x24_00u)
                        writeTV(0x4F_00u)
                    }
                    writeTV(0x38_01u)
                    writeTV(0x48_01u)
                    writeTUVarint(0x50u, image.width.toUInt())
                    writeTUVarint(0x58u, image.height.toUInt())
                    writeTV(0x60_04u)//这个似乎会变 有时候是02, 有时候是03
                    writeTByteArray(0x6Au, value0x6A)

                    writeTV(0x70_00u)
                    writeTV(0x78_03u)
                    writeTV(0x80_01u)
                    writeUByte(0u)
                }
            }

            /*
             this.debugColorizedPrintThis(compareTo =  buildPacket {
                 writeHex("00 00 00 07 00 00 00 5E 08 01 12 03 98 01 01 10 01 1A")
                 writeHex("5A 08")
                 writeUVarInt(groupId)
                 writeUByte(0x10u)
                 writeUVarInt(bot)
                 writeHex("18 00 22 10")
                 writeFully(image.md5)
                 writeUByte(0x28u)
                 writeUVarInt(image.fileSize.toUInt())
                 writeHex("32 1A 37 00 4D 00 32 00 25 00 4C 00 31 00 56 00 32 00 7B 00 39 00 30 00 29 00 52 00")
                 writeHex("38 01 48 01 50")
                 writeUVarInt(image.width.toUInt())
                 writeUByte(0x58u)
                 writeUVarInt(image.height.toUInt())
                 writeHex("60 04 6A 05 32 36 36 35 36 70 00 78 03 80 01 00")
             }.readBytes().toUHexString())
                */
        }
    }

    companion object {
        private val value0x6A: UByteArray = ubyteArrayOf(0x05u, 0x32u, 0x36u, 0x36u, 0x35u, 0x36u)
    }

    @PacketId(0x0388u)
    @PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        lateinit var state: State

        /**
         * 访问 HTTP API 时需要使用的一个 key. 128 位
         */
        var uKey: ByteArray? = null

        enum class State {
            /**
             * 需要上传. 此时 [uKey] 不为 `null`
             */
            REQUIRE_UPLOAD,
            /**
             * 服务器已有这个图片. 此时 [uKey] 为 `null`
             */
            ALREADY_EXISTS,
            /**
             * 图片过大. 此时 [uKey] 为 `null`
             */
            OVER_FILE_SIZE_MAX,
        }

        override fun decode(): Unit = with(input) {
            discardExact(6)//00 00 00 05 00 00

            val length = remaining - 128 - 14
            if (length < 0) {
                state = if (readUShort().toUInt() == 0x0025u) {
                    State.OVER_FILE_SIZE_MAX
                } else {
                    State.ALREADY_EXISTS
                }

                //图片过大 00 25 12 03 98 01 01 08 9B A4 DC 92 06 10 01 1A 1B 08 00 10 C5 01 1A 12 6F 76 65 72 20 66 69 6C 65 20 73 69 7A 65 20 6D 61 78 20 00
                //图片过大 00 25 12 03 98 01 01 08 9B A4 DC 92 06 10 01 1A 1B 08 00 10 C5 01 1A 12 6F 76 65 72 20 66 69 6C 65 20 73 69 7A 65 20 6D 61 78 20 00
                //图片已有 00 3F 12 03 98 01 01 08 9B A4 DC 92 06 10 01 1A 35 08 00 10 00 20 01 2A 1F 0A 10 24 66 B9 6B E8 58 FE C0 12 BD 1E EC CB 74 A8 8E 10 04 18 83 E2 AF 01 20 80 3C 28 E0 21 30 EF 9A 88 B9 0B 38 50 48 90 D7 DA B0 08
                //debugPrint("后文")
                return@with
            }

            discardExact(length)
            uKey = readBytes(128)
            state = State.REQUIRE_UPLOAD
            //} else {
            //    println("服务器已经有了这个图片")
            //println("后文 = ${readRemainingBytes().toUHexString()}")
            //}


            // 已经有了的一张图片
            // 00 3B 12 03 98 01 01
            // 08 AB A7 89 D8 02 //群ID
            // 10 01 1A 31 08 00 10 00 20 01 2A 1B 0A 10 7A A4 B3 AA 8C 3C 0F 45 2D 9B 7F 30 2A 0A CE AA 10 04 18 F3 06 20 41 28 34 30 DF CF A2 93 02 38 50 48 D0 A9 E5 C8 0B

            // 服务器还没有的一张图片
            // 02 4E 12 03 98 01 02
            // 08 AB A7 89 D8 02 //群ID
            // 10 02 22 C3 04 08 F8 9D D0 F5 09 12 10 2F CA 6B E7 B7 95 B7 27 06 35 27 54 0E 43 B4 30 18 00 48 BD EE 92 8D 05 48 BD EE 92 E5 01 48 BB CA 80 A3 02 48 BA F6 D7 5C 48 EF BC 90 F5 0A 50 50 50 50 50 50 50 50 50 50 5A 0D 67 63 68 61 74 2E 71 70 69 63 2E 63 6E 62 79 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 33 39 36 37 39 34 39 34 32 37 2F 33 39 36 37 39 34 39 34 32 37 2D 32 36 36 32 36 30 30 34 34 30 2D 32 46 43 41 36 42 45 37 42 37 39 35 42 37 32 37 30 36 33 35 32 37 35 34 30 45 34 33 42 34 33 30 2F 31 39 38 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 6A 77 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 33 39 36 37 39 34 39 34 32 37 2F 33 39 36 37 39 34 39 34 32 37 2D 32 36 36 32 36 30 30 34 34 30 2D 32 46 43 41 36 42 45 37 42 37 39 35 42 37 32 37 30 36 33 35 32 37 35 34 30 45 34 33 42 34 33 30 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 72 79 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 33 39 36 37 39 34 39 34 32 37 2F 33 39 36 37 39 34 39 34 32 37 2D 32 36 36 32 36 30 30 34 34 30 2D 32 46 43 41 36 42 45 37 42 37 39 35 42 37 32 37 30 36 33 35 32 37 35 34 30 45 34 33 42 34 33 30 2F 37 32 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 78 00
            // [80 01] 04 9A 01 79 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 33 39 36 37 39 34 39 34 32 37 2F 33 39 36 37 39 34 39 34 32 37 2D 32 36 36 32 36 30 30 34 34 30 2D 32 46 43 41 36 42 45 37 42 37 39 35 42 37 32 37 30 36 33 35 32 37 35 34 30 45 34 33 42 34 33 30 2F 34 30 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 A0 01 00
        }
    }
}