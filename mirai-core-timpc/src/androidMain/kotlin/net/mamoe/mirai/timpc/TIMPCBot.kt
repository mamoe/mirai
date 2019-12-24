@file:Suppress("unused")

package net.mamoe.mirai.timpc

import kotlinx.io.InputStream
import kotlinx.io.streams.inputStream
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.BotConfiguration

internal actual class TIMPCBot actual constructor(
    account: BotAccount,
    configuration: BotConfiguration
) : TIMPCBotBase(account, configuration) {
    suspend inline fun Image.downloadAsStream(): InputStream = download().inputStream()
}