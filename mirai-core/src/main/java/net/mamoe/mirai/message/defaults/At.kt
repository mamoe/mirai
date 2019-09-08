package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId

/**
 * At 一个人
 *
 * @author Him188moe
 */
class At(val target: Long) : Message() {
    override val type: Int = MessageId.AT

    constructor(target: QQ) : this(target.number)

    override fun toStringImpl(): String = "[@$target]"

    override fun toByteArray(): ByteArray {
        TODO()
    }

    override fun valueEquals(another: Message): Boolean {
        if (another !is At) {
            return false
        }

        return another.target == this.target
    }
}
