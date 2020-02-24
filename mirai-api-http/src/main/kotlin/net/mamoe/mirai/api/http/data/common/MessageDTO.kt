/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.data.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.net.URL

/*
*   DTO data class
* */

// MessagePacket
@Serializable
@SerialName("FriendMessage")
data class FriendMessagePacketDTO(val sender: QQDTO) : MessagePacketDTO()

@Serializable
@SerialName("GroupMessage")
data class GroupMessagePacketDTO(val sender: MemberDTO) : MessagePacketDTO()


// Message
@Serializable
@SerialName("Source")
data class MessageSourceDTO(val id: Long) : MessageDTO()

@Serializable
@SerialName("At")
data class AtDTO(val target: Long, val display: String = "") : MessageDTO()

@Serializable
@SerialName("AtAll")
data class AtAllDTO(val target: Long = 0) : MessageDTO() // target为保留字段

@Serializable
@SerialName("Face")
data class FaceDTO(val faceId: Int) : MessageDTO()

@Serializable
@SerialName("Plain")
data class PlainDTO(val text: String) : MessageDTO()

@Serializable
@SerialName("Image")
data class ImageDTO(val imageId: String? = null, val url: String? = null) : MessageDTO()

@Serializable
@SerialName("Xml")
data class XmlDTO(val xml: String) : MessageDTO()

@Serializable
@SerialName("Unknown")
object UnknownMessageDTO : MessageDTO()

/*
*   Abstract Class
* */
@Serializable
sealed class MessagePacketDTO : EventDTO() {
    lateinit var messageChain: MessageChainDTO
}

typealias MessageChainDTO = List<MessageDTO>

@Serializable
sealed class MessageDTO : DTO


/*
    Extend function
 */
suspend fun MessagePacket<*, *>.toDTO() = when (this) {
    is FriendMessage -> FriendMessagePacketDTO(QQDTO(sender))
    is GroupMessage -> GroupMessagePacketDTO(MemberDTO(sender))
    else -> IgnoreEventDTO
}.apply {
    if (this is MessagePacketDTO) {
        // 将MessagePacket中的所有Message转为DTO对象，并添加到messageChain
        // foreachContent会忽略MessageSource，一次主动获取
        messageChain = mutableListOf(messageDTO(message[MessageSource])).apply {
            message.foreachContent { content -> messageDTO(content).takeUnless { it == UnknownMessageDTO }?.let(::add) }
        }
        // else: `this` is bot event
    }
}

suspend fun MessageChainDTO.toMessageChain(contact: Contact) =
    buildMessageChain { this@toMessageChain.forEach { it.toMessage(contact)?.let(::add) } }


@UseExperimental(ExperimentalUnsignedTypes::class)
suspend fun MessagePacket<*, *>.messageDTO(message: Message) = when (message) {
    is MessageSource -> MessageSourceDTO(message.id)
    is At -> AtDTO(message.target, message.display)
    is AtAll -> AtAllDTO(0L)
    is Face -> FaceDTO(message.id)
    is PlainText -> PlainDTO(message.stringValue)
    is Image -> ImageDTO(message.imageId, message.url())
    is XMLMessage -> XmlDTO(message.stringValue)
    else -> UnknownMessageDTO
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
suspend fun MessageDTO.toMessage(contact: Contact) = when (this) {
    is AtDTO -> At((contact as Group)[target])
    is AtAllDTO -> AtAll
    is FaceDTO -> Face(faceId)
    is PlainDTO -> PlainText(text)
    is ImageDTO -> when {
        !imageId.isNullOrBlank() -> Image(imageId)
        !url.isNullOrBlank() -> contact.uploadImage(URL(url))
        else -> null
    }
    is XmlDTO -> XMLMessage(xml)
    is MessageSourceDTO, is UnknownMessageDTO -> null
}

