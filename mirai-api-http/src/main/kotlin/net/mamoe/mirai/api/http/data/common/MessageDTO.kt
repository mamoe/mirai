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
import net.mamoe.mirai.utils.MiraiInternalAPI

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
data class ImageDTO(val imageId: String) : MessageDTO()

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
fun MessagePacket<*, *>.toDTO() = when (this) {
    is FriendMessage -> FriendMessagePacketDTO(QQDTO(sender))
    is GroupMessage -> GroupMessagePacketDTO(MemberDTO(sender))
    else -> IgnoreEventDTO
}.apply {
    if (this is MessagePacketDTO) { messageChain = message.toDTOChain() }
    // else: `this` is bot event
}

fun MessageChain.toDTOChain() = mutableListOf(this[MessageSource].toDTO()).apply {
    foreachContent { content -> content.toDTO().takeUnless { it == UnknownMessageDTO }?.let(::add) }
}

fun MessageChainDTO.toMessageChain(contact: Contact) =
    buildMessageChain { this@toMessageChain.forEach { add(it.toMessage(contact)) } }

@UseExperimental(ExperimentalUnsignedTypes::class)
fun Message.toDTO() = when (this) {
    is MessageSource -> MessageSourceDTO(id)
    is At -> AtDTO(target, display)
    is AtAll -> AtAllDTO(0L)
    is Face -> FaceDTO(id)
    is PlainText -> PlainDTO(stringValue)
    is Image -> ImageDTO(imageId)
    is XMLMessage -> XmlDTO(stringValue)
    else -> UnknownMessageDTO
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
fun MessageDTO.toMessage(contact: Contact) = when (this) {
    is AtDTO -> At((contact as Group)[target])
    is AtAllDTO -> AtAll
    is FaceDTO -> Face(faceId)
    is PlainDTO -> PlainText(text)
    is ImageDTO -> Image(imageId)
    is XmlDTO -> XMLMessage(xml)
    is MessageSourceDTO, is UnknownMessageDTO -> PlainText("assert cannot reach")
}

