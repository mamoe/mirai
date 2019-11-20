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
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.login
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.network.protocol.tim.packet.event.GroupMessage
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
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

    private fun login(qq: UInt, pwd: String) {
        GlobalScope.launch {
            mBot = Bot(qq, pwd).apply {
                val loginResult = login {
                    captchaSolver = {
                        val bytes = it.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        mCaptchaDeferred = CompletableDeferred()
                        mCallback?.get()?.onCaptcha(bitmap)
                        mCaptchaDeferred.await()
                    }
                }
                if (loginResult == LoginResult.SUCCESS) {
                    mCallback?.get()?.onSuccess()
                } else {
                    mCallback?.get()?.onFailed()
                }
            }


            mBot.subscribeMessages {
                content({ true }) {
                    mCallback?.get()?.onMessage("收到来自${sender.id}的消息")
                }

                // 当接收到消息 == "你好" 时就回复 "你好!"
                "你好" reply "你好!"

                // 当消息 == "查看 subject" 时, 执行 lambda
                case("查看 subject") {
                    if (subject is QQ) {
                        reply("消息主体为 QQ, 你在跟发私聊消息")
                    } else {
                        reply("消息主体为 Group, 你在群里发消息")
                    }

                    // 在回复的时候, 一般使用 subject 来作为回复对象.
                    // 因为当群消息时, subject 为这个群.
                    // 当好友消息时, subject 为这个好友.
                    // 所有在 MessagePacket(也就是此时的 this 指代的对象) 中实现的扩展方法, 如刚刚的 "reply", 都是以 subject 作为目标
                }


                // 当消息里面包含这个类型的消息时
                has<Image> {
                    // this: MessagePacket
                    // message: MessageChain
                    // sender: QQ
                    // it: String (MessageChain.toString)

                    if (this is GroupMessage) {
                        //如果是群消息
                        // group: Group
                        this.group.sendMessage("你在一个群里")
                        // 等同于 reply("你在一个群里")
                    }

                    reply("图片, ID= ${message[Image].id}")//获取第一个 Image 类型的消息
                    reply(message)
                }


                "123" containsReply "你的消息里面包含 123"


                // 当收到 "我的qq" 就执行 lambda 并回复 lambda 的返回值 String
                "我的qq" reply { sender.id.toString() }


                // 当消息前缀为 "我是" 时
                startsWith("我是", removePrefix = true) {
                    // it: 删除了消息前缀 "我是" 后的消息
                    // 如一条消息为 "我是张三", 则此时的 it 为 "张三".

                    reply("你是$it")
                }


                // 当消息中包含 "复读" 时
                contains("复读") {
                    reply(message)
                }


                // 自定义的 filter, filter 中 it 为转为 String 的消息.
                // 也可以用任何能在处理时使用的变量, 如 subject, sender, message
                content({ it.length == 3 }) {
                    reply("你发送了长度为 3 的消息")
                }

            }
        }

    }


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }


    inner class MiraiBinder : Binder() {

        fun startLogin(qq: UInt, pwd: String) {
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