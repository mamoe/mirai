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
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.AuthedSession
import net.mamoe.mirai.api.http.SessionManager
import net.mamoe.mirai.api.http.data.*
import net.mamoe.mirai.api.http.data.common.MessageChainDTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import net.mamoe.mirai.api.http.data.common.toDTO
import net.mamoe.mirai.api.http.data.common.toMessageChain
import net.mamoe.mirai.api.http.util.toJson
import net.mamoe.mirai.contact.toList
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadImage
import java.net.URL

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

        miraiVerify<SendDTO>("/quoteMessage") {
            it.session.messageQueue.quoteCache[it.target]?.quoteReply(it.messageChain.toMessageChain())
                ?: throw NoSuchElementException()
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<SendImageDTO>("sendImageMessage") {
            val bot = it.session.bot
            val contact = when {
                it.target != null -> bot[it.target]
                it.qq != null -> bot.getFriend(it.qq)
                it.group != null -> bot.getGroup(it.group)
                else -> throw IllegalParamException("target、qq、group不可全为null")
            }
            val ls = it.urls.map { url -> contact.uploadImage(URL(url)) }
            contact.sendMessage(MessageChain(ls))
            call.respondJson(ls.map { image -> image.imageId }.toJson())
        }

        // TODO: 重构
        post("uploadImage") {
            val parts = call.receiveMultipart().readAllParts()
            val sessionKey = parts.value("sessionKey")
            if (!SessionManager.containSession(sessionKey)) throw IllegalSessionException
            val session = try {
                SessionManager[sessionKey] as AuthedSession
            } catch (e: TypeCastException) {
                throw NotVerifiedSessionException
            }

            val type = parts.value("type")
            parts.file("img")?.apply {
                val image = streamProvider().use {
                    when (type) {
                        "group" -> session.bot.groups.toList().random().uploadImage(it)
                        "friend" -> session.bot.qqs.toList().random().uploadImage(it)
                        else -> null
                    }
                }
                image?.apply {
                    call.respondText(imageId)
                } ?: throw IllegalAccessException("图片上传错误")
            } ?: throw IllegalAccessException("未知错误")
        }
    }
}

@Serializable
private data class SendDTO(
    override val sessionKey: String,
    val target: Long,
    val messageChain: MessageChainDTO
) : VerifyDTO()

@Serializable
private data class SendImageDTO(
    override val sessionKey: String,
    val target: Long? = null,
    val qq: Long? = null,
    val group: Long? = null,
    val urls: List<String>
) : VerifyDTO()

