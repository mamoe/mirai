package net.mamoe.mirai.qqandroid.network.packet

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.BytePacketBuilder


/*
private open fun writeHead(
    always_8001: Int,
    command: Int,
    uin: Long,
    encryptType: Int,
    const8_always_0: Int,
    appClientVersion: Int,
    constp_always_0: Int,
    bodyLength: Int
) {
    val j: Int = this.j + 1
    this.j = j
    this.pos = 0
    util.int8_to_buf(this.buffer, this.pos, 2)
    ++this.pos
    util.int16_to_buf(this.buffer, this.pos, this.d + 2 + bodyLength)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, always_8001)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, command)
    this.pos += 2
    util.int16_to_buf(this.buffer, this.pos, j)
    this.pos += 2
    util.int32_to_buf(this.buffer, this.pos, uin.toInt())
    this.pos += 4
    util.int8_to_buf(this.buffer, this.pos, 3)
    ++this.pos
    util.int8_to_buf(this.buffer, this.pos, encryptType)
    ++this.pos
    util.int8_to_buf(this.buffer, this.pos, const8_always_0)
    ++this.pos
    util.int32_to_buf(this.buffer, this.pos, 2)
    this.pos += 4
    util.int32_to_buf(this.buffer, this.pos, appClientVersion)
    this.pos += 4
    util.int32_to_buf(this.buffer, this.pos, constp_always_0)
    this.pos += 4
}
*/

@UseExperimental(ExperimentalUnsignedTypes::class)
private fun BytePacketBuilder.writeHead(
    always_8001: Short = 8001,
    command: Short,
    uin: Long,
    encryptType: Int, //
    sequenceId: Int = SequenceIdCounter.nextSequenceId(),
    const8_always_0: Byte = 0,
    appClientVersion: Int,
    constp_always_0: Int = 0,
    bodyLength: Int
) {
    writeByte(2)
    writeShort((27 + 2 + bodyLength).toShort())
    writeShort(always_8001)
    writeShort(command)
    writeShort(sequenceId.toShort())
    writeInt(uin.toInt())
    writeByte(3)
    writeByte(encryptType.toByte())
    writeByte(const8_always_0)
    writeInt(2)
    writeInt(appClientVersion)
    writeInt(constp_always_0)
}

fun buildOutgoingPacket(
    command: Short
    ///uin: Long,
) {

}

//private b

private object SequenceIdCounter {
    private val sequenceId: AtomicInt = atomic(0)

    fun nextSequenceId(): Int {
        val id = sequenceId.getAndAdd(1)
        if (id > Short.MAX_VALUE.toInt() * 2) {
            sequenceId.value = 0
            return nextSequenceId()
        }
        return id
    }
}