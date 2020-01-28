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

fun main() {
    JceDecoderTest().testSimpleMap()
}

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
        @SerialId(7) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
        @SerialId(8) val byteList: List<Byte> = listOf(1, 2, 3), // error here
        @SerialId(9) val map: Map<String, Map<String, ByteArray>> = mapOf("哈哈" to mapOf("哈哈" to byteArrayOf(1, 2, 3))),
        //   @SerialId(10) val nestedJceStruct: TestSimpleJceStruct = TestSimpleJceStruct(),
        @SerialId(11) val byteList2: List<List<Int>> = listOf(listOf(1, 2, 3), listOf(1, 2, 3))
    ) : JceStruct

    @Serializable
    class TestComplexNullableJceStruct(
        @SerialId(7) val byteArray: ByteArray? = byteArrayOf(1, 2, 3),
        @SerialId(8) val byteList: List<Byte>? = listOf(1, 2, 3), // error here
        @SerialId(9) val map: Map<String, Map<String, ByteArray>>? = mapOf("哈哈" to mapOf("哈哈" to byteArrayOf(1, 2, 3))),
        @SerialId(10) val nestedJceStruct: TestSimpleJceStruct? = TestSimpleJceStruct(),
        @SerialId(11) val byteList2: List<List<Int>>? = listOf(listOf(1, 2, 3), listOf(1, 2, 3))
    ) : JceStruct

    @Test
    fun testEncoder() {
        println(TestComplexJceStruct().toByteArray(TestComplexJceStruct.serializer()).loadAs(TestComplexNullableJceStruct.serializer()).contentToString())
    }

    @Test
    fun testEncoder2() {
        assertEquals(
            buildJcePacket {
                writeFully(byteArrayOf(1, 2, 3), 7)
                writeCollection(listOf(1, 2, 3), 8)
                writeMap(mapOf("哈哈" to mapOf("哈哈" to byteArrayOf(1, 2, 3))), 9)
                writeJceStruct(TestSimpleJceStruct(), 10)
                writeCollection(listOf(listOf(1, 2, 3), listOf(1, 2, 3)), 11)
            }.readBytes().toUHexString(),
            TestComplexJceStruct().toByteArray(TestComplexJceStruct.serializer()).toUHexString()
        )
    }


    @Test
    fun testNestedList() {
        @Serializable
        class TestNestedList(
            @SerialId(7) val array: List<List<Int>> = listOf(listOf(1, 2, 3), listOf(1, 2, 3), listOf(1, 2, 3))
        )

        println(buildJcePacket {
            writeCollection(listOf(listOf(1, 2, 3), listOf(1, 2, 3), listOf(1, 2, 3)), 7)
        }.readBytes().loadAs(TestNestedList.serializer()).contentToString())
    }

    @Test
    fun testNestedArray() {
        @Serializable
        class TestNestedArray(
            @SerialId(7) val array: Array<Array<Int>> = arrayOf(arrayOf(1, 2, 3), arrayOf(1, 2, 3), arrayOf(1, 2, 3))
        )

        println(buildJcePacket {
            writeFully(arrayOf(arrayOf(1, 2, 3), arrayOf(1, 2, 3), arrayOf(1, 2, 3)), 7)
        }.readBytes().loadAs(TestNestedArray.serializer()).contentToString())
    }

    @Test
    fun testSimpleMap() {

        @Serializable
        class TestSimpleMap(
            @SerialId(7) val map: Map<String, Long> = mapOf("byteArrayOf(1)" to 2222L)
        )
        println(buildJcePacket {
            writeMap(mapOf("byteArrayOf(1)" to 2222), 7)
        }.readBytes().loadAs(TestSimpleMap.serializer()).contentToString())
    }

    @Test
    fun testSimpleList() {

        @Serializable
        class TestSimpleList(
            @SerialId(7) val list: List<String> = listOf("asd", "asdasdasd")
        )
        println(buildJcePacket {
            writeCollection(listOf("asd", "asdasdasd"), 7)
        }.readBytes().loadAs(TestSimpleList.serializer()).contentToString())
    }

    @Test
    fun testNestedMap() {
        @Serializable
        class TestNestedMap(
            @SerialId(7) val map: Map<ByteArray, Map<ByteArray, ShortArray>> = mapOf(byteArrayOf(1) to mapOf(byteArrayOf(1) to shortArrayOf(2)))
        )
        println(buildJcePacket {
            writeMap(mapOf(byteArrayOf(1) to mapOf(byteArrayOf(1) to shortArrayOf(2))), 7)
        }.readBytes().loadAs(TestNestedMap.serializer()).map.entries.first().value!!.contentToString())
    }
}