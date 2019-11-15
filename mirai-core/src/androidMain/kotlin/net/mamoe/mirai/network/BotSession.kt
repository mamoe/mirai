package net.mamoe.mirai.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.use
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocketAdapter
import net.mamoe.mirai.network.protocol.tim.packet.SessionKey
import net.mamoe.mirai.utils.InternalAPI
import java.io.InputStream

/**
 * Android 平台相关扩展. 详情查看 [BotSessionBase]
 *
 * @author Him188moe
 */
@UseExperimental(InternalAPI::class)
actual class BotSession actual constructor(
    bot: Bot,
    sessionKey: SessionKey,
    socket: DataPacketSocketAdapter,
    NetworkScope: CoroutineScope
) : BotSessionBase(bot, sessionKey, socket, NetworkScope) {

    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
    suspend inline fun Image.downloadAsBitmap(): Bitmap = withContext(Dispatchers.IO) { downloadAsStream().use { BitmapFactory.decodeStream(it) } }
    //suspend inline fun Image.downloadAsExternalImage(): ExternalImage = download().use { it.toExternalImage() }

}