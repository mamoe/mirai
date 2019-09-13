package net.mamoe.mirai.network.packet

import net.mamoe.mirai.utils.LoggerTextFormat
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class UnknownServerPacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
        println("UnknownServerPacket data: " + this.input.goto(0).readAllBytes().toUHexString())
    }

    override fun toString(): String {
        return LoggerTextFormat.LIGHT_RED.toString() + super.toString()
    }
}