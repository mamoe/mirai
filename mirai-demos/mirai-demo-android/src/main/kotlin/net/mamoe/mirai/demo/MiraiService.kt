@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.demo

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.timpc.TIMPC
import net.mamoe.mirai.utils.LoginFailedException
import java.lang.ref.WeakReference

class MiraiService : Service() {

    private lateinit var mCaptchaDeferred: CompletableDeferred<String>

    private lateinit var mBot: Bot

    private var mCaptcha = ""
        set(value) {
            field = value
            mCaptchaDeferred.complete(value)
        }

    private var mBinder: MiraiBinder? = null

    private var mCallback: WeakReference<LoginCallback>? = null

    override fun onCreate() {
        super.onCreate()
        mBinder = MiraiBinder()

    }

    private fun login(qq: Long, pwd: String) {
        GlobalScope.launch {
            mBot = TIMPC.Bot(qq, pwd) {
                captchaSolver = {
                    val bytes = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    mCaptchaDeferred = CompletableDeferred()
                    mCallback?.get()?.onCaptcha(bitmap)
                    mCaptchaDeferred.await()
                }
            }.apply {
                try {
                    login()
                    mCallback?.get()?.onSuccess()
                } catch (e: LoginFailedException) {
                    mCallback?.get()?.onFailed()
                }
            }


            mBot.subscribeMessages {
                always {
                    mCallback?.get()?.onMessage("收到来自${sender.id}的消息")
                }

                // 当接收到消息 == "你好" 时就回复 "你好!"
                "你好" reply "你好!"
            }
        }

    }


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }


    inner class MiraiBinder : Binder() {

        fun startLogin(qq: Long, pwd: String) {
            login(qq, pwd)
        }

        fun setCaptcha(captcha: String) {
            mCaptcha = captcha
        }

        fun setCallback(callback: LoginCallback) {
            mCallback = WeakReference(callback)
        }
    }


}