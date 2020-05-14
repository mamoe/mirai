package net.mamoe.mirai.utils.internal

import io.ktor.utils.io.ByteWriteChannel

internal interface ReusableInput {
    val md5: ByteArray
    val size: Long

    fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput>
    suspend fun writeTo(out: ByteWriteChannel): Long
}