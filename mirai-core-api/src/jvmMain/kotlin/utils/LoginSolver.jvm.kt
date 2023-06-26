/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.auth.QRCodeLoginListener
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
public class StandardCharImageLoginSolver
@JvmOverloads constructor(
    input: suspend () -> String = {
        readlnOrNull() ?: @OptIn(MiraiInternalApi::class) throw NoStandardInputForCaptchaException()
    },
    /**
     * 为 `null` 时使用 [Bot.logger]
     */
    private val loggerSupplier: (bot: Bot) -> MiraiLogger = { it.logger }
) : LoginSolver() {
    public constructor(
        input: suspend () -> String = {
            readlnOrNull() ?: @OptIn(MiraiInternalApi::class) throw NoStandardInputForCaptchaException()
        },
        overrideLogger: MiraiLogger?
    ) : this(input, { overrideLogger ?: it.logger })

    private val input: suspend () -> String = suspend {
        withContext(Dispatchers.IO) { input() }
    }

    override val isSliderCaptchaSupported: Boolean get() = true
    override fun createQRCodeLoginListener(bot: Bot): QRCodeLoginListener {
        return object : QRCodeLoginListener {
            private var tmpFile: File? = null

            override val qrCodeMargin: Int get() = 1
            override val qrCodeSize: Int get() = 1

            override fun onFetchQRCode(bot: Bot, data: ByteArray) {
                val logger = loggerSupplier(bot)

                logger.info { "[QRCodeLogin] 已获取登录二维码，请在手机 QQ 使用账号 ${bot.id} 扫码" }
                logger.info { "[QRCodeLogin] Fetched login qrcode, please scan via qq android with account ${bot.id}." }

                try {
                    val tempFile: File
                    if (tmpFile == null) {
                        tempFile = File.createTempFile(
                            "mirai-qrcode-${bot.id}-${currentTimeSeconds()}",
                            ".png"
                        ).apply { deleteOnExit() }

                        tempFile.createNewFile()

                        tmpFile = tempFile
                    } else {
                        tempFile = tmpFile!!
                    }

                    tempFile.writeBytes(data)
                    logger.info { "[QRCodeLogin] 将会显示二维码图片，若看不清图片，请查看文件 ${tempFile.toPath().toUri()}" }
                    logger.info { "[QRCodeLogin] Displaying qrcode image. If not clear, view file ${tempFile.toPath().toUri()}." }
                } catch (e: Exception) {
                    logger.warning("[QRCodeLogin] 无法写出二维码图片. 请尽量关闭终端个性化样式后扫描二维码字符图片", e)
                    logger.warning(
                        "[QRCodeLogin] Failed to export qrcode image. Please try to scan the char-image after disabling custom terminal style.",
                        e
                    )
                }

                data.inputStream().use { stream ->
                    try {
                        val isCacheEnabled = ImageIO.getUseCache()

                        try {
                            ImageIO.setUseCache(false)
                            val img = ImageIO.read(stream)
                            if (img == null) {
                                logger.warning { "[QRCodeLogin] 无法创建字符图片. 请查看文件" }
                                logger.warning { "[QRCodeLogin] Failed to create char-image. Please see the file." }
                            } else {
                                logger.info { "[QRCodeLogin] \n" + img.renderQRCode() }
                            }
                        } finally {
                            ImageIO.setUseCache(isCacheEnabled)
                        }

                    } catch (throwable: Throwable) {
                        logger.warning("[QRCodeLogin] 创建字符图片时出错. 请查看文件.", throwable)
                        logger.warning("[QRCodeLogin] Failed to create char-image. Please see the file.", throwable)
                    }
                }
            }

            override fun onStateChanged(bot: Bot, state: QRCodeLoginListener.State) {
                val logger = loggerSupplier(bot)
                logger.info {
                    buildString {
                        append("[QRCodeLogin] ")
                        when (state) {
                            QRCodeLoginListener.State.WAITING_FOR_SCAN -> append("等待扫描二维码中")
                            QRCodeLoginListener.State.WAITING_FOR_CONFIRM -> append("扫描完成，请在手机 QQ 确认登录")
                            QRCodeLoginListener.State.CANCELLED -> append("已取消登录，将会重新获取二维码")
                            QRCodeLoginListener.State.TIMEOUT -> append("扫描超时，将会重新获取二维码")
                            QRCodeLoginListener.State.CONFIRMED -> append("已确认登录")
                            else -> append("default state")
                        }
                    }
                }
                logger.info {
                    buildString {
                        append("[QRCodeLogin] ")
                        when (state) {
                            QRCodeLoginListener.State.WAITING_FOR_SCAN -> append("Waiting for scanning qrcode.")
                            QRCodeLoginListener.State.WAITING_FOR_CONFIRM -> append("Scan complete. Please confirm login.")
                            QRCodeLoginListener.State.CANCELLED -> append("Login cancelled, we will try to fetch qrcode again.")
                            QRCodeLoginListener.State.TIMEOUT -> append("Timeout scanning, we will try to fetch qrcode again.")
                            QRCodeLoginListener.State.CONFIRMED -> append("Login confirmed.")
                            else -> append("default state")
                        }
                    }
                }

                if (state == QRCodeLoginListener.State.CONFIRMED) {
                    kotlin.runCatching { tmpFile?.delete() }.onFailure { logger.warning(it) }
                }
            }

        }
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? = loginSolverLock.withLock {
        val logger = loggerSupplier(bot)
        runInterruptible(Dispatchers.IO) {
            val tempFile: File = File.createTempFile("tmp", ".png").apply { deleteOnExit() }
            tempFile.createNewFile()
            logger.info { "[PicCaptcha] 需要图片验证码登录, 验证码为 4 字母" }
            logger.info { "[PicCaptcha] Picture captcha required. Captcha consists of 4 letters." }
            try {
                tempFile.writeBytes(data)
                logger.info { "[PicCaptcha] 将会显示字符图片. 若看不清字符图片, 请查看文件 file://${tempFile.absolutePath}" }
                logger.info { "[PicCaptcha] Displaying char-image. If not clear, view file file://${tempFile.absolutePath}." }
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
        logger.info { "[SliderCaptcha] 或者输入 helper 来使用 TxCaptchaHelper 完成滑动验证码" }
        logger.info { "[SliderCaptcha] Or type helper to resolve slider captcha with TxCaptchaHelper.apk" }
        logger.warning { "[SliderCaptcha] TxCaptchaHelper 的在线服务疑似被屏蔽，可能无法使用。TxCaptchaHelper 现已无法满足登录QQ机器人，请在以下链接下载全新的验证器" }
        logger.warning { "[SliderCaptcha] The service of TxCaptchaHelper might be blocked. We recommend you to download the new login solver plugin in below link." }
        logger.warning { "[SliderCaptcha] @see https://github.com/KasukuSakura/mirai-login-solver-sakura" }
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
            if (it == "TxCaptchaHelper" || it == "`TxCaptchaHelper`" || it == "helper" || it == "`helper`") {
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

@Suppress("LocalVariableName", "SpellCheckingInspection")
private fun BufferedImage.renderQRCode(
    blackPlaceholder: String = "   ",
    whitePlaceholder: String = "   ",
    doColorSwitch: Boolean = true,
): String {
    var lastStatus: Boolean? = null

    fun isBlackBlock(rgb: Int): Boolean {
        val r = rgb and 0xff0000 shr 16
        val g = rgb and 0x00ff00 shr 8
        val b = rgb and 0x0000ff

        return r < 10 && g < 10 && b < 10
    }

    val sb = StringBuilder()
    sb.append("\n")

    val BLACK = "\u001b[30;40m"
    val WHITE = "\u001b[97;107m"
    val RESET = "\u001b[0m"

    for (y in 0 until height) {
        for (x in 0 until width) {
            val rgbcolor = getRGB(x, y)
            val crtStatus = isBlackBlock(rgbcolor)

            if (doColorSwitch && crtStatus != lastStatus) {
                lastStatus = crtStatus
                sb.append(
                    if (crtStatus) BLACK else WHITE
                )
            }

            sb.append(
                if (crtStatus) blackPlaceholder else whitePlaceholder
            )
        }

        if (doColorSwitch) {
            sb.append(RESET)
        }

        sb.append("\n")
        lastStatus = null
    }

    if (doColorSwitch) {
        sb.append(RESET)
    }

    return sb.toString()
}
