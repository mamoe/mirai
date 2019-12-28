@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.charsets.Charsets
import kotlinx.io.core.*
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.ImageId
import net.mamoe.mirai.message.data.ImageId0x06
import net.mamoe.mirai.message.data.requireLength
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.*


// region FriendImageResponse

internal interface FriendImageResponse : EventPacket

/**
 * 图片数据地址.
 */
// TODO: 2019/11/15 应该为 inline class, 但 kotlin 有 bug
internal data class FriendImageLink(override inline val original: String) : FriendImageResponse, ImageLink {
    override fun toString(): String = "FriendImageLink($original)"
}

/**
 * 访问 HTTP API 时使用的 uKey
 */
internal class FriendImageUKey(inline val imageId: ImageId, inline val uKey: ByteArray) : FriendImageResponse {
    override fun toString(): String = "FriendImageUKey(imageId=${imageId.value}, uKey=${uKey.toUHexString()})"
}

/**
 * 图片 ID 已存在
 * 发送消息时使用的 id
 */
internal inline class FriendImageAlreadyExists(inline val imageId: ImageId) : FriendImageResponse {
    override fun toString(): String = "FriendImageAlreadyExists(imageId=${imageId.value})"
}

/**
 * 超过文件大小上限
 */
internal object FriendImageOverFileSizeMax : FriendImageResponse {
    override fun toString(): String = "FriendImageOverFileSizeMax"
}

// endregion

/**
 * 请求上传图片. 将发送图片的 md5, size, width, height.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片
 * - 服务器未存有, 返回一个 key 用于客户端上传
 */
@PacketVersion(date = "2019.11.16", timVersion = "2.3.2 (21173)")
internal object FriendImagePacket : SessionPacketFactory<FriendImageResponse>() {
    @Suppress("FunctionName")
    fun RequestImageId(
        bot: Long,
        sessionKey: SessionKey,
        target: Long,
        image: ExternalImage
    ): OutgoingPacket = buildSessionPacket(
        bot,
        sessionKey,
        version = TIMProtocol.version0x04,
        name = "FriendImagePacket.RequestPacketId"
    ) {
        writeHex("00 00 00 07 00 00")


        // TODO: 2019/11/22 should be ProtoBuf

        writeShortLVPacket(lengthOffset = { it - 7 }) {
            writeUByte(0x08u)
            writeTV(0x01_12u)
            writeTV(0x03_98u)
            writeTV(0x01_01u)
            writeTV(0x08_01u)

            writeUVarIntLVPacket(tag = 0x12u, lengthOffset = { it + 1 }) {
                writeTUVarint(0x08u, bot.toUInt())
                writeTUVarint(0x10u, target.toUInt())
                writeTV(0x18_00u)
                writeTLV(0x22u, image.md5)
                writeTUVarint(0x28u, image.inputSize.toUInt())
                writeUVarIntLVPacket(tag = 0x32u) {
                    writeTV(0x28_00u)
                    writeTV(0x46_00u)
                    writeTV(0x51_00u)
                    writeTV(0x56_00u)
                    writeTV(0x4B_00u)
                    writeTV(0x41_00u)
                    writeTV(0x49_00u)
                    writeTV(0x25_00u)
                    writeTV(0x4B_00u)
                    writeTV(0x24_00u)
                    writeTV(0x55_00u)
                    writeTV(0x30_00u)
                    writeTV(0x24_00u)
                }
                writeTV(0x38_01u)
                writeTV(0x48_00u)
                writeTUVarint(0x70u, image.width.toUInt())
                writeTUVarint(0x78u, image.height.toUInt())
            }
        }

    }

    @Suppress("FunctionName")
    fun RequestImageLink(
        bot: Long,
        sessionKey: SessionKey,
        imageId: ImageId
    ): OutgoingPacket {
        imageId.requireLength()
        require(imageId.value.length == 37) { "ImageId.value.length must == 37 but given length=${imageId.value.length} value=${imageId.value}" }

        // 00 00 00 07 00 00 00
        // [4B]
        // 08
        // 01 12
        // 03 98
        // 01 02
        // 08 02
        //
        // 1A [47]
        // 08 [A2 FF 8C F0 03] UVarInt
        // 10 [DD F1 92 B7 07] UVarInt
        // 1A [25] 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66
        // 20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01


        // 00 00 00 07 00 00 00
        // [4B]
        // 08
        // 01 12
        // 03 98
        // 01 02
        // 08 02
        //
        // 1A
        // [47]
        // 08 [A2 FF 8C F0 03]
        // 10 [A6 A7 F1 EA 02]
        // 1A [25] 2F 39 61 31 66 37 31 36 32 2D 38 37 30 38 2D 34 39 30 38 2D 38 31 63 30 2D 66 34 63 64 66 33 35 63 38 64 37 65
        // 20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01

        // TODO: 2019/11/22 should be ProtoBuf

        return buildSessionPacket(
            bot,
            sessionKey,
            version = TIMProtocol.version0x04,
            name = "FriendImagePacket.RequestImageLink"
        ) {
            writeHex("00 00 00 07 00 00")

            writeUShort(0x004Bu)

            writeUByte(0x08u)
            writeTV(0x01_12u)
            writeTV(0x03_98u)
            writeTV(0x01_02u)
            writeTV(0x08_02u)

            writeUByte(0x1Au)
            writeUByte(0x47u)
            writeTUVarint(0x08u, bot.toUInt())
            writeTUVarint(0x10u, bot.toUInt()) // 这里实际上应该是这张图片来自哪个 QQ 号. 但传 bot 也没事.
            writeTLV(0x1Au, imageId.value.toByteArray(Charsets.ISO_8859_1))
            writeHex("20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01")
        }
    }

    override suspend fun ByteReadPacket.decode(
        id: PacketId,
        sequenceId: UShort,
        handler: BotNetworkHandler
    ): FriendImageResponse {

        // 上传图片, 成功获取ID
        //00 00 00 08 00 00
        // [01 0D]
        // 12 06
        // 98 01 01 A0 01 00
        // 08 01 //packet type 01=上传图片; 02=下载图片
        // 12 [86 02]
        //   08 00
        //   10 [9B A4 D4 9A 0A]
        //   18 00
        //   28 00
        //   38 F1 C0 A1 BF 05
        //   38 BB C8 E4 E2 0F
        //   38 FB AE FA 9D 0A
        //   38 E5 C6 8B CD 06
        //   40 BB 03 // ports
        //   40 90 3F
        //   40 50
        //   40 BB 03
        //   4A [80 01] 76 B2 58 23 B8 F6 B1 E6 AE D4 76 EC 3C 08 79 B1 DF 05 D5 C2 4A E0 CC F1 2F 26 4F D4 DC 44 5A 9A 16 A9 E4 22 EB 92 96 05 C3 C9 8F C5 5F 84 00 A3 4E 63 BE 76 F7 B9 7B 09 43 A6 14 EE C8 6D 6A 48 02 E3 9D 62 CD 42 3E 15 93 64 8F FC F5 88 50 74 6A 6A 03 C9 FE F0 96 EA 76 02 DC 4F 09 D0 F5 60 73 B2 62 8F 8B 11 06 BF 06 1B 18 00 FE B4 5E F3 12 72 F2 66 9C F5 01 97 1C 0A 5B 68 5B 85 ED 9C
        //   52 [25] 2F 37 38 62 36 34 64 63 32 2D 31 66 32 31 2D 34 33 62 38 2D 39 32 62 31 2D 61 30 35 30 35 30 34 30 35 66 65 32
        //   5A [25] 2F 37 38 62 36 34 64 63 32 2D 31 66 32 31 2D 34 33 62 38 2D 39 32 62 31 2D 61 30 35 30 35 30 34 30 35 66 65 32
        //   60 00 68 80 80 08
        // 20 01

        // 上传图片, 图片过大
        //00 00 00 09 00 00
        // [00 1D]
        // 12 [07] 98 01 01 A0 01 C7 01
        // 08 01
        // 12 19 08 00 18 C7 01 22 12 66 69 6C 65 20 73 69 7A 65 20 6F 76 65 72 20 6D 61 78
        discardExact(3) // 00 00 00
        if (readUByte().toUInt() == 0x09u) {
            return FriendImageOverFileSizeMax
        }
        discardExact(2) //00 00

        discardExact(2) //全长 (有 offset)

        discardExact(1); discardExact(readUVarInt().toInt()) // 12 [06] 98 01 01 A0 01 00
        // TODO: 2019/11/22 should be ProtoBuf

        check(readUByte().toUInt() == 0x08u)
        return when (val flag = readUByte().toUInt()) {
            0x01u -> {
                //00 00 00 08 00 00
                // [00 83]
                // 12 06
                // 98 01 01
                // A0 01 00 08 01 12 7D
                // 08 00
                // 10 9B A4 D4 9A 0A
                // 18 00
                // 28 01
                // 32 1B
                // 0A [10] 81 9B B9 33 52 BD CE 88 A9 BA 3B 1C A4 A8 8B EF
                // 10 00
                // 18 8E 4B
                // 20 40
                // 28 40
                // 52 25 2F 63 36 62 38 37 61 39 63 2D 37 30 64 36 2D 34 61 38 38 2D 61 39 33 36 2D 36 34 31 33 65 37 39 62 33 66 64 34
                // 5A 25 2F 63 36 62 38 37 61 39 63 2D 37 30 64 36 2D 34 61 38 38 2D 61 39 33 36 2D 36 34 31 33 65 37 39 62 33 66 64 34
                // 60 00
                // 68 80 80 08
                // 20 01

                try {
                    while (readUByte().toUInt() != 0x4Au) readUVarLong()
                    val uKey = readBytes(readUVarInt().toInt())//128
                    while (readUByte().toUInt() != 0x52u) readUVarLong()
                    val imageId = ImageId0x06(readString(readUVarInt().toInt()))//37
                    return FriendImageUKey(imageId, uKey)
                } catch (e: EOFException) {
                    val toDiscard = readUByte().toInt() - 37

                    return if (toDiscard < 0) {
                        FriendImageOverFileSizeMax
                    } else {
                        discardExact(toDiscard)
                        val imageId = ImageId0x06(readString(37))
                        FriendImageAlreadyExists(imageId)
                    }
                }
            }
            0x02u -> {
                //00 00 00 08 00 00
                // [02 2B]
                // 12 [06] 98 01 02 A0 01 00
                // 08 02
                // 1A [A6 04]
                //   0A [25] 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66
                //   18 00
                //   32 [7B] 68 74 74 70 3A 2F 2F 36 31 2E 31 35 31 2E 32 33 34 2E 35 34 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 32 7C 68 74 74 70 3A 2F 2F 31 30 31 2E 32 32 37 2E 31 33 31 2E 36 37 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 32 7D 68 74 74 70 3A 2F 2F 31 35 37 2E 32 35 35 2E 31 39 32 2E 31 30 35 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 32 7C 68 74 74 70 3A 2F 2F 31 32 30 2E 32 34 31 2E 31 39 30 2E 34 31 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33
                //   3A 00 80 01 00


                //00 00 00 08 00 00
                // [02 29]
                // 12 [06] 98 01 02 A0 01 00
                // 08 02
                // 1A [A4 04]
                //   0A [25] 2F 62 61 65 30 63 64 66 66 2D 65 33 34 30 2D 34 38 39 34 2D 39 37 36 65 2D 30 66 62 35 38 61 61 31 36 35 66 64
                //   18 00
                //   32 [7A] 68 74 74 70 3A 2F 2F 31 30 31 2E 38 39 2E 33 39 2E 32 31 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 62 61 65 30 63 64 66 66 2D 65 33 34 30 2D 34 38 39 34 2D 39 37 36 65 2D 30 66 62 35 38 61 61 31 36 35 66 64 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33
                //   32 7B 68 74 74 70 3A 2F 2F 36 31 2E 31 35 31 2E 31 38 33 2E 32 31 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 62 61 65 30 63 64 66 66 2D 65 33 34 30 2D 34 38 39 34 2D 39 37 36 65 2D 30 66 62 35 38 61 61 31 36 35 66 64 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 32 7D 68 74 74 70 3A 2F 2F 31 35 37 2E 32 35 35 2E 31 39 32 2E 31 30 35 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 62 61 65 30 63 64 66 66 2D 65 33 34 30 2D 34 38 39 34 2D 39 37 36 65 2D 30 66 62 35 38 61 61 31 36 35 66 64 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 32 7C 68 74 74 70 3A 2F 2F 31 32 30 2E 32 34 31 2E 31 39 30 2E 34 31 3A 38 30 2F 6F 66 66 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 2F 62 61 65 30 63 64 66 66 2D 65 33 34 30 2D 34 38 39 34 2D 39 37 36 65 2D 30 66 62 35 38 61 61 31 36 35 66 64 2F 30 3F 76 75 69 6E 3D 31 30 34 30 34 30 30 32 39 30 26 74 65 72 6D 3D 32 35 35 26 73 72 76 76 65 72 3D 32 36 39 33 33 3A 00 80 01 00

                discardExact(1)
                discardExact(2)// [A4 04] 后文长度
                check(readUByte().toUInt() == 0x0Au) { "Illegal identity. Required 0x0Au" }
                /* val imageId = */ImageId0x06(readString(readUByte().toInt()))

                check(readUByte().toUInt() == 0x18u) { "Illegal identity. Required 0x18u" }
                check(readUShort().toUInt() == 0x0032u) { "Illegal identity. Required 0x0032u" }

                val link = readUVarIntLVString()
                discard()
                FriendImageLink(link)
            }
            else -> error("Unknown FriendImageIdRequestPacket flag $flag")
        }
    }
}
