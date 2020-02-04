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
            try {
                val bot = Bot.instanceWhose(it.qq)
                with(SessionManager) {
                    closeSession(it.sessionKey)
                    allSession[it.sessionKey] = AuthedSession(bot, EmptyCoroutineContext)
                }
                call.respondStateCode(StateCode.Success)
            } catch (e: NoSuchElementException) {
                call.respondStateCode(StateCode.NoBot)
            }
        }

        miraiVerify<BindDTO>("/release") {
            SessionManager.closeSession(it.sessionKey)
            call.respondStateCode(StateCode.Success)
        }

    }
}
