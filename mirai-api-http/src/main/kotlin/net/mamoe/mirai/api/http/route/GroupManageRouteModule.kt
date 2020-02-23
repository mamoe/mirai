/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import kotlinx.serialization.Serializable
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
            it.session.bot.getGroup(it.target).isMuteAll = true
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/unmuteAll") {
            it.session.bot.getGroup(it.target).isMuteAll = false
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/mute") {
            it.session.bot.getGroup(it.target)[it.memberId].mute(it.time)
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/unmute") {
            it.session.bot.getGroup(it.target).members[it.memberId].unmute()
            call.respondStateCode(StateCode.Success)
        }

        /**
         * 移出群聊（需要相关权限）
         */
        miraiVerify<KickDTO>("/kick") {
            it.session.bot.getGroup(it.target)[it.memberId].kick(it.msg)
            call.respondStateCode(StateCode.Success)
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
                announcement?.let { group.entranceAnnouncement = it }
                confessTalk?.let { group.isConfessTalkEnabled = it }
                allowMemberInvite?.let { group.isAllowMemberInvite = it }
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
        group.name, group.entranceAnnouncement, group.isConfessTalkEnabled, group.isAllowMemberInvite,
        group.isAutoApproveEnabled, group.isAnonymousChatEnabled
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
