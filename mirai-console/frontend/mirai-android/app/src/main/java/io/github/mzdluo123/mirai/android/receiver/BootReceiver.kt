package io.github.mzdluo123.mirai.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.service.BotService
import splitties.experimental.ExperimentalSplittiesApi

@ExperimentalSplittiesApi
class BootReceiver : BroadcastReceiver() {
    //    companion object{
//        const val TAG = "BootReceiver"
//    }
    private val ACTION = "android.intent.action.BOOT_COMPLETED"
    override fun onReceive(context: Context, intent: Intent) {
//        Log.e(TAG,"收到广播")
        if (AppSettings.startOnBoot) {
            return
        }

        if (intent.action == ACTION) {
            val startIntent = Intent(context, BotService::class.java)
            startIntent.putExtra(
                "action",
                BotService.START_SERVICE
            )
            val account =
                context.getSharedPreferences("account", Context.MODE_PRIVATE)
            val qq = account.getLong("qq", 0)
            val pwd = account.getString("pwd", null)
            startIntent.putExtra("qq", qq)
            startIntent.putExtra("pwd", pwd)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }
    }
}