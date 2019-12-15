package net.mamoe.mirai.network

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.io.core.copyTo
import kotlinx.io.core.use
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.network.protocol.tim.packet.SessionKey
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.toExternalImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * JVM 平台相关扩展. 详情查看 [BotSessionBase]
 */
@UseExperimental(MiraiInternalAPI::class)
@Suppress("unused")
actual class BotSession internal actual constructor(
    bot: Bot,
    sessionKey: SessionKey
) : BotSessionBase(bot, sessionKey) {

    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
    suspend inline fun Image.downloadAsBufferedImage(): BufferedImage = withContext(IO) { downloadAsStream().use { ImageIO.read(it) } }
    suspend inline fun Image.downloadAsExternalImage(): ExternalImage = download().use { it.toExternalImage() }

    suspend inline fun Image.downloadTo(file: File) = file.outputStream().use { downloadTo(it) }

    /**
     * 需要调用者自行 close [output]
     */
    @UseExperimental(KtorExperimentalAPI::class)
    suspend inline fun Image.downloadTo(output: OutputStream) =
        download().inputStream().asInput().use { input -> withContext(IO) { input.copyTo(output.asOutput()) } }
}