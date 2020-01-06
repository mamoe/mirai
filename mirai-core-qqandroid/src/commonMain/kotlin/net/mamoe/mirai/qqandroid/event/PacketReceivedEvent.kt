package net.mamoe.mirai.qqandroid.event

import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.event.Event

/**
 * 接收到数据包
 */
class PacketReceivedEvent(val packet: Packet) : Event(), Cancellable