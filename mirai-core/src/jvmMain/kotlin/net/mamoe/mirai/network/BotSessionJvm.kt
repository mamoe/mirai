package net.mamoe.mirai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.io.core.use
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocketAdapter
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
    sessionKey: SessionKey,
    socket: DataPacketSocketAdapter,
    NetworkScope: CoroutineScope
) : BotSessionBase(bot, sessionKey, socket, NetworkScope) {

    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
    suspend inline fun Image.downloadAsBufferedImage(): BufferedImage = withContext(IO) { downloadAsStream().use { ImageIO.read(it) } }
    suspend inline fun Image.downloadAsExternalImage(): ExternalImage = download().use { it.toExternalImage() }

    suspend inline fun Image.downloadTo(file: File) = file.outputStream().use { downloadTo(it) }
    suspend inline fun Image.downloadTo(output: OutputStream) = download().inputStream().use { input -> withContext(IO) { input.transferTo(output) } }
}