/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.handler.LengthDelimitedPacketReader
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.io.writeIntLVPacket
import net.mamoe.mirai.internal.utils.io.writeShortLVString
import net.mamoe.mirai.utils.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LengthDelimitedPacketReaderTest : AbstractTest() {
    init {
        setSystemProp("mirai.network.handler.length.delimited.packet.reader.debug", "true")
    }

    private val received = mutableListOf<ByteArray>()
    private val reader = LengthDelimitedPacketReader { received.add(it.readBytes()) }

    /*
     * All these tests cases can happen in the real time, and even before logon is complete.
     */

    @Test
    fun `can read exact packet`() {
        val original = buildLVPacket {
            writeShortLVString("some strings")
            writeInt(123)
        }
        val originalLength = original.remaining
        reader.offer(original)

        assertEquals(1, received.size)
        received.single().read {
            assertEquals(originalLength - 4, this.remaining)
            assertEquals("some strings", readUShortLVString())
            assertEquals(123, readInt())
            assertEquals(0, remaining)
        }

        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())
    }

    @Test
    fun `can read 2 part packets`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(123)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(123)
        }.readBytes()

        reader.offer(buildPacket {
            writeInt(part1.size + part2.size + 4)
            writeFully(part1)
        })
        assertEquals(0, received.size)

        reader.offer(part2.toReadPacket())
        assertEquals(1, received.size)

        received.single().read {
            assertEquals(part1.size + part2.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(123, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(123, readInt())
            assertEquals(0, remaining)
        }

        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())
    }

    @Test
    fun `can read 3 part packets`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(111)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        val part3 = buildPacket {
            writeShortLVString("some strings")
            writeInt(333)
        }.readBytes()

        reader.offer(buildPacket {
            writeInt(part1.size + part2.size + part3.size + 4)
            writeFully(part1)

            // part2 and part3 missing
        })
        assertEquals(0, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part2.size + part3.size, reader.getMissingLength().toInt())

        reader.offer(part2.toReadPacket())

        assertEquals(0, received.size)
        assertEquals(2, reader.getBufferedPackets().size)
        assertEquals(part3.size, reader.getMissingLength().toInt())

        reader.offer(part3.toReadPacket())

        assertEquals(1, received.size)
        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())

        received.single().read {
            assertEquals(part1.size + part2.size + part3.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(111, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(333, readInt())
            assertEquals(0, remaining)
        }
    }

    @Test
    fun `can read 3 part packets with a combined`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(111)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        val part3 = buildPacket {
            writeShortLVString("some strings")
            writeInt(333)
        }.readBytes()

        val part4 = buildPacket {
            writeShortLVString("some strings")
            writeInt(444)
        }.readBytes()

        reader.offer(buildPacket {
            writeInt(part1.size + part2.size + part3.size + 4)
            writeFully(part1)

            // part2 and part3 missing
        })
        assertEquals(0, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part2.size + part3.size, reader.getMissingLength().toInt())

        reader.offer(part2.toReadPacket())

        assertEquals(0, received.size)
        assertEquals(2, reader.getBufferedPackets().size)
        assertEquals(part3.size, reader.getMissingLength().toInt())

        reader.offer(buildPacket {
            writeFully(part3)
            writePacket(buildLVPacket { writeFully(part4) })
        })

        assertEquals(2, received.size)
        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())

        received[0].read {
            assertEquals(part1.size + part2.size + part3.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(111, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(333, readInt())
            assertEquals(0, remaining)
        }

        received[1].read {
            assertEquals(part4.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(444, readInt())
            assertEquals(0, remaining)
        }
    }

    @Test
    fun `can read 3 part packets from combined with a combined`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(111)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        val part3 = buildPacket {
            writeShortLVString("some strings")
            writeInt(333)
        }.readBytes()

        val part4 = buildPacket {
            writeShortLVString("some strings")
            writeInt(444)
        }.readBytes()

        val part5 = buildPacket {
            writeShortLVString("some strings")
            writeInt(555)
        }.readBytes()

        reader.offer(buildPacket {
            writeInt(part1.size + part2.size + part3.size + 4)
            writeFully(part1)

            // part2 and part3 missing
        })

        assertEquals(0, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part2.size + part3.size, reader.getMissingLength().toInt())

        reader.offer(part2.toReadPacket())

        assertEquals(0, received.size)
        assertEquals(2, reader.getBufferedPackets().size)
        assertEquals(part3.size, reader.getMissingLength().toInt())

        reader.offer(buildPacket {
            writeFully(part3)
            writePacket(buildPacket {
                writeInt(part4.size + part5.size + 4)
                writeFully(part4)

                // part5 missing
            })
        })

        assertEquals(1, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part5.size, reader.getMissingLength().toInt())

        reader.offer(part5.toReadPacket())

        assertEquals(2, received.size)
        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength().toInt())

        received[0].read {
            assertEquals(part1.size + part2.size + part3.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(111, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(333, readInt())
            assertEquals(0, remaining)
        }

        received[1].read {
            assertEquals(part4.size + part5.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(444, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(555, readInt())
            assertEquals(0, remaining)
        }
    }

    // Ensures it will not emit without any length check if received a missing part.
    @Test
    fun `can read 4 part packets`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(111)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        val part3 = buildPacket {
            writeShortLVString("some strings")
            writeInt(333)
        }.readBytes()

        val part4 = buildPacket {
            writeShortLVString("some strings")
            writeInt(444)
        }.readBytes()

        reader.offer(buildPacket {
            writeInt(part1.size + part2.size + part3.size + part4.size + 4)
            writeFully(part1)

            // part2, part3 and part4 missing
        })
        assertEquals(0, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part2.size + part3.size + part4.size, reader.getMissingLength().toInt())

        reader.offer(part2.toReadPacket())

        assertEquals(0, received.size)
        assertEquals(2, reader.getBufferedPackets().size)
        assertEquals(part3.size + part4.size, reader.getMissingLength().toInt())

        reader.offer(part3.toReadPacket())

        assertEquals(0, received.size)
        assertEquals(3, reader.getBufferedPackets().size)
        assertEquals(part4.size, reader.getMissingLength().toInt())

        reader.offer(part4.toReadPacket())

        assertEquals(1, received.size)
        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())

        received.single().read {
            assertEquals(part1.size + part2.size + part3.size + part4.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(111, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(333, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(444, readInt())
            assertEquals(0, remaining)
        }
    }


    @Test
    fun `can read 2 combined packets`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(123)
        }.readBytes()

        println("part1.size = ${part1.size}")

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        println("part2.size = ${part2.size}")

        reader.offer(buildPacket {
            writePacket(buildLVPacket { writeFully(part1) })
            writePacket(buildLVPacket { writeFully(part2) })
        })

        assertEquals(2, received.size)

        received[0].read {
            assertEquals(part1.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(123, readInt())
            assertEquals(0, remaining)
        }

        received[1].read {
            assertEquals(part2.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals(0, remaining)
        }

        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())
    }

    @Test
    fun `can emit 2 combined packets with another part`() {
        val part1 = buildPacket {
            writeShortLVString("some strings")
            writeInt(111)
        }.readBytes()

        val part2 = buildPacket {
            writeShortLVString("some strings")
            writeInt(222)
        }.readBytes()

        val part3 = buildPacket {
            writeShortLVString("some strings")
            writeInt(333)
        }.readBytes()

        val part4 = buildPacket {
            writeShortLVString("some strings")
            writeInt(444)
        }.readBytes()


        println("part1.size = ${part1.size}")
        println("part2.size = ${part2.size}")
        println("part3.size = ${part3.size}")
        println("part4.size = ${part4.size}")

        reader.offer(buildPacket {
            // should emit two packets
            writePacket(buildLVPacket { writeFully(part1) })
            writePacket(buildLVPacket { writeFully(part2) })

            // and process this part
            writePacket(buildPacket {
                writeInt(part3.size + part4.size + 4)
                writeFully(part3)
                // part4 missing
            })
        })

        assertEquals(2, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(part4.size, reader.getMissingLength().toInt())

        received[0].read {
            assertEquals(part1.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(111, readInt())
            assertEquals(0, remaining)
        }

        received[1].read {
            assertEquals(part2.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(222, readInt())
            assertEquals(0, remaining)
        }

        // part4, here you are
        reader.offer(buildPacket {
            writePacket(buildPacket { writeFully(part4) })
        })

        received[2].read {
            assertEquals(part3.size + part4.size, this.remaining.toInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(333, readInt())
            assertEquals("some strings", readUShortLVString())
            assertEquals(444, readInt())
            assertEquals(0, remaining)
        }

        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength())
    }

    private inline fun buildLVPacket(block: BytePacketBuilder.() -> Unit): ByteReadPacket {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        return buildPacket {
            writeIntLVPacket(lengthOffset = { it + 4 }) {
                block()
            }
        }
    }

}