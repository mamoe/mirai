package net.mamoe.mirai.qqandroid.network.protocol.jce

import kotlinx.io.core.BytePacketBuilder
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.buildJcePacket
import net.mamoe.mirai.qqandroid.network.io.writeJcePacket

fun BytePacketBuilder.writeUniRequestPacket(requestPacket: RequestPacket) {
    writeJcePacket {
        requestPacket.writeTo(this)
    }
}