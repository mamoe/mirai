package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageKey
import net.mamoe.mirai.network.protocol.tim.packet.readLVByteArray
import net.mamoe.mirai.network.protocol.tim.packet.readNBytes
import net.mamoe.mirai.utils.dataDecode
import net.mamoe.mirai.utils.dataEncode
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.reflect.KClass

class MessageChain : Message {
    companion object Key : MessageKey(0xff)//only used to compare

    override val type: MessageKey = Key

    /**
     * Elements will not be instances of [MessageChain]
     */
    val list: MutableList<Message> = Collections.synchronizedList(LinkedList<Message>())

    constructor(head: Message, tail: Message) {
        Objects.requireNonNull(head)
        Objects.requireNonNull(tail)

        list.add(head)
        list.add(tail)
    }

    constructor(message: Message) {
        Objects.requireNonNull(message)
        list.add(message)
    }

    constructor(messages: Collection<Message>) {
        list.addAll(messages)
    }

    constructor()

    /**
     * 获取第一个这个类型的消息
     */
    operator fun get(type: MessageKey): Message? = list.firstOrNull { it.type == type }

    fun size(): Int {
        return list.size
    }

    fun containsType(clazz: KClass<out Message>): Boolean = list.any { clazz.isInstance(it) }
    fun containsType(clazz: Class<out Message>): Boolean = list.any { clazz.isInstance(it) }
    operator fun contains(sub: KClass<out Message>): Boolean = containsType(sub)
    operator fun contains(sub: Class<out Message>): Boolean = containsType(sub)

    fun stream(): Stream<Message> {
        return list.stream()
    }

    override fun toStringImpl(): String {
        return this.list.stream().map { it.toString() }.collect(Collectors.joining(""))
    }

    override fun toObjectString(): String {
        return String.format("MessageChain(%s)", this.list.stream().map { it.toObjectString() }.collect(Collectors.joining(", ")))
    }

    override fun concat(tail: Message): MessageChain {
        if (tail is MessageChain) {
            tail.list.forEach { child -> this.concat(child) }
            return this
        }
        this.list.add(tail)
        clearToStringCache()
        return this
    }

    override fun toChain(): MessageChain {
        return this
    }

    override fun toByteArray(): ByteArray = dataEncode {
        stream().forEach { message ->
            it.write(message.toByteArray())
        }
    }

    override fun eq(another: Message): Boolean {
        if (another !is MessageChain) {
            return false
        }
        return this.list == another.list
    }

    override operator fun contains(sub: String): Boolean = list.any { it.contains(sub) }

    operator fun component1(): Message = this.list[0]
    operator fun component2(): Message = this.list[1]
    operator fun component3(): Message = this.list[2]

    object PacketHelper {
        @JvmStatic
        fun ofByteArray(byteArray: ByteArray): MessageChain = dataDecode(byteArray) {
            it.readMessageChain()
        }
    }
}

fun DataInputStream.readMessage(): Message? {
    val messageType = this.readByte().toInt()
    val sectionLength = this.readShort().toLong()//sectionLength: short
    val sectionData = this.readNBytes(sectionLength)
    return when (messageType) {
        0x01 -> PlainText.PacketHelper.ofByteArray(sectionData)
        0x02 -> Face.PacketHelper.ofByteArray(sectionData)
        0x03 -> Image.PacketHelper.ofByteArray0x03(sectionData)
        0x06 -> Image.PacketHelper.ofByteArray0x06(sectionData)


        0x19 -> {//长文本
            val value = readLVByteArray()
            //todo 未知压缩算法
            PlainText(String(value))

            // PlainText(String(GZip.uncompress( value)))
        }


        0x14 -> {//长文本
            val value = readLVByteArray()
            println(value.size)
            println(value.toUHexString())
            //todo 未知压缩算法
            this.skip(7)//几个TLV
            return PlainText(String(value))
        }

        0x0E -> {
            //null
            null
        }

        else -> {
            println("未知的messageType=0x${messageType.toByte().toUHexString()}")
            println("后文=${this.readAllBytes().toUHexString()}")
            null
        }
    }

}

fun DataInputStream.readMessageChain(): MessageChain {
    val chain = MessageChain()
    var got: Message? = null
    do {
        if (got != null) {
            chain.concat(got)
        }
        if (this.available() == 0) {
            return chain
        }
        got = this.readMessage()
    } while (got != null)
    return chain
}