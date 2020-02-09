/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.demo

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    @SuppressLint("SetTextI18n")
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

    private var needCaptcha = false


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
                val qq = et_qq.text.toString().toLong()
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