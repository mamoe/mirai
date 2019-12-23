package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import kotlin.coroutines.CoroutineContext

internal class QQAndroidBotNetworkHandler(override val bot: QQAndroidBot) : BotNetworkHandler() {
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    override suspend fun login() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun awaitDisconnection() {
        TODO()
    }

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}