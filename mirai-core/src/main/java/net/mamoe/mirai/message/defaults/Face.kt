package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.FaceID
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageKey
import net.mamoe.mirai.network.packet.readLVNumber
import net.mamoe.mirai.network.packet.writeHex
import net.mamoe.mirai.network.packet.writeLVByteArray
import net.mamoe.mirai.utils.lazyDecode
import net.mamoe.mirai.utils.lazyEncode

/**
 * QQ 自带表情
 *
 * @author Him188moe
 */
class Face(val id: FaceID) : Message() {
    companion object Key : MessageKey(0x02)

    override val type: MessageKey = Key

    override fun toStringImpl(): String {
        return String.format("[face%d]", id.id)
    }

    override fun toByteArray(): ByteArray = lazyEncode { section ->
        section.writeByte(this.type.intValue)

        section.writeLVByteArray(lazyEncode { child ->
            child.writeShort(1)
            child.writeByte(this.id.id)

            child.writeHex("0B 00 08 00 01 00 04 52 CC F5 D0 FF")

            child.writeShort(2)
            child.writeByte(0x14)//??
            child.writeByte(this.id.id + 65)
        })
    }


    override fun eq(another: Message): Boolean {
        if (another !is Face) {
            return false
        }
        return this.id == another.id
    }

    override operator fun contains(sub: String): Boolean = false

    internal object PacketHelper {
        fun ofByteArray(data: ByteArray): Face = lazyDecode(data) {
            //00  01  AF  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  F0
            //00  01  0C  0B  00  08  00  01  00  04  52  CC  F5  D0  FF  00  02  14  4D
            it.skip(1)

            val id1 = FaceID.ofId(it.readLVNumber().toInt())//可能这个是id, 也可能下面那个
            it.skip(it.readByte().toLong())
            it.readLVNumber()//某id?
            return@lazyDecode Face(id1)
        }
    }
}

