package net.mamoe.mirai.network.protocol.timpc.packet.event

import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.network.protocol.timpc.packet.Packet

/**
 * 事件包. 可被监听.
 *
 * @see Subscribable
 */
interface EventPacket : Subscribable, Packet