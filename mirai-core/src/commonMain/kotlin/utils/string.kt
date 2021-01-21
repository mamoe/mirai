package net.mamoe.mirai.internal.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.MiraiInternalApi

@Serializable
internal data class MessageData(
    val data: String,
    val cmd: Int,
    val text: String
)

@Suppress("RegExpRedundantEscape")
internal val extraJsonPattern = Regex("<(\\{.*?\\})>")

@MiraiInternalApi
internal fun String.parseToMessageDataList(): Sequence<MessageData> {
    return extraJsonPattern.findAll(this).filter { it.groups.size == 2 }.mapNotNull { result ->
        Json.decodeFromString(MessageData.serializer(), result.groups[1]!!.value)
    }
}

