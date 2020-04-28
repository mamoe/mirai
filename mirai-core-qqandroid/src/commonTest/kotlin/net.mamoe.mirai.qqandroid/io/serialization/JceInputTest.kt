@file:Suppress("unused", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.buildPacket
import kotlinx.io.core.toByteArray
import kotlinx.io.core.writeFully
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.FileStoragePushFSSvcListFuckKotlin
import net.mamoe.mirai.qqandroid.utils.autoHexToBytes
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.*
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


internal const val BYTE: Byte = 0
internal const val DOUBLE: Byte = 5
internal const val FLOAT: Byte = 4
internal const val INT: Byte = 2
internal const val JCE_MAX_STRING_LENGTH = 104857600
internal const val LIST: Byte = 9
internal const val LONG: Byte = 3
internal const val MAP: Byte = 8
internal const val SHORT: Byte = 1
internal const val SIMPLE_LIST: Byte = 13
internal const val STRING1: Byte = 6
internal const val STRING4: Byte = 7
internal const val STRUCT_BEGIN: Byte = 10
internal const val STRUCT_END: Byte = 11
internal const val ZERO_TYPE: Byte = 12


@Suppress("INVISIBLE_MEMBER") // bug
internal class JceInputTest {
    init {
        JceDecoder.debuggingMode = true
    }


    @Test
    fun testConfigPush() {
        val data = """
            9A 
            09 00 0B 
                0A 
                    00 0F 
                    19 00 01 
                        0A 
                            12 71 19 A3 B4 
                            20 50 
                        0B 
                    29 0C 
                0B 
                0A 
                    00 04 
                    19 00 01 
                        0A 
                            12 0B 27 59 65 
                            20 50
                        0B 
                    29 0C 
                0B 
                0A 
                    00 0D 19 00 02 0A 12 55 31 BA DE 20 50 0B 0A 12 5B A0 6A 72 20 50 0B 29 0C 0B 0A 00 03 19 00 02 0A 12 C3 B9 D3 74 20 50 0B 0A 12 CC 43 E4 DD 20 50 0B 29 0C 0B 0A 00 07 19 00 01 0A 12 75 A2 E3 65 20 50 0B 29 0C 0B 0A 00 09 19 00 02 0A 12 BC 6C 24 B7 20 50 0B 0A 12 A6 6C 24 B7 20 50 0B 29 0C 0B 0A 00 0A 19 00 02 0A 12 11 B4 12 0E 20 50 0B 0A 12 15 8C D7 0E 20 50 0B 29 0C 0B 0A 00 05 19 00 01 0A 12 1D E2 03 B7 20 50 0B 29 0C 0B 0A 00 08 19 00 02 0A 12 DE 3F 5B 65 20 50 0B 0A 12 78 09 61 B4 20 50 0B 29 0C 0B 0A 00 06 19 00 02 0A 12 16 CF 97 3D 20 50 0B 0A 12 54 10 59 65 20 50 0B 29 0C 0B 0A 00 0E 19 00 02 0A 12 76 01 B1 6F 20 50 0B 0A 12 6B 89 31 3A 20 50 0B 29 0C 0B 0B 
            AD 00 01 01 5B 08 01 10 A4 F6 AA 16 18 00 22 0A 31 39 39 34 37 30 31 30 32 31 28 AB E1 89 EF 0E 32 12 08 8E A4 D8 A5 09 10 50 18 89 D8 AC F0 08 20 50 28 64 32 12 08 8E A4 C4 DD 08 10 50 18 89 F4 DE E0 05 20 50 28 64 32 13 08 B4 C7 DA B0 02 10 50 18 8A EE D4 F2 0D 20 50 28 C8 01 32 13 08 B4 C7 DA A0 02 10 50 18 8A EC D0 86 0E 20 50 28 C8 01 32 13 08 8C 9D 9B 85 07 10 50 18 89 D6 AD 9C 09 20 50 28 AC 02 32 13 08 B7 81 97 F6 06 10 50 18 8A EC D4 96 02 20 50 28 AC 02 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 29 10 50 18 89 EC 8C B1 05 20 50 28 64 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 64 10 50 18 89 EC 8C D1 07 20 50 28 64 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 6F 10 50 18 E4 E6 B1 F0 04 20 50 28 C8 01 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 72 10 50 18 E4 E6 AD F0 0E 20 50 28 C8 01 3A 1F 0A 10 24 09 8C 1E 75 B0 00 13 00 00 00 00 00 00 00 36 10 50 18 89 EC 9C E8 0D 20 50 28 AC 02 3A 1F 0A 10 24 09 8C 54 10 03 00 10 00 00 00 00 00 00 00 55 10 50 18 89 CA 8C A0 01 20 50 28 AC 02

                   """.trimIndent().autoHexToBytes()
        /*
        9A
            09 00 0B
                0A
                    00 0F
                    19 00 01
                        0A
                            12 71 19 A3 B4
                            20 50
                        0B
                    29 0C
                0B
                0A
                    00 04
                    19 00 01
                        0A
                            12 0B 27 59 65
                            20 50
                        0B
                    29 0C
                0B
                0A
                    00 0D 19 00 02 0A 12 55 31 BA DE 20 50 0B 0A 12 5B A0 6A 72 20 50 0B 29 0C 0B 0A 00 03 19 00 02 0A 12 C3 B9 D3 74 20 50 0B 0A 12 CC 43 E4 DD 20 50 0B 29 0C 0B 0A 00 07 19 00 01 0A 12 75 A2 E3 65 20 50 0B 29 0C 0B 0A 00 09 19 00 02 0A 12 BC 6C 24 B7 20 50 0B 0A 12 A6 6C 24 B7 20 50 0B 29 0C 0B 0A 00 0A 19 00 02 0A 12 11 B4 12 0E 20 50 0B 0A 12 15 8C D7 0E 20 50 0B 29 0C 0B 0A 00 05 19 00 01 0A 12 1D E2 03 B7 20 50 0B 29 0C 0B 0A 00 08 19 00 02 0A 12 DE 3F 5B 65 20 50 0B 0A 12 78 09 61 B4 20 50 0B 29 0C 0B 0A 00 06 19 00 02 0A 12 16 CF 97 3D 20 50 0B 0A 12 54 10 59 65 20 50 0B 29 0C 0B 0A 00 0E 19 00 02 0A 12 76 01 B1 6F 20 50 0B 0A 12 6B 89 31 3A 20 50 0B 29 0C 0B 0B
            AD 00 01 01 5B 08 01 10 A4 F6 AA 16 18 00 22 0A 31 39 39 34 37 30 31 30 32 31 28 AB E1 89 EF 0E 32 12 08 8E A4 D8 A5 09 10 50 18 89 D8 AC F0 08 20 50 28 64 32 12 08 8E A4 C4 DD 08 10 50 18 89 F4 DE E0 05 20 50 28 64 32 13 08 B4 C7 DA B0 02 10 50 18 8A EE D4 F2 0D 20 50 28 C8 01 32 13 08 B4 C7 DA A0 02 10 50 18 8A EC D0 86 0E 20 50 28 C8 01 32 13 08 8C 9D 9B 85 07 10 50 18 89 D6 AD 9C 09 20 50 28 AC 02 32 13 08 B7 81 97 F6 06 10 50 18 8A EC D4 96 02 20 50 28 AC 02 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 29 10 50 18 89 EC 8C B1 05 20 50 28 64 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 64 10 50 18 89 EC 8C D1 07 20 50 28 64 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 6F 10 50 18 E4 E6 B1 F0 04 20 50 28 C8 01 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 72 10 50 18 E4 E6 AD F0 0E 20 50 28 C8 01 3A 1F 0A 10 24 09 8C 1E 75 B0 00 13 00 00 00 00 00 00 00 36 10 50 18 89 EC 9C E8 0D 20 50 28 AC 02 3A 1F 0A 10 24 09 8C 54 10 03 00 10 00 00 00 00 00 00 00 55 10 50 18 89 CA 8C A0 01 20 50 28 AC 02

         */
        /*

             39 00 06
             0A
                16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 31
                20 50
             0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 34 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 35 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 35 32 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 35 33 20 50 0B 0A 16 11 73 63 61 6E 6E 6F 6E 2E 33 67 2E 71 71 2E 63 6F 6D 20 50 0B 49 00 04 0A 16 0E 31 31 33 2E 39 36 2E 32 33 32 2E 31 30 38 20 50 0B 0A 16 0D 31 38 33 2E 33 2E 32 33 33 2E 32 32 35 21 1F 90 0B 0A 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 21 01 BB 0B 0A 16 0D 31 38 30 2E 31 36 33 2E 32 35 2E 33 38 20 50 0B 5A 09 00 03 0A 00 01 19 00 04 0A 00 01 16 0E 31 31 33 2E 39 36 2E 32 33 32 2E 31 30 38 20 50 0B 0A 00 01 16 0D 31 38 33 2E 33 2E 32 33 33 2E 32 32 35 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 21 01 BB 0B 0A 00 01 16 0D 31 38 30 2E 31 36 33 2E 32 35 2E 33 38 20 50 0B 29 0C 3C 0B 0A 00 05 19 00 04 0A 00 01 16 0E 31 31 33 2E 39 36 2E 32 33 32 2E 31 30 38 20 50 0B 0A 00 01 16 0D 31 38 33 2E 33 2E 32 33 33 2E 32 32 35 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 21 01 BB 0B 0A 00 01 16 0D 31 38 30 2E 31 36 33 2E 32 35 2E 33 38 20 50 0B 29 0C 3C 0B 0A 00 0A 19 00 04 0A 00 01 16 0E 31 31 33 2E 39 36 2E 32 33 32 2E 31 30 38 20 50 0B 0A 00 01 16 0D 31 38 33 2E 33 2E 32 33 33 2E 32 32 35 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 21 01 BB 0B 0A 00 01 16 0D 31 38 30 2E 31 36 33 2E 32 35 2E 33 38 20 50 0B 29 00 05 0A 0C 11 20 00 20 10 30 01 0B 0A 00 01 11 20 00 20 08 30 02 0B 0A 00 02 11 20 00 20 08 30 01 0B 0A 00 03 12 00 01 00 00 20 08 30 02 0B 0A 00 04 11 20 00 20 08 30 02 0B 3C 0B 1D 00 00 68 CA 62 F1 01 C2 AF E6 CF 29 4B 18 71 B5 EE 6B 63 EB F0 0B AB EE A0 5C 20 B9 83 E2 52 F7 BF C7 46 80 BC C3 7F 22 6B 6E 23 42 D0 8F C8 6A C4 F4 49 AA E7 94 EF D4 80 0A E4 8B BF E2 C0 4F FC C5 3F 97 1A E8 0F 0F 7D 06 47 62 C3 C8 07 4F E6 F6 E9 DB CB 4C F5 95 6A AD EC FD D0 46 A5 16 8D 30 02 D5 8A 86 2E 5F E8 D6 8C 2D 00 00 10 33 6E 59 70 73 47 38 52 6E 48 6A 64 51 48 46 54 32 76 E4 B8 DD 40 01 5D 00 01 02 54 8A 50 D0 04 0A 68 CA 62 F1 01 C2 AF E6 CF 29 4B 18 71 B5 EE 6B 63 EB F0 0B AB EE A0 5C 20 B9 83 E2 52 F7 BF C7 46 80 BC C3 7F 22 6B 6E 23 42 D0 8F C8 6A C4 F4 49 AA E7 94 EF D4 80 0A E4 8B BF E2 C0 4F FC C5 3F 97 1A E8 0F 0F 7D 06 47 62 C3 C8 07 4F E6 F6 E9 DB CB 4C F5 95 6A AD EC FD D0 46 A5 16 8D 30 02 D5 8A 86 2E 5F E8 D6 8C 12 10 33 6E 59 70 73 47 38 52 6E 48 6A 64 51 48 46 54 1A 40 08 01 12 0D 08 01 15 71 60 E8 6C 18 50 20 01 28 01 12 0E 08 01 15 B7 03 E9 E1 18 90 3F 20 01 28 01 12 0E 08 01 15 7B 96 4C A8 18 BB 03 20 02 28 00 12 0D 08 01 15 B4 A3 19 26 18 50 20 04 28 00 1A 40 08 05 12 0D 08 01 15 71 60 E8 6C 18 50 20 01 28 01 12 0E 08 01 15 B7 03 E9 E1 18 90 3F 20 01 28 01 12 0E 08 01 15 7B 96 4C A8 18 BB 03 20 02 28 00 12 0D 08 01 15 B4 A3 19 26 18 50 20 04 28 00 1A 78 08 0A 12 0D 08 01 15 71 60 E8 6C 18 50 20 01 28 01 12 0E 08 01 15 B7 03 E9 E1 18 90 3F 20 01 28 01 12 0E 08 01 15 7B 96 4C A8 18 BB 03 20 02 28 00 12 0D 08 01 15 B4 A3 19 26 18 50 20 04 28 00 22 09 08 00 10 80 40 18 10 20 01 22 09 08 01 10 80 40 18 08 20 02 22 09 08 02 10 80 40 18 08 20 01 22 0A 08 03 10 80 80 04 18 08 20 02 22 09 08 04 10 80 40 18 08 20 02 20 01 32 04 08 00 10 01 3A 2A 08 10 10 10 18 09 20 09 28 0F 30 0F 38 05 40 05 48 5A 50 01 58 5A 60 5A 68 5A 70 5A 78 0A 80 01 0A 88 01 0A 90 01 0A 98 01 0A 42 0A 08 00 10 00 18 00 20 00 28 00 4A 06 08 01 10 01 18 03 52 42 08 01 12 0A 08 00 10 80 80 04 18 10 20 02 12 0A 08 01 10 80 80 04 18 08 20 02 12 0A 08 02 10 80 80 01 18 08 20 01 12 0A 08 03 10 80 80 04 18 08 20 02 12 0A 08 04 10 80 80 04 18 08 20 02 18 00 20 00 5A 40 08 01 12 0A 08 00 10 80 80 04 18 10 20 02 12 0A 08 01 10 80 80 04 18 08 20 02 12 0A 08 02 10 80 80 01 18 08 20 01 12 0A 08 03 10 80 80 04 18 08 20 02 12 0A 08 04 10 80 80 04 18 08 20 02 18 00 70 02 78 02 80 01 FA 01 0B 69 00 01 0A 16 26 69 6D 67 63 61 63 68 65 2E 71 71 2E 63 6F 6D 2E 73 63 68 65 64 2E 70 31 76 36 2E 74 64 6E 73 76 36 2E 63 6F 6D 2E 20 50 0B 79 00 02 0A 16 0E 31 30 31 2E 32 32 37 2E 31 33 31 2E 36 37 20 50 0B 0A 16 0D 36 31 2E 31 35 31 2E 31 38 33 2E 32 31 20 50 0B 8A 06 0F 31 37 31 2E 31 31 32 2E 32 32 36 2E 32 33 37 10 03 0B 9A 09 00 0B 0A 00 0F 19 00 01 0A 12 71 19 A3 B4 20 50 0B 29 0C 0B 0A 00 04 19 00 01 0A 12 0B 27 59 65 20 50 0B 29 0C 0B 0A 00 0D 19 00 02 0A 12 55 31 BA DE 20 50 0B 0A 12 5B A0 6A 72 20 50 0B 29 0C 0B 0A 00 03 19 00 02 0A 12 C3 B9 D3 74 20 50 0B 0A 12 CC 43 E4 DD 20 50 0B 29 0C 0B 0A 00 07 19 00 01 0A 12 75 A2 E3 65 20 50 0B 29 0C 0B 0A 00 09 19 00 02 0A 12 BC 6C 24 B7 20 50 0B 0A 12 A6 6C 24 B7 20 50 0B 29 0C 0B 0A 00 0A 19 00 02 0A 12 11 B4 12 0E 20 50 0B 0A 12 15 8C D7 0E 20 50 0B 29 0C 0B 0A 00 05 19 00 01 0A 12 1D E2 03 B7 20 50 0B 29 0C 0B 0A 00 08 19 00 02 0A 12 DE 3F 5B 65 20 50 0B 0A 12 78 09 61 B4 20 50 0B 29 0C 0B 0A 00 06 19 00 02 0A 12 16 CF 97 3D 20 50 0B 0A 12 54 10 59 65 20 50 0B 29 0C 0B 0A 00 0E 19 00 02 0A 12 76 01 B1 6F 20 50 0B 0A 12 6B 89 31 3A 20 50 0B 29 0C 0B 0B AD 00 01 01 5B 08 01 10 A4 F6 AA 16 18 00 22 0A 31 39 39 34 37 30 31 30 32 31 28 AB E1 89 EF 0E 32 12 08 8E A4 D8 A5 09 10 50 18 89 D8 AC F0 08 20 50 28 64 32 12 08 8E A4 C4 DD 08 10 50 18 89 F4 DE E0 05 20 50 28 64 32 13 08 B4 C7 DA B0 02 10 50 18 8A EE D4 F2 0D 20 50 28 C8 01 32 13 08 B4 C7 DA A0 02 10 50 18 8A EC D0 86 0E 20 50 28 C8 01 32 13 08 8C 9D 9B 85 07 10 50 18 89 D6 AD 9C 09 20 50 28 AC 02 32 13 08 B7 81 97 F6 06 10 50 18 8A EC D4 96 02 20 50 28 AC 02 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 29 10 50 18 89 EC 8C B1 05 20 50 28 64 3A 1E 0A 10 24 0E 00 E1 A9 00 00 50 00 00 00 00 00 00 00 64 10 50 18 89 EC 8C D1 07 20 50 28 64 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 6F 10 50 18 E4 E6 B1 F0 04 20 50 28 C8 01 3A 1F 0A 10 24 0E 00 FF F1 01 00 10 00 00 00 00 00 00 01 72 10 50 18 E4 E6 AD F0 0E 20 50 28 C8 01 3A 1F 0A 10 24 09 8C 1E 75 B0 00 13 00 00 00 00 00 00 00 36 10 50 18 89 EC 9C E8 0D 20 50 28 AC 02 3A 1F 0A 10 24 09 8C 54 10 03 00 10 00 00 00 00 00 00 00 55 10 50 18 89 CA 8C A0 01 20 50 28 AC 02

         */

        data.loadAs(FileStoragePushFSSvcListFuckKotlin.serializer())
    }

    @Test
    fun testIntToStructMap() {
        @kotlinx.serialization.Serializable
        data class VipOpenInfo(
            @JceId(0) val open: Boolean? = false,
            @JceId(1) val iVipType: Int = -1,
            @JceId(2) val iVipLevel: Int = -1,
            @JceId(3) val iVipFlag: Int? = null,
            @JceId(4) val nameplateId: Long? = null
        ) : JceStruct

        @Serializable
        data class VipBaseInfo(
            @JceId(0) val mOpenInfo: Map<Int, VipOpenInfo>? = null
        ) : JceStruct


        @Serializable
        data class FriendInfo(
            @JceId(0) val friendUin: Long,
            @JceId(14) val nick: String = "",
            @JceId(19) val oVipInfo: VipBaseInfo? = null, //? bad
            @JceId(20) val network: Byte? = null
        ) : JceStruct

        val value = FriendInfo(
            friendUin = 123,
            nick = "h",
            oVipInfo = VipBaseInfo(
                mapOf(
                    1 to VipOpenInfo(true, -1),
                    999999999 to VipOpenInfo(true, -1)
                )
            ),
            network = 1
        )
        assertEquals(
            value.toString(),
            Jce.UTF_8.load(FriendInfo.serializer(), value.toByteArray(FriendInfo.serializer())).toString()
        )
    }

    @Test
    fun testSkippingMap() {
        @Serializable
        data class TestSerializableClassC(
            @JceId(5) val value3: Int = 123123
        )

        @Serializable
        data class TestSerializableClassB(
            @JceId(0) val value: Int,
            @JceId(123) val nested2: TestSerializableClassC,
            @JceId(5) val value5: Int
        )

        @Serializable
        data class TestSerializableClassA(
            //@JceId(0) val map: Map<TestSerializableClassB, TestSerializableClassC>
            @JceId(1) val optional: String = ""
        )


        val input = buildPacket {
            writeJceHead(MAP, 0) // TestSerializableClassB
            writeJceHead(BYTE, 0)
            writeByte(1);

            {
                writeJceHead(STRUCT_BEGIN, 0);
                {
                    writeJceHead(INT, 0)
                    writeInt(123)

                    writeJceHead(STRUCT_BEGIN, 123); // TestSerializableClassC
                    {
                        writeJceHead(INT, 5)
                        writeInt(123123)
                    }()
                    writeJceHead(STRUCT_END, 0)

                    writeJceHead(INT, 5)
                    writeInt(9)
                }()
                writeJceHead(STRUCT_END, 0)

                writeJceHead(STRUCT_BEGIN, 1);
                {
                    writeJceHead(INT, 5)
                    writeInt(123123)
                }()
                writeJceHead(STRUCT_END, 0)
            }()

            writeJceHead(STRING1, 1)
            writeByte(1)
            writeStringUtf8("1")
        }

        assertEquals(
            TestSerializableClassA(
                /*mapOf(
                    TestSerializableClassB(123, TestSerializableClassC(123123), 9)
                            to TestSerializableClassC(123123)
                ),*/

                "1"
            ),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input)
        )
    }

    @Test
    fun testFuckingComprehensiveStruct() {
        @Serializable
        data class TestSerializableClassC(
            @JceId(5) val value3: Int = 123123
        )

        @Serializable
        data class TestSerializableClassB(
            @JceId(0) val value: Int,
            @JceId(123) val nested2: TestSerializableClassC,
            @JceId(5) val value5: Int
        )

        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val map: Map<TestSerializableClassB, TestSerializableClassC>
        )


        val input = buildPacket {
            writeJceHead(MAP, 0) // TestSerializableClassB
            writeJceHead(BYTE, 0)
            writeByte(1)

            writeJceHead(STRUCT_BEGIN, 0);
            {
                writeJceHead(INT, 0)
                writeInt(123)

                writeJceHead(STRUCT_BEGIN, 123); // TestSerializableClassC
                {
                    writeJceHead(INT, 5)
                    writeInt(123123)
                }()
                writeJceHead(STRUCT_END, 0)

                writeJceHead(INT, 5)
                writeInt(9)
            }()
            writeJceHead(STRUCT_END, 0)

            writeJceHead(STRUCT_BEGIN, 1);
            {
                writeJceHead(INT, 5)
                writeInt(123123)
            }()
            writeJceHead(STRUCT_END, 0)
        }

        assertEquals(
            TestSerializableClassA(
                mapOf(
                    TestSerializableClassB(123, TestSerializableClassC(123123), 9)
                            to TestSerializableClassC(123123)
                )
            ),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input)
        )
    }

    @Test
    fun testNestedJceStruct() {
        @Serializable
        data class TestSerializableClassC(
            @JceId(5) val value3: Int
        )

        @Serializable
        data class TestSerializableClassB(
            @JceId(0) val value: Int,
            @JceId(123) val nested2: TestSerializableClassC,
            @JceId(5) val value5: Int
        )

        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val value1: Int,
            @JceId(4) val notOptional: Int,
            @JceId(1) val nestedStruct: TestSerializableClassB,
            @JceId(2) val optional: Int = 3
        )

        val input = buildPacket {
            writeJceHead(INT, 0)
            writeInt(444)

            writeJceHead(STRUCT_BEGIN, 1); // TestSerializableClassB
            {
                writeJceHead(INT, 0)
                writeInt(123)

                writeJceHead(STRUCT_BEGIN, 123); // TestSerializableClassC
                {
                    writeJceHead(INT, 5)
                    writeInt(123123)
                }()
                writeJceHead(STRUCT_END, 0)

                writeJceHead(INT, 5)
                writeInt(9)
            }()
            writeJceHead(STRUCT_END, 0)

            writeJceHead(INT, 4)
            writeInt(5)
        }

        assertEquals(
            TestSerializableClassA(
                444,
                5,
                TestSerializableClassB(123, TestSerializableClassC(123123), 9)
            ),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input)
        )
    }

    @Test
    fun testNestedList() {
        @Serializable
        data class TestSerializableClassA(
            // @JceId(0) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
            @JceId(3) val byteArray2: List<List<Int>> = listOf(listOf(1, 2, 3, 4), listOf(1, 2, 3, 4))
        )

        val input = buildPacket {
            //writeJceHead(SIMPLE_LIST, 0)
            //writeJceHead(BYTE, 0)

            //writeJceHead(BYTE, 0)
            //byteArrayOf(1, 2, 3).let {
            //    writeByte(it.size.toByte())
            //    writeFully(it)
            //}

            writeJceHead(LIST, 3)

            writeJceHead(BYTE, 0)
            writeByte(2)
            listOf(listOf(1, 2, 3, 4), listOf(1, 2, 3, 4)).forEach { child ->
                writeJceHead(LIST, 0)

                writeJceHead(BYTE, 0)
                writeByte(child.size.toByte())

                child.forEach {
                    writeJceHead(INT, 0)
                    writeInt(it)
                }
            }
        }

        assertEquals(TestSerializableClassA(), Jce.UTF_8.load(TestSerializableClassA.serializer(), input))
    }

    @Test
    fun testMap() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val byteArray: Map<Int, Int>
        )

        val input = buildPacket {
            writeJceHead(MAP, 0)

            mapOf(1 to 2, 33 to 44).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())

                it.forEach { (key, value) ->
                    writeJceHead(INT, 0)
                    writeInt(key)

                    writeJceHead(INT, 1)
                    writeInt(value)
                }
            }

            writeJceHead(SIMPLE_LIST, 3)
            writeJceHead(BYTE, 0)

            byteArrayOf(1, 2, 3, 4).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())
                writeFully(it)
            }
        }

        assertEquals(
            TestSerializableClassA(mapOf(1 to 2, 33 to 44)),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input)
        )
    }

    @Test
    fun testMapStringInt() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val byteArray: Map<String, Int>
        )

        val input = buildPacket {
            writeJceHead(MAP, 0)

            mapOf("str1" to 2, "str2" to 44).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())

                it.forEach { (key, value) ->
                    writeJceHead(STRING1, 0)
                    writeByte(key.length.toByte())
                    writeStringUtf8(key)

                    writeJceHead(INT, 1)
                    writeInt(value)
                }
            }
        }

        assertEquals(
            TestSerializableClassA(mapOf("str1" to 2, "str2" to 44)),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input)
        )
    }

    @Test
    fun testMapStringByteArray() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val map: Map<String, ByteArray>
        ) {
            override fun toString(): String {
                @Suppress("EXPERIMENTAL_API_USAGE")
                return map.entries.joinToString { "${it.key}=${it.value.contentToString()}" }
            }
        }

        val input = buildPacket {
            writeJceHead(MAP, 0)

            mapOf("str1" to byteArrayOf(2, 3, 4), "str2" to byteArrayOf(2, 3, 4)).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())

                it.forEach { (key, value) ->
                    writeJceHead(STRING1, 0)
                    writeByte(key.length.toByte())
                    writeFully(key.toByteArray())

                    writeJceHead(SIMPLE_LIST, 1)
                    writeJceHead(BYTE, 0)
                    writeJceHead(INT, 0)
                    writeInt(value.size)
                    writeFully(value)
                }
            }
        }

        assertEquals(
            TestSerializableClassA(mapOf("str1" to byteArrayOf(2, 3, 4), "str2" to byteArrayOf(2, 3, 4))).toString(),
            Jce.UTF_8.load(TestSerializableClassA.serializer(), input).toString()
        )
    }

    @Test
    fun testSimpleByteArray() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
            @JceId(3) val byteArray2: List<Byte> = listOf(1, 2, 3, 4)
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as TestSerializableClassA

                if (!byteArray.contentEquals(other.byteArray)) return false
                if (byteArray2 != other.byteArray2) return false

                return true
            }

            override fun hashCode(): Int {
                var result = byteArray.contentHashCode()
                result = 31 * result + byteArray2.hashCode()
                return result
            }
        }

        val input = buildPacket {
            writeJceHead(SIMPLE_LIST, 0)
            writeJceHead(BYTE, 0)

            byteArrayOf(1, 2, 3).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())
                writeFully(it)
            }

            writeJceHead(SIMPLE_LIST, 3)
            writeJceHead(BYTE, 0)

            byteArrayOf(1, 2, 3, 4).let {
                writeJceHead(BYTE, 0)
                writeByte(it.size.toByte())
                writeFully(it)
            }
        }

        assertEquals(TestSerializableClassA(), Jce.UTF_8.load(TestSerializableClassA.serializer(), input))
    }


    @Test
    fun testSerializableClassA() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val byte: Byte = 66,
            @JceId(1) val short: Short = 123,
            @JceId(3) val int: Int = 123456,
            @JceId(8) val float: Float = 123f,
            @JceId(15) val long: Long = 123456789123456789L,
            @JceId(16) val double: Double = 123456.0,
            @JceId(17) val boolean: Boolean = true,
            @JceId(11111) val nullable: Int? = null,
            @JceId(111112) val nullable2: Int? = null,
            @JceId(111113) val optional: Int = 123
        )

        val input = buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
            writeJceHead(INT, 3)
            writeInt(123456)
            writeJceHead(FLOAT, 8)
            writeFloat(123f)
            writeJceHead(LONG, 15)
            writeLong(123456789123456789L)
            writeJceHead(DOUBLE, 16)
            writeDouble(123456.0)
            writeJceHead(BYTE, 17)
            writeByte(1) // boolean
        }

        assertEquals(TestSerializableClassA(), Jce.UTF_8.load(TestSerializableClassA.serializer(), input))
    }

    @Test
    fun testNoSuchField() {
        @Serializable
        data class TestSerializableClassA(
            @JceId(0) val byte: Byte = 66,
            @JceId(1) val short: Short = 123,
            @JceId(3) val int: Int
        )

        val input = buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
        }

        assertFailsWith<MissingFieldException> { Jce.UTF_8.load(TestSerializableClassA.serializer(), input) }
    }

    @Test
    fun testHeadSkip() {
        val input = JceInput(buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
            writeJceHead(INT, 3)
            writeInt(123456)
            writeJceHead(FLOAT, 8)
            writeFloat(123f)
            writeJceHead(LONG, 15)
            writeLong(123456789123456789L)
            writeJceHead(DOUBLE, 16)
            writeDouble(123456.0)
            writeJceHead(BYTE, 17)
            writeByte(1) // boolean
        }, JceCharset.UTF8)

        assertEquals(123456, input.skipToHeadAndUseIfPossibleOrFail(3) { input.readJceIntValue(it) })

        assertEquals(true, input.skipToHeadAndUseIfPossibleOrFail(17) { input.readJceBooleanValue(it) })

        assertFailsWith<IllegalStateException> {
            input.skipToHeadAndUseIfPossibleOrFail(18) {
                error("test failed")
            }
        }
    }

    @Test
    fun testReadPrimitive() {
        val input = JceInput(buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
            writeJceHead(INT, 3)
            writeInt(123456)
            writeJceHead(FLOAT, 8)
            writeFloat(123f)
            writeJceHead(LONG, 15)
            writeLong(123456789123456789L)
            writeJceHead(DOUBLE, 16)
            writeDouble(123456.0)
            writeJceHead(BYTE, 17)
            writeByte(1) // boolean
        }, JceCharset.UTF8)
        assertEquals(66, input.useHead { input.readJceByteValue(it) })
        assertEquals(123, input.useHead { input.readJceShortValue(it) })
        assertEquals(123456, input.useHead { input.readJceIntValue(it) })
        assertEquals(123f, input.useHead { input.readJceFloatValue(it) })
        assertEquals(123456789123456789, input.useHead { input.readJceLongValue(it) })
        assertEquals(123456.0, input.useHead { input.readJceDoubleValue(it) })
        assertEquals(true, input.useHead { input.readJceBooleanValue(it) })
    }
}