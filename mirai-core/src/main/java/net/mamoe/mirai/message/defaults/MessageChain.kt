package net.mamoe.mirai.message.defaults

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageId
import net.mamoe.mirai.utils.lazyEncode
import org.intellij.lang.annotations.MagicConstant
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class MessageChain : Message {
    override val type: Int = MessageId.CHAIN

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

    fun size(): Int {
        return list.size
    }

    fun containsType(@MagicConstant(valuesFromClass = MessageId::class) type: Int): Boolean {
        for (message in list) {
            if (message.type == type) {
                return true
            }
        }
        return false
    }

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
        this.list.add(tail)
        clearToStringCache()
        return this
    }

    override fun toChain(): MessageChain {
        return this
    }

    override fun toByteArray(): ByteArray = lazyEncode {
        stream().forEach { message ->
            it.write(message.toByteArray())
        }
    }

    override fun valueEquals(another: Message): Boolean {
        if (another !is MessageChain) {
            return false
        }
        return this.list == another.list
    }
}
