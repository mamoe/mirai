@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.buildPacket
import kotlinx.io.streams.asInput
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO
import java.awt.image.BufferedImage as JavaBufferedImage

fun JavaBufferedImage.toMiraiImage(formatName: String = "gif"): BufferedImage {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()

    val buffer = buildPacket {
        ImageIO.write(this@toMiraiImage, formatName, object : OutputStream() {
            override fun write(b: Int) {
                b.toByte().let {
                    this@buildPacket.writeByte(it)
                    digest.update(it)
                }
            }
        })
    }

    return BufferedImage(width, height, digest.digest(), formatName, buffer)
}

fun BufferedImage.toJavaImage(): JavaBufferedImage = ImageIO.read(object : InputStream() {
    override fun read(): Int = with(this@toJavaImage.input) {
        if (!endOfInput)
            readByte().toInt()
        else -1
    }
})

fun File.toMiraiImage(): BufferedImage {
    val image = ImageIO.getImageReaders(this.inputStream()).asSequence().first()

    val digest = MessageDigest.getInstance("md5")
    digest.reset()
    FileInputStream(this).transferTo(object : OutputStream() {
        override fun write(b: Int) {
            b.toByte().let {
                digest.update(it)
            }
        }
    })

    val dimension = image.defaultReadParam.sourceRenderSize
    return BufferedImage(
        width = dimension.width,
        height = dimension.height,
        md5 = digest.digest(),
        format = image.formatName,
        input = this.inputStream().asInput(IoBuffer.Pool),
        inputSize = this.length()
    )
}