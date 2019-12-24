@file:Suppress("unused")

package net.mamoe.mirai.timpc

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.io.InputStream
import kotlinx.io.core.use
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.toExternalImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

internal actual class TIMPCBot actual constructor(
    account: BotAccount,
    configuration: BotConfiguration
) : TIMPCBotBase(account, configuration) {
    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
    suspend inline fun Image.downloadAsBufferedImage(): BufferedImage = withContext(IO) { downloadAsStream().use { ImageIO.read(it) } }
    suspend inline fun Image.downloadAsExternalImage(): ExternalImage = download().use { it.toExternalImage() }

    suspend inline fun Image.downloadTo(file: File) = file.outputStream().use { downloadTo(it) }
}