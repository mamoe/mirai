@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.io.chunkedHexToBytes
import kotlin.jvm.JvmName

/**
 * 自定义表情 (收藏的表情), 图片
 */
sealed class Image : Message {
    companion object Key : Message.Key<Image> {
        @JvmName("fromId")
        operator fun invoke(miraiImageId: String): Image = when (miraiImageId.length) {
            37 -> NotOnlineImageFromFile(miraiImageId) // /f8f1ab55-bf8e-4236-b55e-955848d7069f
            42 -> CustomFaceFromFile(miraiImageId) // {01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
            else -> throw IllegalArgumentException("Bad miraiImageId, expecting length=37 or 42, got ${miraiImageId.length}")
        }
    }

    abstract val miraiImageId: String
    abstract override fun toString(): String

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
    abstract val md5: ByteArray
    abstract val bizType: Int
    abstract val imageType: Int
    abstract val width: Int
    abstract val height: Int
    abstract val source: Int
    abstract val size: Int
    abstract val pbReserve: ByteArray
    abstract val original: Int

    override fun toString(): String {
        return "[CustomFace]"
    }

    override fun eq(other: Message): Boolean {
        return this.toString() == other.toString()
    }
}

private val EMPTY_BYTE_ARRAY = ByteArray(0)

private fun calculateImageMd5ByMiraiImageId(miraiImageId: String): ByteArray {
    return if (miraiImageId.startsWith('/')) {
        miraiImageId
            .drop(1)
            .replace('-', ' ')
            .take(16 * 2)
            .chunkedHexToBytes()
    } else {
        miraiImageId
            .substringAfter('{')
            .substringBefore('}')
            .replace('-', ' ')
            .chunkedHexToBytes()
    }
}

@Serializable
data class CustomFaceFromFile(
    override val filepath: String, // {01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png
    override val md5: ByteArray
) : CustomFace() {
    constructor(miraiImageId: String) : this(filepath = miraiImageId, md5 = calculateImageMd5ByMiraiImageId(miraiImageId))

    override val fileId: Int get() = 0
    override val serverIp: Int get() = 0
    override val serverPort: Int get() = 0
    override val fileType: Int get() = 0 // 0
    override val signature: ByteArray get() = EMPTY_BYTE_ARRAY
    override val useful: Int get() = 1
    override val bizType: Int get() = 0
    override val imageType: Int get() = 0
    override val width: Int get() = 0
    override val height: Int get() = 0
    override val source: Int get() = 200
    override val size: Int get() = 0
    override val original: Int get() = 1
    override val pbReserve: ByteArray get() = EMPTY_BYTE_ARRAY
    override val miraiImageId: String get() = filepath

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
        if (original != this.original) return false
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

/**
 * 电脑可能看不到这个消息.
 */
abstract class NotOnlineImage : Image() {
    abstract val resourceId: String
    abstract val md5: ByteArray
    abstract val filepath: String
    abstract val fileLength: Int
    abstract val height: Int
    abstract val width: Int
    open val bizType: Int get() = 0
    open val imageType: Int get() = 1000
    abstract val fileId: Int
    open val downloadPath: String get() = resourceId
    open val original: Int get() = 1

    override val miraiImageId: String get() = resourceId

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
    override val filepath: String = resourceId,
    override val fileLength: Int = 0,
    override val height: Int = 0,
    override val width: Int = 0,
    override val bizType: Int = 0,
    override val imageType: Int = 1000,
    override val downloadPath: String = resourceId,
    override val fileId: Int = 0
) : NotOnlineImage() {
    constructor(miraiImageId: String) : this(resourceId = miraiImageId, md5 = calculateImageMd5ByMiraiImageId(miraiImageId))

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