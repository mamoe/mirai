/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.data.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.api.http.AuthedSession

interface DTO

@Serializable
data class AuthDTO(val authKey: String) : DTO

@Serializable
abstract class VerifyDTO : DTO {
    abstract val sessionKey: String
    @Transient
    internal lateinit var session: AuthedSession // 反序列化验证成功后传入
}

@Serializable
abstract class EventDTO : DTO

object IgnoreEventDTO : EventDTO()