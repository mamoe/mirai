package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.api.http.AuthedSession

@Serializable
abstract class VerifyDTO : DTO {
    abstract val sessionKey: String
    @Transient lateinit var session: AuthedSession // 反序列化验证后传入
}

@Serializable
data class BindDTO(override val sessionKey: String, val qq: Long) : VerifyDTO()


// 写成data class并继承DTO接口是为了返回时的形式统一
@Serializable
open class StateCodeDTO(val code: Int, val msg: String) : DTO {
    companion object {
        val SUCCESS = StateCodeDTO(0, "success") // 成功
//        val AUTH_WRONG = CodeDTO(1) // AuthKey错误, @see AuthResDTO
        val NO_BOT = StateCodeDTO(2, "指定Bot不存在")
        val ILLEGAL_SESSION = StateCodeDTO(3, "Session失效或不存在")
        val NOT_VERIFIED_SESSION = StateCodeDTO(3, "Session未认证")
    }
    class ILLEGAL_ACCESS(msg: String) : StateCodeDTO(400, msg) // 非法访问
}

@Serializable
data class SendDTO(
    override val sessionKey: String,
    val target: Long,
    val messageChain: MessageChainDTO
) : VerifyDTO()
