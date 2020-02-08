/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.demo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.qqandroid.QQAndroid
import net.mamoe.mirai.utils.LoginSolver
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

    private fun login(context: Context, qq: Long, pwd: String) {
        GlobalScope.launch {
            mBot = QQAndroid.Bot(context, qq, pwd) {
                loginSolver = object : LoginSolver() {
                    override suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String? {
                        val bytes = data.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        mCaptchaDeferred = CompletableDeferred()
                        mCallback?.get()?.onCaptcha(bitmap)
                        return mCaptchaDeferred.await()
                    }

                    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
                        TODO("not implemented")
                    }

                    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
                        TODO("not implemented")
                    }

                }
            }.apply {
                try {
                    login()
                    mCallback?.get()?.onSuccess()
                } catch (e: Exception) {
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
            login(applicationContext, qq, pwd)
        }

        fun setCaptcha(captcha: String) {
            mCaptcha = captcha
        }

        fun setCallback(callback: LoginCallback) {
            mCallback = WeakReference(callback)
        }
    }


}