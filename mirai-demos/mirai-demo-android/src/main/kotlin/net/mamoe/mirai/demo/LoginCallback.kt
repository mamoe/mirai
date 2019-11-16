package net.mamoe.mirai.demo

import android.graphics.Bitmap

interface LoginCallback {

    suspend fun onCaptcha(bitmap: Bitmap)
    suspend fun onSuccess()
    suspend fun onFailed()
    suspend fun onMessage(message:String)
}