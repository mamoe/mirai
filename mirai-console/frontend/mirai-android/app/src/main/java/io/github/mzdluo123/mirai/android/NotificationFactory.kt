package io.github.mzdluo123.mirai.android

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidLoginSolver
import io.github.mzdluo123.mirai.android.service.BotService

@ExperimentalUnsignedTypes
object NotificationFactory {

    const val SERVICE_NOTIFICATION = "service"
    const val CAPTCHA_NOTIFICATION = "captcha"
    const val OFFLINE_NOTIFICATION = "offline"

    val context by lazy {
        BotApplication.context
    }

    private val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    private val launchMainActivity = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    internal fun initNotification() {
        val notificationManager =
            context.getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 只在8.0系统上注册通知通道，防止程序崩溃
            val statusChannel = NotificationChannel(
                SERVICE_NOTIFICATION, "状态通知",
                NotificationManager.IMPORTANCE_MIN
            )

            statusChannel.description = "Mirai正在运行的通知"

            val captchaChannel = NotificationChannel(
                CAPTCHA_NOTIFICATION, "验证码通知",
                NotificationManager.IMPORTANCE_HIGH
            )
            captchaChannel.description = "登录需要输入验证码时的通知"

            val offlineChannel = NotificationChannel(
                OFFLINE_NOTIFICATION, "离线通知",
                NotificationManager.IMPORTANCE_HIGH
            )
            offlineChannel.description = "Mirai因各种原因离线的通知"

            if (BuildConfig.DEBUG) {
                offlineChannel.importance = NotificationManager.IMPORTANCE_MIN
                captchaChannel.importance = NotificationManager.IMPORTANCE_MIN
            }

            notificationManager.createNotificationChannel(statusChannel)
            notificationManager.createNotificationChannel(captchaChannel)
            notificationManager.createNotificationChannel(offlineChannel)
        }
    }

    internal fun dismissAllNotification() {
        NotificationManagerCompat.from(context).apply {
            cancel(BotService.OFFLINE_NOTIFICATION_ID)
            cancel(AndroidLoginSolver.CAPTCHA_NOTIFICATION_ID)

        }
    }


    internal fun statusNotification(
        content: String = "请完成登录并将软件添加到系统后台运行白名单确保能及时处理消息",
        avatar: Bitmap? = null
    ): Notification {

        return NotificationCompat.Builder(
            context,
            SERVICE_NOTIFICATION
        )
            .setSmallIcon(R.drawable.ic_extension_black_24dp)//设置状态栏的通知图标
            .setAutoCancel(false) //禁止用户点击删除按钮删除
            .setOngoing(true) //禁止滑动删除
            .setShowWhen(true) //右上角的时间显示
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(launchMainActivity)
            .setContentTitle("MiraiAndroid") //创建通知
            .setContentText(content)
            .setLargeIcon(avatar)
            .build()

    }

    internal fun offlineNotification(content: String, bigTheme: Boolean = false): Notification {

        val builder = NotificationCompat.Builder(
            context,
            OFFLINE_NOTIFICATION
        )
            .setAutoCancel(false)
            .setOngoing(false)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
            .setContentTitle("Mirai离线")
            .setContentText(content)

        if (bigTheme) {
            builder.setStyle(NotificationCompat.BigTextStyle())
        }
        return builder.build()

    }

    internal fun captchaNotification(activity: Class<*>): Notification {

        val notifyIntent = Intent(context, activity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(
            context,
            CAPTCHA_NOTIFICATION
        )
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(false)
            //禁止滑动删除
            .setOngoing(false)
            //右上角的时间显示
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
            .setContentTitle("本次登录需要进行登录验证")
            .setContentText("点击这里开始验证")
            .build()
    }

}