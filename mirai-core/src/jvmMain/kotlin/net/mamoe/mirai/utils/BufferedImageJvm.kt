@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.buildPacket
import kotlinx.io.streams.asInput
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO
import java.awt.image.BufferedImage as JavaBufferedImage

fun JavaBufferedImage.toExternalImage(formatName: String = "gif"): ExternalImage {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()

    val buffer = buildPacket {
        ImageIO.write(this@toExternalImage, formatName, object : OutputStream() {
            override fun write(b: Int) {
                b.toByte().let {
                    this@buildPacket.writeByte(it)
                    digest.update(it)
                }
            }
        })
    }

    return ExternalImage(width, height, digest.digest(), formatName, buffer)
}

fun File.toExternalImage(): ExternalImage {
    val input = ImageIO.createImageInputStream(this)
    val image = ImageIO.getImageReaders(input).asSequence().firstOrNull() ?: error("Unable to read file(${this.path}), no ImageReader found")
    image.input = input

    return ExternalImage(
        width = image.getWidth(0),
        height = image.getHeight(0),
        md5 = this.md5(),
        imageFormat = image.formatName,
        input = this.inputStream().asInput(IoBuffer.Pool),
        inputSize = this.length()
    )
}