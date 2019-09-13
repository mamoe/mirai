package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import net.mamoe.mirai.network.packet.writeHex
import net.mamoe.mirai.network.packet.writeLVByteArray
import net.mamoe.mirai.network.packet.writeLVString
import net.mamoe.mirai.utils.lazyEncode

/**
 * 图片消息.
 * 由接收消息时构建, 可直接发送
 *
 * @author Him188moe
 */
open class Image internal constructor(val imageID: String) : Message() {
    override val type: Int = MessageId.IMAGE

    override fun toStringImpl(): String {
        return imageID
    }

    override fun toByteArray(): ByteArray = lazyEncode { section ->
        section.writeByte(0x03)//todo 可能是 0x03?

        section.writeLVByteArray(lazyEncode { child ->
            child.writeByte(0x02)
            child.writeLVString(this.imageID)
            child.writeHex("04 00 " +
                    "04 9B 53 B0 08 " +
                    "05 00 " +
                    "04 D9 8A 5A 70 " +
                    "06 00 " +
                    "04 00 00 00 50 " +
                    "07 00 " +
                    "01 43 08 00 00 09 00 01 01 0B 00 00 14 00 04 11 00 00 00 15 00 04 00 00 02 BC 16 00 04 00 00 02 BC 18 00 04 00 00 7D 5E FF 00 5C 15 36 20 39 32 6B 41 31 43 39 62 35 33 62 30 30 38 64 39 38 61 35 61 37 30 20")
            child.writeHex("20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20")
            child.writeBytes(this.imageID)
            child.writeByte(0x41)
        })
    }

    override fun valueEquals(another: Message): Boolean {
        if (another is Image) {
            return this.imageID == another.imageID
        }

        return false
    }
}