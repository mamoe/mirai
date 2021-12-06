package io.github.mzdluo123.mirai.android.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidLoginSolver
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import kotlinx.android.synthetic.main.activity_captcha.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalUnsignedTypes
class CaptchaActivity : AppCompatActivity() {
    private lateinit var conn: ServiceConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captcha)
        conn = ServiceConnector(this)
        lifecycle.addObserver(conn)
        conn.connectStatus.observe(this, Observer {
            if (it) {
                val data = conn.botService.captcha
                lifecycleScope.launch(Dispatchers.Main) {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    captcha_view.setImageBitmap(bitmap)
                }
            }
        })
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(baseContext, BotService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
        captchaConfirm_btn.setOnClickListener {
            conn.botService.submitVerificationResult(captcha_input.text.toString())
            // 删除通知
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(AndroidLoginSolver.CAPTCHA_NOTIFICATION_ID)
            finish()
        }
    }


}