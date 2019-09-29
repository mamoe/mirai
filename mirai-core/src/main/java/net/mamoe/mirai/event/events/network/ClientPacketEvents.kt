package net.mamoe.mirai.event.events.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket

abstract class ClientPacketEvent<out P : ClientPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

class PacketSentEvent(bot: Bot, packet: ClientPacket) : ClientPacketEvent<ClientPacket>(bot, packet)

class BeforePacketSendEvent(bot: Bot, packet: ClientPacket) : ClientPacketEvent<ClientPacket>(bot, packet)