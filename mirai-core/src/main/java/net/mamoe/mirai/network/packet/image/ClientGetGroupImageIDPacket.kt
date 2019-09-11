package net.mamoe.mirai.network.packet.image

import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.writeProtoInt
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
        val bot: Long,
        val sessionKey: ByteArray,
        val group: Long,
        val image: BufferedImage
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)

        this.writeQQ(bot)
        this.writeHex("04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00")
        this.encryptAndWrite(sessionKey) {
            it.writeHex("00 00 00 07 00 00 00 5E 08 01 12 03 98 01 01 10 01 1A")
            it.writeHex("5A")
            it.writeHex("08")
            it.writeProtoInt(group)
            it.writeHex("08")
            it.writeProtoInt(image.height)
        }
    }
}