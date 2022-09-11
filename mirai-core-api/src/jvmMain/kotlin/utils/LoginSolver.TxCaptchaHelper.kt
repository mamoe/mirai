/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal abstract class TxCaptchaHelper {
    private val newClient: Boolean = true
    private lateinit var queue: Job

    private var latestDisplay = "Sending request..."

    abstract fun onComplete(ticket: String)
    abstract fun updateDisplay(msg: String)

    fun start(scope: CoroutineScope, url: String) {
        val url0 = url.replace("ssl.captcha.qq.com", "txhelper.glitch.me")
        val queue = scope.launch {
            updateDisplay(latestDisplay)
            while (isActive) {
                try {
                    val response: String = runInterruptible(Dispatchers.IO) { httpGet(url0) }
                    if (response.startsWith("请在")) {
                        if (response != latestDisplay) {
                            latestDisplay = response
                            updateDisplay(response)
                        }
                    } else {
                        onComplete(response)
                        return@launch
                    }
                } catch (e: Throwable) {
                    updateDisplay(e.toString().also { latestDisplay = it })
                }
                delay(1000)
            }
        }
        if (newClient) {
            queue.invokeOnCompletion { }
        }
        this.queue = queue
    }

    fun dispose() {
        queue.cancel()
    }

    @Throws(IOException::class)
    private fun httpGet(url: String): String {
        val connection = URL(url).openConnection() as? HttpURLConnection
            ?: throw UnsupportedOperationException("Could not create HttpURLConnection")
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.instanceFollowRedirects = true
        connection.doInput = true

        val result = connection.inputStream.use { it.readBytes() }.decodeToString()
        connection.disconnect()
        return result
    }
}