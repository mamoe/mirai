package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import net.mamoe.mirai.message.MessageKey
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.lazyDecode
import net.mamoe.mirai.utils.lazyEncode
import net.mamoe.mirai.utils.skip
import net.mamoe.mirai.utils.toUHexString

/**
 * 图片消息.
 * 由接收消息时构建, 可直接发送
 *
 * @param imageId 类似 `{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg`. 群的是大写id, 好友的是小写id
 *
 * @author Him188moe
 */
open class Image(val imageId: String) : Message() {
    companion object Key : MessageKey(0x03)

    override val type: MessageKey = Key

    override fun toStringImpl(): String {
        return imageId
    }

    override fun toByteArray(): ByteArray = lazyEncode { section ->
        section.writeByte(MessageId.IMAGE)

        section.writeLVByteArray(lazyEncode { child ->
            child.writeByte(0x02)
            child.writeLVString(this.imageId)
            child.writeHex("04 00 " +
                    "04 9B 53 B0 08 " +
                    "05 00 " +
                    "04 D9 8A 5A 70 " +
                    "06 00 " +
                    "04 00 00 00 50 " +
                    "07 00 " +
                    "01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 11 00 00 00 15 00 04 00 00 02 BC 16 00 04 00 00 02 BC 18 00 04 00 00 7D 5E FF 00 5C 15 36 20 39 32 6B 41 31 43 39 62 35 33 62 30 30 38 64 39 38 61 35 61 37 30 20")
            child.writeHex("20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
            child.writeBytes(this.imageId)
            child.writeByte(0x41)
        })
    }

    override fun eq(another: Message): Boolean {
        if (another is Image) {
            return this.imageId == another.imageId
        }

        return false
    }

    override operator fun contains(sub: String): Boolean = false //No string can be contained in a image

    internal object PacketHelper {
        @JvmStatic
        fun ofByteArray0x06(data: ByteArray): Image = lazyDecode(data) {
            it.skip(1)
            println("好友的图片")
            println(data.toUHexString())
            val filenameLength = it.readShort()
            val suffix = it.readString(filenameLength).substringAfter(".")
            it.skip(data.size - 37 - 1 - filenameLength - 2)
            val imageId = String(it.readNBytes(36))
            println(imageId)
            it.skip(1)//0x41
            return@lazyDecode Image("{$imageId}.$suffix")
        }

        @JvmStatic
        fun ofByteArray0x03(data: ByteArray): Image = lazyDecode(data) {
            it.skip(1)
            return@lazyDecode Image(String(it.readLVByteArray()))
            /*
            println(String(it.readLVByteArray()))
            it.readTLVMap()
            return@lazyDecode Image(String(it.readLVByteArray().cutTail(5).getRight(42)))
            /
            it.skip(data.size - 47)
            val imageId = String(it.readNBytes(42))
            it.skip(1)//0x41
            it.skip(1)//0x42
            it.skip(1)//0x43
            it.skip(1)//0x41

            return@lazyDecode Image(imageId)*/
        }
    }
}