package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.data.PermissionDeniedException
import net.mamoe.mirai.api.http.data.StateCode
import net.mamoe.mirai.api.http.data.common.DTO
import net.mamoe.mirai.api.http.data.common.VerifyDTO
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member


fun Application.groupManageModule() {
    routing {

        /**
         * 禁言（需要相关权限）
         */
        miraiVerify<MuteDTO>("/muteAll") {
            it.session.bot.getGroup(it.target).muteAll = true
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/unmuteAll") {
            it.session.bot.getGroup(it.target).muteAll = false
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/mute") {
            when (it.session.bot.getGroup(it.target)[it.memberId].mute(it.time)) {
                true -> call.respondStateCode(StateCode.Success)
                else -> throw PermissionDeniedException
            }
        }

        miraiVerify<MuteDTO>("/unmute") {
            when (it.session.bot.getGroup(it.target).members[it.memberId].unmute()) {
                true -> call.respondStateCode(StateCode.Success)
                else -> throw PermissionDeniedException
            }
        }

        /**
         * 移出群聊（需要相关权限）
         */
        miraiVerify<KickDTO>("/kick") {
            when (it.session.bot.getGroup(it.target)[it.memberId].kick(it.msg)) {
                true -> call.respondStateCode(StateCode.Success)
                else -> throw PermissionDeniedException
            }
        }

        /**
         * 群设置（需要相关权限）
         */
        miraiGet("/groupConfig") {
            val group = it.bot.getGroup(paramOrNull("target"))
            call.respondDTO(GroupDetailDTO(group))
        }

        miraiVerify<GroupConfigDTO>("/groupConfig") { dto ->
            val group = dto.session.bot.getGroup(dto.target)
            with(dto.config) {
                name?.let { group.name = it }
                announcement?.let { group.announcement = it }
                confessTalk?.let { group.confessTalk = it }
                allowMemberInvite?.let { group.allowMemberInvite = it }
                // TODO: 待core接口实现设置可改
//                autoApprove?.let { group.autoApprove = it }
//                anonymousChat?.let { group.anonymousChat = it }
            }
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 群员信息管理（需要相关权限）
         */
        miraiGet("/memberInfo") {
            val member = it.bot.getGroup(paramOrNull("target"))[paramOrNull("memberId")]
            call.respondDTO(MemberDetailDTO(member))
        }

        miraiVerify<MemberInfoDTO>("/memberInfo") { dto ->
            val member = dto.session.bot.getGroup(dto.target)[dto.memberId]
            with(dto.info) {
                name?.let { member.nameCard = it }
                specialTitle?.let { member.specialTitle = it }
            }
            call.respondStateCode(StateCode.Success)
        }

    }
}


@Serializable
private data class MuteDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long = 0,
    val time: Int = 0
) : VerifyDTO()

@Serializable
private data class KickDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long,
    val msg: String = ""
) : VerifyDTO()

@Serializable
private data class GroupConfigDTO(
    override val sessionKey: String,
    val target: Long,
    val config: GroupDetailDTO
) : VerifyDTO()

@Serializable
private data class GroupDetailDTO(
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
private data class MemberInfoDTO(
    override val sessionKey: String,
    val target: Long,
    val memberId: Long,
    val info: MemberDetailDTO
) : VerifyDTO()

@Serializable
private data class MemberDetailDTO(
    val name: String? = null,
    val specialTitle: String? = null
) : DTO {
    constructor(member: Member) : this(member.nameCard, member.specialTitle)
}
