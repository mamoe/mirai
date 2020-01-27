package net.mamoe.mirai.qqandroid.network.protocol.jce

import kotlinx.io.core.BytePacketBuilder
import net.mamoe.mirai.qqandroid.io.JceOutput
import net.mamoe.mirai.qqandroid.io.buildJcePacket
import net.mamoe.mirai.qqandroid.io.writeJcePacket

inline fun BytePacketBuilder.writeUniRequestPacket(requestPacket: RequestPacket.() -> Unit) {
    writeJcePacket {
        RequestPacket().apply(requestPacket).writeTo(this)
    }
}