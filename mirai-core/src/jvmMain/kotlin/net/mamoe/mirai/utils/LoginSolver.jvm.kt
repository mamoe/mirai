/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.io.jvm.nio.copyTo
import kotlinx.coroutines.io.reader
import kotlinx.coroutines.io.writeFully
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.core.use
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.NoStandardInputForCaptchaException
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext

/**
 * 自动选择 [SwingSolver] 或 [StandardCharImageLoginSolver]
 */
@MiraiExperimentalAPI
public class DefaultLoginSolver(
    public val input: suspend () -> String,
    public val overrideLogger: MiraiLogger? = null
) : LoginSolver() {
    private val delegate: LoginSolver

    init {
        if (WindowHelperJvm.isDesktopSupported) {
            delegate = SwingSolver
        } else {
            delegate = StandardCharImageLoginSolver(input, overrideLogger)
        }
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return delegate.onSolvePicCaptcha(bot, data)
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return delegate.onSolveSliderCaptcha(bot, url)
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return delegate.onSolveUnsafeDeviceLoginVerify(bot, url)
    }
}

/**
 * 使用字符图片展示验证码, 使用 [input] 获取输入, 使用 [overrideLogger] 输出
 */
@MiraiExperimentalAPI
public class StandardCharImageLoginSolver(
    input: suspend () -> String,
    /**
     * 为 `null` 时使用 [Bot.logger]
     */
    private val overrideLogger: MiraiLogger? = null
) : LoginSolver() {
    private val input: suspend () -> String = suspend {
        withContext(Dispatchers.IO) { input() }
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? = loginSolverLock.withLock {
        val logger = overrideLogger ?: bot.logger
        val tempFile: File = createTempFile(suffix = ".png").apply { deleteOnExit() }
        withContext(Dispatchers.IO) {
            tempFile.createNewFile()
            logger.info("需要图片验证码登录, 验证码为 4 字母")
            try {
                tempFile.writeChannel().apply { writeFully(data); close() }
                logger.info("将会显示字符图片. 若看不清字符图片, 请查看文件 ${tempFile.absolutePath}")
            } catch (e: Exception) {
                logger.info("无法写出验证码文件(${e.message}), 请尝试查看以上字符图片")
            }

            tempFile.inputStream().use {
                try {
                    val img = ImageIO.read(it)
                    if (img == null) {
                        logger.info("无法创建字符图片. 请查看文件")
                    } else {
                        logger.info(img.createCharImg())
                    }
                } catch (throwable: Throwable) {
                    logger.info("创建字符图片时出错(${throwable.toString()})。请查看文件")
                }
            }
        }
        logger.info("请输入 4 位字母验证码. 若要更换验证码, 请直接回车")
        return input().takeUnless { it.isEmpty() || it.length != 4 }.also {
            logger.info("正在提交[$it]中...")
        }
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? = loginSolverLock.withLock {
        val logger = overrideLogger ?: bot.logger
        logger.info("需要滑动验证码")
        logger.info("请在任意浏览器中打开以下链接并完成验证码. ")
        logger.info("完成后请输入任意字符 ")
        logger.info(url)
        return input().also {
            logger.info("正在提交中...")
        }
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? = loginSolverLock.withLock {
        val logger = overrideLogger ?: bot.logger
        logger.info("需要进行账户安全认证")
        logger.info("该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题")
        logger.info("完成以下账号认证即可成功登录|理论本认证在mirai每个账户中最多出现1次")
        logger.info("请将该链接在QQ浏览器中打开并完成认证, 成功后输入任意字符")
        logger.info("这步操作将在后续的版本中优化")
        logger.info(url)
        return input().also {
            logger.info("正在提交中...")
        }
    }
}

/**
 * 验证码, 设备锁解决器
 */
public actual abstract class LoginSolver {
    public actual abstract suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String?
    public actual abstract suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String?
    public actual abstract suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String?

    public actual companion object {
        public actual val Default: LoginSolver =

            DefaultLoginSolver({ readLine() ?: throw NoStandardInputForCaptchaException(null) })
    }
}

///////////////////////////////
//////////////// internal
///////////////////////////////

internal fun BotConfiguration.getFileBasedDeviceInfoSupplier(filename: String): ((Context) -> DeviceInfo)? {
    return {
        File(filename).loadAsDeviceInfo(json, it)
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

private val loginSolverLock = Mutex()

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

    fun grayCompare(g1: Int, g2: Int): Boolean =
        kotlin.math.min(g1, g2).toDouble() / kotlin.math.max(g1, g2) >= ignoreRate

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
