@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.buildPacket
import kotlinx.io.streams.writePacket
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO
import java.awt.image.BufferedImage as JavaBufferedImage

fun JavaBufferedImage.toMiraiImage(formatName: String = "png"): BufferedImage {
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
    override fun read(): Int = with(this@toJavaImage.data) {
        if (remaining != 0L)
            readByte().toInt()
        else -1
    }
})

/**
 * 将缓存的图片写入流. 注意, 写入后缓存将会被清空.
 */
fun OutputStream.writeImage(image: BufferedImage) = this.writePacket(image.data)