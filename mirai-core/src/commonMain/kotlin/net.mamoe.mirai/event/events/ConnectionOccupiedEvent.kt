package net.mamoe.mirai.event.events

import net.mamoe.mirai.data.EventPacket

/**
 * 被挤下线. 只能获取到中文的消息
 */
inline class ConnectionOccupiedEvent(val message: String) : EventPacket {
    override fun toString(): String = "ConnectionOccupiedEvent(${message.replace("\n", "")})"
}
