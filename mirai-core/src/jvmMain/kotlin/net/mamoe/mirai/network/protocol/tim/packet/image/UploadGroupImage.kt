@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.image

import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.writeUVarInt
import java.awt.image.BufferedImage
import java.io.DataInputStream

/**
 * 请求上传图片. 将发送图片的 md5, size.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片 [ServerTryGetImageIDFailedPacket]
 * - 服务器未存有, 返回一个 key 用于客户端上传 [ServerTryGetImageIDSuccessPacket]
 *
 * @author Him188moe
 */
@PacketId("03 88")
class ClientTryGetImageIDPacket(
        private val botNumber: Long,
        private val sessionKey: ByteArray,
        private val groupNumberOrQQNumber: Long,
        private val image: BufferedImage
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)

        this.writeQQ(botNumber)
        this.writeHex("04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00")

        val byteArray = image.toByteArray()
        this.encryptAndWrite(sessionKey) {
            writeZero(3)

            writeHex("07 00")

            writeZero(2)

            writeHex("5E")
            writeHex("08")
            writeHex("01 12 03 98 01 01 10 01")

            writeHex("1A")
            writeHex("5A")

            writeHex("08")
            writeUVarInt(groupNumberOrQQNumber)

            writeHex("10")
            writeUVarInt(botNumber)

            writeHex("18 00")

            writeHex("22")
            writeHex("10")
            write(md5(byteArray))

            writeHex("28")
            writeUVarInt(byteArray.size.toUInt())

            writeHex("32")
            writeHex("1A")
            writeHex("37 00 4D 00 32 00 25 00 4C 00 31 00 56 00 32 00 7B 00 39 00 30 00 29 00 52 00")

            writeHex("38 01")

            writeHex("48 01")

            writeHex("50")
            writeUVarInt(image.width.toUInt())
            writeHex("58")
            writeUVarInt(image.height.toUInt())

            writeHex("60 04")

            writeHex("6A")
            writeHex("05")
            writeHex("32 36 36 35 36")

            writeHex("70 00")

            writeHex("78 03")

            writeHex("80 01")

            writeHex("00")
        }
    }
}

abstract class ServerTryGetImageIDResponsePacket(input: DataInputStream) : ServerPacket(input) {

    class Encrypted(input: DataInputStream) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerTryGetImageIDResponsePacket {
            val data = this.decryptAsByteArray(sessionKey)
            println(data.size)
            println(data.size)
            if (data.size == 209) {
                return ServerTryGetImageIDSuccessPacket(data.dataInputStream()).setId(this.idHex)
            }

            return ServerTryGetImageIDFailedPacket(data.dataInputStream())
        }
    }
}

/**
 * 服务器未存有图片, 返回一个 key 用于客户端上传
 */
class ServerTryGetImageIDSuccessPacket(input: DataInputStream) : ServerTryGetImageIDResponsePacket(input) {
    lateinit var uKey: ByteArray


    override fun decode() {
        this.input.gotoWhere(ubyteArrayOf(0x42u, 0x80u, 0x01u))
        uKey = this.input.readNBytes(128)
    }
}

/**
 * 服务器已经存有这个图片
 */
class ServerTryGetImageIDFailedPacket(input: DataInputStream) : ServerTryGetImageIDResponsePacket(input) {
    override fun decode() {

    }
}