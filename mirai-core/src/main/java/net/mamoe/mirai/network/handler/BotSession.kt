package net.mamoe.mirai.network.handler

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler

/**
 * @author Him188moe
 */
data class BotSession(
        val bot: Bot,
        val sessionKey: ByteArray,
        val networkHandler: BotNetworkHandler
) {
}