package jceTest

import io.ktor.util.InternalAPI
import jce.jce.JceInputStream
import jce.jce.JceOutputStream
import jce.jce.JceStruct
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.buildJcePacket
import net.mamoe.mirai.utils.io.toUHexString
import org.junit.Test

private infix fun ByteReadPacket.shouldEqualTo(another: ByteArray) {
    this.readBytes().let {
        check(it.contentEquals(another)) {
            """actual:   ${it.toUHexString()}
              |required: ${another.toUHexString()} 
        """.trimMargin()
        }
    }
}

@UseExperimental(InternalAPI::class)
private fun qqJce(block: JceOutputStream.() -> Unit): ByteArray {
    return JceOutputStream().apply(block).toByteArray()
}

internal class JceOutputTest {

    @Test
    fun writeByte() {
        buildJcePacket {
            writeByte(1, 1)
            writeByte(-128, 2)
        } shouldEqualTo qqJce {
            write(1.toByte(), 1)
            write((-128).toByte(), 2)
        }
    }

    @Test
    fun writeDouble() {
        buildJcePacket {
            writeDouble(1.0, 1)
            writeDouble(-128.0, 2)
        } shouldEqualTo qqJce {
            write(1.toDouble(), 1)
            write((-128).toDouble(), 2)
        }
    }

    @Test
    fun writeFloat() {
        buildJcePacket {
            writeFloat(1.0f, 1)
            writeFloat(-128.0f, 2)
        } shouldEqualTo qqJce {
            write(1.toFloat(), 1)
            write((-128).toFloat(), 2)
        }
    }

    @Test
    fun writeFully() {
        buildJcePacket {
            writeFully(byteArrayOf(1, 2), 1)
            writeFully(byteArrayOf(1, 2), 2)
        } shouldEqualTo qqJce {
            write(byteArrayOf(1, 2), 1)
            write(byteArrayOf(1, 2), 2)
        }
    }

    @Test
    fun testWriteFully() {
        buildJcePacket {
            writeFully(intArrayOf(1, 2), 1)
            writeFully(intArrayOf(1, 2), 2)
        } shouldEqualTo qqJce {
            write(intArrayOf(1, 2), 1)
            write(intArrayOf(1, 2), 2)
        }
    }

    @Test
    fun testWriteFully1() {
        buildJcePacket {
            writeFully(shortArrayOf(1, 2), 1)
            writeFully(shortArrayOf(1, 2), 2)
        } shouldEqualTo qqJce {
            write(shortArrayOf(1, 2), 1)
            write(shortArrayOf(1, 2), 2)
        }
    }

    @Test
    fun testWriteFully2() {
        buildJcePacket {
            writeFully(booleanArrayOf(true, false), 1)
            writeFully(booleanArrayOf(true, false), 2)
        } shouldEqualTo qqJce {
            write(booleanArrayOf(true, false), 1)
            write(booleanArrayOf(true, false), 2)
        }
    }

    @Test
    fun testWriteFully3() {
        buildJcePacket {
            writeFully(longArrayOf(1, 2), 1)
            writeFully(longArrayOf(1, 2), 2)
        } shouldEqualTo qqJce {
            write(longArrayOf(1, 2), 1)
            write(longArrayOf(1, 2), 2)
        }
    }

    @Test
    fun testWriteFully4() {
        buildJcePacket {
            writeFully(floatArrayOf(1f, 2f), 1)
            writeFully(floatArrayOf(1f, 2f), 2)
        } shouldEqualTo qqJce {
            write(floatArrayOf(1f, 2f), 1)
            write(floatArrayOf(1f, 2f), 2)
        }
    }

    @Test
    fun testWriteFully5() {
        buildJcePacket {
            writeFully(doubleArrayOf(1.0, 2.0), 1)
            writeFully(doubleArrayOf(1.0, 2.0), 2)
        } shouldEqualTo qqJce {
            write(doubleArrayOf(1.0, 2.0), 1)
            write(doubleArrayOf(1.0, 2.0), 2)
        }
    }

    @Test
    fun testWriteFully6() {
        buildJcePacket {
            writeFully(arrayOf("123", "哈哈"), 1)
            writeFully(arrayOf("123", "哈哈"), 2)
        } shouldEqualTo qqJce {
            write(arrayOf("123", "哈哈"), 1)
            write(arrayOf("123", "哈哈"), 2)
        }
    }

    @Test
    fun writeInt() {
        buildJcePacket {
            writeInt(1, 1)
            writeInt(-128, 2)
        } shouldEqualTo qqJce {
            write(1, 1)
            write(-128, 2)
        }
    }

    @Test
    fun writeLong() {
        buildJcePacket {
            writeLong(1, 1)
            writeLong(-128, 2)
        } shouldEqualTo qqJce {
            write(1L, 1)
            write(-128L, 2)
        }
    }

    @Test
    fun writeShort() {
        buildJcePacket {
            writeShort(1, 1)
            writeShort(-128, 2)
        } shouldEqualTo qqJce {
            write(1.toShort(), 1)
            write((-128).toShort(), 2)
        }
    }

    @Test
    fun writeBoolean() {
        buildJcePacket {
            writeBoolean(true, 1)
            writeBoolean(false, 2)
        } shouldEqualTo qqJce {
            write(true, 1)
            write(false, 2)
        }
    }

    @Test
    fun writeString() {
        buildJcePacket {
            writeString("1", 1)
            writeString("哈啊", 2)
        } shouldEqualTo qqJce {
            write("1", 1)
            write("哈啊", 2)
        }
    }

    @Test
    fun writeMap() {
        buildJcePacket {
            writeMap(mapOf("" to ""), 1)
            writeMap(mapOf("" to 123), 2)
            writeMap(mapOf(123.0 to "Hello"), 3)
        } shouldEqualTo qqJce {
            write(mapOf("" to ""), 1)
            write(mapOf("" to 123), 2)
            write(mapOf(123.0 to "Hello"), 3)
        }
    }

    @Test
    fun writeCollection() {
        buildJcePacket {
            writeMap(mapOf("" to ""), 1)
            writeMap(mapOf("" to 123), 2)
            writeMap(mapOf(123.0 to "Hello"), 3)
        } shouldEqualTo qqJce {
            write(mapOf("" to ""), 1)
            write(mapOf("" to 123), 2)
            write(mapOf(123.0 to "Hello"), 3)
        }
    }

    data class TestMiraiStruct(
        val message: String
    ) : net.mamoe.mirai.qqandroid.network.io.JceStruct() {
        override fun writeTo(builder: JceOutput) {
            builder.writeString(message, 0)
        }

        companion object : Factory<TestMiraiStruct> {
            override fun newInstanceFrom(input: JceInput): TestMiraiStruct {
                return TestMiraiStruct(input.readString(0))
            }
        }
    }

    class TestQQStruct(
        private var message: String
    ) : JceStruct() {
        override fun readFrom(var1: JceInputStream) {
            message = var1.read("", 0, true)
        }

        override fun writeTo(var1: JceOutputStream) {
            var1.write(message, 0)
        }
    }

    @Test
    fun writeJceStruct() {
        buildJcePacket {
            writeJceStruct(TestMiraiStruct("Hello"), 0)
            writeJceStruct(TestMiraiStruct("嗨"), 1)
        } shouldEqualTo qqJce {
            write(TestQQStruct("Hello"), 0)
            write(TestQQStruct("嗨"), 1)
        }
    }

    @Test
    fun writeObject() {
        buildJcePacket {
            writeObject(0.toByte(), 1)
            writeObject(0.toShort(), 2)
            writeObject(0, 3)
            writeObject(0L, 4)
            writeObject(0f, 5)
            writeObject(0.0, 6)
            writeObject("hello", 7)
            writeObject(TestMiraiStruct("Hello"), 8)
        } shouldEqualTo qqJce {
            write(0.toByte(), 1)
            write(0.toShort(), 2)
            write(0, 3)
            write(0L, 4)
            write(0f, 5)
            write(0.0, 6)
            write("hello", 7)
            write(TestQQStruct("Hello"), 8)
        }
    }
}