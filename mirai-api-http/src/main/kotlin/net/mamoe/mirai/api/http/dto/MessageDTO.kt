package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.message.data.*

/*
    DTO data class
 */

@Serializable
@SerialName("FriendMessage")
data class FriendMessagePacketDTO(val sender: QQDTO) : MessagePacketDTO()

@Serializable
@SerialName("GroupMessage")
data class GroupMessagePacketDTO(val sender: MemberDTO) : MessagePacketDTO()

@Serializable
data class MessageDTO(val type: MessageType, val data: String) : DTO

typealias MessageChainDTO = Array<MessageDTO>

@Serializable
abstract class MessagePacketDTO : DTO {
    lateinit var messageChain : MessageChainDTO

    companion object {
        val EMPTY = @SerialName("UnknownMessage") object : MessagePacketDTO() {}
    }
}


/*
    Extend function
 */
suspend fun MessagePacket<*, *>.toDTO(): MessagePacketDTO = when (this) {
    is FriendMessage -> FriendMessagePacketDTO(QQDTO(sender))
    is GroupMessage -> GroupMessagePacketDTO(MemberDTO(sender, senderName))
    else -> MessagePacketDTO.EMPTY
}.apply { messageChain = Array(message.size){ message[it].toDTO() }}

fun MessageChainDTO.toMessageChain() =
    MessageChain().apply { this@toMessageChain.forEach { add(it.toMessage()) } }

@UseExperimental(ExperimentalUnsignedTypes::class)
fun Message.toDTO() = when (this) {
    is At -> MessageDTO(MessageType.AT, target.toString())
    is Face -> MessageDTO(MessageType.FACE, id.value.toString())
    is PlainText -> MessageDTO(MessageType.PLAIN, stringValue)
//    is Image -> MessageDTO(MessageType.IMAGE, ???)
    is Image -> MessageDTO(MessageType.IMAGE, "NOT SUPPORT IMAGE NOW")
    is XMLMessage -> MessageDTO(MessageType.XML, stringValue)
    else -> MessageDTO(MessageType.UNKNOWN, "not support type")
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun MessageDTO.toMessage() = when (type) {
    MessageType.AT -> At(data.toLong())
    MessageType.FACE -> Face(FaceId(data.toUByte()))
    MessageType.PLAIN -> PlainText(data)
//    MessageType.IMAGE -> Image(???)
    MessageType.IMAGE -> PlainText(data)
    MessageType.XML -> XMLMessage(data)
    MessageType.UNKNOWN -> PlainText(data)
}


/*
    Enum
 */

// TODO: will be replace by [net.mamoe.mirai.message.MessageType]
enum class MessageType {
    AT,
    FACE,
    PLAIN,
    IMAGE,
    XML,
    UNKNOWN,
}


