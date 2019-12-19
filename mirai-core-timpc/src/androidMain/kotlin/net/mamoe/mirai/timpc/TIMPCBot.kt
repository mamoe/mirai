@file:Suppress("unused")

package net.mamoe.mirai.timpc

import kotlinx.io.InputStream
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

internal actual class TIMPCBot actual constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : TIMPCBotBase(account, logger, context) {
    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
}