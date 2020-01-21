package net.mamoe.mirai.qqandroid.network.protocol.jce

import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.JceStruct

private val EMPTY_MAP = mapOf<String, String>()

class RequestPacket() : JceStruct() {
    lateinit var sBuffer: ByteArray
    var cPacketType: Byte = 0
    var iMessageType: Int = 0
    var iRequestId: Int = 0
    var iTimeout: Int = 3000
    var iVersion: Short = 3
    var context: Map<String, String> = EMPTY_MAP
    var sFuncName: String = ""
    var sServantName: String = ""
    var status: Map<String, String> = EMPTY_MAP

    constructor(
        sBuffer: ByteArray,
        cPacketType: Byte = 0,
        iMessageType: Int = 0,
        iRequestId: Int = 0,
        iTimeout: Int = 3000,
        iVersion: Short = 3,
        context: Map<String, String> = EMPTY_MAP,
        sFuncName: String = "",
        sServantName: String = "",
        status: Map<String, String> = EMPTY_MAP
    ) : this() {
        this.sBuffer = sBuffer
        this.cPacketType = cPacketType
        this.iMessageType = iMessageType
        this.iRequestId = iRequestId
        this.iTimeout = iTimeout
        this.iVersion = iVersion
        this.context = context
        this.sFuncName = sFuncName
        this.sServantName = sServantName
        this.status = status
    }

    companion object : Factory<RequestPacket> {
        override fun newInstanceFrom(input: JceInput): RequestPacket {
            val iVersion = input.readShort(1)
            val cPacketType = input.readByte(2)
            val iMessageType = input.readInt(3)
            val iRequestId = input.readInt(4)
            val sServantName = input.readString(5)
            val sFuncName = input.readString(6)
            val sBuffer = input.readByteArray(7)
            val iTimeout = input.readInt(8)
            val context = input.readMap("", "", 9)
            val status = input.readMap("", "", 10)
            return RequestPacket(sBuffer, cPacketType, iMessageType, iRequestId, iTimeout, iVersion, context, sFuncName, sServantName, status)
        }
    }

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