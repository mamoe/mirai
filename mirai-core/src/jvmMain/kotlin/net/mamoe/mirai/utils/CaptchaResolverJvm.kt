package net.mamoe.mirai.utils

import io.ktor.util.cio.writeChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.core.IoBuffer
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min


/**
 * 让用户处理验证码
 *
 * @return 用户输入得到的验证码
 */
internal actual suspend fun solveCaptcha(captchaBuffer: IoBuffer): String? = captchaLock.withLock {
    val tempFile = File(System.getProperty("user.dir") + "/temp/Captcha.png").also {
        withContext(Dispatchers.IO) {
            it.createNewFile(); @Suppress("EXPERIMENTAL_API_USAGE")
        it.writeChannel().writeFully(captchaBuffer)
        }
    }
    withContext(Dispatchers.IO) {
        MiraiLogger.info(ImageIO.read(tempFile.inputStream()).createCharImg())
    }
    MiraiLogger.info("需要验证码登录, 验证码为 4 字母")
    try {

        MiraiLogger.info("若看不清字符图片, 请查看 Mirai 目录下 /temp/Captcha.png")
    } catch (e: Exception) {
        MiraiLogger.info("无法写出验证码文件(${e.message}), 请尝试查看以上字符图片")
    }
    MiraiLogger.info("若要更换验证码, 请直接回车")
    readLine()?.takeUnless { it.isEmpty() || it.length != 4 }
}

private val captchaLock = Mutex()


/**
 * @author NaturalHG
 */
@JvmOverloads
internal fun BufferedImage.createCharImg(outputWidth: Int = 100, ignoreRate: Double = 0.95): String {
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

    fun grayCompare(g1: Int, g2: Int): Boolean = min(g1, g2).toDouble() / max(g1, g2) >= ignoreRate

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

