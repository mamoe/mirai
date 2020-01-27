package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.readBytes
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.CharsetUTF8
import net.mamoe.mirai.qqandroid.io.JceOutput
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.buildJcePacket
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.test.Test
import kotlin.test.assertEquals


class JceEncoderTest {

    @Serializable
    class TestSimpleJceStruct(
        @SerialId(0) val string: String = "123",
        @SerialId(1) val byte: Byte = 123,
        @SerialId(2) val short: Short = 123,
        @SerialId(3) val int: Int = 123,
        @SerialId(4) val long: Long = 123,
        @SerialId(5) val float: Float = 123f,
        @SerialId(6) val double: Double = 123.0
    ) : JceStruct() {
        override fun writeTo(builder: JceOutput) = builder.run {
            writeString("123", 0)
            writeByte(123, 1)
            writeShort(123, 2)
            writeInt(123, 3)
            writeLong(123, 4)
            writeFloat(123f, 5)
            writeDouble(123.0, 6)
        }
    }

    @Test
    fun testEncoder() {
        assertEquals(
            buildJcePacket {
                writeString("123", 0)
                writeByte(123, 1)
                writeShort(123, 2)
                writeInt(123, 3)
                writeLong(123, 4)
                writeFloat(123f, 5)
                writeDouble(123.0, 6)
            }.readBytes().toUHexString(),
            Jce.GBK.dump(
                TestSimpleJceStruct.serializer(),
                TestSimpleJceStruct()
            ).toUHexString()
        )
    }

    @Test
    fun testEncoder2() {
        assertEquals(
            buildJcePacket(stringCharset = CharsetUTF8) {
                writeFully(byteArrayOf(1, 2, 3), 7)
                writeCollection(listOf(1, 2, 3), 8)
                writeMap(mapOf("哈哈" to "嘿嘿"), 9)
                writeJceStruct(TestSimpleJceStruct(), 10)
            }.readBytes().toUHexString(),
            Jce.UTF8.dump(
                TestComplexJceStruct.serializer(),
                TestComplexJceStruct()
            ).toUHexString()
        )
    }

    @Serializable
    class TestComplexJceStruct(
        @SerialId(7) val byteArray: ByteArray = byteArrayOf(1, 2, 3),
        @SerialId(8) val byteList: List<Byte> = listOf(1, 2, 3),
        @SerialId(9) val map: Map<String, String> = mapOf("哈哈" to "嘿嘿"),
        @SerialId(10) val nestedJceStruct: TestSimpleJceStruct = TestSimpleJceStruct()
    )
}