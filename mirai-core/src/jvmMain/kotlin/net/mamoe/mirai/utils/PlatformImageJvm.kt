package net.mamoe.mirai.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual typealias PlatformImage = BufferedImage

@JvmOverloads
actual fun BufferedImage.toByteArray(formatName: String): ByteArray = ByteArrayOutputStream().use { ImageIO.write(this, "PNG", it); it.toByteArray() }

actual val PlatformImage.imageWidth: Int get() = this.width

actual val PlatformImage.imageHeight: Int get() = this.height