/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import net.mamoe.mirai.Mirai

internal abstract class TxCaptchaHelper {
    private val newClient: Boolean
    val client: HttpClient
    private lateinit var queue: Job

    init {
        var newClient = false
        client = try {
            Mirai.Http
        } catch (ignore: Throwable) {
            newClient = true
            HttpClient()
        }
        this.newClient = newClient
    }

    internal var latestDisplay = "Sending request..."

    abstract fun onComplete(ticket: String)
    abstract fun updateDisplay(msg: String)

    fun start(scope: CoroutineScope, url: String) {
        val url0 = url.replace("ssl.captcha.qq.com", "txhelper.glitch.me")
        val queue = scope.launch {
            updateDisplay(latestDisplay)
            while (isActive) {
                try {
                    val response: String = client.get(url0)
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
            queue.invokeOnCompletion { client.close() }
        }
        this.queue = queue
    }

    fun dispose() {
        queue.cancel()
    }
}
