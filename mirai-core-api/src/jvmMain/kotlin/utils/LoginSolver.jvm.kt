/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.NoStandardInputForCaptchaException
import net.mamoe.mirai.utils.StandardCharImageLoginSolver.Companion.createBlocking
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual object PlatformLoginSolverImplementations {
    actual val isSliderCaptchaSupported: Boolean get() = default!!.isSliderCaptchaSupported
    actual val default: LoginSolver? by lazy {
        StandardCharImageLoginSolver()
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

    override val isSliderCaptchaSupported: Boolean get() = true

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? = loginSolverLock.withLock {
        val logger = loggerSupplier(bot)
        runInterruptible(Dispatchers.IO) {
            val tempFile: File = File.createTempFile("tmp", ".png").apply { deleteOnExit() }
            tempFile.createNewFile()
            logger.info { "[PicCaptcha] 需要图片验证码登录, 验证码为 4 字母" }
            logger.info { "[PicCaptcha] Picture captcha required. Captcha consists of 4 letters." }
            try {
                tempFile.writeBytes(data)
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
        logger.info { "[SliderCaptcha] 需要滑动验证码, 请按照以下链接的步骤完成滑动验证码, 然后输入获取到的 ticket" }
        logger.info { "[SliderCaptcha] Slider captcha required. Please solve the captcha with following link. Type ticket here after completion." }
        logger.info { "[SliderCaptcha] @see https://github.com/project-mirai/mirai-login-solver-selenium" }
        logger.info { "[SliderCaptcha] @see https://docs.mirai.mamoe.net/mirai-login-solver-selenium/" }
        logger.info { "[SliderCaptcha] 或者输入 TxCaptchaHelper 来使用 TxCaptchaHelper 完成滑动验证码" }
        logger.info { "[SliderCaptcha] Or type `TxCaptchaHelper` to resolve slider captcha with TxCaptchaHelper.apk" }
        logger.info { "[SliderCaptcha] Captcha link: $url" }

        suspend fun runTxCaptchaHelper(): String {
            logger.info { "[SliderCaptcha] @see https://github.com/mzdluo123/TxCaptchaHelper" }
            return coroutineScope {
                suspendCoroutine { coroutine ->
                    val helper = object : TxCaptchaHelper() {
                        override fun onComplete(ticket: String) {
                            coroutine.resume(ticket)
                        }

                        override fun updateDisplay(msg: String) {
                            logger.info(msg)
                        }
                    }
                    helper.start(this, url)
                }
            }
        }

        return input().also {
            if (it == "TxCaptchaHelper" || it == "`TxCaptchaHelper`") {
                return runTxCaptchaHelper()
            }
            logger.info { "[SliderCaptcha] 正在提交中..." }
            logger.info { "[SliderCaptcha] Submitting..." }
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun onSolveDeviceVerification(
        bot: Bot, requests: DeviceVerificationRequests
    ): DeviceVerificationResult {
        val logger = loggerSupplier(bot)
        requests.sms?.let { req ->
            solveSms(logger, req)?.let { return it }
        }
        requests.fallback?.let { fallback ->
            solveFallback(logger, fallback.url)
            return fallback.solved()
        }
        error("User rejected SMS login while fallback login method not available.")
    }

    private suspend fun solveSms(
        logger: MiraiLogger, request: DeviceVerificationRequests.SmsRequest
    ): DeviceVerificationResult? = loginSolverLock.withLock {
        val countryCode = request.countryCode
        val phoneNumber = request.phoneNumber
        if (countryCode != null && phoneNumber != null) {
            logger.info("一条短信验证码将发送到你的手机 (+$countryCode) $phoneNumber. 运营商可能会收取正常短信费用, 是否继续? 输入 yes 继续, 输入其他终止并尝试其他验证方式.")
            logger.info(
                "A verification code will be send to your phone (+$countryCode) $phoneNumber, which may be charged normally, do you wish to continue? Type yes to continue, type others to cancel and try other methods."
            )
        } else {
            logger.info("一条短信验证码将发送到你的手机 (无法获取到手机号码). 运营商可能会收取正常短信费用, 是否继续? 输入 yes 继续, 输入其他终止并尝试其他验证方式.")
            logger.info(
                "A verification code will be send to your phone (failed to get phone number), " + "which may be charged normally by your carrier, " + "do you wish to continue? Type yes to continue, type others to cancel and try other methods."
            )
        }
        val answer = input().trim()
        return if (answer.equals("yes", ignoreCase = true)) {
            logger.info("Attempting SMS verification.")
            request.requestSms()
            logger.info("Please enter code: ")
            val code = input().trim()
            logger.info("Continuing with code '$code'.")
            request.solved(code)
        } else {
            logger.info("Cancelled.")
            null
        }
    }

    @Deprecated(
        "Please use onSolveDeviceVerification instead",
        replaceWith = ReplaceWith("onSolveDeviceVerification(bot, url, null)"),
        level = DeprecationLevel.WARNING
    )
    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String =
        solveFallback(loggerSupplier(bot), url)

    private suspend fun solveFallback(
        logger: MiraiLogger, url: String
    ): String {
        return loginSolverLock.withLock {
            logger.info { "[UnsafeLogin] 当前登录环境不安全，服务器要求账户认证。请在 QQ 浏览器打开 $url 并完成验证后输入任意字符。" }
            logger.info { "[UnsafeLogin] Account verification required by the server. Please open $url in QQ browser and complete challenge, then type anything here to submit." }
            input().also {
                logger.info { "[UnsafeLogin] 正在提交中..." }
                logger.info { "[UnsafeLogin] Submitting..." }
            }
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