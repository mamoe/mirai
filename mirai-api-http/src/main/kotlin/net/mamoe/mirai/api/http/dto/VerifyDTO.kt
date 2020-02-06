package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.api.http.AuthedSession
import net.mamoe.mirai.contact.Group

@Serializable
abstract class VerifyDTO : DTO {
    abstract val sessionKey: String
    @Transient
    lateinit var session: AuthedSession // 反序列化验证后传入
}

@Serializable
data class BindDTO(override val sessionKey: String, val qq: Long) : VerifyDTO()

@Serializable
data class SendDTO(
    override val sessionKey: String,
    val target: Long,
    val messageChain: MessageChainDTO
) : VerifyDTO()

typealias GroupTargetDTO = FriendTargetDTO

@Serializable
data class FriendTargetDTO(
    override val sessionKey: String,
    val target: Long
) : VerifyDTO()

@Serializable
data class MuteDTO(
    override val sessionKey: String,
    val target: Long,
    val member: Long = 0,
    val time: Int = 0
) : VerifyDTO()

@Serializable
data class GroupConfigDTO(
    override val sessionKey: String,
    val target: Long,
    val config: GroupInfoDTO
) : VerifyDTO()

@Serializable
data class GroupInfoDTO(
    val name: String? = null,
    val announcement: String? = null,
    val confessTalk: Boolean? = null,
    val allowMemberInvite: Boolean? = null,
    val autoApprove: Boolean? = null,
    val anonymousChat: Boolean? = null
) : DTO {
    constructor(group: Group) : this(
        group.name, group.announcement, group.confessTalk, group.allowMemberInvite,
        group.autoApprove, group.anonymousChat
    )
}

@Serializable
open class StateCode(val code: Int, var msg: String) {
    object Success : StateCode(0, "success") // 成功
    object NoBot : StateCode(2, "指定Bot不存在")
    object IllegalSession : StateCode(3, "Session失效或不存在")
    object NotVerifySession : StateCode(4, "Session未认证")
    object NoElement : StateCode(5, "指定对象不存在")
    object PermissionDenied : StateCode(10, "无操作权限")

    // KS bug: 主构造器中不能有非字段参数 https://github.com/Kotlin/kotlinx.serialization/issues/575
    @Serializable
    class IllegalAccess() : StateCode(400, "") { // 非法访问
        constructor(msg: String) : this() {
            this.msg = msg
        }
    }
}

