package net.mamoe.mirai.utils

import io.ktor.util.cio.use
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.jvm.nio.copyTo
import kotlinx.coroutines.io.reader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.core.use
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext

/**
 * 平台默认的验证码识别器.
 *
 * 可被修改, 除覆盖配置外全局生效.
 */
actual var DefaultCaptchaSolver: CaptchaSolver = {
    captchaLock.withLock {
        val tempFile: File = createTempFile(suffix = ".png").apply { deleteOnExit() }
        withContext(Dispatchers.IO) {
            tempFile.createNewFile()
            MiraiLogger.info("需要验证码登录, 验证码为 4 字母")
            try {
                tempFile.writeChannel().use { writeFully(it) }
                MiraiLogger.info("将会显示字符图片. 若看不清字符图片, 请查看文件 ${tempFile.absolutePath}")
            } catch (e: Exception) {
                MiraiLogger.info("无法写出验证码文件(${e.message}), 请尝试查看以上字符图片")
            }

            tempFile.inputStream().use {
                val img = ImageIO.read(it)
                if (img == null) {
                    MiraiLogger.info("无法创建字符图片. 请查看文件")
                } else {
                    MiraiLogger.info(img.createCharImg())
                }
            }
        }
        MiraiLogger.info("请输入 4 位字母验证码. 若要更换验证码, 请直接回车")
        readLine()?.takeUnless { it.isEmpty() || it.length != 4 }
    }
}

// Copied from Ktor CIO
private fun File.writeChannel(
    coroutineContext: CoroutineContext = Dispatchers.IO
): ByteWriteChannel = GlobalScope.reader(CoroutineName("file-writer") + coroutineContext, autoFlush = true) {
    @Suppress("BlockingMethodInNonBlockingContext")
    RandomAccessFile(this@writeChannel, "rw").use { file ->
        val copied = channel.copyTo(file.channel)
        file.setLength(copied) // truncate tail that could remain from the previously written data
    }
}.channel


private val captchaLock = Mutex()

/**
 * @author NaturalHG
 */
private fun BufferedImage.createCharImg(outputWidth: Int = 100, ignoreRate: Double = 0.95): String {
    val newHeight = (this.height * (outputWidth.toDouble() / this.width)).toInt()
    val tmp = this.getScaledInstance(outputWidth, newHeight, Image.SCALE_SMOOTH)
    val image = BufferedImage(outputWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    fun gray(rgb: Int): Int {
        val r = rgb and 0xff0000 shr 16
        val g = rgb and 0x00ff00 shr 8
        val b = rgb and 0x0000ff
        return (r * 30 + g * 59 + b * 11 + 50) / 100
    }

    fun grayCompare(g1: Int, g2: Int): Boolean = kotlin.math.min(g1, g2).toDouble() / kotlin.math.max(g1, g2) >= ignoreRate

    val background = gray(image.getRGB(0, 0))

    return buildString(capacity = height) {

        val lines = mutableListOf<StringBuilder>()

        var minXPos = outputWidth
        var maxXPos = 0

        for (y in 0 until image.height) {
            val builderLine = StringBuilder()
            for (x in 0 until image.width) {
                val gray = gray(image.getRGB(x, y))
                if (grayCompare(gray, background)) {
                    builderLine.append(" ")
                } else {
                    builderLine.append("#")
                    if (x < minXPos) {
                        minXPos = x
                    }
                    if (x > maxXPos) {
                        maxXPos = x
                    }
                }
            }
            if (builderLine.toString().isBlank()) {
                continue
            }
            lines.add(builderLine)
        }
        for (line in lines) {
            append(line.substring(minXPos, maxXPos)).append("\n")
        }
    }
}

