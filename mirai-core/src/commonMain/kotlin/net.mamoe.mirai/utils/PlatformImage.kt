package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.message.ImageId

data class PlatformImage(
        val width: Int,
        val height: Int,
        val md5: ByteArray,
        val format: String,
        val fileData: ByteReadPacket
) {
    val fileSize: Long = fileData.remaining

    val id: ImageId by lazy { ImageId("{${md5[0..4]}-${md5[0..2]}-${md5[0..2]}-${md5[0..2]}-${md5[0..6]}}.$format") }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformImage) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (!md5.contentEquals(other.md5)) return false
        if (format != other.format) return false
        if (fileData != other.fileData) return false
        if (fileSize != other.fileSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + md5.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + fileData.hashCode()
        result = 31 * result + fileSize.hashCode()
        return result
    }
}

private operator fun ByteArray.get(range: IntRange): String = buildString {
    range.forEach {
        append(this@get[it].toUHexString())
    }
}

expect val PlatformImage.imageWidth: Int

expect val PlatformImage.imageHeight: Int