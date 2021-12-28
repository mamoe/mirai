package io.github.mzdluo123.mirai.android.miraiconsole

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger
import splitties.experimental.ExperimentalSplittiesApi

@ExperimentalSplittiesApi
@ExperimentalUnsignedTypes
class AndroidMiraiConsole(context: Context) : MiraiConsoleUI {
    private val logBuffer = AppSettings.logBuffer
    private val printToLogcat = AppSettings.printToLogcat
    val logStorage = LoopQueue<String>(logBuffer)
    val loginSolver =
        AndroidLoginSolver(context)

    // 使用一个[60s/refreshPerMinute]的数组存放每4秒消息条数
    // 读取时增加最新一分钟，减去最老一分钟
    private val refreshPerMinute = AppSettings.refreshPerMinute
    private val msgSpeeds = IntArray(refreshPerMinute)
    private var refreshCurrentPos = 0

    private var sendOfflineMsgJob: Job? = null

    companion object {
        const val TAG = "MA"
    }

    override fun createLoginSolver(): LoginSolver = loginSolver

    override fun prePushBot(identity: Long) = Unit

    override fun pushBot(bot: Bot) {
        bot.pushToScriptManager(ScriptManager.instance)
        bot.subscribeBotLifeEvent()
        bot.startRefreshNotificationJob()
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) = Unit

    override fun pushLog(identity: Long, message: String) {
        logStorage.add(message)
        if (printToLogcat) {
            Log.i(TAG, message)
        }
    }

    override fun pushLog(
        priority: SimpleLogger.LogPriority,
        identityStr: String,
        identity: Long,
        message: String
    ) {
        logStorage.add("[${priority.name}] $message")
        if (printToLogcat) {
            Log.i(TAG, "[${priority.name}] $message")
        }

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        logStorage.add(MiraiAndroidStatus.recentStatus().format())
    }

    override suspend fun requestInput(hint: String): String = ""

    private fun Bot.startRefreshNotificationJob() {
        subscribeMessages { always { msgSpeeds[refreshCurrentPos] += 1 } }
        launch {
            val avatar = downloadAvatar()  // 获取通知展示用的头像
            var msgSpeed = 0
            while (isActive) {
                /*
                * 总速度+=最新速度 [0] [1] ... [14]
                * 总速度-=最老速度 [1] [2] ... [0]
                */
                msgSpeed += msgSpeeds[refreshCurrentPos]
                if (refreshCurrentPos != refreshPerMinute - 1) {
                    refreshCurrentPos += 1
                } else {
                    refreshCurrentPos = 0
                }
                msgSpeed -= msgSpeeds[refreshCurrentPos]
                msgSpeeds[refreshCurrentPos] = 0
                NotificationManagerCompat.from(BotApplication.context).apply {
                    notify(
                        BotService.NOTIFICATION_ID,
                        NotificationFactory.statusNotification("消息速度 ${msgSpeed}/min", avatar)
                    )
                }
                delay(60L / refreshPerMinute * 1000)
            }
        }
    }

    private suspend fun Bot.downloadAvatar(): Bitmap =
        try {
            pushLog(0L, "[INFO] 正在加载头像....")
            HttpClient().get<ByteArray>(selfQQ.avatarUrl).let { avatarData ->
                BitmapFactory.decodeByteArray(avatarData, 0, avatarData.size)
            }
        } catch (e: Exception) {
            delay(1000)
            downloadAvatar()
        }

    private fun Bot.subscribeBotLifeEvent() {
        subscribeAlways<BotOfflineEvent>(priority = Listener.EventPriority.HIGHEST) {
            if (this is BotOfflineEvent.Force) {
                NotificationManagerCompat.from(BotApplication.context).apply {
                    notify(
                        BotService.OFFLINE_NOTIFICATION_ID,
                        NotificationFactory.offlineNotification(message, true)
                    )
                }
                return@subscribeAlways
            }
            if (this is BotOfflineEvent.Dropped) {
                sendOfflineMsgJob = GlobalScope.launch {
                    delay(2000)
                    if (!isActive) {
                        return@launch
                    }
                    NotificationManagerCompat.from(BotApplication.context).apply {
                        notify(
                            BotService.OFFLINE_NOTIFICATION_ID,
                            NotificationFactory.offlineNotification("请检查网络设置")
                        )
                    }
                }
            }

            pushLog(0L, "[INFO] 发送离线通知....")
        }
        subscribeAlways<BotReloginEvent>(priority = Listener.EventPriority.HIGHEST) {
            pushLog(0L, "[INFO] 发送上线通知....")
            if (sendOfflineMsgJob != null && sendOfflineMsgJob!!.isActive) {
                sendOfflineMsgJob!!.cancel()
            }
            NotificationManagerCompat.from(BotApplication.context)
                .cancel(BotService.OFFLINE_NOTIFICATION_ID)
        }
    }

    private fun Bot.pushToScriptManager(manager: ScriptManager) {
        launch { manager.addBot(this@pushToScriptManager) }
    }
}
