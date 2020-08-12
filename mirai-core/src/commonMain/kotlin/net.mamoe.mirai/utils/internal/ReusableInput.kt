package net.mamoe.mirai.utils.internal

import io.ktor.utils.io.ByteWriteChannel
import kotlinx.io.core.Input

internal interface ReusableInput {
    val md5: ByteArray
    val size: Long

    fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput>
    suspend fun writeTo(out: ByteWriteChannel): Long

    /**
     * Remember to close.
     */
    fun asInput(): Input
}