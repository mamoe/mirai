/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.util

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.api.http.data.common.*

// 解析失败时直接返回null，由路由判断响应400状态
@UseExperimental(ImplicitReflectionSerializer::class)
inline fun <reified T : Any> String.jsonParseOrNull(
    serializer: DeserializationStrategy<T>? = null
): T? = try {
    if(serializer == null) MiraiJson.json.parse(this) else Json.parse(this)
} catch (e: Exception) { null }


@UseExperimental(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified T : Any> T.toJson(
    serializer: SerializationStrategy<T>? = null
): String = if (serializer == null) MiraiJson.json.stringify(this)
else MiraiJson.json.stringify(serializer, this)


// 序列化列表时，stringify需要使用的泛型是T，而非List<T>
// 因为使用的stringify的stringify(objs: List<T>)重载
@UseExperimental(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified T : Any> List<T>.toJson(
    serializer: SerializationStrategy<List<T>>? = null
): String = if (serializer == null) MiraiJson.json.stringify(this)
else MiraiJson.json.stringify(serializer, this)


/**
 * Json解析规则，需要注册支持的多态的类
 */
object MiraiJson {
    val json = Json(context = SerializersModule {

        polymorphic(EventDTO.serializer()) {
            GroupMessagePacketDTO::class with GroupMessagePacketDTO.serializer()
            FriendMessagePacketDTO::class with FriendMessagePacketDTO.serializer()

            BotOnlineEventDTO::class with BotOnlineEventDTO.serializer()
            BotOfflineEventActiveDTO::class with BotOfflineEventActiveDTO.serializer()
            BotOfflineEventForceDTO::class with BotOfflineEventForceDTO.serializer()
            BotOfflineEventDroppedDTO::class with BotOfflineEventDroppedDTO.serializer()
            BotReloginEventDTO::class with BotReloginEventDTO.serializer()
            BotGroupPermissionChangeEventDTO::class with BotGroupPermissionChangeEventDTO.serializer()
            BotMuteEventDTO::class with BotMuteEventDTO.serializer()
            BotUnmuteEventDTO::class with BotUnmuteEventDTO.serializer()
            BotJoinGroupEventDTO::class with BotJoinGroupEventDTO.serializer()
            GroupNameChangeEventDTO::class with GroupNameChangeEventDTO.serializer()
            GroupEntranceAnnouncementChangeEventDTO::class with GroupEntranceAnnouncementChangeEventDTO.serializer()
            GroupMuteAllEventDTO::class with GroupMuteAllEventDTO.serializer()
            GroupAllowAnonymousChatEventDTO::class with GroupAllowAnonymousChatEventDTO.serializer()
            GroupAllowConfessTalkEventDTO::class with GroupAllowConfessTalkEventDTO.serializer()
            GroupAllowMemberInviteEventDTO::class with GroupAllowMemberInviteEventDTO.serializer()
            MemberJoinEventDTO::class with MemberJoinEventDTO.serializer()
            MemberLeaveEventKickDTO::class with MemberLeaveEventKickDTO.serializer()
            MemberLeaveEventQuitDTO::class with MemberLeaveEventQuitDTO.serializer()
            MemberCardChangeEventDTO::class with MemberCardChangeEventDTO.serializer()
            MemberSpecialTitleChangeEventDTO::class with MemberSpecialTitleChangeEventDTO.serializer()
            MemberPermissionChangeEventDTO::class with MemberPermissionChangeEventDTO.serializer()
            MemberMuteEventDTO::class with MemberMuteEventDTO.serializer()
            MemberUnmuteEventDTO::class with MemberUnmuteEventDTO.serializer()
        }

        // Message Polymorphic
//        polymorphic(MessageDTO.serializer()) {
//            MessageSourceDTO::class with MessageSourceDTO.serializer()
//            AtDTO::class with AtDTO.serializer()
//            AtAllDTO::class with AtAllDTO.serializer()
//            FaceDTO::class with FaceDTO.serializer()
//            PlainDTO::class with PlainDTO.serializer()
//            ImageDTO::class with ImageDTO.serializer()
//            XmlDTO::class with XmlDTO.serializer()
//            UnknownMessageDTO::class with UnknownMessageDTO.serializer()
//        }
    })
}