package net.mamoe.mirai.event.events.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket


abstract class ServerPacketEvent<out P : ServerPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

class ServerPacketReceivedEvent(bot: Bot, packet: ServerPacket) : ServerPacketEvent<ServerPacket>(bot, packet)