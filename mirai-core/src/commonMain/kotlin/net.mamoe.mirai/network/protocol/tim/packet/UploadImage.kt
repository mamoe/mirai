@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.account
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.session
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.*
import kotlin.properties.Delegates


//fixVer2=00 00 00 01 2E 01 00 00 69 35
//01 [3E 03 3F A2] [76 E4 B8 DD] 00 00 50 7A 00 0A 00 01 00 01    00 2D 55 73 65 72 44 61 74 61 49 6D 61 67 65 3A 43 32 43 5C 48 31 30 50 60 35 29 24 52 7D 57 45 56 54 41 4B 52 24 45 4E 54 45 58 2E 70 6E 67
// 00 00 00 F2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01

//01 3E 03 3F A2 76 E4 B8 DD     00 00 50 7B 00 0A 00 01 00 01    00 5E 4F 53 52 6F 6F 74 3A 43 3A 5C 55 73 65 72 73 5C 48 69 6D 31 38 5C 44 6F 63 75 6D 65 6E 74 73 5C 54 65 6E 63 65 6E 74 20 46 69 6C 65 73 5C 31 30 34 30 34 30 30 32 39 30 5C 49 6D 61 67 65 5C 43 32 43 5C 4E 41 4B 60 52 52 4E 24 49 24 24 4B 44 24 34 5B 5B 45 4E 24 4D 4A 30 2E 6A 70 67
// 00 00 06 99 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01

//01 3E 03 3F A2 76 E4 B8 DD     00 00 50 7C 00 0A 00 01 00 01    00 2D 55 73 65 72 44 61 74 61 49 6D 61 67 65 3A 43 32 43 5C 40 53 51 25 4F 46 43 50 36 4C 48 30 47 34 43 47 57 53 49 52 46 37 32 2E 70 6E 67
// 00 01 61 A7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01
@PacketId(0X01_BDu)
class ClientSubmitImageFilenamePacket(
        private val bot: UInt,
        private val target: UInt,
        private val filename: String,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)
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
            writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01")//35  02? 最后这个值是与是哪个哈有有关

            //this.debugPrintThis("ClientSubmitImageFilenamePacket")
        }

        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1A 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1B 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1C 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 29 37 42 53 4B 48 32 44 35 54 51 28 5A 35 7D 35 24 56 5D 32 35 49 4E 2E 6A 70 67 00 00 03 73 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
    }
}

@PacketId(0x01_BDu)
class ServerSubmitImageFilenameResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    override fun decode() = with(input) {
        require(readBytes().contentEquals(expecting))
    }

    companion object {
        private val expecting = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00)
    }

    @PacketId(0x01_BDu)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerSubmitImageFilenameResponsePacket = ServerSubmitImageFilenameResponsePacket(this.decryptBy(sessionKey)).applySequence()
    }
}

@PacketId(0x03_52u)
expect class ClientTryGetImageIDPacket(
        botNumber: UInt,
        sessionKey: ByteArray,
        target: UInt,
        image: PlatformImage
) : ClientPacket

@PacketId(0x03_52u)
sealed class ServerTryGetImageIDResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    @PacketId(0x03_52u)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerTryGetImageIDResponsePacket {
            val data = this.decryptAsByteArray(sessionKey)
            //00 00 00 08 00 00
            //01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 B3 90 73 32 C0 5D 72 4B 18 AC 16 8A 23 92 21 3C E6 FD 51 33 CE C4 84 1C 4C 7B A2 E5 27 65 1C 99 EE D6 D4 D2 0D B8 10 2D 88 7E 13 71 75 09 36 46 0F BA 87 B3 EA 54 B2 2B 18 8F F3 5A 9D 55 C6 3B E4 CB 9E B3 69 79 E2 51 61 98 1B 04 49 76 58 29 75 E3 73 56 4B 89 A4 54 A2 E1 0C 17 72 8D 77 EA CD CF 9E 68 B7 01 65 7B F1 E3 B7 FC 04 0C F4 D8 8D B3 51 1B B2 4C 14 59 DE FA 0D 64 BD 50 2E ED 52 25 2F 62 63 66 38 63 39 39 65 2D 30 65 63 35 2D 34 33 33 31 2D 62 37 30 61 2D 31 36 33 35 32 66 66 64 38 33 33 33 5A 25 2F 62 63 66 38 63 39 39 65 2D 30 65 63 35 2D 34 33 33 31 2D 62 37 30 61 2D 31 36 33 35 32 66 66 64 38 33 33 33 60 00 68 80 80 08 20 01
            println("ServerTryGetImageIDResponsePacket.size=" + data.size)


            //00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 BB C8 E4 E2 0F 38 FB AE FA 9D 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 0E 26 8D 39 E7 88 22 74 EC 88 2B 04 C5 D1 3D D2 09 A4 2E 48 22 F5 91 51 D5 82 7A 43 9F 45 70 77 79 83 21 87 4E AA 63 6E 73 D5 D3 DA 5F FC 36 BA 97 31 74 49 D9 97 83 58 74 06 BE F2 00 83 CC B9 50 D0 C4 D1 63 33 5F AE EA 1C 99 2D 0D E7 A2 94 97 6E 18 92 86 2C C0 36 E9 D9 E3 82 01 A3 B9 AC F1 90 67 73 F3 3C 0B 26 4C C4 DE 20 AF 3D B3 20 F8 50 B4 0E 78 0E 0E 1E 8C 56 02 21 10 5B 61 39 52 25 2F 31 38 37 31 34 66 66 39 2D 61 30 39 39 2D 34 61 38 64 2D 38 34 39 62 2D 38 37 35 65 65 30 36 65 34 64 32 36 5A 25 2F 31 38 37 31 34 66 66 39 2D 61 30 39 39 2D 34 61 38 64 2D 38 34 39 62 2D 38 37 35 65 65 30 36 65 34 64 32 36 60 00 68 80 80 08 20 01
            if (data.size == 285) {
                return ServerTryGetImageIDSuccessPacket(data.toReadPacket()).applySequence(sequenceId)
            }

            return ServerTryGetImageIDFailedPacket(data.toReadPacket()).applySequence(sequenceId)
        }
    }
}

fun main() {
    //GlobalSysTemp:II%E]PA}OVFK]61EGGF$356.jpg
    //实际文件名为 II%E]PA}OVFK]61EGGF$356.jpg

    println(ClientSubmitImageFilenamePacket(
            1994701021u,
            1040400290u,
            "testfilename.png",
            "99 82 67 D4 62 20 CA 5D 81 F8 6F 83 EE 8A F7 68".hexToBytes()

    ).packet.readBytes().toUHexString())

    val data = "00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03 4A 80 01 B5 29 1A 1B 0E 63 79 8B 34 B1 4E 2A 2A 9E 69 09 A7 69 F5 C6 4F 95 DA 96 A9 1B E3 CD 6F 3D 30 EE 59 C0 30 22 BF F0 2D 88 2D A7 6C B2 09 AD D6 CE E1 46 84 FC 7D 19 AF 1A 37 91 98 AD 2C 45 25 AA 17 2F 81 DC 5A 7F 30 F4 2D 73 E5 1C 8B 8A 23 85 42 9D 8D 5C 18 15 32 D1 CA A3 4D 01 7C 59 11 73 DA B6 09 C2 6D 58 35 EF 48 88 44 0F 2D 17 09 52 DF D4 EA A7 85 2F 27 CE DF A8 F5 9B CD C9 84 C2 52 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 5A 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 60 00 68 80 80 08 20 01".hexToBytes()
    println(ServerTryGetImageIDSuccessPacket(data.toReadPacket()).applySequence(1u).also { it.decode() })

    println("01ee6426-5ff1-4cf0-8278-e8634d2909e".toByteArray().toUHexString())

    "5A 25 2F 36 61 38 35 32 66 64 65 2D 38 32 38 35 2D 34 33 35 31 2D 61 65 65 38 2D 35 34 65 37 35 65 65 32 65 61 37 63 60 00 68 80 80 08 20 01"
            .printStringFromHex()

    "25 2F ".hexToBytes().read {
        println(readUnsignedVarInt())
    }
}

/**
 * 服务器未存有图片, 返回一个 key 用于客户端上传
 */
@PacketId(0x03_52u)
class ServerTryGetImageIDSuccessPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    lateinit var uKey: ByteArray
    var imageId: ImageId by Delegates.notNull()

    override fun decode() = with(input) {
        //00 00 00 08 00 00 01 0D 12 06 98 01 01 A0 01 00 08 01 12 86 02 08 00 10 AB A7 89 D8 02 18 00 28 00 38 B4 C7 E6 B0 02 38 F1 C0 A1 BF 05 38 FB AE FA 95 0A 38 E5 C6 BF EC 06 40 B0 6D 40 90 3F 40 50 40 BB 03
        // 4A [80 01] B5 29 1A 1B 0E 63 79 8B 34 B1 4E 2A 2A 9E 69 09 A7 69 F5 C6 4F 95 DA 96 A9 1B E3 CD 6F 3D 30 EE 59 C0 30 22 BF F0 2D 88 2D A7 6C B2 09 AD D6 CE E1 46 84 FC 7D 19 AF 1A 37 91 98 AD 2C 45 25 AA 17 2F 81 DC 5A 7F 30 F4 2D 73 E5 1C 8B 8A 23 85 42 9D 8D 5C 18 15 32 D1 CA A3 4D 01 7C 59 11 73 DA B6 09 C2 6D 58 35 EF 48 88 44 0F 2D 17 09 52 DF D4 EA A7 85 2F 27 CE DF A8 F5 9B CD C9 84 C2 // 52 [25] 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 5A 25 2F 30 31 65 65 36 34 32 36 2D 35 66 66 31 2D 34 63 66 30 2D 38 32 37 38 2D 65 38 36 33 34 64 32 39 30 39 65 66 60 00 68 80 80 08 20 01

        discardExact(68)

        discardExact(1)//4A, id
        uKey = readBytes(readUnsignedVarInt().toInt())//128

        discardExact(1)//52, id
        imageId = ImageId(readString(readUnsignedVarInt().toInt()))//37

        DebugLogger.logPurple("获得 uKey(${uKey.size})=${uKey.toUHexString()}")
        DebugLogger.logPurple("获得 imageId(${imageId.value.length})=${imageId}")
        println("ServerTryGetImageIDSuccessPacket后文=" + readRemainingBytes().toUHexString())
    }
}

/**
 * 服务器已经存有这个图片
 */
@PacketId(0x03_52u)
class ServerTryGetImageIDFailedPacket(input: ByteReadPacket) : ServerTryGetImageIDResponsePacket(input) {
    override fun decode(): Unit = with(input) {
        readRemainingBytes().debugPrint("ServerTryGetImageIDFailedPacket的body")
    }
}

suspend fun Group.uploadImage(image: PlatformImage): ImageId = this.bot.network.session.uploadGroupImage(number, image)

suspend fun QQ.uploadImage(image: PlatformImage): ImageId = this.bot.network.session.uploadFriendImage(number, image)

/**
 * 需要在外 timeout 处理
 */
suspend fun BotSession.uploadFriendImage(qq: UInt, image: PlatformImage): ImageId {
    ClientSubmitImageFilenamePacket(account, qq, "sdiovaoidsa.png", sessionKey).sendAndExpect<ServerSubmitImageFilenameResponsePacket, Unit> {

    }.join()
    return ClientTryGetImageIDPacket(account, sessionKey, qq, image).sendAndExpect<ServerTryGetImageIDResponsePacket, ImageId> {
        when (it) {
            is ServerTryGetImageIDFailedPacket -> {
                //服务器已存有图片
                ImageId("UNKNOWN")
            }

            is ServerTryGetImageIDSuccessPacket -> {
                val data = image.toByteArray()
                require(httpPostFriendImage(
                        uKeyHex = it.uKey.toUHexString(""),
                        botNumber = bot.qqAccount,
                        fileSize = data.size,
                        imageData = data,
                        qq = qq
                ))
                it.imageId
            }
        }
    }.await()
}


suspend fun BotSession.uploadGroupImage(groupNumberOrAccount: UInt, image: PlatformImage): ImageId {
    TODO()
}