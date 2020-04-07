package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.onlinePush0x210

import kotlinx.io.core.String
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs

internal object OnlinePush0x210Factory {

//    fun solve(msg: MsgType0x210, bot: Bot): Any? = when (msg.uSubMsgType) {
//        0x27L -> {
//            val body = msg.vProtobuf?.loadAs(SubMsgType0x27.MsgBody.serializer())
//            if (body?.msgModInfos != null) {
//                body.msgModInfos.firstOrNull()?.msgModGroupMemberProfile?.run {
//                    val member = bot.groups.getOrNull(groupUin)?.getOrNull(uin)
//                    val new = msgGroupMemberProfileInfos?.firstOrNull()
//                    if (member != null && new?.value != null) {
//                        MemberCardChangeEvent(member.nameCard, String(new.value), member, member)
//                    }
//                }
//            } else null
//        }
//        else -> null
//    }
}