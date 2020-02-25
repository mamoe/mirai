/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.AuthedSession
import net.mamoe.mirai.api.http.SessionManager
import net.mamoe.mirai.api.http.data.NoSuchBotException
import net.mamoe.mirai.api.http.data.StateCode
import net.mamoe.mirai.api.http.data.common.DTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import kotlin.coroutines.EmptyCoroutineContext


fun Application.authModule() {
    routing {
        miraiAuth("/auth") {
            if (it.authKey != SessionManager.authKey) {
                call.respondStateCode(StateCode(1, "Auth Key错误"))
            } else {
                call.respondDTO(AuthRetDTO(0, SessionManager.createTempSession().key))
            }
        }

        miraiVerify<BindDTO>("/verify", verifiedSessionKey = false) {
            val bot = getBotOrThrow(it.qq)
            with(SessionManager) {
                closeSession(it.sessionKey)
                allSession[it.sessionKey] = AuthedSession(bot, EmptyCoroutineContext)
            }
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<BindDTO>("/release") {
            val bot = getBotOrThrow(it.qq)
            val session = SessionManager[it.sessionKey] as AuthedSession
            if (bot.uin == session.bot.uin) {
                SessionManager.closeSession(it.sessionKey)
                call.respondStateCode(StateCode.Success)
            } else {
                throw NoSuchElementException()
            }
        }

    }
}

@Serializable
private data class AuthRetDTO(val code: Int, val session: String) : DTO

@Serializable
private data class BindDTO(override val sessionKey: String, val qq: Long) : VerifyDTO()

private fun getBotOrThrow(qq: Long) = try {
    Bot.getInstance(qq)
} catch (e: NoSuchElementException) {
    throw NoSuchBotException
}