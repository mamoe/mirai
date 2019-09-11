package net.mamoe.mirai.network.packet.image

import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.toByteArray
import net.mamoe.mirai.utils.writeUVarInt
import java.awt.image.BufferedImage

/**
 * 查询群消息的 image id.
 * That is, 查询服务器上是否有这个图片, 有就返回 id, 没有就需要上传
 *
 * @author Him188moe
 */
@PacketId("03 88")
@ExperimentalUnsignedTypes
class ClientGetGroupImageIDPacket(
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

            it.writeHex("6A 05")
            it.writeHex("32 36 36 35 36 05")

            it.writeHex("70 00")

            it.writeHex("78 03")

            it.writeHex("80 01")

            it.writeHex("00")
        }
    }
}

fun main() {

    println(0xff)
}