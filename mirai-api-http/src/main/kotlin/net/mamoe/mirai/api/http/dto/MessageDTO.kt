package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

@Serializable
@SerialName("UnKnownMessage")
data class UnKnownMessagePacketDTO(val msg: String) : MessagePacketDTO()

// Message
@Serializable
@SerialName("At")
data class AtDTO(val target: Long, val display: String) : MessageDTO()
@Serializable
@SerialName("Face")
data class FaceDTO(val faceId: Int) : MessageDTO()
@Serializable
@SerialName("Plain")
data class PlainDTO(val text: String) : MessageDTO()
@Serializable
@SerialName("Image")
data class ImageDTO(val path: String) : MessageDTO()
@Serializable
@SerialName("Xml")
data class XmlDTO(val xml: String) : MessageDTO()
@Serializable
@SerialName("Unknown")
data class UnknownMessageDTO(val text: String) : MessageDTO()

/*
*   Abstract Class
* */
@Serializable
sealed class MessagePacketDTO : DTO {
    lateinit var messageChain : MessageChainDTO
}

typealias MessageChainDTO = Array<MessageDTO>

@Serializable
sealed class MessageDTO : DTO


/*
    Extend function
 */
suspend fun MessagePacket<*, *>.toDTO(): MessagePacketDTO = when (this) {
    is FriendMessage -> FriendMessagePacketDTO(QQDTO(sender))
    is GroupMessage -> GroupMessagePacketDTO(MemberDTO(sender))
    else -> UnKnownMessagePacketDTO("UnKnown Message Packet")
}.apply { messageChain = Array(message.size){ message[it].toDTO() }}

fun MessageChainDTO.toMessageChain() =
    MessageChain().apply { this@toMessageChain.forEach { add(it.toMessage()) } }

@UseExperimental(ExperimentalUnsignedTypes::class)
fun Message.toDTO() = when (this) {
    is At -> AtDTO(target, display)
    is Face -> FaceDTO(id.value.toInt())
    is PlainText -> PlainDTO(stringValue)
    is Image -> ImageDTO(this.toString())
    is XMLMessage -> XmlDTO(stringValue)
    else -> UnknownMessageDTO("未知消息类型")
}

@UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
fun MessageDTO.toMessage() = when (this) {
    is AtDTO -> At(target, display)
    is FaceDTO -> Face(FaceId(faceId.toUByte()))
    is PlainDTO -> PlainText(text)
    is ImageDTO -> PlainText("[暂时不支持图片]")
    is XmlDTO -> XMLMessage(xml)
    is UnknownMessageDTO -> PlainText("assert cannot reach")
}



