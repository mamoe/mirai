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

    @Test
    fun `real case test 1`() {
        // 5696
        val part1 =
            """00 00 02 F0 00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 6D A0 E3 2B 65 53 C5 AE 22 D0 7B AC 3D DD 3C 6E FF 24 84 38 F0 B7 A3 DE 5F 3A 15 DE 7E 6D 08 D1 8C 18 A9 F2 89 03 03 43 15 C6 32 01 74 5B DD A4 33 49 47 78 E0 5B 83 2C E7 3A E1 CC 50 9C A8 8C 5C 88 06 A0 90 04 6E 23 6F AD 84 D8 6B 10 64 AD 33 5B 3F B5 3C C3 24 6C BB 28 8C BC A2 BD 5E 91 EA FA FE 5C 3B C3 F4 3B 59 24 37 1C E2 13 DB 75 C1 7C D5 8B 2F 57 AD C0 16 13 97 12 60 D5 4C 21 E8 13 72 B3 F6 98 05 89 BA 49 60 F1 1C D9 6A 82 F6 A1 AE 78 82 48 60 A9 24 3C A7 7A 93 79 96 8B AA E2 FA DE 5F E9 12 FE 51 27 47 CB 6A 20 DB 64 22 B4 3A B4 C8 5E D0 45 31 9F 63 9B 61 A4 F9 56 CB 0B BF 27 21 55 99 2A 53 EA E4 DC EA D3 7E 62 B9 1D 2A 48 54 C3 6B D8 E0 CD A5 CC 84 AF A8 91 05 2D AC C3 64 82 5F 56 EA FB 13 5B A7 78 ED B1 E1 ED 25 74 1D EE 5D A3 5F 2E FA 0B 5C 65 69 EB DE 4E DD 63 08 83 2C 10 40 D0 44 49 F9 AE 48 B7 D1 75 8D B7 45 EC 08 DA B1 B2 EE EF CF F1 A3 75 93 21 AA C1 50 5C 9B FA CF CD 99 34 26 9C D2 E9 AA 1C 13 8A 8A 0A 5E EE 62 D1 89 31 32 44 88 40 6D C6 BF CF 3B 36 FE BB FC 7F 2D 62 E1 0D 06 0C BD 79 2F 0D 8C 9D 16 44 FB EE 84 AF 39 67 5C BD A6 C1 BD 77 C6 78 81 AF F4 DB C7 3D E9 2D 2F 5F 6C 5F BD 7C D5 A6 E8 96 D4 F0 8E F4 0F 5C 34 E1 DD A5 F9 AB 0A 70 54 1C D8 B5 2E 9B A9 11 9E B7 F8 27 40 04 A1 1A 9B B7 29 99 CD E0 13 9E 5C C5 FD E5 3B DE C5 85 48 DE 21 5D D7 10 AD 77 81 B3 90 9E B2 A1 70 C0 45 AA E4 06 0F DB E4 F7 0B CB 25 28 C9 C1 EA 0B 23 E9 EC 16 45 2F 3B 07 2E 05 15 EA E8 89 C9 CE D7 BE 2B 2D 0F C1 BC 16 8A 4B 79 22 78 4C 70 50 46 86 78 C2 6E DB A2 A9 FA 93 F8 B4 3F 32 50 EF FE 42 EC F6 99 AD 5E 9A F0 E8 B5 F3 96 5C 0B A9 93 DC F2 D9 EB 1C D9 B1 38 E1 87 73 B8 48 DF 3E 35 74 68 E7 C7 94 D4 05 83 C2 08 90 9E 7C ED 61 1A 5A B7 32 5F 09 36 37 6F D3 7B 6F 67 38 D2 EB FD 6A 60 98 87 DE A7 61 88 74 99 80 C4 4E A9 9B 9F CA 4D AB 20 E3 FA 37 07 57 BF E5 A2 9D C5 CB F7 BA 56 E0 38 1B 3D AC DC 51 36 7C 60 E7 BA 00 FB 67 BC 48 6D 17 C6 EB E2 93 F3 2D 5B 16 AF 6B 83 CC 27 15 23 76 5A 73 64 21 C6 21 8D C8 9F 5C 3B 61 47 6F 96 3D EA 7B EE 12 FF 20 F3 20 09 AE 60 C0 86 47 D2 45 18 BC 73 1B 21 AF DA 02 28 4C 05 A9 69 52 31 F8 75 B4 47 A5 A9 49 70 A6 5D 33 F2 07 2A 20 AD 5E 31 6A FC F6 96 11 48 F5 9D 85 CD 97 A6 BF D0 28 C3 51 AA 62 90 98 AD 7E 94 73 53 2F 00 00 13 E0 
                |00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 4F 05 F9 A5 25 DD A9 68 1B 80 60 1C D5 63 2F 8D 04 46 D2 7F 20 C1 F4 91 99 FF 90 BD E6 81 8E 24 ED 86 12 2D A4 41 C9 99 4C 70 70 75 29 A7 7A 6C 8C 51 9A CE 2B 43 A1 3E C8 97 CD AB AE 25 21 1A C5 D2 6E 3B 1D F6 3A DF EA 4B 43 C9 5D 1F 16 B3 04 DC BF 6C B8 78 60 A1 56 C9 E9 13 23 77 A3 33 8B B6 88 BD E0 16 74 DE F5 CA BF 50 5E 7F 74 66 29 5D 00 56 E0 AD 04 EF 07 A6 FB E0 A1 85 0F 11 C8 BF F2 E7 C1 21 DA 71 B3 D6 42 A7 0D 8B BB 38 8F BF 2C 54 C8 24 14 0D E3 DB 77 FC 25 C3 D9 3A 12 10 12 E2 A7 E2 81 61 F7 88 BD 2E 05 C4 AC 84 DE 33 6B 9B 8A 9C 78 97 AB 15 4C AE F2 FB AF 93 94 45 69 69 66 8E 46 59 78 53 6A 12 CA 4C E6 AF B7 CB D7 CD C2 47 A6 71 2C 66 D8 76 4D 13 3D 8F B1 33 E9 D4 F1 2B 6B B9 E7 DD 37 E1 91 C0 19 C5 4F 73 C8 89 AC 71 CE BD 3C 64 25 5F FF 00 67 C7 5D 22 CA B1 53 D4 05 22 1A 05 FA 3E BD 13 A6 F9 4B 08 C9 68 4D C0 43 1B 84 AC 2B C2 EA D6 82 8B 28 A2 32 C4 E9 87 36 C0 E7 13 3E B3 CD D6 70 0C 2C 4F E6 F4 D4 5E 32 80 04 D1 CA 4F E6 A1 D3 C3 71 5D 57 18 8B 6A E4 B2 54 47 FA 95 8E BB 3B 92 99 94 33 86 D2 EC 36 69 78 67 BB 17 DB 58 C7 6C 2F 95 0F D8 E5 B0 B5 6F ED 90 88 46 2D 46 BE 23 BE F5 3C 89 AE FB 63 BC F8 C5 E2 5B 96 F4 58 7D 60 FB F8 4F B8 F0 66 3C 89 EF 16 4E 51 30 76 B0 6A 73 DE 95 D2 AA 26 B4 DC BA AB E1 18 17 A2 91 68 FC E0 C3 CE 74 EC F8 5A AE AB E4 4C 6D 66 C3 EF 73 A3 C2 8B 3B C0 32 37 DE 05 A5 EF 7B 2A FB 40 E5 1F E2 FE 0B 95 76 1C 41 87 FC 34 3C 84 B0 63 4D 5B C2 DA 57 08 68 69 3C CC AB CB 66 09 BA 6A 18 25 80 76 E2 20 B8 AC 9F 84 A1 EC D4 21 75 80 0E C1 3C 6B 19 6E BF D6 6A 38 83 C1 21 64 8E 09 CC DF C2 01 D6 68 6A F9 38 8A 6F B2 74 32 EA 8C 90 88 9E 8B CC 21 66 82 76 2D A5 09 27 AC 3F C9 71 BC 0C 86 57 14 37 BC 87 50 A1 F3 9D 9F 27 C2 2E F0 01 10 D6 7F A8 78 AC DB 93 A7 40 42 84 74 8D 15 08 6D 6F EA 02 E2 68 20 BE 3E 70 18 5D A8 08 BA 93 7C D7 0B 8C 76 C3 6C EB 2C 2D BD 59 70 63 83 3D 26 A6 76 D8 11 A7 A8 E9 AA BF D5 EA 99 F1 08 10 9E 31 58 F1 59 99 ED 94 EB 53 64 49 0E 0F 1D 7A 10 3C FA 19 06 AB 49 A8 EE A9 21 2A 29 86 87 C7 8C 31 C0 E9 CA 57 99 E3 DC 32 F7 89 21 C7 72 5B 58 85 B8 65 6B D6 56 D6 C2 9F E5 03 6E BB B4 9D 09 FE 34 A2 DE 3E 86 8F FD A9 6D 8C 86 BC 4F B6 0F 1B 16 1E C4 36 CD F4 80 63 13 36 A7 2F 49 22 42 3E E9 FD 8B 0B BC E0 BA 2A 28 B8 7B 05 A9 72 23 17 51 30 BE CC CB A3 0B 7F 73 A4 9A E7 DA E6 D3 B1 92 0A CF 43 F3 6D D6 16 01 F5 0F 69 2C 67 72 39 BB 1B 87 F8 52 B7 AD 2D 47 D0 82 FB 1F EB 36 CC CC 19 40 F5 EA C1 B2 76 C3 3B E9 0D 8C 3C 1A 05 91 B4 E9 AA 9B FF 40 EF E7 36 D8 94 B2 76 99 B6 27 4E E9 01 A3 E5 A3 CE F1 7B 4E 6D F2 53 05 33 9D 1A C5 3C BC F3 E8 BF 84 0F A8 21 C0 10 CD D1 26 1B 34 80 AE 7B 48 97 45 F6 DF 8F 79 C3 4E C6 47 34 E4 FD 0D F9 20 D2 62 20 FE 96 32 73 69 04 6F 44 8B 68 AA A6 30 4C 28 BB C1 B4 0B 6B BC EF CD DA CA 45 C2 5D 71 C2 53 DD B0 92 3D 0F 80 37 C2 A9 8D 89 E3 D9 11 1C 98 F2 7E 4A 9A EE 6D 50 E0 04 E3 97 71 F2 AC 8D 5D 8A FE 75 F0 B5 E8 AD 3A 25 4D 2F 0C 82 1D 94 5A 7C 63 32 3E E3 82 95 80 2A 6F BB BA 51 38 1C EF 9E DC 1D B2 31 A9 F9 AA 05 27 EB A9 4A 58 D1 21 DE 1C 55 88 3F A5 EB 27 F4 CF DA DF 67 7E 24 5C A0 B6 BC 1F 4E E1 3F 70 86 62 DB A1 BE 5A 88 C6 20 22 B3 F3 DD 9D 08 1B 78 15 55 EC 47 76 AB E6 4C 77 35 D3 08 9F 52 9B 9F 42 58 28 A4 8D 96 F0 BF C1 42 90 1C 18 A2 98 25 86 B0 9B 3B DE 75 C1 87 4F 9A 7E FD 03 9F 0F 95 E4 39 04 FE F4 B7 DA 57 BE F0 D8 09 B6 42 F4 CD CF 8E 38 98 6B 29 77 D3 19 63 03 2D CC DB 6C 54 85 18 5D FA AB FE CE 46 6A 1F 8B 63 0F 67 27 55 E7 F4 03 7F B1 52 7F 2E 01 F7 8C B9 9F F6 03 4F DD 05 D2 3A D7 2D C9 1C 3D A1 2E 60 7A 0A E5 7B 72 C7 8D 18 9A FA 4B 3F 16 23 17 21 6B BF 32 9B A2 E9 64 F2 E4 F9 B2 65 81 8A 99 F7 23 36 8D 57 E7 A6 26 26 1F 7F 19 F7 5C 5B C3 08 A1 D8 27 C3 BE 98 26 B3 50 63 F0 05 6A 29 19 05 CA 4A 12 CF 33 95 27 53 14 A2 5F D0 37 B6 03 0E 0C 72 44 E4 DB 89 B3 E0 D6 0D AA 3F 1F DF E8 86 E1 55 D2 6B 15 AA 98 A2 92 E9 1B 2E 20 8A 6B C8 6B 0F 22 37 FD F5 36 06 BB 3F 03 23 9F 40 08 1C 58 DE 20 74 BB C2 7B 0A 81 FB 6F D8 89 DB 03 DA 89 86 B1 40 A3 AB 8D A8 CD 2D 9B 97 98 20 6D 73 8F 35 F2 26 F2 8B A1 47 35 27 9D 85 BC 65 4E 7A 42 FC 39 1D 62 24 7C 1A B1 01 CC FA 40 15 FC 4B 2F CE 1B 9B 0B EB 07 F7 12 55 BD 7C B1 5C BC AC 53 16 DB 34 CF D3 91 6A 92 9E FA E8 A2 6B 4D 49 36 3F 33 79 49 4D 52 EC 1A 3F 0A EB 9E 61 E8 8D FA A2 D4 A2 E4 26 1B 2A A2 CA 6A 94 35 8D 77 CE 0A 7F 99 7D D8 53 61 28 F7 27 43 38 99 05 9E B9 7E 9C B5 61 84 AE 0D 9C 44 EF D1 54 B8 3F B0 E8 D4 FE EB 42 A5 DA 24 07 28 F5 21 93 A2 53 30 18 26 AB 3C 99 88 EB 0A C8 D4 6E 76 25 A7 65 13 59 16 FE C0 E7 23 BD 6D 4D 5B DC 4C 02 8A 93 56 B5 3D B0 ED 2A 09 CD 01 9A 5A A0 62 FF 4C 23 7E 1C 2B 33 AD 29 B8 7B 4F A6 71 7B 1A C1 3E 2D B9 2F 78 56 CF D2 65 EA 02 E9 1D 4A 27 CD 94 B0 67 79 E3 9B 2B 9C 50 1B 78 ED 06 3C 06 C0 07 6E 53 49 73 2C 74 59 AF A7 92 16 A3 DA 35 B7 B9 B8 D8 CF 58 3E BF 43 BF 43 E8 C5 B7 36 B0 7E 65 AF 6F 75 89 C5 76 87 40 19 9C 4F 30 54 18 B2 09 74 4B D6 A5 29 22 76 E8 19 7C A5 A9 B0 98 12 06 0A 9E 87 F3 1F 75 44 32 CD 47 FE 56 7F 84 4E 73 EF 28 A3 C6 78 08 94 1D 6A D2 E4 31 20 99 63 D4 F7 A8 84 CE C8 5E 43 45 01 3F BC 9A D3 0C B7 FF B0 A8 3D C7 EE D7 74 76 50 9C 61 67 E3 E2 19 BA DD CB AF 8F A2 CD 55 56 1C 32 5F 16 AA AE 24 2B 10 D4 A1 F9 2D 91 93 1F C5 BE 7D 5F EC 4B F4 E5 3D FF 10 3B F7 2D 27 F1 97 FF 46 BF 6E 48 1C 7B 49 BE 1F 7A F8 79 0A A4 C4 2D 73 C2 FA 1A 71 56 6E A7 6B 1A 46 83 DA 80 47 1D E7 FD 03 3A 98 51 E5 3D 30 71 05 41 D5 F5 8F 7E BC F1 47 6E D2 2E 9B 6C 18 30 D0 92 F1 BD 5F 67 D4 5D BA 75 2F C2 5F 98 64 5F 2C 7B 72 8F 77 F9 7D 07 F9 66 9B 21 6F B1 ED F0 22 B4 0E 4D 3B 79 42 CD 84 66 F7 6F 57 B4 CD EE 22 B0 DB 31 74 12 DC D8 B3 54 07 04 18 77 D8 80 D4 FD 33 AE 57 64 E1 23 37 68 4D 1A F0 36 ED C5 FA 97 20 93 8D 37 A3 A1 84 CE C9 4B 2B E8 15 CB 3D 9C 10 08 A1 FE 7E 51 B9 F7 F5 0A 8A 65 FF 6B 7C E6 01 2A ED BA E2 A7 26 32 F9 79 F5 2C 16 81 AA DD 15 1F D2 A4 C7 B7 26 7A 5A C6 CE EA 59 26 68 07 6F 10 E8 6B 3F 22 B3 B0 64 7B 05 AA 76 98 0B 03 1D 6F 25 B6 08 54 A8 87 8A 84 92 29 9A 2D A4 59 81 BF 52 DE 24 F9 5F C2 5A FA 1A D7 EE 03 6A 00 C3 8E 28 97 31 55 CC 07 1A 98 11 1E F3 E8 E3 5A 6B F1 91 87 4A 74 83 7E C1 DF 06 AF 2B C6 B0 B0 56 F6 96 D0 C4 AC F3 A8 7C D8 4A BF 50 66 09 AA D1 0F E8 8F 7D DE 70 56 8D D4 68 E3 1A BC C1 EA 9F 6C 8B F3 8D 04 D7 24 95 E5 E9 FC 72 36 D7 9D 01 C9 0B 81 46 01 4C B7 BB 48 36 B4 3E 52 3C D3 0E 24 36 64 94 CC 23 F9 A0 86 56 36 0E 67 B1 EF 86 1D 93 7F 58 3E B6 91 49 14 31 F6 EB C3 5A DF A8 C8 26 2E 29 37 7A 23 7C 3B D9 C0 53 33 52 02 AC 6B B5 FB F6 A1 6D 13 E4 87 CA 56 3A B2 0C 75 92 75 5C A0 1C C2 31 97 E8 1B 13 AC D7 E1 47 E3 E7 CE 86 FF 85 AE E2 BD 5C 6F 3F 65 FA 11 C6 C0 12 CE 86 19 C8 E2 72 8E B6 4E 88 73 E0 C1 31 86 42 F7 51 05 68 E0 73 99 0E 69 86 59 C9 4A 77 CA 76 10 AC DE 95 F9 17 4A 8A 5B 14 A8 16 57 C4 16 60 CF 0E 48 EE D0 DC 63 8B 9B 46 6D 96 41 1D 38 8B DF 70 31 95 3A F7 A8 1F B8 4D 7A BA E0 CC E9 58 B5 2A 87 42 43 63 DC 12 47 AC 32 49 D7 02 87 DB C9 5D 10 BC 93 65 F4 3C C3 FF C7 28 86 14 4D 4C 70 FD FE 5A 06 EA 01 C5 27 FA 46 9B 98 5E 71 D5 BE C1 0A A2 45 7F 09 1A 7D 12 4F 38 49 09 77 1B 8A 39 48 27 22 4C B1 A3 AA 68 8E 1B 73 B0 A6 EA 78 AA C3 33 16 3B 00 69 4A D5 07 06 42 65 50 2E F2 60 8B F0 9E 8A 7C 05 B7 DC 85 E7 EF 10 FD C8 FE 80 A9 C6 B0 46 35 0F 77 17 6C 7D EC 60 24 D9 21 76 AC DF 6B B2 FC B0 75 D1 3C 2D 69 60 C2 4E CD 62 58 72 D8 AC B3 F5 75 56 80 E3 D1 9B 95 1B E7 CD D1 C3 C2 FE 50 DC DD 10 D5 89 43 7E FB 90 54 4D 42 13 1A 47 20 DB 18 E7 BA 3D 2D AF 53 DC 8F 37 08 8C 3B 52 00 A4 62 AB 22 93 46 05 38 A8 C2 FF 2C F5 F8 5D B5 4B 04 7E 48 F7 03 75 DA C8 B0 E9 E0 0B 3B 7E 57 0A 1A 53 98 EE 1A 2A 48 C7 FD 22 D4 B0 E7 5C B3 75 8F B7 4E 8A EF B0 29 6C A9 C8 02 24 4E D6 3B 2C 82 4E F3 1E 62 94 40 64 2E 77 BB B9 3D 1E F4 BD DD BD FA DA 25 09 65 8D 5E 1B 3C 2E 80 22 11 D6 1D 32 B4 7E 89 B0 F6 85 83 C3 34 78 57 3C 97 DF 05 01 6C 8F 91 25 95 D8 BD D8 86 7C 8C F0 50 88 25 10 6D E6 E2 DA 76 31 80 55 2E E6 1F 37 A1 82 8B B5 FC 2B 71 5D 9B E6 89 CD 14 BB 49 28 BA 1D 49 71 08 A7 D2 B5 A4 1C 9C 1B DF EF C3 E7 55 2B F4 2E 4E 93 4A 74 B6 A9 47 18 23 9C 44 08 3D 13 06 24 41 06 1E BF 88 BC 3D 1B 75 92 80 C7 78 A4 CA 5A F1 79 EF 3F DC F5 5F AF 49 73 68 96 C9 DB 8E F2 A7 B4 2D 0F 50 64 0C EB EB F6 08 09 0A F5 0A 2C 88 5E AE 44 25 89 DF 7F 4C 5D 0C 73 47 3C 1A 2B 41 7B 9E 28 48 39 0A A2 D8 04 78 90 DE D5 88 9E C7 00 5F F8 63 CD BF 8D F1 5C 12 CE 7E 2B 09 D0 4C 42 7A 84 0F 36 C6 C9 5D AA 44 87 23 4A 76 76 94 1A 7A 2D D6 23 B4 84 EF 6D 3B C6 01 B2 F7 20 09 40 51 E4 80 58 5E A6 A5 97 2F 75 CE E7 97 78 F0 0B F9 31 98 CE 6C 40 4F A4 B3 14 AA A5 89 AA F9 9B 21 11 69 A8 E0 56 B0 F1 C9 CC 44 83 1C 32 E3 79 14 1A 5D C8 F3 41 C5 9A CE A1 B7 5B C2 53 22 49 15 FE 52 A9 38 DB BD 7A 69 83 06 0F 69 77 98 D1 F4 93 3C 83 37 B7 21 A5 D7 2A 9E D9 A3 BC 1C 9D 4F A4 22 51 4C 44 B9 35 C9 8E 41 FF 0D 6D FC BD 12 F5 CA 3E F9 EE 75 1D 01 9B CD 34 B2 C6 4A 4B 9D 2B 79 A5 12 52 BF FF F4 42 62 39 53 58 8E 82 F6 94 2C 00 97 0E 8A B3 5B 73 3D D6 CF 3E D5 14 BB 2D 5B E6 2D 78 88 D5 E8 33 BC E9 C9 A4 2D 36 41 F5 DA 5D 68 6E B6 B9 93 89 E0 15 E5 68 65 48 42 FA 9E 16 20 5A C8 CB 8D 61 52 E1 23 DE 0A 40 8C F5 6F 86 AD 36 F6 20 9E D4 6A B2 D9 7E 5E 3E F3 D7 C8 A8 25 CB 49 E5 FE BA A8 4E 34 41 B4 CB 9B AA BA 8F B9 53 F9 0A 67 AC 3A 02 D2 55 AB E3 6F 8C 79 E6 C0 2E 70 5C 6C E5 89 17 F2 A4 C8 8C 58 58 8C 3C 22 10 68 0D 9A 70 63 59 F6 C0 60 6F 1E B6 9E B5 69 B3 1B 0E D8 F8 8C 5D 06 2F 42 4F 69 BB 83 DF F8 20 8A BF 74 30 4A 57 C8 FE 13 AD 8B 09 B9 92 7A DD 53 DA A9 33 00 85 53 88 2E 58 3D A4 02 ED D0 E9 25 E2 EE 7A C0 08 B3 B5 BF 7D 36 07 0D 87 F7 6B 68 BF C6 7B A6 E4 B6 D7 76 FA BE 23 E6 53 94 D3 54 94 6D 39 87 3F 74 F3 ED 5C D6 E0 4F E8 5C AD 86 3E E7 E1 12 32 51 F0 E9 42 B5 12 46 26 B1 86 AD 19 1D A2 81 4E 3E F2 FF 8A 5F 9C 80 02 56 70 AA 0B 0D 2C 61 DA B9 1C 5A B3 6F 2E C5 32 C0 48 FE 19 9E F8 F4 30 3E E2 2C D8 A5 81 E9 C9 6B 44 6B B7 95 5D 94 0E C5 63 D7 78 FD 58 54 E1 19 E2 22 B5 48 10 D5 87 28 F7 A3 30 43 9D 69 B5 D5 38 D5 05 22 77 FC DC 93 62 08 CD 58 19 E4 65 E6 AC 05 6C FE C9 02 3A 28 48 77 AF D9 63 40 25 45 CF 89 4C 0A ED 60 C9 A8 75 A0 68 79 69 8C 13 5C 0C B4 FD 8F 27 8E BA 16 E5 1E AA 78 84 AF BE BB E9 09 9C 51 8A 10 2B DB 0E 7E F4 E2 A4 DE 15 85 A2 E2 40 E7 37 56 A5 AF 91 4E EA 80 E3 8F 3A F3 28 15 8D F4 FA D1 EF C0 09 98 43 1A A3 98 CA 94 BB DD EF 98 E1 98 6E AD 53 9F 48 E5 9E A5 1B 7A ED EE CC 5C 8B 05 AF B9 76 54 E1 C5 3A 91 00 A8 CE 1C AC D8 FC B6 2D 96 D3 CE E7 81 3F B0 E7 D5 39 C4 E0 E8 85 D4 18 51 9E 41 BA FE FA 32 0E 4C 4A 10 25 75 21 9E 0E 5D 29 A4 A0 6B F2 EA FF 70 9F 38 85 35 94 B6 90 39 DB 0D 25 E0 FF 89 97 93 94 88 84 8A BE D2 97 AE BF AC 2A 28 30 F4 A2 59 95 A1 F0 66 86 A5 2A 55 7A F0 35 0A CA CC DC 61 32 A8 B8 5C 47 90 19 91 E0 1E E1 1C BE FE A4 A3 02 27 7A 30 F6 10 11 CF ED 34 3F 18 74 E8 A5 23 C4 D9 73 5D 46 AA A5 EC BA 6E CE 40 EE E1 2D A0 9F CD DC B6 6D 6E CE 3A 94 A0 90 4D 7E DD 38 A9 52 27 3E 3D CF 94 C3 A7 07 F1 46 30 DB 0D BF 9F 30 12 25 FB 51 42 3F 74 49 3F 6C 1A 20 91 96 7C 1F 62 6C DA CA DE 99 48 74 D4 E2 5E E4 B8 AE BA 55 B5 95 E1 51 57 5B 0D B8 F9 4A 1F 86 CD FF AB 69 80 6F 65 76 DE 10 DF 91 EE 3F C4 69 81 76 6A 03 BB C5 73 5F 59 5A 0D BE 64 38 66 D0 75 EC 93 E7 52 E5 A6 3B 8F 99 8B A4 5E 6E B0 53 F5 4D 21 F4 D7 3E 7E 18 59 4F 83 56 E9 09 78 9C 90 9E 73 A2 87 F6 CC F4 DD 7C E5 24 BD DB E6 12 AC 48 28 96 98 B8 8F 92 20 F0 3D 22 0C 0B 34 93 9D D0 19 0E 9C 81 C2 FD A7 B1 09 56 E0 B8 C1 49 4F 76 9A 47 5A 69 17 32 99 61 76 05 F8 66 80 C3 D5 16 9D 78 88 C8 9F 2D 44 60 C5 44 18 C0 5F 90 59 52 9C 54 A8 C1 21 73 E9 B8 23 CC 8B A2 E9 E1 E9 6D 7A AF 2B 59 3E EB E3 E4 65 9C 5F 31 D3 EF 18 33 09 49 C8 C2 36 D5 4B 6C A4 79 66 B6 5B 8F C8 5F 74 88 5C 89 45 57 A8 1F 60 A0 2C D0 C9 6F 8D E5 D7 EF 64 47 B3 2F 5E D1 F7 A5 51 B2 E7 66 14 5C 9B 94 6B 47 C4 B1 D1 7E 25 01 70 9B E6 F9 02 B6 84 30 42 4C 9A 10 D9 95 FE F7 D1 60 05 9B CB 58 BE 92 A3 BD 7E B8 D2 CA E3 2D E3 EF 80 E5 86 64 BC BD BC 28 6D B8 C4 37 FB BD C3 5C BD 67 FA 22 98 79 22 5C 99 E2 90 02 FB 3F 09 10 A5 71 A3 95 17 AC 14 8F 6F 48 8F 39 B4 4E 23 78 2D FF FF 77 D9 E8 B9 F6 5D E0 39 44 92 C7 56 E9 25 F4 52 1C 6A CE 4C A2 BF 44 94 97 D4 42 3E 71 FC 59 68 16 67 AB DC 7A 4A 96 1E F1 F3 3E 85 F2 4C 2B 1E 2A 78 04 73 28 60 87 4B D9 4D 62 05 3C 33 DA 0B 13 90 EC 62 AF 6A 15 30 D2 46 1D 45 EA 4B 8A 23 7A D6 8B C5 89 6C 8E CE 6E 84 03 82 2D B5 19 E6 20 93 01 C2 24 84 74 23 79 F1 F4 99 FD 7C CC 0A B4 B4 18 5D 34 09 9F EE CF 6A 89 33 B3 DF D2 00 AC 4F 3E D9 A6 99 C8 13 A5 6B C8 2C 3C 30 B0 FD B7 13 CB FB C5 21 3B 1A EB 90 FA C1 4F 08 99 04 5D C5 8A 72 42 30 A4 49 88 5B 1C 31 14 1F 12 A6 8A 52 E9 1C 09 16 86 A2 81 19 30 50 9F A9 66 31 1B 6D 97 31 01 DF A9 77 F5 EB 59 AE 37 90 02 63 FE 85 25 79 3D A1 C2 30 12 83 50 3F 77 CA 2A 28 3C 87 84 C4 BE 4A D1 8E A4 A8 89 DD F7 21 03 9E 6C D3 6E AA 0A 25 27 8D 66 E3 C3 7E 17 95 0B CE E7 2C 37 55 80 85 CA 61 83 94 E3 63 0A 4F B0 CB C5 42 CE 0F 00 3D 4B 77 49 A7 96 22 E2 E0 AF 43 6B CE F2 D5 5F 0D F6 4F DB 37 D1 EE 8D 69 97 92 CD 2A BC 45 9D 3B 47 27 37 6B E3 DF 4B D8 FB A2 67 8F 7C 19 95 EE 98 E9 3A 05 28 8D C9 26 56 CB E0 4E A8 99 F9 CD 80 6B 65 18 B2 AD 66 E2 1A 14 7A 03 2D 24 E3 90 12 6A 25 05 2D 5C 32 7A 1C 13 D4 B6 F5 6D 4E A2 5D 71 32 88 50 1D 0F B6 DD 61 23 F7 DC BB 5C 27 B6 EB 7F 5B 89 5D 6E 8A 19 CF 36 D2 7F E6 55 0F E5 18 F9 9F 49 6D AC E8 5D 8C 2A 6B B7 D8 6F 00 ED 2A F0 B0 5F DE 96 D4 3D 23 C9 B1 75 EE 91 EC B6 31 DF 50 2C F9 C8 BD FD F7 A6 BC 98 02 BA 9C 23 24 6C A4 3E D6 2A 5A 65 4C 39 72 0C 8E 43 29 08 4C 21 B1 A2 29 76 6F 19 03 6A 2C 8D DB 9F 2D A5 6F 01 34 A6 6C 80 3D A1 2E 90 FA D5 56 A3 89 C0 B8 09 04 31 60 73 A5 7C 8F 04 92 7E A3 7F 60 24 B7 69 E8 5C 5B 53 55 D9 03 B6 32 6E 0B BB 2D 57 AC C5 7E 70 58 C3 4A CE 35 E9 26 AB F6 B4 B7 AE 5D 03 82 8B 30 D8 9D 30 9B 93 4C B0 48 D2 D6 3B 04 95 20 18 14 EC 75 58 E8 42 DE 1E C7 B9 3F BF 86 E9 3C 82 D8 62 0F 97 4D D1 D8 7D EE B1 1C C0 15 17 A8 D4 A9 D2 5B 2F 12 95 C2 7D E8 4C 51 6D 95 88 C8 33 7E 69 24 8F E4 2F 7F 22 20 32 BD 01 C8 71 C6 C1 A8 3C CA BC 8F 64 37 5A 4A 15 A6 36 F5 31 34 7E 70 78 5B 1C 8F 0B 14 28 9B 43 D1 49 3B F7 91 DC 12 F5 EF C6 0D 39 FE B4 6B 4D E1 BA 87 96 CB 75 87 B2 78 A1 D7 F6 7D DA ED 77 FE 42 DE 28 29 DF 1A 1F 11 CA 70 BC AF 9D 7D 5E 07 23 C8 8B 2C F7 1A 40 A2 15 73 2B 92 DE 98 57 E7 3D 43 A2 2A 8C 66 6A 0C 9D 15 A4 65 17 F5 A5 15 F7 CF 4B AE D0 46 E6 4E 62 E2 6E D9 FB 64 17 A8 96 57 8A 8B E8 89 C3 73 A8 FB 49 92 F6 4A 51 0A BF 2E 33 2E F6 6D BD C0 C7 0C 4F 69 8D 10 BB 46 C8 B4 88 0C D8 ED 2E 2B 0D 1E 77 E3 C6 52 E6 6D 56 80 5E A6 EA 34 86 5D 3B 3D EE 19 F4 62 9C BF 7F B1 6E 78 A1 75 58 EB 94 DB B9 1F 9A D9 72 EB 1E 60 58 87 46 B3 9D 2B 2B D7 04 8A 13 30 CD 35 16 C2 0E AB C3 93 B7 F4 F8 25 C5 BA 20 B5 62 64 A6 78 D7 E9 FD AD BC D0 69 F9 A7 AF C1 9D B7 8A B2 DA A6 CF F4 5E F4 FB 20 52 2A F2 3D 2F C1 CD A5 70 3A 8F 1C 4D 93 77 DC 35 1E 77 9F B9 15 7B 84 0A 7B 69 86 20 CC BA 7F 60 6D F8 D4 0E 97 9A 90 62 ED 9F BE FC 65 C6 A1 BD 58 D2 64 3A E8 73 69 37 B7 33 80 6F 5F D3 6B 7A 24 A5 62 A0 23 0A B9 51 7F AC E6 A0 E3 95 DA 61 97"""
                .trimMargin().replace("\n", " ").hexToBytes()

        // 792
        val part2 =
            """31 9D 14 F5 35 51 07 F7 6B 8E 3E 0B 5D C2 B9 C1 70 16 9E D2 2D 75 62 5E 62 82 CB 8B 22 A9 E9 DA 91 37 97 3B F9 95 AB 12 72 D1 7E 8C 87 2C A8 D1 AC CD 40 7B B1 5B 3E 4F 29 CA A6 D2 D8 9C 38 15 42 E6 66 35 9E C5 41 2C 4D BC 22 1F A1 AD 24 CE 4C C1 B2 E2 E0 A4 A4 DF E7 01 8B 10 90 C3 38 02 A9 0F 49 AA 8B 86 EC 58 B2 AA 79 6E FF 1E 1F 2A 35 3F 3D 96 A7 DC D0 F6 E5 18 4E EA C8 C5 72 76 3E 74 2E 12 05 78 6F FC BD 07 50 CA AF EE C2 11 00 00 02 88 
                |00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 7C CC CC 8A 82 A1 2F A3 F6 08 8D 5C 7B 64 F8 BB 4F 09 87 B1 7C E3 01 53 5F C0 32 A8 6E A4 0A 1F 49 38 E5 FD 7F 20 52 A3 1E 66 A5 43 85 C4 96 97 3C 8F 2A 33 98 D7 A6 90 50 37 BE 76 2D 94 CE 1F D8 63 07 DC D1 5A 6F F0 FA 1F 41 9E 74 BE 8D FC 61 2C 66 3F BB 4C FB 9A 02 53 FF BE E5 FD 52 B7 FD CE DB 80 C0 6A 55 14 31 31 0C 8A 7D 24 DC EC 8C 45 62 ED D9 F6 DA BB EA C5 6E 76 66 11 F4 CB 2B 3B C7 45 6C BE 6F ED 9E 1D 2B 8A ED 3B C1 5B 06 DC 75 58 C9 66 13 4C 7D EA C5 F9 A4 D1 37 EF 2B BE 98 E3 37 1C 30 17 20 80 98 AD 5F BC 85 98 5B A1 7F 9E AE 54 0C 36 B0 6F FA 4A 5B 6F DC 7E B1 9E 99 F6 07 16 43 11 7A BC 82 F7 FB B2 95 7C 29 37 AF 38 70 6E A8 75 12 84 73 CF 15 A3 65 B9 2B E5 0A 01 30 67 A2 D2 BF 9F 30 AF 4D A5 A6 EA 4A B3 E4 BB AC 15 E3 FB CD FC B0 52 69 05 8D BE A0 11 1D A7 AB 65 6D F1 68 06 71 89 4F 0A 7E F4 68 A0 71 21 07 E4 85 41 76 16 07 78 2E C5 30 A6 2F 5D C7 DB EF 24 61 20 E6 6D FF 01 28 0D D6 73 42 BF F1 65 EA 7E DE 81 94 89 EB D0 65 8D 94 39 BE 89 7F B0 B3 0C 74 2F FA B4 2B 86 22 08 67 EB FA 62 83 FA BD CA 9D 92 95 A6 B0 BB 0C 76 07 C4 DC B9 E0 CD 67 AB 84 4D CC 55 52 E5 22 CD FD AC 08 35 81 36 9A 2D 7B 56 66 E8 B4 D9 A5 AA 7A 57 F2 F6 FB 20 A1 78 D5 5D F3 7C E4 7F 34 53 59 52 38 06 35 AD 6D 7A 97 99 AA 97 1D 9D 4A E4 22 7A 11 0C EA 16 27 7C 8A 9E F2 5A 82 D5 23 C3 01 BF D7 43 99 E7 26 25 AA 5A 27 29 ED 84 7F 9D 46 47 F8 C3 94 94 D7 C8 27 9F F7 93 16 4E 57 62 FC AC 32 2E A8 9F B2 62 EA 45 B0 53 A2 6C 52 D2 76 5F 48 00 93 5B 95 24 1F 3C E7 EC 40 26 66 0F C7 7D 30 91 A5 F2 6A 2F 2D CB 24 0B A3 F8 71 23 CA 31 A1 42 17 AC 9B 25 5A 2E 46 2D BC 34 DE 70 DA EF 4C E1 2D AE 2A 88 66 19 EE 35 02 C6 68 2C C4 0A 90 82 CA AB 42 D6 57 01 62 22 0D DF 65 6A 7E 6E 68 07 1D E6 1E DB E3 E6 22 F2 9A 36 F4 25 06 FA 91 83 9C 98 EC FB FD F0 F9 62 97 E6 37 30 F3 B3 09 46 D9 7D 7F D5 51 FF 8A FA B7 33 C9 15 AC D3 93 B7 8F 36 E8 1A 85 42 E5 C5 00 E6 28 9C FA 6C"""
                .trimMargin().replace("\n", " ").hexToBytes()

        // packets sizes: 748, 5084 644

        println("part1.size = ${part1.size}")
        println("part2.size = ${part2.size}")

        reader.offer(buildPacket { writeFully(part1) })

        assertEquals(1, received.size)
        assertEquals(1, reader.getBufferedPackets().size)
        assertEquals(5084 - (5696 - 4 - 748 - 4), reader.getMissingLength().toInt())

        assertEquals(
            """00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 6D A0 E3 2B 65 53 C5 AE 22 D0 7B AC 3D DD 3C 6E FF 24 84 38 F0 B7 A3 DE 5F 3A 15 DE 7E 6D 08 D1 8C 18 A9 F2 89 03 03 43 15 C6 32 01 74 5B DD A4 33 49 47 78 E0 5B 83 2C E7 3A E1 CC 50 9C A8 8C 5C 88 06 A0 90 04 6E 23 6F AD 84 D8 6B 10 64 AD 33 5B 3F B5 3C C3 24 6C BB 28 8C BC A2 BD 5E 91 EA FA FE 5C 3B C3 F4 3B 59 24 37 1C E2 13 DB 75 C1 7C D5 8B 2F 57 AD C0 16 13 97 12 60 D5 4C 21 E8 13 72 B3 F6 98 05 89 BA 49 60 F1 1C D9 6A 82 F6 A1 AE 78 82 48 60 A9 24 3C A7 7A 93 79 96 8B AA E2 FA DE 5F E9 12 FE 51 27 47 CB 6A 20 DB 64 22 B4 3A B4 C8 5E D0 45 31 9F 63 9B 61 A4 F9 56 CB 0B BF 27 21 55 99 2A 53 EA E4 DC EA D3 7E 62 B9 1D 2A 48 54 C3 6B D8 E0 CD A5 CC 84 AF A8 91 05 2D AC C3 64 82 5F 56 EA FB 13 5B A7 78 ED B1 E1 ED 25 74 1D EE 5D A3 5F 2E FA 0B 5C 65 69 EB DE 4E DD 63 08 83 2C 10 40 D0 44 49 F9 AE 48 B7 D1 75 8D B7 45 EC 08 DA B1 B2 EE EF CF F1 A3 75 93 21 AA C1 50 5C 9B FA CF CD 99 34 26 9C D2 E9 AA 1C 13 8A 8A 0A 5E EE 62 D1 89 31 32 44 88 40 6D C6 BF CF 3B 36 FE BB FC 7F 2D 62 E1 0D 06 0C BD 79 2F 0D 8C 9D 16 44 FB EE 84 AF 39 67 5C BD A6 C1 BD 77 C6 78 81 AF F4 DB C7 3D E9 2D 2F 5F 6C 5F BD 7C D5 A6 E8 96 D4 F0 8E F4 0F 5C 34 E1 DD A5 F9 AB 0A 70 54 1C D8 B5 2E 9B A9 11 9E B7 F8 27 40 04 A1 1A 9B B7 29 99 CD E0 13 9E 5C C5 FD E5 3B DE C5 85 48 DE 21 5D D7 10 AD 77 81 B3 90 9E B2 A1 70 C0 45 AA E4 06 0F DB E4 F7 0B CB 25 28 C9 C1 EA 0B 23 E9 EC 16 45 2F 3B 07 2E 05 15 EA E8 89 C9 CE D7 BE 2B 2D 0F C1 BC 16 8A 4B 79 22 78 4C 70 50 46 86 78 C2 6E DB A2 A9 FA 93 F8 B4 3F 32 50 EF FE 42 EC F6 99 AD 5E 9A F0 E8 B5 F3 96 5C 0B A9 93 DC F2 D9 EB 1C D9 B1 38 E1 87 73 B8 48 DF 3E 35 74 68 E7 C7 94 D4 05 83 C2 08 90 9E 7C ED 61 1A 5A B7 32 5F 09 36 37 6F D3 7B 6F 67 38 D2 EB FD 6A 60 98 87 DE A7 61 88 74 99 80 C4 4E A9 9B 9F CA 4D AB 20 E3 FA 37 07 57 BF E5 A2 9D C5 CB F7 BA 56 E0 38 1B 3D AC DC 51 36 7C 60 E7 BA 00 FB 67 BC 48 6D 17 C6 EB E2 93 F3 2D 5B 16 AF 6B 83 CC 27 15 23 76 5A 73 64 21 C6 21 8D C8 9F 5C 3B 61 47 6F 96 3D EA 7B EE 12 FF 20 F3 20 09 AE 60 C0 86 47 D2 45 18 BC 73 1B 21 AF DA 02 28 4C 05 A9 69 52 31 F8 75 B4 47 A5 A9 49 70 A6 5D 33 F2 07 2A 20 AD 5E 31 6A FC F6 96 11 48 F5 9D 85 CD 97 A6 BF D0 28 C3 51 AA 62 90 98 AD 7E 94 73 53 2F""",
            received[0].toUHexString()
        )

        reader.offer(buildPacket { writeFully(part2) })

        assertEquals(3, received.size)
        assertEquals(0, reader.getBufferedPackets().size)
        assertEquals(0, reader.getMissingLength().toInt())

        assertEquals(
            """00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 4F 05 F9 A5 25 DD A9 68 1B 80 60 1C D5 63 2F 8D 04 46 D2 7F 20 C1 F4 91 99 FF 90 BD E6 81 8E 24 ED 86 12 2D A4 41 C9 99 4C 70 70 75 29 A7 7A 6C 8C 51 9A CE 2B 43 A1 3E C8 97 CD AB AE 25 21 1A C5 D2 6E 3B 1D F6 3A DF EA 4B 43 C9 5D 1F 16 B3 04 DC BF 6C B8 78 60 A1 56 C9 E9 13 23 77 A3 33 8B B6 88 BD E0 16 74 DE F5 CA BF 50 5E 7F 74 66 29 5D 00 56 E0 AD 04 EF 07 A6 FB E0 A1 85 0F 11 C8 BF F2 E7 C1 21 DA 71 B3 D6 42 A7 0D 8B BB 38 8F BF 2C 54 C8 24 14 0D E3 DB 77 FC 25 C3 D9 3A 12 10 12 E2 A7 E2 81 61 F7 88 BD 2E 05 C4 AC 84 DE 33 6B 9B 8A 9C 78 97 AB 15 4C AE F2 FB AF 93 94 45 69 69 66 8E 46 59 78 53 6A 12 CA 4C E6 AF B7 CB D7 CD C2 47 A6 71 2C 66 D8 76 4D 13 3D 8F B1 33 E9 D4 F1 2B 6B B9 E7 DD 37 E1 91 C0 19 C5 4F 73 C8 89 AC 71 CE BD 3C 64 25 5F FF 00 67 C7 5D 22 CA B1 53 D4 05 22 1A 05 FA 3E BD 13 A6 F9 4B 08 C9 68 4D C0 43 1B 84 AC 2B C2 EA D6 82 8B 28 A2 32 C4 E9 87 36 C0 E7 13 3E B3 CD D6 70 0C 2C 4F E6 F4 D4 5E 32 80 04 D1 CA 4F E6 A1 D3 C3 71 5D 57 18 8B 6A E4 B2 54 47 FA 95 8E BB 3B 92 99 94 33 86 D2 EC 36 69 78 67 BB 17 DB 58 C7 6C 2F 95 0F D8 E5 B0 B5 6F ED 90 88 46 2D 46 BE 23 BE F5 3C 89 AE FB 63 BC F8 C5 E2 5B 96 F4 58 7D 60 FB F8 4F B8 F0 66 3C 89 EF 16 4E 51 30 76 B0 6A 73 DE 95 D2 AA 26 B4 DC BA AB E1 18 17 A2 91 68 FC E0 C3 CE 74 EC F8 5A AE AB E4 4C 6D 66 C3 EF 73 A3 C2 8B 3B C0 32 37 DE 05 A5 EF 7B 2A FB 40 E5 1F E2 FE 0B 95 76 1C 41 87 FC 34 3C 84 B0 63 4D 5B C2 DA 57 08 68 69 3C CC AB CB 66 09 BA 6A 18 25 80 76 E2 20 B8 AC 9F 84 A1 EC D4 21 75 80 0E C1 3C 6B 19 6E BF D6 6A 38 83 C1 21 64 8E 09 CC DF C2 01 D6 68 6A F9 38 8A 6F B2 74 32 EA 8C 90 88 9E 8B CC 21 66 82 76 2D A5 09 27 AC 3F C9 71 BC 0C 86 57 14 37 BC 87 50 A1 F3 9D 9F 27 C2 2E F0 01 10 D6 7F A8 78 AC DB 93 A7 40 42 84 74 8D 15 08 6D 6F EA 02 E2 68 20 BE 3E 70 18 5D A8 08 BA 93 7C D7 0B 8C 76 C3 6C EB 2C 2D BD 59 70 63 83 3D 26 A6 76 D8 11 A7 A8 E9 AA BF D5 EA 99 F1 08 10 9E 31 58 F1 59 99 ED 94 EB 53 64 49 0E 0F 1D 7A 10 3C FA 19 06 AB 49 A8 EE A9 21 2A 29 86 87 C7 8C 31 C0 E9 CA 57 99 E3 DC 32 F7 89 21 C7 72 5B 58 85 B8 65 6B D6 56 D6 C2 9F E5 03 6E BB B4 9D 09 FE 34 A2 DE 3E 86 8F FD A9 6D 8C 86 BC 4F B6 0F 1B 16 1E C4 36 CD F4 80 63 13 36 A7 2F 49 22 42 3E E9 FD 8B 0B BC E0 BA 2A 28 B8 7B 05 A9 72 23 17 51 30 BE CC CB A3 0B 7F 73 A4 9A E7 DA E6 D3 B1 92 0A CF 43 F3 6D D6 16 01 F5 0F 69 2C 67 72 39 BB 1B 87 F8 52 B7 AD 2D 47 D0 82 FB 1F EB 36 CC CC 19 40 F5 EA C1 B2 76 C3 3B E9 0D 8C 3C 1A 05 91 B4 E9 AA 9B FF 40 EF E7 36 D8 94 B2 76 99 B6 27 4E E9 01 A3 E5 A3 CE F1 7B 4E 6D F2 53 05 33 9D 1A C5 3C BC F3 E8 BF 84 0F A8 21 C0 10 CD D1 26 1B 34 80 AE 7B 48 97 45 F6 DF 8F 79 C3 4E C6 47 34 E4 FD 0D F9 20 D2 62 20 FE 96 32 73 69 04 6F 44 8B 68 AA A6 30 4C 28 BB C1 B4 0B 6B BC EF CD DA CA 45 C2 5D 71 C2 53 DD B0 92 3D 0F 80 37 C2 A9 8D 89 E3 D9 11 1C 98 F2 7E 4A 9A EE 6D 50 E0 04 E3 97 71 F2 AC 8D 5D 8A FE 75 F0 B5 E8 AD 3A 25 4D 2F 0C 82 1D 94 5A 7C 63 32 3E E3 82 95 80 2A 6F BB BA 51 38 1C EF 9E DC 1D B2 31 A9 F9 AA 05 27 EB A9 4A 58 D1 21 DE 1C 55 88 3F A5 EB 27 F4 CF DA DF 67 7E 24 5C A0 B6 BC 1F 4E E1 3F 70 86 62 DB A1 BE 5A 88 C6 20 22 B3 F3 DD 9D 08 1B 78 15 55 EC 47 76 AB E6 4C 77 35 D3 08 9F 52 9B 9F 42 58 28 A4 8D 96 F0 BF C1 42 90 1C 18 A2 98 25 86 B0 9B 3B DE 75 C1 87 4F 9A 7E FD 03 9F 0F 95 E4 39 04 FE F4 B7 DA 57 BE F0 D8 09 B6 42 F4 CD CF 8E 38 98 6B 29 77 D3 19 63 03 2D CC DB 6C 54 85 18 5D FA AB FE CE 46 6A 1F 8B 63 0F 67 27 55 E7 F4 03 7F B1 52 7F 2E 01 F7 8C B9 9F F6 03 4F DD 05 D2 3A D7 2D C9 1C 3D A1 2E 60 7A 0A E5 7B 72 C7 8D 18 9A FA 4B 3F 16 23 17 21 6B BF 32 9B A2 E9 64 F2 E4 F9 B2 65 81 8A 99 F7 23 36 8D 57 E7 A6 26 26 1F 7F 19 F7 5C 5B C3 08 A1 D8 27 C3 BE 98 26 B3 50 63 F0 05 6A 29 19 05 CA 4A 12 CF 33 95 27 53 14 A2 5F D0 37 B6 03 0E 0C 72 44 E4 DB 89 B3 E0 D6 0D AA 3F 1F DF E8 86 E1 55 D2 6B 15 AA 98 A2 92 E9 1B 2E 20 8A 6B C8 6B 0F 22 37 FD F5 36 06 BB 3F 03 23 9F 40 08 1C 58 DE 20 74 BB C2 7B 0A 81 FB 6F D8 89 DB 03 DA 89 86 B1 40 A3 AB 8D A8 CD 2D 9B 97 98 20 6D 73 8F 35 F2 26 F2 8B A1 47 35 27 9D 85 BC 65 4E 7A 42 FC 39 1D 62 24 7C 1A B1 01 CC FA 40 15 FC 4B 2F CE 1B 9B 0B EB 07 F7 12 55 BD 7C B1 5C BC AC 53 16 DB 34 CF D3 91 6A 92 9E FA E8 A2 6B 4D 49 36 3F 33 79 49 4D 52 EC 1A 3F 0A EB 9E 61 E8 8D FA A2 D4 A2 E4 26 1B 2A A2 CA 6A 94 35 8D 77 CE 0A 7F 99 7D D8 53 61 28 F7 27 43 38 99 05 9E B9 7E 9C B5 61 84 AE 0D 9C 44 EF D1 54 B8 3F B0 E8 D4 FE EB 42 A5 DA 24 07 28 F5 21 93 A2 53 30 18 26 AB 3C 99 88 EB 0A C8 D4 6E 76 25 A7 65 13 59 16 FE C0 E7 23 BD 6D 4D 5B DC 4C 02 8A 93 56 B5 3D B0 ED 2A 09 CD 01 9A 5A A0 62 FF 4C 23 7E 1C 2B 33 AD 29 B8 7B 4F A6 71 7B 1A C1 3E 2D B9 2F 78 56 CF D2 65 EA 02 E9 1D 4A 27 CD 94 B0 67 79 E3 9B 2B 9C 50 1B 78 ED 06 3C 06 C0 07 6E 53 49 73 2C 74 59 AF A7 92 16 A3 DA 35 B7 B9 B8 D8 CF 58 3E BF 43 BF 43 E8 C5 B7 36 B0 7E 65 AF 6F 75 89 C5 76 87 40 19 9C 4F 30 54 18 B2 09 74 4B D6 A5 29 22 76 E8 19 7C A5 A9 B0 98 12 06 0A 9E 87 F3 1F 75 44 32 CD 47 FE 56 7F 84 4E 73 EF 28 A3 C6 78 08 94 1D 6A D2 E4 31 20 99 63 D4 F7 A8 84 CE C8 5E 43 45 01 3F BC 9A D3 0C B7 FF B0 A8 3D C7 EE D7 74 76 50 9C 61 67 E3 E2 19 BA DD CB AF 8F A2 CD 55 56 1C 32 5F 16 AA AE 24 2B 10 D4 A1 F9 2D 91 93 1F C5 BE 7D 5F EC 4B F4 E5 3D FF 10 3B F7 2D 27 F1 97 FF 46 BF 6E 48 1C 7B 49 BE 1F 7A F8 79 0A A4 C4 2D 73 C2 FA 1A 71 56 6E A7 6B 1A 46 83 DA 80 47 1D E7 FD 03 3A 98 51 E5 3D 30 71 05 41 D5 F5 8F 7E BC F1 47 6E D2 2E 9B 6C 18 30 D0 92 F1 BD 5F 67 D4 5D BA 75 2F C2 5F 98 64 5F 2C 7B 72 8F 77 F9 7D 07 F9 66 9B 21 6F B1 ED F0 22 B4 0E 4D 3B 79 42 CD 84 66 F7 6F 57 B4 CD EE 22 B0 DB 31 74 12 DC D8 B3 54 07 04 18 77 D8 80 D4 FD 33 AE 57 64 E1 23 37 68 4D 1A F0 36 ED C5 FA 97 20 93 8D 37 A3 A1 84 CE C9 4B 2B E8 15 CB 3D 9C 10 08 A1 FE 7E 51 B9 F7 F5 0A 8A 65 FF 6B 7C E6 01 2A ED BA E2 A7 26 32 F9 79 F5 2C 16 81 AA DD 15 1F D2 A4 C7 B7 26 7A 5A C6 CE EA 59 26 68 07 6F 10 E8 6B 3F 22 B3 B0 64 7B 05 AA 76 98 0B 03 1D 6F 25 B6 08 54 A8 87 8A 84 92 29 9A 2D A4 59 81 BF 52 DE 24 F9 5F C2 5A FA 1A D7 EE 03 6A 00 C3 8E 28 97 31 55 CC 07 1A 98 11 1E F3 E8 E3 5A 6B F1 91 87 4A 74 83 7E C1 DF 06 AF 2B C6 B0 B0 56 F6 96 D0 C4 AC F3 A8 7C D8 4A BF 50 66 09 AA D1 0F E8 8F 7D DE 70 56 8D D4 68 E3 1A BC C1 EA 9F 6C 8B F3 8D 04 D7 24 95 E5 E9 FC 72 36 D7 9D 01 C9 0B 81 46 01 4C B7 BB 48 36 B4 3E 52 3C D3 0E 24 36 64 94 CC 23 F9 A0 86 56 36 0E 67 B1 EF 86 1D 93 7F 58 3E B6 91 49 14 31 F6 EB C3 5A DF A8 C8 26 2E 29 37 7A 23 7C 3B D9 C0 53 33 52 02 AC 6B B5 FB F6 A1 6D 13 E4 87 CA 56 3A B2 0C 75 92 75 5C A0 1C C2 31 97 E8 1B 13 AC D7 E1 47 E3 E7 CE 86 FF 85 AE E2 BD 5C 6F 3F 65 FA 11 C6 C0 12 CE 86 19 C8 E2 72 8E B6 4E 88 73 E0 C1 31 86 42 F7 51 05 68 E0 73 99 0E 69 86 59 C9 4A 77 CA 76 10 AC DE 95 F9 17 4A 8A 5B 14 A8 16 57 C4 16 60 CF 0E 48 EE D0 DC 63 8B 9B 46 6D 96 41 1D 38 8B DF 70 31 95 3A F7 A8 1F B8 4D 7A BA E0 CC E9 58 B5 2A 87 42 43 63 DC 12 47 AC 32 49 D7 02 87 DB C9 5D 10 BC 93 65 F4 3C C3 FF C7 28 86 14 4D 4C 70 FD FE 5A 06 EA 01 C5 27 FA 46 9B 98 5E 71 D5 BE C1 0A A2 45 7F 09 1A 7D 12 4F 38 49 09 77 1B 8A 39 48 27 22 4C B1 A3 AA 68 8E 1B 73 B0 A6 EA 78 AA C3 33 16 3B 00 69 4A D5 07 06 42 65 50 2E F2 60 8B F0 9E 8A 7C 05 B7 DC 85 E7 EF 10 FD C8 FE 80 A9 C6 B0 46 35 0F 77 17 6C 7D EC 60 24 D9 21 76 AC DF 6B B2 FC B0 75 D1 3C 2D 69 60 C2 4E CD 62 58 72 D8 AC B3 F5 75 56 80 E3 D1 9B 95 1B E7 CD D1 C3 C2 FE 50 DC DD 10 D5 89 43 7E FB 90 54 4D 42 13 1A 47 20 DB 18 E7 BA 3D 2D AF 53 DC 8F 37 08 8C 3B 52 00 A4 62 AB 22 93 46 05 38 A8 C2 FF 2C F5 F8 5D B5 4B 04 7E 48 F7 03 75 DA C8 B0 E9 E0 0B 3B 7E 57 0A 1A 53 98 EE 1A 2A 48 C7 FD 22 D4 B0 E7 5C B3 75 8F B7 4E 8A EF B0 29 6C A9 C8 02 24 4E D6 3B 2C 82 4E F3 1E 62 94 40 64 2E 77 BB B9 3D 1E F4 BD DD BD FA DA 25 09 65 8D 5E 1B 3C 2E 80 22 11 D6 1D 32 B4 7E 89 B0 F6 85 83 C3 34 78 57 3C 97 DF 05 01 6C 8F 91 25 95 D8 BD D8 86 7C 8C F0 50 88 25 10 6D E6 E2 DA 76 31 80 55 2E E6 1F 37 A1 82 8B B5 FC 2B 71 5D 9B E6 89 CD 14 BB 49 28 BA 1D 49 71 08 A7 D2 B5 A4 1C 9C 1B DF EF C3 E7 55 2B F4 2E 4E 93 4A 74 B6 A9 47 18 23 9C 44 08 3D 13 06 24 41 06 1E BF 88 BC 3D 1B 75 92 80 C7 78 A4 CA 5A F1 79 EF 3F DC F5 5F AF 49 73 68 96 C9 DB 8E F2 A7 B4 2D 0F 50 64 0C EB EB F6 08 09 0A F5 0A 2C 88 5E AE 44 25 89 DF 7F 4C 5D 0C 73 47 3C 1A 2B 41 7B 9E 28 48 39 0A A2 D8 04 78 90 DE D5 88 9E C7 00 5F F8 63 CD BF 8D F1 5C 12 CE 7E 2B 09 D0 4C 42 7A 84 0F 36 C6 C9 5D AA 44 87 23 4A 76 76 94 1A 7A 2D D6 23 B4 84 EF 6D 3B C6 01 B2 F7 20 09 40 51 E4 80 58 5E A6 A5 97 2F 75 CE E7 97 78 F0 0B F9 31 98 CE 6C 40 4F A4 B3 14 AA A5 89 AA F9 9B 21 11 69 A8 E0 56 B0 F1 C9 CC 44 83 1C 32 E3 79 14 1A 5D C8 F3 41 C5 9A CE A1 B7 5B C2 53 22 49 15 FE 52 A9 38 DB BD 7A 69 83 06 0F 69 77 98 D1 F4 93 3C 83 37 B7 21 A5 D7 2A 9E D9 A3 BC 1C 9D 4F A4 22 51 4C 44 B9 35 C9 8E 41 FF 0D 6D FC BD 12 F5 CA 3E F9 EE 75 1D 01 9B CD 34 B2 C6 4A 4B 9D 2B 79 A5 12 52 BF FF F4 42 62 39 53 58 8E 82 F6 94 2C 00 97 0E 8A B3 5B 73 3D D6 CF 3E D5 14 BB 2D 5B E6 2D 78 88 D5 E8 33 BC E9 C9 A4 2D 36 41 F5 DA 5D 68 6E B6 B9 93 89 E0 15 E5 68 65 48 42 FA 9E 16 20 5A C8 CB 8D 61 52 E1 23 DE 0A 40 8C F5 6F 86 AD 36 F6 20 9E D4 6A B2 D9 7E 5E 3E F3 D7 C8 A8 25 CB 49 E5 FE BA A8 4E 34 41 B4 CB 9B AA BA 8F B9 53 F9 0A 67 AC 3A 02 D2 55 AB E3 6F 8C 79 E6 C0 2E 70 5C 6C E5 89 17 F2 A4 C8 8C 58 58 8C 3C 22 10 68 0D 9A 70 63 59 F6 C0 60 6F 1E B6 9E B5 69 B3 1B 0E D8 F8 8C 5D 06 2F 42 4F 69 BB 83 DF F8 20 8A BF 74 30 4A 57 C8 FE 13 AD 8B 09 B9 92 7A DD 53 DA A9 33 00 85 53 88 2E 58 3D A4 02 ED D0 E9 25 E2 EE 7A C0 08 B3 B5 BF 7D 36 07 0D 87 F7 6B 68 BF C6 7B A6 E4 B6 D7 76 FA BE 23 E6 53 94 D3 54 94 6D 39 87 3F 74 F3 ED 5C D6 E0 4F E8 5C AD 86 3E E7 E1 12 32 51 F0 E9 42 B5 12 46 26 B1 86 AD 19 1D A2 81 4E 3E F2 FF 8A 5F 9C 80 02 56 70 AA 0B 0D 2C 61 DA B9 1C 5A B3 6F 2E C5 32 C0 48 FE 19 9E F8 F4 30 3E E2 2C D8 A5 81 E9 C9 6B 44 6B B7 95 5D 94 0E C5 63 D7 78 FD 58 54 E1 19 E2 22 B5 48 10 D5 87 28 F7 A3 30 43 9D 69 B5 D5 38 D5 05 22 77 FC DC 93 62 08 CD 58 19 E4 65 E6 AC 05 6C FE C9 02 3A 28 48 77 AF D9 63 40 25 45 CF 89 4C 0A ED 60 C9 A8 75 A0 68 79 69 8C 13 5C 0C B4 FD 8F 27 8E BA 16 E5 1E AA 78 84 AF BE BB E9 09 9C 51 8A 10 2B DB 0E 7E F4 E2 A4 DE 15 85 A2 E2 40 E7 37 56 A5 AF 91 4E EA 80 E3 8F 3A F3 28 15 8D F4 FA D1 EF C0 09 98 43 1A A3 98 CA 94 BB DD EF 98 E1 98 6E AD 53 9F 48 E5 9E A5 1B 7A ED EE CC 5C 8B 05 AF B9 76 54 E1 C5 3A 91 00 A8 CE 1C AC D8 FC B6 2D 96 D3 CE E7 81 3F B0 E7 D5 39 C4 E0 E8 85 D4 18 51 9E 41 BA FE FA 32 0E 4C 4A 10 25 75 21 9E 0E 5D 29 A4 A0 6B F2 EA FF 70 9F 38 85 35 94 B6 90 39 DB 0D 25 E0 FF 89 97 93 94 88 84 8A BE D2 97 AE BF AC 2A 28 30 F4 A2 59 95 A1 F0 66 86 A5 2A 55 7A F0 35 0A CA CC DC 61 32 A8 B8 5C 47 90 19 91 E0 1E E1 1C BE FE A4 A3 02 27 7A 30 F6 10 11 CF ED 34 3F 18 74 E8 A5 23 C4 D9 73 5D 46 AA A5 EC BA 6E CE 40 EE E1 2D A0 9F CD DC B6 6D 6E CE 3A 94 A0 90 4D 7E DD 38 A9 52 27 3E 3D CF 94 C3 A7 07 F1 46 30 DB 0D BF 9F 30 12 25 FB 51 42 3F 74 49 3F 6C 1A 20 91 96 7C 1F 62 6C DA CA DE 99 48 74 D4 E2 5E E4 B8 AE BA 55 B5 95 E1 51 57 5B 0D B8 F9 4A 1F 86 CD FF AB 69 80 6F 65 76 DE 10 DF 91 EE 3F C4 69 81 76 6A 03 BB C5 73 5F 59 5A 0D BE 64 38 66 D0 75 EC 93 E7 52 E5 A6 3B 8F 99 8B A4 5E 6E B0 53 F5 4D 21 F4 D7 3E 7E 18 59 4F 83 56 E9 09 78 9C 90 9E 73 A2 87 F6 CC F4 DD 7C E5 24 BD DB E6 12 AC 48 28 96 98 B8 8F 92 20 F0 3D 22 0C 0B 34 93 9D D0 19 0E 9C 81 C2 FD A7 B1 09 56 E0 B8 C1 49 4F 76 9A 47 5A 69 17 32 99 61 76 05 F8 66 80 C3 D5 16 9D 78 88 C8 9F 2D 44 60 C5 44 18 C0 5F 90 59 52 9C 54 A8 C1 21 73 E9 B8 23 CC 8B A2 E9 E1 E9 6D 7A AF 2B 59 3E EB E3 E4 65 9C 5F 31 D3 EF 18 33 09 49 C8 C2 36 D5 4B 6C A4 79 66 B6 5B 8F C8 5F 74 88 5C 89 45 57 A8 1F 60 A0 2C D0 C9 6F 8D E5 D7 EF 64 47 B3 2F 5E D1 F7 A5 51 B2 E7 66 14 5C 9B 94 6B 47 C4 B1 D1 7E 25 01 70 9B E6 F9 02 B6 84 30 42 4C 9A 10 D9 95 FE F7 D1 60 05 9B CB 58 BE 92 A3 BD 7E B8 D2 CA E3 2D E3 EF 80 E5 86 64 BC BD BC 28 6D B8 C4 37 FB BD C3 5C BD 67 FA 22 98 79 22 5C 99 E2 90 02 FB 3F 09 10 A5 71 A3 95 17 AC 14 8F 6F 48 8F 39 B4 4E 23 78 2D FF FF 77 D9 E8 B9 F6 5D E0 39 44 92 C7 56 E9 25 F4 52 1C 6A CE 4C A2 BF 44 94 97 D4 42 3E 71 FC 59 68 16 67 AB DC 7A 4A 96 1E F1 F3 3E 85 F2 4C 2B 1E 2A 78 04 73 28 60 87 4B D9 4D 62 05 3C 33 DA 0B 13 90 EC 62 AF 6A 15 30 D2 46 1D 45 EA 4B 8A 23 7A D6 8B C5 89 6C 8E CE 6E 84 03 82 2D B5 19 E6 20 93 01 C2 24 84 74 23 79 F1 F4 99 FD 7C CC 0A B4 B4 18 5D 34 09 9F EE CF 6A 89 33 B3 DF D2 00 AC 4F 3E D9 A6 99 C8 13 A5 6B C8 2C 3C 30 B0 FD B7 13 CB FB C5 21 3B 1A EB 90 FA C1 4F 08 99 04 5D C5 8A 72 42 30 A4 49 88 5B 1C 31 14 1F 12 A6 8A 52 E9 1C 09 16 86 A2 81 19 30 50 9F A9 66 31 1B 6D 97 31 01 DF A9 77 F5 EB 59 AE 37 90 02 63 FE 85 25 79 3D A1 C2 30 12 83 50 3F 77 CA 2A 28 3C 87 84 C4 BE 4A D1 8E A4 A8 89 DD F7 21 03 9E 6C D3 6E AA 0A 25 27 8D 66 E3 C3 7E 17 95 0B CE E7 2C 37 55 80 85 CA 61 83 94 E3 63 0A 4F B0 CB C5 42 CE 0F 00 3D 4B 77 49 A7 96 22 E2 E0 AF 43 6B CE F2 D5 5F 0D F6 4F DB 37 D1 EE 8D 69 97 92 CD 2A BC 45 9D 3B 47 27 37 6B E3 DF 4B D8 FB A2 67 8F 7C 19 95 EE 98 E9 3A 05 28 8D C9 26 56 CB E0 4E A8 99 F9 CD 80 6B 65 18 B2 AD 66 E2 1A 14 7A 03 2D 24 E3 90 12 6A 25 05 2D 5C 32 7A 1C 13 D4 B6 F5 6D 4E A2 5D 71 32 88 50 1D 0F B6 DD 61 23 F7 DC BB 5C 27 B6 EB 7F 5B 89 5D 6E 8A 19 CF 36 D2 7F E6 55 0F E5 18 F9 9F 49 6D AC E8 5D 8C 2A 6B B7 D8 6F 00 ED 2A F0 B0 5F DE 96 D4 3D 23 C9 B1 75 EE 91 EC B6 31 DF 50 2C F9 C8 BD FD F7 A6 BC 98 02 BA 9C 23 24 6C A4 3E D6 2A 5A 65 4C 39 72 0C 8E 43 29 08 4C 21 B1 A2 29 76 6F 19 03 6A 2C 8D DB 9F 2D A5 6F 01 34 A6 6C 80 3D A1 2E 90 FA D5 56 A3 89 C0 B8 09 04 31 60 73 A5 7C 8F 04 92 7E A3 7F 60 24 B7 69 E8 5C 5B 53 55 D9 03 B6 32 6E 0B BB 2D 57 AC C5 7E 70 58 C3 4A CE 35 E9 26 AB F6 B4 B7 AE 5D 03 82 8B 30 D8 9D 30 9B 93 4C B0 48 D2 D6 3B 04 95 20 18 14 EC 75 58 E8 42 DE 1E C7 B9 3F BF 86 E9 3C 82 D8 62 0F 97 4D D1 D8 7D EE B1 1C C0 15 17 A8 D4 A9 D2 5B 2F 12 95 C2 7D E8 4C 51 6D 95 88 C8 33 7E 69 24 8F E4 2F 7F 22 20 32 BD 01 C8 71 C6 C1 A8 3C CA BC 8F 64 37 5A 4A 15 A6 36 F5 31 34 7E 70 78 5B 1C 8F 0B 14 28 9B 43 D1 49 3B F7 91 DC 12 F5 EF C6 0D 39 FE B4 6B 4D E1 BA 87 96 CB 75 87 B2 78 A1 D7 F6 7D DA ED 77 FE 42 DE 28 29 DF 1A 1F 11 CA 70 BC AF 9D 7D 5E 07 23 C8 8B 2C F7 1A 40 A2 15 73 2B 92 DE 98 57 E7 3D 43 A2 2A 8C 66 6A 0C 9D 15 A4 65 17 F5 A5 15 F7 CF 4B AE D0 46 E6 4E 62 E2 6E D9 FB 64 17 A8 96 57 8A 8B E8 89 C3 73 A8 FB 49 92 F6 4A 51 0A BF 2E 33 2E F6 6D BD C0 C7 0C 4F 69 8D 10 BB 46 C8 B4 88 0C D8 ED 2E 2B 0D 1E 77 E3 C6 52 E6 6D 56 80 5E A6 EA 34 86 5D 3B 3D EE 19 F4 62 9C BF 7F B1 6E 78 A1 75 58 EB 94 DB B9 1F 9A D9 72 EB 1E 60 58 87 46 B3 9D 2B 2B D7 04 8A 13 30 CD 35 16 C2 0E AB C3 93 B7 F4 F8 25 C5 BA 20 B5 62 64 A6 78 D7 E9 FD AD BC D0 69 F9 A7 AF C1 9D B7 8A B2 DA A6 CF F4 5E F4 FB 20 52 2A F2 3D 2F C1 CD A5 70 3A 8F 1C 4D 93 77 DC 35 1E 77 9F B9 15 7B 84 0A 7B 69 86 20 CC BA 7F 60 6D F8 D4 0E 97 9A 90 62 ED 9F BE FC 65 C6 A1 BD 58 D2 64 3A E8 73 69 37 B7 33 80 6F 5F D3 6B 7A 24 A5 62 A0 23 0A B9 51 7F AC E6 A0 E3 95 DA 61 97"""
                    + """ 31 9D 14 F5 35 51 07 F7 6B 8E 3E 0B 5D C2 B9 C1 70 16 9E D2 2D 75 62 5E 62 82 CB 8B 22 A9 E9 DA 91 37 97 3B F9 95 AB 12 72 D1 7E 8C 87 2C A8 D1 AC CD 40 7B B1 5B 3E 4F 29 CA A6 D2 D8 9C 38 15 42 E6 66 35 9E C5 41 2C 4D BC 22 1F A1 AD 24 CE 4C C1 B2 E2 E0 A4 A4 DF E7 01 8B 10 90 C3 38 02 A9 0F 49 AA 8B 86 EC 58 B2 AA 79 6E FF 1E 1F 2A 35 3F 3D 96 A7 DC D0 F6 E5 18 4E EA C8 C5 72 76 3E 74 2E 12 05 78 6F FC BD 07 50 CA AF EE C2 11""",
            received[1].toUHexString()
        )

        assertEquals(
            """00 00 00 0B 01 00 00 00 00 0E 11 11 11 11 11 11 11 11 11 11 7C CC CC 8A 82 A1 2F A3 F6 08 8D 5C 7B 64 F8 BB 4F 09 87 B1 7C E3 01 53 5F C0 32 A8 6E A4 0A 1F 49 38 E5 FD 7F 20 52 A3 1E 66 A5 43 85 C4 96 97 3C 8F 2A 33 98 D7 A6 90 50 37 BE 76 2D 94 CE 1F D8 63 07 DC D1 5A 6F F0 FA 1F 41 9E 74 BE 8D FC 61 2C 66 3F BB 4C FB 9A 02 53 FF BE E5 FD 52 B7 FD CE DB 80 C0 6A 55 14 31 31 0C 8A 7D 24 DC EC 8C 45 62 ED D9 F6 DA BB EA C5 6E 76 66 11 F4 CB 2B 3B C7 45 6C BE 6F ED 9E 1D 2B 8A ED 3B C1 5B 06 DC 75 58 C9 66 13 4C 7D EA C5 F9 A4 D1 37 EF 2B BE 98 E3 37 1C 30 17 20 80 98 AD 5F BC 85 98 5B A1 7F 9E AE 54 0C 36 B0 6F FA 4A 5B 6F DC 7E B1 9E 99 F6 07 16 43 11 7A BC 82 F7 FB B2 95 7C 29 37 AF 38 70 6E A8 75 12 84 73 CF 15 A3 65 B9 2B E5 0A 01 30 67 A2 D2 BF 9F 30 AF 4D A5 A6 EA 4A B3 E4 BB AC 15 E3 FB CD FC B0 52 69 05 8D BE A0 11 1D A7 AB 65 6D F1 68 06 71 89 4F 0A 7E F4 68 A0 71 21 07 E4 85 41 76 16 07 78 2E C5 30 A6 2F 5D C7 DB EF 24 61 20 E6 6D FF 01 28 0D D6 73 42 BF F1 65 EA 7E DE 81 94 89 EB D0 65 8D 94 39 BE 89 7F B0 B3 0C 74 2F FA B4 2B 86 22 08 67 EB FA 62 83 FA BD CA 9D 92 95 A6 B0 BB 0C 76 07 C4 DC B9 E0 CD 67 AB 84 4D CC 55 52 E5 22 CD FD AC 08 35 81 36 9A 2D 7B 56 66 E8 B4 D9 A5 AA 7A 57 F2 F6 FB 20 A1 78 D5 5D F3 7C E4 7F 34 53 59 52 38 06 35 AD 6D 7A 97 99 AA 97 1D 9D 4A E4 22 7A 11 0C EA 16 27 7C 8A 9E F2 5A 82 D5 23 C3 01 BF D7 43 99 E7 26 25 AA 5A 27 29 ED 84 7F 9D 46 47 F8 C3 94 94 D7 C8 27 9F F7 93 16 4E 57 62 FC AC 32 2E A8 9F B2 62 EA 45 B0 53 A2 6C 52 D2 76 5F 48 00 93 5B 95 24 1F 3C E7 EC 40 26 66 0F C7 7D 30 91 A5 F2 6A 2F 2D CB 24 0B A3 F8 71 23 CA 31 A1 42 17 AC 9B 25 5A 2E 46 2D BC 34 DE 70 DA EF 4C E1 2D AE 2A 88 66 19 EE 35 02 C6 68 2C C4 0A 90 82 CA AB 42 D6 57 01 62 22 0D DF 65 6A 7E 6E 68 07 1D E6 1E DB E3 E6 22 F2 9A 36 F4 25 06 FA 91 83 9C 98 EC FB FD F0 F9 62 97 E6 37 30 F3 B3 09 46 D9 7D 7F D5 51 FF 8A FA B7 33 C9 15 AC D3 93 B7 8F 36 E8 1A 85 42 E5 C5 00 E6 28 9C FA 6C""",
            received[2].toUHexString()
        )
    }

    private inline fun buildLVPacket(crossinline block: BytePacketBuilder.() -> Unit): ByteReadPacket {
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