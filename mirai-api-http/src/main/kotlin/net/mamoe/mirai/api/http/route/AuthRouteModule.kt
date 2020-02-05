package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.AuthedSession
import net.mamoe.mirai.api.http.SessionManager
import net.mamoe.mirai.api.http.dto.*
import kotlin.coroutines.EmptyCoroutineContext


fun Application.authModule() {
    routing {
        miraiAuth("/auth") {
            if (it.authKey != SessionManager.authKey) {
                call.respondStateCode(StateCode(1, "Auth Key错误"))
            } else {
                call.respondStateCode(StateCode(0, SessionManager.createTempSession().key))
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

private fun getBotOrThrow(qq: Long) = try {
    Bot.instanceWhose(qq)
} catch (e: NoSuchElementException) {
    throw NoSuchBotException
}