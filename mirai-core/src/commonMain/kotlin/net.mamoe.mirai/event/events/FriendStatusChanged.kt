package net.mamoe.mirai.event.events

import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.utils.OnlineStatus

data class FriendStatusChanged(
    val qq: QQ,
    val status: OnlineStatus
) : EventPacket
