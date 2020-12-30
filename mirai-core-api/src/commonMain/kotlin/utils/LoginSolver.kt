/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.*
import kotlinx.coroutines.io.jvm.nio.copyTo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.NoStandardInputForCaptchaException
import net.mamoe.mirai.utils.LoginSolver.Companion.Default
import net.mamoe.mirai.utils.StandardCharImageLoginSolver.Companion.createBlocking
import net.mamoe.mirai.utils.internal.SeleniumLoginSolver
import net.mamoe.mirai.utils.internal.isSliderCaptchaSupportKind
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext

/**
 * 验证码, 设备锁解决器
 *
 * @see Default
 * @see BotConfiguration.loginSolver
 */
public abstract class LoginSolver {
    /**
     * 处理图片验证码.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止
     *
     * @throws LoginFailedException
     */
    public abstract suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String?

    /**
     * 为 `true` 表示支持滑动验证码, 遇到滑动验证码时 mirai 会请求 [onSolveSliderCaptcha].
     * 否则会跳过滑动验证码并告诉服务器此客户端不支持, 有可能导致登录失败
     */
    public open val isSliderCaptchaSupported: Boolean
        get() = isSliderCaptchaSupportKind ?: true

    /**
     * 处理滑动验证码.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止
     *
     * @throws LoginFailedException
     * @return 验证码解决成功后获得的 ticket.
     */
    public abstract suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String?

    /**
     * 处理不安全设备验证.
     *
     * 返回值保留给将来使用. 目前在处理完成后返回任意内容 (包含 `null`) 均视为处理成功.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止.
     *
     * @return 任意内容. 返回值保留以供未来更新.
     * @throws LoginFailedException
     */
    public abstract suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String?

    public companion object {
        /**
         * 当前平台默认的 [LoginSolver]。
         *
         * 检测策略:
         * 1. 检测 `android.util.Log`, 如果存在, 返回 `null`.
         * 2. 检测 JVM 属性 `mirai.no-desktop`. 若存在, 返回 [StandardCharImageLoginSolver]
         * 3. 检测 JVM 桌面环境, 若支持, 返回 [SwingSolver]
         * 4. 返回 [StandardCharImageLoginSolver]
         *
         * @return [SwingSolver] 或 [StandardCharImageLoginSolver] 或 `null`
         */
        @JvmField
        public val Default: LoginSolver? = when (WindowHelperJvm.platformKind) {
            WindowHelperJvm.PlatformKind.ANDROID -> null
            WindowHelperJvm.PlatformKind.SWING -> {
                when (isSliderCaptchaSupportKind) {
                    null, false -> SwingSolver
                    true -> SeleniumLoginSolver ?: SwingSolver
                }
            }
            WindowHelperJvm.PlatformKind.CLI -> StandardCharImageLoginSolver()
        }

        @Suppress("unused")
        @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
        public fun getDefault(): LoginSolver = Default
            ?: error("LoginSolver is not provided by default on your platform. Please specify by BotConfiguration.loginSolver")
    }

}

/**
 * CLI 环境 [LoginSolver]. 将验证码图片转为字符画并通过 `output` 输出, [input] 获取用户输入.
 *
 * 使用字符图片展示验证码, 使用 [input] 获取输入, 使用 [loggerSupplier] 输出
 *
 * @see createBlocking
 */
public class StandardCharImageLoginSolver @JvmOverloads constructor(
    input: suspend () -> String = { readLine() ?: throw NoStandardInputForCaptchaException() },
    /**
     * 为 `null` 时使用 [Bot.logger]
     */
    private val loggerSupplier: (bot: Bot) -> MiraiLogger = { it.logger }
) : LoginSolver() {
    public constructor(
        input: suspend () -> String = { readLine() ?: throw NoStandardInputForCaptchaException() },
        overrideLogger: MiraiLogger?
    ) : this(input, { overrideLogger ?: it.logger })

    private val input: suspend () -> String = suspend {
        withContext(Dispatchers.IO) { input() }
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? = loginSolverLock.withLock {
        val logger = loggerSupplier(bot)
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            val tempFile: File = File.createTempFile("tmp", ".png").apply { deleteOnExit() }
            tempFile.createNewFile()
            logger.info { "[PicCaptcha] 需要图片验证码登录, 验证码为 4 字母" }
            logger.info { "[PicCaptcha] Picture captcha required. Captcha consists of 4 letters." }
            try {
                tempFile.writeChannel().apply { writeFully(data); close() }
                logger.info { "[PicCaptcha] 将会显示字符图片. 若看不清字符图片, 请查看文件 ${tempFile.absolutePath}" }
                logger.info { "[PicCaptcha] Displaying char-image. If not clear, view file ${tempFile.absolutePath}" }
            } catch (e: Exception) {
                logger.warning("[PicCaptcha] 无法写出验证码文件, 请尝试查看以上字符图片", e)
                logger.warning("[PicCaptcha] Failed to export captcha image. Please see the char-image.", e)
            }

            tempFile.inputStream().use { stream ->
                try {
                    val img = ImageIO.read(stream)
                    if (img == null) {
                        logger.warning { "[PicCaptcha] 无法创建字符图片. 请查看文件" }
                        logger.warning { "[PicCaptcha] Failed to create char-image. Please see the file." }
                    } else {
                        logger.info { "[PicCaptcha] \n" + img.createCharImg() }
                    }
                } catch (throwable: Throwable) {
                    logger.warning("[PicCaptcha] 创建字符图片时出错. 请查看文件.", throwable)
                    logger.warning("[PicCaptcha] Failed to create char-image. Please see the file.", throwable)
                }
            }
        }
        logger.info { "[PicCaptcha] 请输入 4 位字母验证码. 若要更换验证码, 请直接回车" }
        logger.info { "[PicCaptcha] Please type 4-letter captcha. Press Enter directly to refresh." }
        return input().takeUnless { it.isEmpty() || it.length != 4 }.also {
            logger.info { "[PicCaptcha] 正在提交 $it..." }
            logger.info { "[PicCaptcha] Submitting $it..." }
        }
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String = loginSolverLock.withLock {
        val logger = loggerSupplier(bot)
        logger.info { "[SliderCaptcha] 需要滑动验证码, 请在浏览器中打开以下链接并完成验证码, 完成后请输入提示 ticket." }
        logger.info { "[SliderCaptcha] Slider captcha required, please open the following link in any browser and solve the captcha. Type ticket here after completion." }
        logger.info { "[SliderCaptcha] @see https://github.com/project-mirai/mirai-login-solver-selenium#%E4%B8%8B%E8%BD%BD-chrome-%E6%89%A9%E5%B1%95%E6%8F%92%E4%BB%B6" }
        logger.info(url)
        return input().also {
            logger.info { "[SliderCaptcha] 正在提交中..." }
            logger.info { "[SliderCaptcha] Submitting..." }
        }
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String = loginSolverLock.withLock {
        val logger = loggerSupplier(bot)
        logger.info { "[UnsafeLogin] 当前登录环境不安全，服务器要求账户认证。请在 QQ 浏览器打开 $url 并完成验证后输入任意字符。" }
        logger.info { "[UnsafeLogin] Account verification required by the server. Please open $url in QQ browser and complete challenge, then type anything here to submit." }
        return input().also {
            logger.info { "[UnsafeLogin] 正在提交中..." }
            logger.info { "[UnsafeLogin] Submitting..." }
        }
    }

    public companion object {
        /**
         * 创建 Java 阻塞版 [input] 的 [StandardCharImageLoginSolver]
         *
         * @param input 将在协程 IO 池执行, 可以有阻塞调用
         */
        @JvmStatic
        public fun createBlocking(input: () -> String, output: MiraiLogger?): StandardCharImageLoginSolver {
            return StandardCharImageLoginSolver({ withContext(Dispatchers.IO) { input() } }, output)
        }

        /**
         * 创建 Java 阻塞版 [input] 的 [StandardCharImageLoginSolver]
         *
         * @param input 将在协程 IO 池执行, 可以有阻塞调用
         */
        @JvmStatic
        public fun createBlocking(input: () -> String): StandardCharImageLoginSolver {
            return StandardCharImageLoginSolver({ withContext(Dispatchers.IO) { input() } })
        }
    }
}

///////////////////////////////
//////////////// internal
///////////////////////////////

internal fun BotConfiguration.getFileBasedDeviceInfoSupplier(filename: String): (Bot) -> DeviceInfo {
    return {
        File(filename).loadAsDeviceInfo(json)
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
