package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.utils.cryptor.contentToString
import kotlin.test.Test


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
    ) : JceStruct

    @Test
    fun testEncoder() {
        println(TestComplexJceStruct().toByteArray(TestComplexJceStruct.serializer()).loadAs(TestComplexJceStruct.serializer()).contentToString())
    }

    @Test
    fun testEncoder2() {

    }

    @Serializable
    class TestComplexJceStruct(
        @SerialId(7) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
        @SerialId(8) val byteList: List<Byte> = listOf(1, 2, 3),
        @SerialId(9) val map: Map<String, String> = mapOf("哈哈" to "嘿嘿"),
        @SerialId(10) val nestedJceStruct: TestSimpleJceStruct = TestSimpleJceStruct()
    ) : JceStruct
}