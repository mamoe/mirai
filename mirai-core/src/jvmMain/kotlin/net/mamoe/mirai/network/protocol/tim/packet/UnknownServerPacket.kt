package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.utils.LoggerTextFormat
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class UnknownServerPacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
        MiraiLogger.debug("UnknownServerPacket data: " + this.input.goto(0).readAllBytes().toUHexString())
    }

    override fun toString(): String {
        return LoggerTextFormat.LIGHT_RED.toString() + super.toString()
    }
}