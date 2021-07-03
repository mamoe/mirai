/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.utils.MiraiInternalApi

@Serializable
internal data class MessageData(
    val data: String,
    val cmd: Int,
    val text: String,
)

internal fun MessageData.toMemberInfo() = MemberInfoImpl(
    uin = data.toLong(),
    nick = text,
    permission = MemberPermission.MEMBER,
    remark = "",
    nameCard = "",
    specialTitle = "",
    muteTimestamp = 0,
    anonymousId = null,
    isOfficialBot = true
)

@Suppress("RegExpRedundantEscape")
internal val extraJsonPattern = Regex("<(\\{.*?\\})>")

@MiraiInternalApi
internal fun String.parseToMessageDataList(): Sequence<MessageData> {
    return extraJsonPattern.findAll(this).filter { it.groups.size == 2 }.mapNotNull { result ->
        Json.decodeFromString(MessageData.serializer(), result.groups[1]!!.value)
    }
}

