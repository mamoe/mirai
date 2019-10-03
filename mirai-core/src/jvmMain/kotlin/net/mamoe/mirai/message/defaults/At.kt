package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageKey

/**
 * At 一个人
 *
 * @author Him188moe
 */
class At(val target: Long) : Message() {
    companion object Key : MessageKey(0x06)

    override val type: MessageKey = Key

    constructor(target: QQ) : this(target.number)

    override fun toStringImpl(): String = "[@$target]"

    override fun toByteArray(): ByteArray {
        TODO()
    }

    override operator fun contains(sub: String): Boolean = false

    override fun eq(another: Message): Boolean {
        if (another !is At) {
            return false
        }

        return another.target == this.target
    }
}
