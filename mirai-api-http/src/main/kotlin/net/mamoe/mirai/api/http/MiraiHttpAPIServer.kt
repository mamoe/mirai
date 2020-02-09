/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http

import io.ktor.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI
import net.mamoe.mirai.api.http.route.mirai
import net.mamoe.mirai.utils.DefaultLogger

object MiraiHttpAPIServer {

    private val logger = DefaultLogger("Mirai HTTP API")

    init {
        SessionManager.authKey = generateSessionKey()//用于验证的key, 使用和SessionKey相同的方法生成, 但意义不同
    }

    @UseExperimental(KtorExperimentalAPI::class)
    fun start(
        port: Int = 8080,
        authKey: String,
        callback: (() -> Unit)? = null
    ) {
        require(authKey.length in 8..128) { "Expected authKey length is between 8 to 128" }
        SessionManager.authKey = authKey

        // TODO: start是无阻塞的，理应获取启动状态后再执行后续代码
        try {
            embeddedServer(CIO, port, module = Application::mirai).start()

            logger.info("Http api server is running with authKey: ${SessionManager.authKey}")
            callback?.invoke()
        } catch (e: Exception) {
            logger.error("Http api server launch error")
        }
    }
}