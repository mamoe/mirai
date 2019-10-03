package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import java.io.DataInputStream

/**
 * Congratulations!
 *
 * @author Him188moe
 */
@PacketId("00 EC")
class ServerLoginSuccessPacket(input: DataInputStream) : ServerPacket(input)