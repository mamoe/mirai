package net.mamoe.mirai.network.protocol.tim.packet.event

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.InternalAPI

/**
 * 平台相关扩展
 */
@UseExperimental(InternalAPI::class)
actual sealed class MessagePacket<TSubject : Contact> : MessagePacketBase<TSubject>()