package net.mamoe.mirai.event.events.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.bot.BotEvent
import net.mamoe.mirai.network.protocol.tim.packet.Packet

/**
 * @author Him188moe
 */
abstract class PacketEvent<out P : Packet>(bot: Bot, open val packet: P) : BotEvent(bot)
