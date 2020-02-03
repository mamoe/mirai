package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import net.mamoe.mirai.api.http.dto.*

fun Application.messageModule() {
    routing {

        miraiGet("/fetchMessage") {
            val count: Int = paramOrNull("count")
            val fetch = it.messageQueue.fetch(count)
            val ls = Array(fetch.size) { index -> fetch[index].toDTO() }

            call.respondJson(ls.toList().toJson())
        }

        miraiVerify<SendDTO>("/sendFriendMessage") {
            it.session.bot.getQQ(it.target).sendMessage(it.messageChain.toMessageChain())
            call.respondDTO(StateCodeDTO.Success)
        }

        miraiVerify<SendDTO>("/sendGroupMessage") {
            it.session.bot.getGroup(it.target).sendMessage(it.messageChain.toMessageChain())
            call.respondDTO(StateCodeDTO.Success)
        }

        miraiVerify<VerifyDTO>("/event/message") {

        }

        miraiVerify<VerifyDTO>("/addFriend") {

        }
    }
}