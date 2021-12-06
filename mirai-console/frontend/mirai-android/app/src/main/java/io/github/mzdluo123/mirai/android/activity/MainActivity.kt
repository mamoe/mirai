package io.github.mzdluo123.mirai.android.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.SafeDns
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.okButton
import splitties.toast.toast
import java.io.File
import java.io.FileReader


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.nav_console,
                R.id.nav_plugins,
                R.id.nav_scripts,
                R.id.nav_setting,
                R.id.nav_about
            ), drawer_layout
        )
    }
    private val navController:NavController by lazy{
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)

        BotApplication.context.startBotService()
        btn_exit.setOnClickListener {
            BotApplication.context.stopBotService()
            NotificationFactory.dismissAllNotification()
            finish()
        }
        btn_reboot.setOnClickListener {
            NotificationFactory.dismissAllNotification()
            launch {
                BotApplication.context.stopBotService()
                delay(200)
                BotApplication.context.startBotService()
                navController.popBackStack()
                navController.navigate(R.id.nav_console)  // 重新启动console fragment，使其能够链接到服务
                drawer_layout.closeDrawers()
            }
        }

        checkCrash()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            toast("检查更新失败")
            throwable.printStackTrace()
            Log.e(TAG, throwable.message ?: return@CoroutineExceptionHandler)
            finish()
            BotApplication.context.stopBotService()
        }

        if (!BuildConfig.DEBUG) {
            toast("跳过更新检查")
        } else {
            lifecycleScope.launch(exceptionHandler) {
                checkUpdate()
            }
        }
        //throw Exception("测试异常")
    }


    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    private suspend fun checkUpdate() {
        val rep = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().dns(SafeDns()).build()
            val res = client.newCall(
                Request.Builder()
                    .url("https://api.github.com/repos/mzdluo123/MiraiAndroid/releases/latest")
                    .build()
            ).execute().body?.string()
            client.dispatcher.executorService.shutdown();
            client.connectionPool.evictAll();
            client.cache?.close()
            return@withContext res
        }

        val json =
            BotApplication.json.value.parseToJsonElement(rep ?: throw IllegalStateException("返回为空"))
        if (json.jsonObject.containsKey("url")) {
            val body = json.jsonObject["body"]?.jsonPrimitive?.content ?: "暂无更新记录"
            val htmlUrl = json.jsonObject["html_url"]!!.jsonPrimitive.content
            val version = json.jsonObject["tag_name"]!!.jsonPrimitive.content
            if (version == BuildConfig.VERSION_NAME) {
                return
            }
            withContext(Dispatchers.Main) {
                alertDialog(title = "发现新版本 $version", message = body) {
                    setPositiveButton("立即更新") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl)))
                    }
                }.show()
            }
        } else {
            throw IllegalStateException("检查更新失败")
        }
    }

    private fun checkCrash() {
        val crashDataFile = File(getExternalFilesDir("crash"), "crashdata")
        if (!crashDataFile.exists()) return
        var crashData: String
        FileReader(crashDataFile).also {
            crashData = it.readText()
        }.close()
        alertDialog {
            message = "检测到你上一次异常退出，是否上传崩溃日志？"
            okButton {
                shareText(crashData, lifecycleScope)
            }
            cancelButton { }
        }.show()
        crashDataFile.renameTo(
            File(
                getExternalFilesDir("crash"),
                "crashdata${System.currentTimeMillis()}"
            )
        )
    }

}
