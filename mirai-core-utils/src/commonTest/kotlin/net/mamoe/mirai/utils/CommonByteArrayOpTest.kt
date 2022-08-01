/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal expect class ByteArrayOpTest() :
    CommonByteArrayOpTest // will run all tests in CommonByteArrayOpTest again but fine

internal open class CommonByteArrayOpTest {
    protected val sampleLongText = // 574 chars
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

    @Test
    fun testAvailableProcessors() {
        val processors = availableProcessors()
        assertTrue(processors.toString()) { processors > 0 }
    }

    @Test
    fun testMd5() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = str.md5()
        assertContentEquals(
            "30 3B 36 B3 42 00 39 E2 EC 18 22 79 10 32 05 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testMd5WithOffset() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = (byteArrayOf(1) + str.toByteArray()).md5(1)
        assertContentEquals(
            "30 3B 36 B3 42 00 39 E2 EC 18 22 79 10 32 05 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testSha1() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = str.sha1()
        assertContentEquals(
            "54 98 CD 62 6C DE E3 9B 96 D4 34 5E 13 51 48 BB FC 32 1C 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testDeflate() {
        val str = "qGnJ1RrFC9"
        println(str)
        val hash = str.toByteArray().deflate()
        assertContentEquals( // if we change
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )

        assertEquals(str, str.toByteArray().deflate().inflate().decodeToString())
    }

    @Test
    fun testInflate() {
        val result =
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes()
                .inflate().decodeToString()
        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }

    @Test
    fun testInflateRealLongData() {
        val result =
            "78 DA ED D9 6D 50 DB E6 1D 00 70 49 E6 45 D6 B5 19 73 9B 8E B0 2F 9A 3E AC 59 EF 8A 1F C9 06 D9 5E DC DD B1 DB 5D BB 6B EF F6 65 B7 DE F6 21 A3 AE 21 4E 8C 63 C0 64 D9 3E 51 F2 E6 40 78 33 64 25 6F 84 92 00 CE 08 85 00 0D 0A 25 90 B6 59 E9 BA 8C D2 A4 D7 5E 9B A4 C9 06 B6 43 5E 69 93 90 34 69 93 3D B2 1C 59 C6 12 71 81 72 C7 45 3E CE 77 92 2D F1 7F FE CF EF F9 DF 5F 7E 70 44 87 64 50 69 DE E3 57 EA 53 D2 EB 1B 0E B7 13 64 5F EB 58 39 B1 BC A2 F9 DE 69 F4 E5 F2 3B 1F 06 B4 7F 5E EA 46 48 F4 99 F2 35 F8 7D F8 FA 51 5A 5F EB BE 1B 58 3A 4A ED 46 89 E5 78 F8 20 ED EC 57 BE BE D4 F4 33 C9 24 B2 BC A5 0C 05 9F 1E 1A A9 4F 31 9D D9 E4 DD 50 0A 5F 18 87 22 3A 1C 47 D2 90 74 84 44 32 F2 08 44 F7 4A A6 F7 14 FB 63 E4 4F 7F FC 4D 46 06 7E AB 96 4C FB 84 BF 22 BD BB 31 38 A1 81 F7 40 C0 77 0D 9B 9B 52 F0 F0 FB 33 18 8E 00 C4 84 FC 76 19 8E EB 96 06 76 35 8D 73 FD C1 DD 5B 27 EA 77 04 BC ED 81 8E D7 C7 4A CB 38 CC 7F B7 E5 14 86 A7 92 E1 70 FC D8 ED 5A 92 DA 85 12 4F C7 87 37 F0 2C D8 D1 9D 78 74 CB F0 EF 60 74 F5 FC 15 E9 65 9F 85 56 85 83 1B BB F3 CD E7 04 1E 7E 17 83 CB C0 93 75 4F 05 BA 0E 05 1B 3A 2F 0C EE 1B 3F E2 0D B5 34 86 DA 1A 26 F7 36 54 71 D8 9D F3 5D 5D DA 68 74 F7 60 74 F2 C9 AB BF AD 01 57 0F 7F 9F E4 95 F9 C8 B4 CB FC 15 E9 97 A7 DA CE A4 CE 2A 79 BB BA 6A 7A 93 A2 E1 C1 5B 52 77 92 E5 C2 3B 7C 40 03 6E F4 09 E1 9D A8 90 09 EF BD 64 18 DF 60 32 1F E0 93 58 1F FE A4 70 CB 55 4F 9C 44 1B 93 F1 C7 D2 5A F0 F4 54 70 05 B7 3C 4E 68 5D 0E DB 9A 95 C5 9E 22 5A 87 48 0F 19 78 F8 14 81 97 38 5C C2 87 04 63 00 26 33 60 0C 46 56 72 9E 81 E7 19 36 0B B0 0C 0B 80 65 09 A1 F5 FC D5 6D 17 3E 40 81 E5 17 04 51 5C 92 97 E7 58 CF 9F D1 FD 34 B4 73 E3 44 47 7D B0 67 FB 44 C3 5B 81 EA 86 A0 B7 66 BC 7F 73 68 5B 7B C0 DF 60 79 9E 58 92 6B F3 38 D6 BA 56 3A 0A F2 57 96 14 39 75 D9 AB 3C 1E B7 45 AF F7 38 72 5D 85 25 B9 AE CC 7C 0F FC 28 D3 E6 D2 BB 4A 5E CD B7 DB F3 F2 EC 36 8F DE E1 B1 17 E8 59 BD CD E9 B0 BB 3C 99 F9 8E 3C CB 52 02 CF 75 7A 84 A0 B5 41 6F 0F FC 1F F0 DD F2 07 22 75 B5 5B 38 FB 22 7F E7 62 78 EB BF BD 92 B9 CE E1 CE 2C 2C CC B4 AD 2D D0 AF 63 F4 EE DC 7C 7B B1 70 FF 97 72 9D CE 5F AD FC CB 3A 2B F3 F3 DC 02 F7 2F 85 E0 5E 78 D5 0A C2 87 C2 7F 87 87 6C CE 07 D8 0A 18 99 9B CC 75 3A F2 5D 56 CA 06 E3 B0 17 51 CF 91 F0 B5 A2 B0 90 84 89 B2 52 D1 DC 51 A4 6D AD D3 4A D1 14 E9 2A B0 52 14 A9 17 BE 08 47 46 16 17 D9 AC D4 EC 06 4D 91 AB DD C2 B5 F3 35 2C 0A 8E C7 63 A5 C4 FC 51 7C A0 D1 F1 88 73 3E 7D 3C 1E 18 07 A0 22 A3 72 AD 2D 22 3D EB E1 6D 66 98 79 FE BB 2B F4 7C 06 9F FB 5D 55 E3 F1 6F 34 1C 76 7F 70 E3 AF FD 58 25 C4 7F 4D 16 FF D4 B7 18 B8 15 C1 7F 4C 0E 3F C7 E3 EF 89 E0 3F 28 C5 EF 13 F1 5F 9E 2D FE A5 12 FC 5A 13 9B 6D 30 18 0D 66 43 9C 7D 7D 8C FD 9F C1 0C 5C D8 C0 4D 6C F0 07 AA 6B 03 5B FA E1 F0 27 7C CD C1 3D 43 F0 7C 60 5F D7 C4 DE 1E CB 4F 08 22 B2 02 F8 0B B4 C1 8A 4A 3E F3 15 95 96 17 E3 96 86 65 66 25 C2 B7 05 25 B4 DE BE DE 5D 64 2F 2E 86 67 32 57 BB F3 E1 42 13 D7 81 75 2E 60 E8 9C 63 0A F0 13 53 9F 28 F9 99 06 33 77 F6 B4 10 8B 08 55 CC 7A 98 E5 83 81 88 B3 AC A8 3D 86 FA CC 13 2D 05 FF D5 40 F3 B5 54 0E 3B 3D D4 5E A7 F1 63 55 50 FC B0 AC F8 52 9F 06 DC 8D 88 9F 2C 97 11 5F C3 8B AF 88 88 DF 28 15 7F 31 69 76 E2 E9 6C 96 35 31 06 1A D0 D3 CA 7D 74 25 4C 27 9F 11 43 FE 31 7E C4 5D D5 A1 AD BB E0 5F 9C EE CA 6A 3E CF 95 D5 73 D3 6D FE A1 74 9B 73 0E 3E 4C 77 34 41 F3 A4 DB 3C EF BA CD D3 75 3F C8 7A 8C 6E E5 65 2A CF 5B 9C 54 A9 E4 A6 1B 3B 26 61 E9 AE EA DD 73 1D F5 63 D5 50 F2 78 B2 5C D7 77 9E 00 F7 22 90 0F CA 95 EE 37 79 C8 07 22 90 9B A5 90 BD 73 2F DD 31 90 25 C0 1F DE B7 04 FD 6F 04 1A 2B AE 0D 6C 0B 36 77 06 FA 5B 43 DE CD A1 32 0E E6 72 F1 55 ED B7 1F 99 AA AD BC 40 E5 5D 2B CD B1 94 F9 7F 83 F7 CB 92 38 AC EE 64 B8 60 F3 CC AF CA 32 1F 7D 02 6C E2 D4 0E 45 ED 50 16 7B 87 12 FA DF 7F CA 60 87 52 F7 E5 B1 2E AD 00 FE AE 6C 87 72 EA 04 0A BC 11 F1 A3 72 E2 8F F3 E2 87 22 E2 39 F5 81 54 7D 20 5D 04 0F A4 53 DF DE 3D 90 02 FB 73 DF D4 17 B0 AB A9 51 D4 1F 68 45 41 68 DF E8 82 EA 07 66 63 16 63 62 61 61 57 F5 7F 5F FD 62 EE 54 FD 33 E8 6F DB 7F E2 24 AC FD 17 CF 36 37 63 7E EC 9F 8A BF C7 34 0D A3 E0 62 44 FF 42 75 3B 51 FD 6A B7 93 70 B7 33 13 7B B5 DB 81 E2 CF FA 03 95 50 7C 7F C7 BF 8F E2 7E EC 7D C5 F6 FE 74 12 F0 1D 5E 58 F0 70 79 67 65 D1 2C C3 A8 E0 13 07 2F 26 4D 05 2F 0F BE AF FB DC 30 CA 61 7D 53 6D 47 53 FC D8 07 8A 25 7E E8 35 0D F8 E8 CA C2 8A 37 D1 34 63 64 0D 46 A0 8A 4F 5C BC 98 34 55 BC BC F8 DE ED 6F F7 26 71 D8 9E E1 E1 F3 B0 A5 FF 48 51 FC CD 33 18 38 AD 8A 57 C5 2F 7A F1 83 DB C6 7C 1A 0E 1B FD BA 76 E4 71 3F 36 0A C5 77 C8 36 35 FF F8 3D 38 D2 F6 69 18 FC 80 DC 1E D3 AD 24 08 FE EB 24 01 FC 1A 29 F8 8F 67 B9 C7 C4 18 69 B3 81 31 01 26 3B F6 19 D6 90 6D C8 CE 02 0C 30 31 71 E2 97 C4 88 47 E2 40 8B 0F 98 73 02 0D 7E 28 D0 20 A7 E9 A1 A0 C5 A4 CC 13 68 30 EF A0 C1 74 D0 D2 C7 52 71 20 D1 59 4C 40 B4 14 EC CD CB 2D 4E 0E 2B BF 79 96 EF 49 B6 D4 91 D4 29 D9 0A 7D EE B6 46 04 EB 95 AB D0 BB F9 0A FD 7A A4 42 D7 48 C1 4E CD 12 AC 04 66 EC 8F 2E 51 C8 D3 C1 92 31 60 75 FC 0A DD DF CB 6F 32 54 6D 85 39 BB 36 F0 DA E2 AB C9 DD 0F 23 AC 3C F3 8B AC 26 2B AF 45 85 AD D1 D8 A9 95 A2 FE 7B E9 C8 78 32 87 DD 38 32 D2 AE 15 54 77 CA AA EE E8 44 C1 97 11 D5 C7 E4 CA F0 6D BE 0C 5F 8F 94 61 A7 54 F5 C9 D9 6E F5 9B 58 93 89 36 19 4C B4 5A 86 A3 BB 87 62 52 1E D5 32 1C 1A 69 DD 92 CA 61 23 E5 93 6F 44 EA F0 9B B2 7D C3 78 1A 08 2D 2C 58 06 C0 C7 7A 90 C5 64 99 55 B0 92 5F F7 1E 24 E5 51 05 7B E9 52 DD 18 DF E8 1E EF D9 AF F1 63 5E 01 AC 4C 89 DD 54 8E 81 EB 0B DC E9 AA 62 55 B1 F1 62 3B BA 4B 21 D8 F7 EB FE F5 0E EC 09 B6 2A F6 04 9F F8 35 60 CF BB 0B 5B 62 59 9A 36 02 C6 90 C5 AA 60 25 BF 27 3F 48 CA A3 0A 76 43 E8 EA 25 D8 C5 4E DE 7F A7 19 F6 04 E5 75 4A 1B E2 C3 53 51 B1 0B B4 21 AE F8 6C 16 95 3C 5D EC D3 31 62 97 F1 BF AA 6C E4 2E 74 C2 1E 7E 67 D0 5B 1B F2 1E 1A 3F DA 34 B9 D7 B7 DD F2 42 9C 58 36 F1 9D 61 DA A8 B8 1F 3E DE DF 0D 67 07 BE 5B 5E 8E 2A 7E 69 2E F3 6F 88 DD 38 A6 8D 09 6D 88 CF F8 C0 36 CB 0D F1 98 61 CF 1D 76 DC C0 22 5B E2 62 0A A7 6D 89 2B 2E 55 C5 2D 71 F9 B9 97 EA 1F 6A DF 79 11 E5 B0 73 B5 1F 0E 0A FA 01 6A 42 73 90 E7 91 FF 03 32 BC E8 A3"
                .hexToBytes().inflate().toUHexString()

        println(result)
    }

    @Test
    fun testGzip() {
        val str = sampleLongText
//        val hash = str.toByteArray().gzip()
//        assertContentEquals(
//            "1F 8B 08 00 00 00 00 00 00 13 2B 74 CF F3 32 0C 1F 8B 08 00 00 00 00 00 00 13 2B 74 CF F3".hexToBytes(),
//            hash,
//            message = hash.toUHexString()
//        )

        assertEquals(str, str.toByteArray().gzip().ungzip().decodeToString())
    }

    @Test
    fun testUnGzip() {
        val result =
            "1F 8B 08 00 00 00 00 00 00 FF 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 A8 35 6D D9 0A 00 00 00".hexToBytes()
                .ungzip().decodeToString()
        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }


    ///////////////////////////////////////////////////////////////////////////
    // Input consume all
    ///////////////////////////////////////////////////////////////////////////


    @Test
    fun testInputAllUnGzip() {
        var released = false
        val result =
            "1F 8B 08 00 00 00 00 00 00 FF 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 A8 35 6D D9 0A 00 00 00".hexToBytes()
                .toReadPacket(release = { released = true }).let { input ->
                    input.ungzipAllAvailable().decodeToString().also {
                        assertEquals(true, input.endOfInput)
                        assertEquals(true, released)
                    }
                }

        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }

    @Test
    fun testInputAllGzip() {
        var released = false
        val result =
            sampleLongText.toByteArray()
                .toReadPacket(release = { released = true }).let { input ->
                    input.gzipAllAvailable().also {
                        assertEquals(true, input.endOfInput)
                        assertEquals(true, released)
                    }
                }
        println(result.toUHexString())
        assertEquals(sampleLongText, result.ungzip().decodeToString())
    }

    @Test
    fun testInputAllDeflate() {
        var released = false

        val str = "qGnJ1RrFC9"
        println(str)
        val hash = str.toByteArray().toReadPacket(release = { released = true }).let { input ->
            input.deflateAllAvailable().also {
                assertEquals(true, input.endOfInput)
                assertEquals(true, released)
            }
        }

        assertContentEquals( // if we change
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )

        assertEquals(str, hash.inflate().decodeToString())
    }

    @Test
    fun testInputAllInflate() {
        var released = false

        val result =
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes()
                .toReadPacket(release = { released = true }).let { input ->
                    input.inflateAllAvailable().also {
                        assertEquals(true, input.endOfInput)
                        assertEquals(true, released)
                    }
                }.decodeToString()

        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Input
    ///////////////////////////////////////////////////////////////////////////


    @Test
    fun testInputUnGzip() {
        var released = false
        val result =
            "1F 8B 08 00 00 00 00 00 00 FF 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 A8 35 6D D9 0A 00 00 00".hexToBytes()
                .toReadPacket(release = { released = true }).let { input ->
                    GzipDecompressionInput(input).readAllText().also {
                        assertEquals(true, input.endOfInput)
                        assertEquals(true, released)
                    }
                }

        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }

    // not supported on JVM
//    @Test
//    fun testInputGzip() {
//        var released = false
//        val result =
//            sampleLongText.toByteArray()
//                .toReadPacket(release = { released = true }).let { input ->
//                    GzipCompressionInput(input).readBytes().also {
//                        assertEquals(true, input.endOfInput)
//                        assertEquals(true, released)
//                    }
//                }
//        println(result.toUHexString())
//        assertEquals(sampleLongText, result.ungzip().decodeToString())
//    }

    @Test
    fun testInputDeflate() {
        var released = false

        val str = "qGnJ1RrFC9"
        println(str)
        val hash = str.toByteArray().toReadPacket(release = { released = true }).let { input ->
            DeflateInput(input).readBytes().also {
                assertEquals(true, input.endOfInput)
                assertEquals(true, released)
            }
        }

        assertContentEquals( // if we change
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )

        assertEquals(str, hash.inflate().decodeToString())
    }

    @Test
    fun testInputInflate() {
        var released = false

        val result =
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes()
                .toReadPacket(release = { released = true }).let { input ->
                    InflateInput(input).readAllText().also {
                        assertEquals(true, input.endOfInput)
                        assertEquals(true, released)
                    }
                }

        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }
}