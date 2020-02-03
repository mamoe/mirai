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
                call.respondDTO(AuthResDTO(1, ""))
            } else {
                call.respondDTO(AuthResDTO(0, SessionManager.createTempSession().key))
            }
        }

        miraiVerify<BindDTO>("/verify", verifiedSessionKey = false) {
            try {
                val bot = Bot.instanceWhose(it.qq)
                with(SessionManager) {
                    closeSession(it.sessionKey)
                    allSession[it.sessionKey] = AuthedSession(bot, EmptyCoroutineContext)
                }
                call.respondDTO(StateCodeDTO.Success)
            } catch (e: NoSuchElementException) {
                call.respondDTO(StateCodeDTO.NoBot)
            }
        }

        miraiVerify<BindDTO>("/release") {
            SessionManager.closeSession(it.sessionKey)
            call.respondDTO(StateCodeDTO.Success)
        }

    }
}
