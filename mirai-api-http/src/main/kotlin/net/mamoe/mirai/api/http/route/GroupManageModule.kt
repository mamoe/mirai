package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.routing
import net.mamoe.mirai.api.http.dto.MuteDTO
import net.mamoe.mirai.api.http.dto.StateCode


fun Application.groupManageModule() {
    routing {

        miraiVerify<MuteDTO>("/muteAll") {
            it.session.bot.getGroup(it.target).muteAll = true
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/unmuteAll") {
            it.session.bot.getGroup(it.target).muteAll = false
            call.respondStateCode(StateCode.Success)
        }

        miraiVerify<MuteDTO>("/mute") {
            when(it.session.bot.getGroup(it.target).members[it.member].mute(it.time)) {
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

    }
}