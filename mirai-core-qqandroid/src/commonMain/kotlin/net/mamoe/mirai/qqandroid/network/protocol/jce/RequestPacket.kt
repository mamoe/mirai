package net.mamoe.mirai.qqandroid.network.protocol.jce

import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.JceStruct

private val EMPTY_MAP = mapOf<String, String>()

class RequestPacket(
    val sBuffer: ByteArray,
    val cPacketType: Byte = 0,
    val iMessageType: Int = 0,
    val iRequestId: Int = 0,
    val iTimeout: Int = 3000,
    val iVersion: Short = 3,
    val context: Map<String, String> = EMPTY_MAP,
    val sFuncName: String = "",
    val sServantName: String = "",
    val status: Map<String, String> = EMPTY_MAP
) : JceStruct() {
    override fun writeTo(builder: JceOutput) {
        builder.write(this.iVersion, 1)
        builder.write(this.cPacketType, 2)
        builder.write(this.iMessageType, 3)
        builder.write(this.iRequestId, 4)
        builder.write(this.sServantName, 5)
        builder.write(this.sFuncName, 6)
        builder.write(this.sBuffer, 7)
        builder.write(this.iTimeout, 8)
        builder.write(this.context, 9)
        builder.write(this.status, 10)
    }
}