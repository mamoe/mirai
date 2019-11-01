@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.packet.AnnotatedId
import net.mamoe.mirai.network.protocol.tim.packet.KnownPacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket

/**
 * Congratulations!
 *
 * @author Him188moe
 */
@AnnotatedId(KnownPacketId.CHANGE_ONLINE_STATUS)
class ServerLoginSuccessPacket(input: ByteReadPacket) : ServerPacket(input)//TODO 可能只是 login status change 的返回包