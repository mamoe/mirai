package net.mamoe.mirai.demo

import android.app.ProgressDialog
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.cio.writeChannel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.login
import net.mamoe.mirai.network.protocol.tim.packet.login.requireSuccess
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File

class MainActivity : AppCompatActivity(),LoginCallback {


    private lateinit var progressDialog : ProgressDialog

    override suspend fun onCaptcha(bitmap: Bitmap) {
        withContext(Dispatchers.Main){
            ll_captcha.visibility = View.VISIBLE
            iv_captcha.setImageBitmap(bitmap)
            needCaptcha = true
            if (progressDialog.isShowing){
                progressDialog.dismiss()
            }
        }
    }

    override suspend fun onMessage(message:String) {
        withContext(Dispatchers.Main){
            msg.text = "${msg.text}\n$message"
        }
    }

    override suspend fun onSuccess() {
        withContext(Dispatchers.Main){
            Toast.makeText(this@MainActivity,"登录成功",Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing){
                progressDialog.dismiss()
            }
            ll_captcha.visibility = View.GONE
            et_pwd.visibility = View.GONE
            et_qq.visibility = View.GONE
            bt_login.visibility = View.GONE
        }

    }

    override suspend fun onFailed() {
        withContext(Dispatchers.Main){
            Toast.makeText(this@MainActivity,"登录失败",Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing){
                progressDialog.dismiss()
            }
        }
    }

    var binder: MiraiService.MiraiBinder? = null

    var needCaptcha = false


    private val conn = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as MiraiService.MiraiBinder?
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, MiraiService::class.java)
        startService(intent)
        bindService(intent, conn, Service.BIND_AUTO_CREATE)
        progressDialog = ProgressDialog(this)
        bt_login.setOnClickListener {
            if (!progressDialog.isShowing){
                progressDialog.show()
            }
            binder?.setCallback(this)
            if (!needCaptcha){
                val qq = et_qq.text.toString().toUInt()
                val pwd = et_pwd.text.toString()
                binder?.startLogin(qq, pwd)
            }else{
                val captcha = et_captcha.text.toString()
                binder?.setCaptcha(captcha)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

}