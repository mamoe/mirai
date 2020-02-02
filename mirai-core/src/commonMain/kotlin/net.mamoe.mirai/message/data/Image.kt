@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

sealed class Image : Message {
    abstract val resourceId: String

    abstract override fun toString(): String

    final companion object Key : Message.Key<Image>

    abstract override fun eq(other: Message): Boolean
}

abstract class NotOnlineImage : Image() {
    abstract override val resourceId: String
    abstract val md5: ByteArray
    abstract val filepath: String
    abstract val fileLength: Int
    abstract val height: Int
    abstract val width: Int
    open val bizType: Int get() = 0
    open val imageType: Int get() = 1000
    open val downloadPath: String get() = resourceId

    override fun toString(): String {
        return "[$resourceId]"
    }

    override fun eq(other: Message): Boolean {
        return other.toString() == this.toString()
    }
}

open class NotOnlineImageFromFile(
    override val resourceId: String,
    override val md5: ByteArray,
    override val filepath: String,
    override val fileLength: Int,
    override val height: Int,
    override val width: Int,
    override val bizType: Int = 0,
    override val imageType: Int = 1000,
    override val downloadPath: String = resourceId
) : NotOnlineImage()