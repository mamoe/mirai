@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.packet.image

import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.writeUVarInt
import java.awt.image.BufferedImage
import java.io.DataInputStream

/**
 * 请求上传图片. 将发送图片的 md5, size.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片 [ServerTryUploadGroupImageFailedPacket]
 * - 服务器未存有, 返回一个 key 用于客户端上传 [ServerTryUploadGroupImageSuccessPacket]
 *
 * @author Him188moe
 */
@PacketId("03 88")

class ClientTryGetGroupImageIDPacket(
        private val bot: Long,
        private val sessionKey: ByteArray,
        private val group: Long,
        private val image: BufferedImage
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)

        this.writeQQ(bot)
        this.writeHex("04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00")

        val byteArray = image.toByteArray()
        this.encryptAndWrite(sessionKey) {
            it.writeZero(3)

            it.writeHex("07 00")

            it.writeZero(2)

            it.writeHex("5E")
            it.writeHex("08")
            it.writeHex("01 12 03 98 01 01 10 01")

            it.writeHex("1A")
            it.writeHex("5A")

            it.writeHex("08")
            it.writeUVarInt(group)

            it.writeHex("10")
            it.writeUVarInt(bot)

            it.writeHex("18 00")

            it.writeHex("22")
            it.writeHex("10")
            it.write(md5(byteArray))

            it.writeHex("28")
            it.writeUVarInt(byteArray.size.toUInt())

            it.writeHex("32")
            it.writeHex("1A")
            it.writeHex("37 00 4D 00 32 00 25 00 4C 00 31 00 56 00 32 00 7B 00 39 00 30 00 29 00 52 00")

            it.writeHex("38 01")

            it.writeHex("48 01")

            it.writeHex("50")
            it.writeUVarInt(image.width.toUInt())
            it.writeHex("58")
            it.writeUVarInt(image.height.toUInt())

            it.writeHex("60 04")

            it.writeHex("6A")
            it.writeHex("05")
            it.writeHex("32 36 36 35 36 05")//6?

            it.writeHex("70 00")

            it.writeHex("78 03")

            it.writeHex("80 01")

            it.writeHex("00")
        }
    }
}

abstract class ServerTryUploadGroupImageResponsePacket(input: DataInputStream) : ServerPacket(input) {

    class Encrypted(input: DataInputStream) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerTryUploadGroupImageResponsePacket {
            val data = this.decryptAsByteArray(sessionKey)
            if (data.size == 239) {
                return ServerTryUploadGroupImageSuccessPacket(data.dataInputStream()).setId(this.idHex)
            }

            return ServerTryUploadGroupImageFailedPacket(data.dataInputStream())
        }
    }
}

/**
 * 服务器未存有图片, 返回一个 key 用于客户端上传
 */
class ServerTryUploadGroupImageSuccessPacket(input: DataInputStream) : ServerTryUploadGroupImageResponsePacket(input) {
    lateinit var uKey: ByteArray


    override fun decode() {
        uKey = this.input.gotoWhere(ubyteArrayOf(0x42u, 0x80u, 0x01u)).readNBytes(128)
    }
}

/**
 * 服务器已经存有这个图片
 */
class ServerTryUploadGroupImageFailedPacket(input: DataInputStream) : ServerTryUploadGroupImageResponsePacket(input) {
    override fun decode() {

    }
}

fun main() {

    println(0xff)
}