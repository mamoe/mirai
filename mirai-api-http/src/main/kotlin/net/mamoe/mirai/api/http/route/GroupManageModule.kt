package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import net.mamoe.mirai.api.http.dto.*


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
            when(it.session.bot.getGroup(it.target)[it.member].mute(it.time)) {
                true -> call.respondStateCode(StateCode.Success)
                else -> throw PermissionDeniedException
            }
        }

        miraiVerify<MuteDTO>("/unmute") {
            when(it.session.bot.getGroup(it.target).members[it.member].unmute()) {
                true -> call.respondStateCode(StateCode.Success)
                else -> throw PermissionDeniedException
            }
        }

        /**
         * 群设置（需要相关权限）
         */
        miraiGet("/groupConfig") {
            val group = it.bot.getGroup(paramOrNull("target"))
            call.respondDTO(GroupInfoDTO(group))
        }

        miraiVerify<GroupConfigDTO>("/groupConfig") { dto ->
            val group = dto.session.bot.getGroup(dto.target)
            with(dto.config) {
                name?.let { group.name = it }
                announcement?.let { group.announcement = it }
                confessTalk?.let { group.confessTalk = it }
                allowMemberInvite?.let{ group.allowMemberInvite = it }
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
            val member = it.bot.getGroup(paramOrNull("target"))[paramOrNull("memberID")]
            call.respondDTO(MemberInfoDTO(member))
        }

        miraiVerify<MemberConfigDTO>("/memberInfo") { dto ->
            val member = dto.session.bot.getGroup(dto.target)[dto.memberID]
            with(dto.config) {
                name?.let { member.groupCard = it }
                specialTitle?.let { member.specialTitle = it }
            }
            call.respondStateCode(StateCode.Success)
        }

    }
}