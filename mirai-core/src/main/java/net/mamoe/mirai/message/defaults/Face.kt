package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.FaceID
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId

/**
 * QQ 自带表情
 *
 * @author Him188moe
 */
class Face(val id: FaceID?) : Message() {
    override val type: Int = MessageId.FACE

    override fun toStringImpl(): String {
        return if (id == null) {
            "[face?]"

        } else String.format("[face%d]", id.id)
    }

    override fun toByteArray(): ByteArray {
        TODO()
    }

    override fun valueEquals(another: Message): Boolean {
        if (another !is Face) {
            return false
        }
        return this.id == another.id
    }
}

