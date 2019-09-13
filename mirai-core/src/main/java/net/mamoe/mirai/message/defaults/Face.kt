package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.FaceID
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import net.mamoe.mirai.network.packet.writeHex
import net.mamoe.mirai.network.packet.writeLVByteArray
import net.mamoe.mirai.utils.lazyEncode

/**
 * QQ 自带表情
 *
 * @author Him188moe
 */
class Face(val id: FaceID) : Message() {
    override val type: Int = MessageId.FACE

    override fun toStringImpl(): String {
        return String.format("[face%d]", id.id)
    }

    override fun toByteArray(): ByteArray = lazyEncode { section ->
        section.writeByte(this.type)

        section.writeLVByteArray(lazyEncode { child ->
            child.writeShort(1)
            child.writeByte(this.id.id)

            child.writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

            child.writeShort(2)
            child.writeByte(0x14)//??
            child.writeByte(this.id.id + 65)
        })
    }


    override fun valueEquals(another: Message): Boolean {
        if (another !is Face) {
            return false
        }
        return this.id == another.id
    }
}

