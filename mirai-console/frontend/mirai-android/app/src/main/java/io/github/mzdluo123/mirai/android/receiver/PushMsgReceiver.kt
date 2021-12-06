package io.github.mzdluo123.mirai.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.mzdluo123.mirai.android.service.BotService

class PushMsgReceiver(private val botService: BotService) : BroadcastReceiver() {
    companion object {
        val TAG = PushMsgReceiver::class.java.name
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val data = intent.data ?: return
            if (data.scheme != "ma") {
                return
            }
            val id = data.getQueryParameter("id")?.toLong() ?: return
            val msg = data.getQueryParameter("msg") ?: return
            when (data.host) {
                "sendGroupMsg" -> {
                    val at = data.getQueryParameter("at")?.toLong()
                    if (at != null) {
                        botService.sendGroupMsgWithAT(id, msg, at)
                        return
                    }
                    botService.sendGroupMsg(id, msg)
                }
                "sendFriendMsg" -> botService.sendFriendMsg(id, msg)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.toString())
        }

    }
}
