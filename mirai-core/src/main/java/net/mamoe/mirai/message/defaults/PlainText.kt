package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import net.mamoe.mirai.utils.lazyOutput

/**
 * @author Him188moe
 */
class PlainText(private val text: String) : Message() {
    override val type: Int = MessageId.TEXT

    override fun toStringImpl(): String {
        return text
    }

    override fun toByteArray(): ByteArray = lazyOutput { section ->
        section.writeByte(this.type)

        section.writeVarByteArray(lazyOutput { child ->
            child.writeByte(0x01)
            child.writeVarString(this.text)
        })
    }

    override fun valueEquals(another: Message): Boolean {
        if (another !is PlainText) {
            return false
        }
        return this.text == another.text
    }
}
