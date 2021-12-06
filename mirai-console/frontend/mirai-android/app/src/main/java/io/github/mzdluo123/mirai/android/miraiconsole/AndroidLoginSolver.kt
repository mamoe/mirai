package io.github.mzdluo123.mirai.android.miraiconsole

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.activity.CaptchaActivity
import io.github.mzdluo123.mirai.android.activity.UnsafeLoginActivity
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.utils.LoginSolver


@ExperimentalUnsignedTypes
class AndroidLoginSolver(private val context: Context) : LoginSolver() {
    lateinit var verificationResult: CompletableDeferred<String>
    lateinit var captchaData: ByteArray
    lateinit var url: String

    companion object {
        const val CAPTCHA_NOTIFICATION_ID = 2
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        MiraiConsole.frontEnd.pushLog(0L,"本次登录需要输入验证码，请在通知栏点击通知来输入")
        verificationResult = CompletableDeferred()
        captchaData = data
        NotificationManagerCompat.from(context).apply {
            notify(
                CAPTCHA_NOTIFICATION_ID,
                NotificationFactory.captchaNotification(CaptchaActivity::class.java)
            )
        }
        return verificationResult.await()
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    private fun sendVerifyNotification() {
        MiraiConsole.frontEnd.pushLog(0L,"本次登录需要进行验证，请在通知栏点击通知进行验证")

        NotificationManagerCompat.from(context).apply {
            notify(
                CAPTCHA_NOTIFICATION_ID,
                NotificationFactory.captchaNotification(UnsafeLoginActivity::class.java)
            )
        }
    }

}

