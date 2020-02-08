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
import net.mamoe.mirai.api.http.data.StateCode
import net.mamoe.mirai.api.http.data.common.MessageChainDTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import net.mamoe.mirai.api.http.data.common.toDTO
import net.mamoe.mirai.api.http.data.common.toMessageChain
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