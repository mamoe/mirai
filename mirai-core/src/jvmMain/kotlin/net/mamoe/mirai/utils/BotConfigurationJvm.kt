/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.use
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.RandomAccessFile
import javax.imageio.ImageIO
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 平台默认的验证码识别器.
 *
 * 可被修改, 除覆盖配置外全局生效.
 */
actual var defaultLoginSolver: LoginSolver = DefaultLoginSolver()


interface LoginSolverInputReader{
    suspend fun read(question:String):String?

    suspend operator fun invoke(question: String):String?{
        return read(question)
    }
}
class DefaultLoginSolverInputReader: LoginSolverInputReader{
    override suspend fun read(question: String): String? {
        return readLine()
    }
}

class DefaultLoginSolver(
    val reader: LoginSolverInputReader = DefaultLoginSolverInputReader(),
    val overrideLogger:MiraiLogger? = null
) : LoginSolver() {
    fun getLogger(bot: Bot):MiraiLogger{
        if(overrideLogger!=null){
            return overrideLogger
        }
        return bot.logger
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String? = loginSolverLock.withLock {
        val logger = getLogger(bot)
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
                val img = ImageIO.read(it)
                if (img == null) {
                    logger.info("无法创建字符图片. 请查看文件")
                } else {
                    logger.info(img.createCharImg())
                }
            }
        }
        logger.info("请输入 4 位字母验证码. 若要更换验证码, 请直接回车")
                return reader("请输入 4 位字母验证码. 若要更换验证码, 请直接回车")!!.takeUnless { it.isEmpty() || it.length != 4 }.also {
            logger.info("正在提交[$it]中...")
        }
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? = loginSolverLock.withLock {
        val logger = getLogger(bot)
        logger.info("需要滑动验证码")
        logger.info("请在任意浏览器中打开以下链接并完成验证码. ")
        logger.info("完成后请输入任意字符 ")
        logger.info(url)
        return reader("完成后请输入任意字符").also {
            logger.info("正在提交中...")
        }
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? = loginSolverLock.withLock {
        val logger = getLogger(bot)
        logger.info("需要进行账户安全认证")
        logger.info("该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题")
        logger.info("完成以下账号认证即可成功登录|理论本认证在mirai每个账户中最多出现1次")
        logger.info("请将该链接在QQ浏览器中打开并完成认证, 成功后输入任意字符")
        logger.info("这步操作将在后续的版本中优化")
        logger.info(url)
        return reader("完成后请输入任意字符").also {
            logger.info("正在提交中...")
        }
    }

}

// Copied from Ktor CIO
public fun File.writeChannel(
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
public fun BufferedImage.createCharImg(outputWidth: Int = 100, ignoreRate: Double = 0.95): String {
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

@Suppress("ClassName", "PropertyName")
actual open class BotConfiguration actual constructor() {
    /**
     * 日志记录器
     */
    actual var botLoggerSupplier: ((Bot) -> MiraiLogger) = { DefaultLogger("Bot(${it.uin})") }
    /**
     * 网络层日志构造器
     */
    actual var networkLoggerSupplier: ((BotNetworkHandler) -> MiraiLogger) = { DefaultLogger("Network(${it.bot.uin})") }
    /**
     * 设备信息覆盖. 默认使用随机的设备信息.
     */
    actual var deviceInfo: ((Context) -> DeviceInfo)? = null

    /**
     * 父 [CoroutineContext]
     */
    actual var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    actual var heartbeatPeriodMillis: Long = 60.secondsToMillis
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 5s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    actual var heartbeatTimeoutMillis: Long = 2.secondsToMillis
    /**
     * 心跳失败后的第一次重连前的等待时间.
     */
    actual var firstReconnectDelayMillis: Long = 5.secondsToMillis
    /**
     * 重连失败后, 继续尝试的每次等待时间
     */
    actual var reconnectPeriodMillis: Long = 5.secondsToMillis
    /**
     * 最多尝试多少次重连
     */
    actual var reconnectionRetryTimes: Int = Int.MAX_VALUE
    /**
     * 验证码处理器
     */
    actual var loginSolver: LoginSolver = defaultLoginSolver

    actual companion object {
        /**
         * 默认的配置实例
         */
        @JvmStatic
        actual val Default = BotConfiguration()
    }

    @Suppress("NOTHING_TO_INLINE")
    @BotConfigurationDsl
    inline operator fun FileBasedDeviceInfo.unaryPlus() {
        deviceInfo = { File(filepath).loadAsDeviceInfo(it) }
    }

    @Suppress("NOTHING_TO_INLINE")
    @BotConfigurationDsl
    inline operator fun FileBasedDeviceInfo.ByDeviceDotJson.unaryPlus() {
        deviceInfo = { File("device.json").loadAsDeviceInfo(it) }
    }

    actual operator fun _NoNetworkLog.unaryPlus() {
        networkLoggerSupplier = supplier
    }

    /**
     * 不记录网络层的 log.
     * 网络层 log 包含包接收, 包发送, 和一些调试用的记录.
     */
    @BotConfigurationDsl
    actual val NoNetworkLog: _NoNetworkLog
        get() = _NoNetworkLog

    @BotConfigurationDsl
    actual object _NoNetworkLog {
        internal val supplier = { _: BotNetworkHandler -> SilentLogger }
    }
}

/**
 * 使用文件系统存储设备信息.
 */
@BotConfigurationDsl
inline class FileBasedDeviceInfo @BotConfigurationDsl constructor(val filepath: String) {
    /**
     * 使用 "device.json" 存储设备信息
     */
    @BotConfigurationDsl
    companion object ByDeviceDotJson
}