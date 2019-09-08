package net.mamoe.mirai.message

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.defaults.At
import net.mamoe.mirai.message.defaults.Image
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.message.defaults.PlainText
import java.awt.image.BufferedImage
import java.io.File
import java.util.*


/**
 * 可发送的或从服务器接收的消息.
 * 采用这样的消息模式是因为 QQ 的消息多元化, 一条消息中可包含 [纯文本][PlainText], [图片][Image] 等.
 *
 * 在 Kotlin, 使用 [Message] 与使用 [String] 几乎没有什么用法上的区别.
 *
 * @author Him188moe
 * @see Contact.sendMessage
 */
abstract class Message {
    internal abstract val type: Int

    private var toStringCache: String? = null
    private val cacheLock = object : Any() {}

    internal abstract fun toStringImpl(): String

    /**
     * 得到用户层的文本消息. 如:
     * - [PlainText] 得到 消息内容
     * - [Image] 得到 "{ID}.png"
     * - [At] 得到 "[@qq]"
     */
    final override fun toString(): String {
        synchronized(cacheLock) {
            if (toStringCache != null) {
                return toStringCache!!
            }

            this.toStringCache = toStringImpl()
            return toStringCache!!
        }
    }

    internal fun clearToStringCache() {
        synchronized(cacheLock) {
            toStringCache = null
        }
    }

    /**
     * 得到类似 "PlainText(内容)", "Image(ID)"
     */
    open fun toObjectString(): String {
        return this.javaClass.simpleName + String.format("(%s)", this.toString())
    }

    /**
     * 转换为数据包使用的 byte array
     */
    abstract fun toByteArray(): ByteArray


    /**
     * 比较两个 Message 的内容是否相等. 如:
     * - [PlainText] 比较 [PlainText.text]
     * - [Image] 比较 [Image.imageID]
     */
    abstract infix fun valueEquals(another: Message): Boolean

    /**
     * 将这个消息的 [toString] 与 [another] 比较
     */
    infix fun valueEquals(another: String): Boolean = this.toString() == another

    /**
     * 把这个消息连接到另一个消息的头部. 相当于字符串相加
     *
     *
     * Connects this Message to the head of another Message.
     * That is, another message becomes the tail of this message.
     * This method does similar to [String.concat]
     *
     *
     * E.g.:
     * PlainText a = new PlainText("Hello ");
     * PlainText b = new PlainText("world");
     * PlainText c = a.concat(b);
     *
     *
     * the text of c is "Hello world"
     *
     * @param tail tail
     * @return message connected
     */
    open fun concat(tail: Message): Message {
        return MessageChain(this, Objects.requireNonNull(tail))
    }

    fun concat(tail: String): Message {
        return concat(PlainText(tail))
    }


    fun withImage(imageId: String): Message {

        // TODO: 2019/9/1
        return this
    }

    fun withImage(image: BufferedImage): Message {
        // TODO: 2019/9/1
        return this

    }

    fun withImage(image: File): Message {
        // TODO: 2019/9/1
        return this
    }

    fun withAt(target: QQ): Message {
        this.concat(target.at())
        return this
    }

    fun withAt(target: Int): Message {
        this.concat(At(target.toLong()))
        return this
    }

    open fun toChain(): MessageChain {
        return MessageChain(this)
    }


    /* For Kotlin */

    /**
     * 实现使用 '+' 操作符连接 [Message] 与 [Message]
     */
    infix operator fun plus(another: Message): Message = this.concat(another)

    /**
     * 实现使用 '+' 操作符连接 [Message] 与 [String]
     */
    infix operator fun plus(another: String): Message = this.concat(another)

    /**
     * 实现使用 '+' 操作符连接 [Message] 与 [Number]
     */
    infix operator fun plus(another: Number): Message = this.concat(another.toString())

    /**
     * 连接 [String] 与 [Message]
     */
    fun String.concat(another: Message): Message = PlainText(this).concat(another)

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false

        if (type != other.type) return false

        return this.toString() == other.toString()
    }
}