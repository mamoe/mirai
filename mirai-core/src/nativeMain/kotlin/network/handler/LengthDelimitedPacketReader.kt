/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import io.ktor.utils.io.core.*
import net.mamoe.mirai.utils.*

private val debugLogger: MiraiLogger by lazy {
    MiraiLogger.Factory.create(
        LengthDelimitedPacketReader::class, "LengthDelimitedPacketReader"
    ).withSwitch(systemProp("mirai.network.handler.length.delimited.packet.reader.debug", false))
}

/**
 * Not thread-safe
 */
internal class LengthDelimitedPacketReader(
    private val sendDecode: (combined: ByteReadPacket) -> Unit
) : Closeable {
    private var missingLength: Long = 0
        set(value) {
            field = value
            debugLogger.info { "missingLength = $field" }
        }
    private val bufferedParts: MutableList<ByteReadPacket> = ArrayList(10)

    @TestOnly
    fun getMissingLength() = missingLength

    @TestOnly
    fun getBufferedPackets() = bufferedParts.toList()

    fun offer(packet: ByteReadPacket) {
        if (missingLength == 0L) {
            // initial
            debugLogger.info { "initial length == 0" }
            missingLength = packet.readInt().toLongUnsigned() - 4
        }
        debugLogger.info { "Offering packet len = ${packet.remaining}" }
        missingLength -= packet.remaining
        bufferedParts.add(packet)
        if (missingLength <= 0) {
            emit()
        }
    }

    private fun emit() {
        debugLogger.info { "Emitting, buffered = ${bufferedParts.map { it.remaining }}" }
        when (bufferedParts.size) {
            0 -> {}
            1 -> {
                val part = bufferedParts.first()
                if (missingLength == 0L) {
                    debugLogger.info { "Single packet length perfectly matched." }
                    sendDecode(part)

                    bufferedParts.clear()
                } else {
                    check(missingLength < 0L) { "Failed check: remainingLength < 0L" }

                    val previousPacketLength = missingLength + part.remaining
                    debugLogger.info { "Got extra packets, previousPacketLength = $previousPacketLength" }
                    sendDecode(part.readPacketExact(previousPacketLength.toInt()))

                    bufferedParts.clear()

                    // now packet contain new part.
                    missingLength = part.readInt().toLongUnsigned() - 4
                    offer(part)
                }
            }
            else -> {
                if (missingLength == 0L) {
                    debugLogger.info { "Multiple packets length perfectly matched." }
                    sendDecode(buildPacket(bufferedParts.sumOf { it.remaining }.toInt()) {
                        bufferedParts.forEach { writePacket(it) }
                    })

                    bufferedParts.clear()
                } else {
                    val lastPart = bufferedParts.last()
                    val previousPacketPartLength = missingLength + lastPart.remaining
                    debugLogger.debug { "previousPacketPartLength = $previousPacketPartLength" }
                    val combinedLength =
                        (bufferedParts.sumOf { it.remaining } - lastPart.remaining // buffered length without last part
                                + previousPacketPartLength).toInt()
                    debugLogger.debug { "combinedLength = $combinedLength" }

                    if (combinedLength < 0) return // not enough, still more parts missing.

                    sendDecode(buildPacket(combinedLength) {
                        repeat(bufferedParts.size - 1) { i ->
                            writePacket(bufferedParts[i])
                        }
                        writePacket(lastPart, previousPacketPartLength)
                    })

                    bufferedParts.clear()

                    // now packet contain new part.
                    missingLength = lastPart.readInt().toLongUnsigned() - 4
                    offer(lastPart)
                }

            }
        }
    }

    override fun close() {
        bufferedParts.forEach { it.close() }
    }
}
