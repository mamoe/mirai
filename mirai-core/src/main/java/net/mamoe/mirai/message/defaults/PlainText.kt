package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageKey
import net.mamoe.mirai.network.packet.readLVString
import net.mamoe.mirai.network.packet.writeLVByteArray
import net.mamoe.mirai.network.packet.writeLVString
import net.mamoe.mirai.utils.lazyDecode
import net.mamoe.mirai.utils.lazyEncode

/**
 * @author Him188moe
 */
class PlainText(private val text: String) : Message() {
    companion object Key : MessageKey(0x01)

    override val type: MessageKey = Key

    override fun toStringImpl(): String {
        return text
    }

    override fun toByteArray(): ByteArray = lazyEncode { section ->
        section.writeByte(this.type.intValue)

        section.writeLVByteArray(lazyEncode { child ->
            child.writeByte(0x01)
            child.writeLVString(this.text)
        })
    }

    override fun eq(another: Message): Boolean {
        if (another !is PlainText) {
            return false
        }
        return this.text == another.text
    }

    override operator fun contains(sub: String): Boolean = this.toString().contains(sub)

    internal object PacketHelper {
        fun ofByteArray(data: ByteArray): PlainText = lazyDecode(data) {
            it.skip(1)
            PlainText(it.readLVString())
        }
    }
}
