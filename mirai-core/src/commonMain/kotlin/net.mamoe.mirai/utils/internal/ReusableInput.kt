package net.mamoe.mirai.utils.internal

import io.ktor.utils.io.ByteWriteChannel
import net.mamoe.mirai.utils.SinceMirai

@SinceMirai("1.0.0")
internal interface ReusableInput {
    val md5: ByteArray
    val size: Long

    fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput>
    suspend fun writeTo(out: ByteWriteChannel): Long
}