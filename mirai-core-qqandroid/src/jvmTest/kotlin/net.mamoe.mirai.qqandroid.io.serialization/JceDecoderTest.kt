package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.readBytes
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceOutput
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.buildJcePacket
import net.mamoe.mirai.utils.cryptor.contentToString
import kotlin.test.Test
import kotlin.test.assertEquals

class JceDecoderTest {
    @Serializable
    class TestSimpleJceStruct(
        @SerialId(0) val string: String = "123",
        @SerialId(1) val byte: Byte = 123,
        @SerialId(2) val short: Short = 123,
        @SerialId(3) val int: Int = 123,
        @SerialId(4) val long: Long = 123,
        @SerialId(5) val float: Float = 123f,
        @SerialId(6) val double: Double = 123.0
    ) : JceStruct {
        override fun writeTo(output: JceOutput) = output.run {
            writeString(string, 0)
            writeByte(byte, 1)
            writeShort(short, 2)
            writeInt(int, 3)
            writeLong(long, 4)
            writeFloat(float, 5)
            writeDouble(double, 6)
        }
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
        class TestNestedList(
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
}