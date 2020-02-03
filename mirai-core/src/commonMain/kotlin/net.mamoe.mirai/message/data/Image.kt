@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable

sealed class Image : Message {
    abstract val md5: ByteArray

    abstract override fun toString(): String

    companion object Key : Message.Key<Image>

    abstract override fun eq(other: Message): Boolean
}

abstract class CustomFace : Image() {
    abstract val filepath: String
    abstract val fileId: Int
    abstract val serverIp: Int
    abstract val serverPort: Int
    abstract val fileType: Int
    abstract val signature: ByteArray
    abstract val useful: Int
    abstract override val md5: ByteArray
    abstract val bizType: Int
    abstract val imageType: Int
    abstract val width: Int
    abstract val height: Int
    abstract val source: Int
    abstract val size:Int
    abstract val pbReserve: ByteArray

    override fun toString(): String {
        return "[CustomFace]"
    }

    override fun eq(other: Message): Boolean {
        return this.toString() == other.toString()
    }
}

@Serializable
data class CustomFaceFromFile(
    override val filepath: String,
    override val fileId: Int,
    override val serverIp: Int,
    override val serverPort: Int,
    override val fileType: Int,
    override val signature: ByteArray,
    override val useful: Int,
    override val md5: ByteArray,
    override val bizType: Int,
    override val imageType: Int,
    override val width: Int,
    override val height: Int,
    override val source: Int,
    override val size: Int,
    override val pbReserve: ByteArray
) : CustomFace() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomFaceFromFile

        if (filepath != other.filepath) return false
        if (fileId != other.fileId) return false
        if (serverIp != other.serverIp) return false
        if (serverPort != other.serverPort) return false
        if (fileType != other.fileType) return false
        if (!signature.contentEquals(other.signature)) return false
        if (useful != other.useful) return false
        if (!md5.contentEquals(other.md5)) return false
        if (bizType != other.bizType) return false
        if (imageType != other.imageType) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (source != other.source) return false
        if (size != other.size) return false
        if (!pbReserve.contentEquals(other.pbReserve)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filepath.hashCode()
        result = 31 * result + fileId
        result = 31 * result + serverIp
        result = 31 * result + serverPort
        result = 31 * result + fileType
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + useful
        result = 31 * result + md5.contentHashCode()
        result = 31 * result + bizType
        result = 31 * result + imageType
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + source
        result = 31 * result + size
        result = 31 * result + pbReserve.contentHashCode()
        return result
    }
}

abstract class NotOnlineImage : Image() {
    abstract val resourceId: String
    abstract override val md5: ByteArray
    abstract val filepath: String
    abstract val fileLength: Int
    abstract val height: Int
    abstract val width: Int
    open val bizType: Int get() = 0
    open val imageType: Int get() = 1000
    open val downloadPath: String get() = resourceId

    override fun toString(): String {
        return "[NotOnlineImage $resourceId]"
    }

    override fun eq(other: Message): Boolean {
        return other.toString() == this.toString()
    }
}

data class NotOnlineImageFromFile(
    override val resourceId: String,
    override val md5: ByteArray,
    override val filepath: String,
    override val fileLength: Int,
    override val height: Int,
    override val width: Int,
    override val bizType: Int = 0,
    override val imageType: Int = 1000,
    override val downloadPath: String = resourceId
) : NotOnlineImage() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NotOnlineImageFromFile

        if (resourceId != other.resourceId) return false
        if (!md5.contentEquals(other.md5)) return false
        if (filepath != other.filepath) return false
        if (fileLength != other.fileLength) return false
        if (height != other.height) return false
        if (width != other.width) return false
        if (bizType != other.bizType) return false
        if (imageType != other.imageType) return false
        if (downloadPath != other.downloadPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resourceId.hashCode()
        result = 31 * result + md5.contentHashCode()
        result = 31 * result + filepath.hashCode()
        result = 31 * result + fileLength
        result = 31 * result + height
        result = 31 * result + width
        result = 31 * result + bizType
        result = 31 * result + imageType
        result = 31 * result + downloadPath.hashCode()
        return result
    }
}