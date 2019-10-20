@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.buildPacket
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO

fun BufferedImage.toPlatformImage(formatName: String = "png"): PlatformImage {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()

    val buffer = buildPacket {
        ImageIO.write(this@toPlatformImage, formatName, object : OutputStream() {
            override fun write(b: Int) {
                b.toByte().let {
                    this@buildPacket.writeByte(it)
                    digest.update(it)
                }
            }
        })
    }

    return PlatformImage(this.width, this.height, digest.digest(), formatName, buffer)
}

actual val PlatformImage.imageWidth: Int get() = this.width

actual val PlatformImage.imageHeight: Int get() = this.height