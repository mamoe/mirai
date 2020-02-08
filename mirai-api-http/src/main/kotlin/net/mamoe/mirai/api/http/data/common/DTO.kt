package net.mamoe.mirai.api.http.data.common

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.api.http.AuthedSession

interface DTO

@Serializable
data class AuthDTO(val authKey: String) : DTO

@Serializable
abstract class VerifyDTO : DTO {
    abstract val sessionKey: String
    @Transient
    lateinit var session: AuthedSession // 反序列化验证成功后传入
}
