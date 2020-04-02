@file:Suppress("unused", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.buildPacket
import kotlinx.io.core.toByteArray
import kotlinx.io.core.writeFully
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.*
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

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@Suppress("INVISIBLE_MEMBER") // bug
internal class JceInputTest {
    init {
        JceDecoder.debuggingMode = true
    }

    @Test
    fun testIntToStructMap() {
        @Serializable
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
                )*/
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