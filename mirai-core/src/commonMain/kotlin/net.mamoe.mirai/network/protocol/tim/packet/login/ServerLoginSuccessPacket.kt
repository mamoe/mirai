@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket

/**
 * Congratulations!
 *
 * @author Him188moe
 */
@PacketId(0x00_ECu)
class ServerLoginSuccessPacket(input: ByteReadPacket) : ServerPacket(input)//TODO 可能只是 login status change 的返回包