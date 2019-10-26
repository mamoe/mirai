@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.network.protocol.tim.packet.ResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendImageIdRequestPacket.Response.State.*
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.httpPostFriendImage
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.readUnsignedVarInt
import net.mamoe.mirai.utils.writeUVarInt
import net.mamoe.mirai.withSession

/**
 * 上传图片
 * 挂起直到上传完成或失败
 *
 * 在 JVM 下, `SendImageUtilsJvm.kt` 内有多个捷径函数
 *
 * @throws OverFileSizeMaxException 如果文件过大, 服务器拒绝接收时
 */
suspend fun QQ.uploadImage(image: ExternalImage): ImageId = bot.withSession {
    FriendImageIdRequestPacket(qqAccount, sessionKey, id, image).sendAndExpect<FriendImageIdRequestPacket.Response, ImageId> {
        when (it.state) {
            REQUIRE_UPLOAD -> {
                require(
                    httpPostFriendImage(
                        botAccount = bot.qqAccount,
                        uKeyHex = it.uKey!!.toUHexString(""),
                        imageInput = image.input,
                        inputSize = image.inputSize
                    )
                )
            }

            ALREADY_EXISTS -> {

            }

            OVER_FILE_SIZE_MAX -> {
                throw OverFileSizeMaxException()
            }
        }

        it.imageId!!
    }.await()
}

//fixVer2=00 00 00 01 2E 01 00 00 69 35
//01 [3E 03 3F A2] [76 E4 B8 DD] 00 00 50 7A 00 0A 00 01 00 01    00 2D 55 73 65 72 44 61 74 61 49 6D 61 67 65 3A 43 32 43 5C 48 31 30 50 60 35 29 24 52 7D 57 45 56 54 41 4B 52 24 45 4E 54 45 58 2E 70 6E 67
// 00 00 00 F2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01

//01 3E 03 3F A2 76 E4 B8 DD     00 00 50 7B 00 0A 00 01 00 01    00 5E 4F 53 52 6F 6F 74 3A 43 3A 5C 55 73 65 72 73 5C 48 69 6D 31 38 5C 44 6F 63 75 6D 65 6E 74 73 5C 54 65 6E 63 65 6E 74 20 46 69 6C 65 73 5C 31 30 34 30 34 30 30 32 39 30 5C 49 6D 61 67 65 5C 43 32 43 5C 4E 41 4B 60 52 52 4E 24 49 24 24 4B 44 24 34 5B 5B 45 4E 24 4D 4A 30 2E 6A 70 67
// 00 00 06 99 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01

//01 3E 03 3F A2 76 E4 B8 DD     00 00 50 7C 00 0A 00 01 00 01    00 2D 55 73 65 72 44 61 74 61 49 6D 61 67 65 3A 43 32 43 5C 40 53 51 25 4F 46 43 50 36 4C 48 30 47 34 43 47 57 53 49 52 46 37 32 2E 70 6E 67
// 00 01 61 A7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01
/**
 * 似乎没有必要. 服务器的返回永远都是 01 00 00 00 02 00 00
 */
@PacketId(0X01_BDu)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
class SubmitImageFilenamePacket(
    private val bot: UInt,
    private val target: UInt,
    private val filename: String,
    private val sessionKey: ByteArray
) : OutgoingPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)//?
        //writeHex("04 00 00 00 01 2E 01 00 00 69 35")

        encryptAndWrite(sessionKey) {
            writeByte(0x01)
            writeQQ(bot)
            writeQQ(target)
            writeZero(2)
            writeUByte(0x02u)
            writeRandom(1)
            writeHex("00 0A 00 01 00 01")
            val name = "UserDataImage:$filename"
            writeShort(name.length.toShort())
            writeStringUtf8(name)
            writeHex("00 00")
            writeRandom(2)//这个也与是哪个好友有关?
            writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01")//35  02? 最后这个值是与是哪个好友有关

            //this.debugPrintThis("SubmitImageFilenamePacket")
        }

        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1A 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1B 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1C 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 29 37 42 53 4B 48 32 44 35 54 51 28 5A 35 7D 35 24 56 5D 32 35 49 4E 2E 6A 70 67 00 00 03 73 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
    }

    @PacketId(0x01_BDu)
    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        override fun decode() = with(input) {
            require(readBytes().contentEquals(expecting))
        }

        companion object {
            private val expecting = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00)
        }
    }
}


/**
 * 请求上传图片. 将发送图片的 md5, size, width, height.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片
 * - 服务器未存有, 返回一个 key 用于客户端上传
 */
@PacketId(0x03_52u)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
class FriendImageIdRequestPacket(
    private val bot: UInt,
    private val sessionKey: ByteArray,
    private val target: UInt,
    private val image: ExternalImage
) : OutgoingPacket() {

    //00 00 00 07 00 00 00 4B 08 01 12 03 98 01 01 08 01 12 47 08 A2 FF 8C F0 03 10 89 FC A6 8C 0B 18 00 22 10 2B 23 D7 05 CA D1 F2 CF 37 10 FE 58 26 92 FC C4 28 FD 08 32 1A 7B 00 47 00 47 00 42 00 7E 00 49 00 31 00 5A 00 4D 00 43 00 28 00 25 00 49 00 38 01 48 00 70 42 78 42

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(bot)
        //04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00
        writeHex("04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00")

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
            writeHex("00 00 00 07 00 00 00")

            //proto
            writeUVarintLVPacket(lengthOffset = { it - 7 }) {
                writeUByte(0x08u)
                writeUShort(0x01_12u)
                writeUShort(0x03_98u)
                writeUShort(0x01_01u)
                writeUShort(0x08_01u)


                writeUVarintLVPacket(tag = 0x12u, lengthOffset = { it + 1 }) {
                    writeUByte(0x08u)
                    writeUVarInt(bot)

                    writeUByte(0x10u)
                    writeUVarInt(target)

                    writeUShort(0x18_00u)

                    writeUByte(0x22u)
                    writeUByte(0x10u)
                    writeFully(image.md5)

                    writeUByte(0x28u)
                    writeUVarInt(image.inputSize.toUInt())


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
            }


            //println(this.build().readBytes().toUHexString())
        }
    }

    @PacketId(0x0352u)
    @PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        /**
         * 访问 HTTP API 时需要使用的一个 key. 128 位
         */
        var uKey: ByteArray? = null

        /**
         * 发送消息时使用的 id
         */
        var imageId: ImageId? = null

        lateinit var state: State

        enum class State {
            /**
             * 需要上传. 此时 [uKey], [imageId] 均不为 `null`
             */
            REQUIRE_UPLOAD,
            /**
             * 服务器已有这个图片. 此时 [uKey] 为 `null`, [imageId] 不为 `null`
             */
            ALREADY_EXISTS,
            /**
             * 图片过大. 此时 [uKey], [imageId] 均为 `null`
             */
            OVER_FILE_SIZE_MAX,
        }

        override fun decode() = with(input) {
            //00 00 00 08 00 00
            //01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 B3 90 73 32 C0 5D 72 4B 18 AC 16 8A 23 92 21 3C E6 FD 51 33 CE C4 84 1C 4C 7B A2 E5 27 65 1C 99 EE D6 D4 D2 0D B8 10 2D 88 7E 13 71 75 09 36 46 0F BA 87 B3 EA 54 B2 2B 18 8F F3 5A 9D 55 C6 3B E4 CB 9E B3 69 79 E2 51 61 98 1B 04 49 76 58 29 75 E3 73 56 4B 89 A4 54 A2 E1 0C 17 72 8D 77 EA CD CF 9E 68 B7 01 65 7B F1 E3 B7 FC 04 0C F4 D8 8D B3 51 1B B2 4C 14 59 DE FA 0D 64 BD 50 2E ED 52 25 2F 62 63 66 38 63 39 39 65 2D 30 65 63 35 2D 34 33 33 31 2D 62 37 30 61 2D 31 36 33 35 32 66 66 64 38 33 33 33 5A 25 2F 62 63 66 38 63 39 39 65 2D 30 65 63 35 2D 34 33 33 31 2D 62 37 30 61 2D 31 36 33 35 32 66 66 64 38 33 33 33 60 00 68 80 80 08 20 01

            //00 00 00 08 00 00 01 0C 12 06 98 01 01 A0 01 00 08 01 12 85 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 B7 87 AC E7 0B 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 50 40 90 3F 40 BB 03 40 50 4A 80 01 F2 65 BC F3 E8 C6 F3 30 B1 85 72 86 C0 95 C0 A7 09 E3 84 AC A6 68 C3 AF BB A8 96 64 AA 18 92 96 F7 3C 7B F8 EA 03 C6 6A AD B7 94 BC 76 D4 36 84 25 76 CB DF 5B 7C E7 40 DF 5D FD DF 3D 93 23 96 5D 23 A8 B2 93 FA 21 BF 68 3E 0B 71 D2 9C FF F2 55 45 11 E2 23 2E D0 49 6E 4F 1F DB 18 28 22 68 45 C9 9E A7 F4 AD EF 20 93 55 EB 0E A3 33 7B 18 E8 7C 15 6F 19 26 2C 41 E9 E4 51 61 48 AA 2F EE 52 25 2F 65 39 61 63 62 63 65 39 2D 61 62 39 36 2D 34 30 30 66 2D 38 61 66 30 2D 32 63 34 64 39 37 31 31 32 33 36 62 5A 25 2F 65 39 61 63 62 63 65 39 2D 61 62 39 36 2D 34 30 30 66 2D 38 61 66 30 2D 32 63 34 64 39 37 31 31 32 33 36 62 60 00 68 80 80 08 20 01
            //00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 BB C8 E4 E2 0F 38 FB AE FA 9D 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 0E 26 8D 39 E7 88 22 74 EC 88 2B 04 C5 D1 3D D2 09 A4 2E 48 22 F5 91 51 D5 82 7A 43 9F 45 70 77 79 83 21 87 4E AA 63 6E 73 D5 D3 DA 5F FC 36 BA 97 31 74 49 D9 97 83 58 74 06 BE F2 00 83 CC B9 50 D0 C4 D1 63 33 5F AE EA 1C 99 2D 0D E7 A2 94 97 6E 18 92 86 2C C0 36 E9 D9 E3 82 01 A3 B9 AC F1 90 67 73 F3 3C 0B 26 4C C4 DE 20 AF 3D B3 20 F8 50 B4 0E 78 0E 0E 1E 8C 56 02 21 10 5B 61 39 52 25 2F 31 38 37 31 34 66 66 39 2D 61 30 39 39 2D 34 61 38 64 2D 38 34 39 62 2D 38 37 35 65 65 30 36 65 34 64 32 36 5A 25 2F 31 38 37 31 34 66 66 39 2D 61 30 39 39 2D 34 61 38 64 2D 38 34 39 62 2D 38 37 35 65 65 30 36 65 34 64 32 36 60 00 68 80 80 08 20 01
            discardExact(6)
            if (readUByte() != UByte.MIN_VALUE) {
                //服务器还没有这个图片

                //00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03
                // 4A [80 01] B5 29 1A 1B 0E 63 79 8B 34 B1 4E 2A 2A 9E 69 09 A7 69 F5 C6 4F 95 DA 96 A9 1B E3 CD 6F 3D 30 EE 59 C0 30 22 BF F0 2D 88 2D A7 6C B2 09 AD D6 CE E1 46 84 FC 7D 19 AF 1A 37 91 98 AD 2C 45 25 AA 17 2F 81 DC 5A 7F 30 F4 2D 73 E5 1C 8B 8A 23 85 42 9D 8D 5C 18 15 32 D1 CA A3 4D 01 7C 59 11 73 DA B6 09 C2 6D 58 35 EF 48 88 44 0F 2D 17 09 52 DF D4 EA A7 85 2F 27 CE DF A8 F5 9B CD C9 84 C2 // 52 [25] 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 5A 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 60 00 68 80 80 08 20 01

                discardExact(60)

                discardExact(1)//4A, id
                uKey = readBytes(readUnsignedVarInt().toInt())//128

                discardExact(1)//52, id
                imageId = ImageId(readString(readUnsignedVarInt().toInt()))//37
                state = State.REQUIRE_UPLOAD

                //DebugLogger.logPurple("获得 uKey(${uKey!!.size})=${uKey!!.toUHexString()}")
                //DebugLogger.logPurple("获得 imageId(${imageId!!.value.length})=${imageId}")
            } else {
                //服务器已经有这个图片了
                //DebugLogger.logPurple("服务器已有好友图片 ")
                // 89
                // 12 06 98 01 01 A0 01 00 08 01 12 82 01 08 00 10 AB A7 89 D8 02 18 00 28 01 32 20 0A 10 5A 39 37 10 EA D5 B5 57 A8 04 14 70 CE 90 67 14 10 67 18 8A 94 17 20 ED 03 28 97 04 30 0A 52 25 2F 39 38 31 65 61 31 64 65 2D 62 32 31 33 2D 34 31 61 39 2D 38 38 37 65 2D 32 38 37 39 39 66 31 39 36 37 35 65 5A 25 2F 39 38 31 65 61 31 64 65 2D 62 32 31 33 2D 34 31 61 39 2D 38 38 37 65 2D 32 38 37 39 39 66 31 39 36 37 35 65 60 00 68 80 80 08 20 01


                //83 12 06 98 01 01 A0 01 00 08 01 12 7D 08 00 10 9B A4 DC 92 06 18 00 28 01 32 1B 0A 10 8E C4 9D 72 26 AE 20 C0 5D A2 B6 78 4D 12 B7 3A 10 00 18 86 1F 20 30 28 30 52 25 2F 30 31 62
                val toDiscard = readUByte().toInt() - 37
                if (toDiscard < 0) {
                    state = OVER_FILE_SIZE_MAX
                } else {
                    discardExact(toDiscard)
                    imageId = ImageId(readString(37))
                    state = ALREADY_EXISTS
                }
            }
        }
    }
}