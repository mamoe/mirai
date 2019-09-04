package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.packet.ServerPacket
import java.io.DataInputStream

/**
 * Congratulations!
 *
 * @author Him188moe
 */
class ServerLoginSuccessPacket(input: DataInputStream) : ServerPacket(input)