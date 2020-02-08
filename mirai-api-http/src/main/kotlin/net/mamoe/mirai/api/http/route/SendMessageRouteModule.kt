package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.data.*
import net.mamoe.mirai.api.http.data.common.*
import net.mamoe.mirai.api.http.util.toJson

fun Application.messageModule() {
    routing {

        miraiGet("/fetchMessage") {
            val count: Int = paramOrNull("count")
            val fetch = it.messageQueue.fetch(count)
            val ls = Array(fetch.size) { index -> fetch[index].toDTO() }

            call.respondJson(ls.toList().toJson())
        }

        miraiVerify<SendDTO>("/sendFriendMessage") {
            it.session.bot.getFriend(it.target).sendMessage(it.messageChain.toMessageChain())
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<SendDTO>("/sendGroupMessage") {
            it.session.bot.getGroup(it.target).sendMessage(it.messageChain.toMessageChain())
            call.respondStateCode(StateCode.Success)
        }

    }
}

@Serializable
private data class SendDTO(
    override val sessionKey: String,
    val target: Long,
    val messageChain: MessageChainDTO
) : VerifyDTO()