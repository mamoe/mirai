@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.network.protocol.tim.packet.EventPacketIdentity
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket
import net.mamoe.mirai.utils.readString


/**
 * 群文件上传
 */
class ServerGroupUploadFileEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    private lateinit var xmlMessage: String

    override fun decode() {
        this.input.discardExact(60)
        val size = this.input.readShort().toInt()
        this.input.discardExact(3)
        xmlMessage = this.input.readString(size)
    }//todo test
}

class ServerGroupUnknownChangedEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {

    override fun decode() = with(input) {
        //00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 01 00 00 F3 55 00 00 00 05 00 00 00 E9 00 00 00 05
        //00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 01 00 00 F3 56 00 00 00 05 00 00 00 EA 00 00 00 05
    }
}