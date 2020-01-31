package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.readBytes
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceOutput
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.buildJcePacket
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.test.Test
import kotlin.test.assertEquals

class JceDecoderTest {
    @Serializable
    data class TestSimpleJceStruct(
        @SerialId(0) val string: String = "123",
        @SerialId(1) val byte: Byte = 123,
        @SerialId(2) val short: Short = 123,
        @SerialId(3) val int: Int = 123,
        @SerialId(4) val long: Long = 123,
        @SerialId(5) val float: Float = 123f,
        @SerialId(6) val double: Double = 123.0,
        @SerialId(7) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
        @SerialId(8) val byteArray2: ByteArray = byteArrayOf(1, 2, 3)
    ) : JceStruct {
        override fun writeTo(output: JceOutput) = output.run {
            writeString(string, 0)
            writeByte(byte, 1)
            writeShort(short, 2)
            writeInt(int, 3)
            writeLong(long, 4)
            writeFloat(float, 5)
            writeDouble(double, 6)
            writeFully(byteArray, 7)
            writeFully(byteArray2, 8)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestSimpleJceStruct

            if (string != other.string) return false
            if (byte != other.byte) return false
            if (short != other.short) return false
            if (int != other.int) return false
            if (long != other.long) return false
            if (float != other.float) return false
            if (double != other.double) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
            if (!byteArray2.contentEquals(other.byteArray2)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = string.hashCode()
            result = 31 * result + byte
            result = 31 * result + short
            result = 31 * result + int
            result = 31 * result + long.hashCode()
            result = 31 * result + float.hashCode()
            result = 31 * result + double.hashCode()
            result = 31 * result + byteArray.contentHashCode()
            result = 31 * result + byteArray2.contentHashCode()
            return result
        }
    }


    @Test
    fun testByteArray() {

        @Serializable
        data class TestByteArray(
            @SerialId(0) val byteArray: ByteArray = byteArrayOf(1, 2, 3)
        ) : JceStruct {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as TestByteArray

                if (!byteArray.contentEquals(other.byteArray)) return false

                return true
            }

            override fun hashCode(): Int {
                return byteArray.contentHashCode()
            }
        }
        assertEquals(
            TestByteArray(),
            TestByteArray().toByteArray(TestByteArray.serializer()).loadAs(TestByteArray.serializer())
        )
    }

    @Test
    fun testSimpleStruct() {
        assertEquals(
            TestSimpleJceStruct(),
            TestSimpleJceStruct().toByteArray(TestSimpleJceStruct.serializer()).loadAs(TestSimpleJceStruct.serializer())
        )
    }


    @Serializable
    class TestComplexJceStruct(
        @SerialId(6) val string: String = "haha",
        @SerialId(7) val byteArray: ByteArray = ByteArray(500),
        @SerialId(8) val byteList: List<Long> = listOf(1, 2, 3), // error here
        @SerialId(9) val map: Map<String, Map<String, ByteArray>> = mapOf("哈哈" to mapOf("哈哈" to byteArrayOf(1, 2, 3))),
        //   @SerialId(10) val nestedJceStruct: TestSimpleJceStruct = TestSimpleJceStruct(),
        @SerialId(11) val byteList2: List<List<Int>> = listOf(listOf(1, 2, 3), listOf(1, 2, 3))
    ) : JceStruct

    @Serializable
    class TestComplexNullableJceStruct(
        @SerialId(6) val string: String = "haha",
        @SerialId(7) val byteArray: ByteArray = ByteArray(2000),
        @SerialId(8) val byteList: List<Long>? = listOf(1, 2, 3), // error here
        @SerialId(9) val map: Map<String, Map<String, ByteArray>>? = mapOf("哈哈" to mapOf("哈哈" to byteArrayOf(1, 2, 3))),
        @SerialId(10) val nestedJceStruct: TestComplexJceStruct? = TestComplexJceStruct(),
        @SerialId(11) val byteList2: List<List<Int>>? = listOf(listOf(1, 2, 3), listOf(1, 2, 3))
    ) : JceStruct

    @Test
    fun testEncoder() {
        println(
            TestComplexJceStruct().toByteArray(TestComplexJceStruct.serializer()).loadAs(
                TestComplexNullableJceStruct.serializer(),
                JceCharset.GBK
            ).contentToString()
        )
    }

    @Test
    fun testEncoder3() {
        println(
            TestComplexNullableJceStruct().toByteArray(TestComplexNullableJceStruct.serializer()).loadAs(
                TestComplexNullableJceStruct.serializer(),
                JceCharset.GBK
            ).contentToString()
        )
    }

    @Test
    fun testNestedList() {
        @Serializable
        data class TestNestedList(
            @SerialId(7) val array: List<List<Int>> = listOf(listOf(1, 2, 3), listOf(1, 2, 3), listOf(1, 2, 3))
        ) : JceStruct

        println(buildJcePacket {
            writeCollection(listOf(listOf(1, 2, 3), listOf(1, 2, 3), listOf(1, 2, 3)), 7)
        }.readBytes().loadAs(TestNestedList.serializer()).contentToString())
    }

    @Test
    fun testNestedArray() {
        @Serializable
        class TestNestedArray(
            @SerialId(7) val array: Array<Array<Int>> = arrayOf(arrayOf(1, 2, 3), arrayOf(1, 2, 3), arrayOf(1, 2, 3))
        ) : JceStruct

        println(buildJcePacket {
            writeFully(arrayOf(arrayOf(1, 2, 3), arrayOf(1, 2, 3), arrayOf(1, 2, 3)), 7)
        }.readBytes().loadAs(TestNestedArray.serializer()).contentToString())
    }

    @Test
    fun testSimpleMap() {

        @Serializable
        data class TestSimpleMap(
            @SerialId(7) val map: Map<String, Long> = mapOf("byteArrayOf(1)" to 2222L)
        ) : JceStruct
        assertEquals(buildJcePacket {
            writeMap(mapOf("byteArrayOf(1)" to 2222), 7)
        }.readBytes().loadAs(TestSimpleMap.serializer()).toString(), TestSimpleMap().toString())
    }

    @Test
    fun testSimpleList() {

        @Serializable
        data class TestSimpleList(
            @SerialId(7) val list: List<String> = listOf("asd", "asdasdasd")
        ) : JceStruct
        assertEquals(buildJcePacket {
            writeCollection(listOf("asd", "asdasdasd"), 7)
        }.readBytes().loadAs(TestSimpleList.serializer()).toString(), TestSimpleList().toString())
    }

    @Test
    fun testNestedMap() {
        @Serializable
        class TestNestedMap(
            @SerialId(7) val map: Map<ByteArray, Map<ByteArray, ShortArray>> = mapOf(byteArrayOf(1) to mapOf(byteArrayOf(1) to shortArrayOf(2)))
        ) : JceStruct
        assertEquals(buildJcePacket {
            writeMap(mapOf(byteArrayOf(1) to mapOf(byteArrayOf(1) to shortArrayOf(2))), 7)
        }.readBytes().loadAs(TestNestedMap.serializer()).map.entries.first().value.contentToString(), "{01=[0x0002(2)]}")
    }

    @Test
    fun testMap3() {
        @Serializable
        class TestNestedMap(
            @SerialId(7) val map: Map<Byte, ShortArray> = mapOf(1.toByte() to shortArrayOf(2))
        ) : JceStruct
        assertEquals("{0x01(1)=[0x0002(2)]}", buildJcePacket {
            writeMap(mapOf(1.toByte() to shortArrayOf(2)), 7)
        }.readBytes().loadAs(TestNestedMap.serializer()).map.contentToString())
    }

    @Test
    fun testNestedMap2() {
        @Serializable
        class TestNestedMap(
            @SerialId(7) val map: Map<Int, Map<Byte, ShortArray>> = mapOf(1 to mapOf(1.toByte() to shortArrayOf(2)))
        ) : JceStruct
        assertEquals(buildJcePacket {
            writeMap(mapOf(1 to mapOf(1.toByte() to shortArrayOf(2))), 7)
        }.readBytes().loadAs(TestNestedMap.serializer()).map.entries.first().value.contentToString(), "{0x01(1)=[0x0002(2)]}")
    }


    @Test
    fun testNullableEncode() {
        @Serializable
        data class AllNullJce(
            @SerialId(6) val string: String? = null,
            @SerialId(7) val byteArray: ByteArray? = null,
            @SerialId(8) val byteList: List<Long>? = null,
            @SerialId(9) val map: Map<String, Map<String, ByteArray>>? = null,
            @SerialId(10) val nestedJceStruct: TestComplexJceStruct? = null,
            @SerialId(11) val byteList2: List<List<Int>>? = null
        ) : JceStruct {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as AllNullJce

                if (string != other.string) return false
                if (byteArray != null) {
                    if (other.byteArray == null) return false
                    if (!byteArray.contentEquals(other.byteArray)) return false
                } else if (other.byteArray != null) return false
                if (byteList != other.byteList) return false
                if (map != other.map) return false
                if (nestedJceStruct != other.nestedJceStruct) return false
                if (byteList2 != other.byteList2) return false

                return true
            }

            override fun hashCode(): Int {
                var result = string?.hashCode() ?: 0
                result = 31 * result + (byteArray?.contentHashCode() ?: 0)
                result = 31 * result + (byteList?.hashCode() ?: 0)
                result = 31 * result + (map?.hashCode() ?: 0)
                result = 31 * result + (nestedJceStruct?.hashCode() ?: 0)
                result = 31 * result + (byteList2?.hashCode() ?: 0)
                return result
            }
        }

        assert(AllNullJce().toByteArray(AllNullJce.serializer()).isEmpty())
        assertEquals(ByteArray(0).loadAs(AllNullJce.serializer()), AllNullJce())
    }

    @Test
    fun testNestedStruct() {
        @Serializable
        data class OuterStruct(
            @SerialId(0) val innerStructList: List<TestSimpleJceStruct>
        ) : JceStruct

        println(buildJcePacket {
            writeCollection(listOf(TestSimpleJceStruct(), TestSimpleJceStruct()), 0)
        }.readBytes().loadAs(OuterStruct.serializer()).innerStructList.toString())
        assertEquals(
            buildJcePacket {
                writeCollection(listOf(TestSimpleJceStruct(), TestSimpleJceStruct()), 0)
            }.readBytes().toUHexString(),
            OuterStruct(listOf(TestSimpleJceStruct(), TestSimpleJceStruct())).toByteArray(OuterStruct.serializer()).toUHexString()
        )

        assertEquals(
            OuterStruct(
                listOf(
                    TestSimpleJceStruct(),
                    TestSimpleJceStruct()
                )
            ).toByteArray(OuterStruct.serializer()).loadAs(OuterStruct.serializer()).contentToString(),
            OuterStruct(listOf(TestSimpleJceStruct(), TestSimpleJceStruct())).contentToString()
        )
    }
}