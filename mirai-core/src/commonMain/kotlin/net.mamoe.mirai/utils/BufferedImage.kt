@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import net.mamoe.mirai.message.ImageId

fun BufferedImage(
    width: Int,
    height: Int,
    md5: ByteArray,
    format: String,
    data: ByteReadPacket
) = BufferedImage(width, height, md5, format, data, data.remaining)

class BufferedImage(
    val width: Int,
    val height: Int,
    val md5: ByteArray,
    val format: String,
    val input: Input,
    val inputSize: Long
) {

    /**
     * 用于发送消息的 [ImageId]
     */
    val groupImageId: ImageId by lazy { ImageId("{${md5[0..3]}-${md5[4..5]}-${md5[6..7]}-${md5[8..9]}-${md5[10..15]}}.$format") }

    override fun toString(): String = "[BufferedImage(${width}x${height} $format)]"
}

private operator fun ByteArray.get(range: IntRange): String = buildString {
    range.forEach {
        append(this@get[it].toUHexString())
    }
}