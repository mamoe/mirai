package io.github.mzdluo123.mirai.android

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import io.github.mzdluo123.mirai.android.NotificationFactory.initNotification
import io.github.mzdluo123.mirai.android.crash.MiraiAndroidReportSenderFactory
import io.github.mzdluo123.mirai.android.service.BotService
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.ToastConfigurationBuilder
import org.acra.data.StringFormat

@ExperimentalUnsignedTypes
class BotApplication : Application() {
    companion object {

        lateinit var context: BotApplication
            private set

        val httpClient = lazy { HttpClient() }
        val json = lazy { Json { } }

    }


    override fun onCreate() {
        super.onCreate()
        context = this
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            getProcessName()
        else
            myGetProcessName()

        // 防止服务进程多次初始化
        if (processName?.isEmpty() == false && processName == packageName) {
            initNotification()
        }
    }


    //崩溃事件注册
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this, CoreConfigurationBuilder(this).apply {
            setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
            setReportSenderFactoryClasses(MiraiAndroidReportSenderFactory::class.java)
            getPluginConfigurationBuilder(ToastConfigurationBuilder::class.java)
                .setResText(R.string.acra_toast_text)
                .setEnabled(true)
            //不知道为什么开启的时候总是显示这个，先暂时禁用
        })
    }

    private fun myGetProcessName(): String? {
        val pid = Process.myPid()
        for (appProcess in (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }

    internal fun startBotService() {
        val account = getSharedPreferences("account", Context.MODE_PRIVATE)
        this.startService(Intent(this, BotService::class.java).apply {
            putExtra("action", BotService.START_SERVICE)
            putExtra("qq", account.getLong("qq", 0))
            putExtra("pwd", account.getString("pwd", null))
        })
    }

    internal fun stopBotService() {
        startService(Intent(this, BotService::class.java).apply {
            putExtra("action", BotService.STOP_SERVICE)
        })
    }


}