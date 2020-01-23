package jceTest

import io.ktor.util.InternalAPI
import jce.jce.JceInputStream
import jceTest.JceOutputTest.TestMiraiStruct
import jceTest.JceOutputTest.TestQQStruct
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.buildJcePacket
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.toIoBuffer
import org.junit.Test

private infix fun <T> T.shouldEqualTo(another: T) {
    if (this is Array<*>) {
        this.contentEquals(another as Array<*>)
    } else
        check(this.contentToString() == another.contentToString()) {
            """actual:   ${this.contentToString()}
              |required: ${another.contentToString()} 
        """.trimMargin()
        }
}

@UseExperimental(InternalAPI::class)
private fun <R> ByteArray.qqJce(block: JceInputStream.() -> R): R {
    return JceInputStream(this).run(block)
}

private fun <R> ByteArray.read(block: JceInput.() -> R): R {
    return JceInput(this.toIoBuffer()).run(block)
}

private fun ByteReadPacket.check(block: ByteArray.() -> Unit) {
    this.readBytes().apply(block)
}

internal class JceInputTest {

    @Test
    fun readByte() = buildJcePacket {
        writeByte(1, 1)
    }.check {
        read {
            readByte(1)
        } shouldEqualTo qqJce {
            read(0.toByte(), 1, true)
        }
    }

    @Test
    fun readDouble() = buildJcePacket {
        writeDouble(1.0, 1)
    }.check {
        read {
            readDouble(1)
        } shouldEqualTo qqJce {
            read(0.toDouble(), 1, true)
        }
    }

    @Test
    fun readFloat() = buildJcePacket {
        writeFloat(1.0f, 1)
    }.check {
        read {
            readFloat(1)
        } shouldEqualTo qqJce {
            read(0.toFloat(), 1, true)
        }
    }

    @Test
    fun readFully() = buildJcePacket {
        writeFully(byteArrayOf(1, 2, 3), 1)
    }.check {
        read {
            readByteArray(1)
        } shouldEqualTo qqJce {
            read(byteArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully() = buildJcePacket {
        writeFully(shortArrayOf(1, 2, 3), 1)
    }.check {
        read {
            readShortArray(1)
        } shouldEqualTo qqJce {
            read(shortArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully1() = buildJcePacket {
        writeFully(intArrayOf(1, 2, 3), 1)
    }.check {
        read {
            readIntArray(1)
        } shouldEqualTo qqJce {
            read(intArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully2() = buildJcePacket {
        writeFully(longArrayOf(1, 2, 3), 1)
    }.check {
        read {
            readLongArray(1)
        } shouldEqualTo qqJce {
            read(longArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully3() = buildJcePacket {
        writeFully(booleanArrayOf(true, false, true), 1)
    }.check {
        read {
            readBooleanArray(1)
        } shouldEqualTo qqJce {
            read(booleanArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully4() = buildJcePacket {
        writeFully(floatArrayOf(1f, 2f, 3f), 1)
    }.check {
        read {
            readFloatArray(1)
        } shouldEqualTo qqJce {
            read(floatArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully5() = buildJcePacket {
        writeFully(doubleArrayOf(1.0, 2.0, 3.0), 1)
    }.check {
        read {
            readDoubleArray(1)
        } shouldEqualTo qqJce {
            read(doubleArrayOf(), 1, true)
        }
    }

    @Test
    fun testWriteFully6() = buildJcePacket {
        writeFully(arrayOf("sss", "哈哈"), 1)
    }.check {
        read {
            readSimpleArray("", 1)
        } shouldEqualTo qqJce {
            read(arrayOf(""), 1, true)
        }
    }

    @Test
    fun testWriteFully7() = buildJcePacket {
        writeFully(arrayOf("sss", "哈哈"), 1)
    }.check {
        read {
            readArrayOrNull("", 1)!!
        } shouldEqualTo qqJce {
            read(arrayOf(""), 1, true)
        }
    }

    @Test
    fun testWriteFully8() = buildJcePacket {
        writeFully(arrayOf(TestMiraiStruct("Haha")), 1)
    }.check {
        read {
            readJceStructArrayOrNull(TestMiraiStruct, 1)!!
        } shouldEqualTo qqJce {
            read(arrayOf(TestQQStruct("stub")), 1, true)
        }
    }

    @Test
    fun readInt() = buildJcePacket {
        writeInt(1, 2)
    }.check {
        read {
            readInt(2)
        } shouldEqualTo qqJce {
            read(0, 2, true)
        }
    }

    @Test
    fun readLong() = buildJcePacket {
        writeLong(1, 2)
    }.check {
        read {
            readLong(2)
        } shouldEqualTo qqJce {
            read(0L, 2, true)
        }
    }

    @Test
    fun readShort() = buildJcePacket {
        writeShort(1, 2)
    }.check {
        read {
            readShort(2)
        } shouldEqualTo qqJce {
            read(0.toShort(), 2, true)
        }
    }

    @Test
    fun readBoolean() = buildJcePacket {
        writeBoolean(true, 2)
    }.check {
        read {
            readBoolean(2)
        } shouldEqualTo qqJce {
            read(false, 2, true)
        }
    }

    @Test
    fun readString() = buildJcePacket {
        writeString("嗨", 2)
    }.check {
        read {
            readString(2)
        } shouldEqualTo qqJce {
            read("", 2, true)
        }
    }

    @Test
    fun readMap() = buildJcePacket {
        writeMap(mapOf(123.0 to "Hello"), 3)
    }.check {
        read {
            readMap(0.0, "", 3)
        } shouldEqualTo qqJce {
            read(mapOf(0.0 to ""), 3, true)
        }
    }

    @Test
    fun readCollection() = buildJcePacket {
        writeCollection(listOf("1", "还"), 3)
    }.check {
        repeat(0) {
            error("fuck kotlin")
        }
        read {
            readList("", 3)
        } shouldEqualTo qqJce {
            read(listOf(""), 3, true)
        }
    }


    @Test
    fun readJceStruct() = buildJcePacket {
        writeJceStruct(TestMiraiStruct("123"), 3)
    }.check {
        read {
            readJceStruct(TestMiraiStruct, 3)
        } shouldEqualTo qqJce {
            read(TestQQStruct("stub"), 3, true)!!
        }
    }

    @Test
    fun readObject() = buildJcePacket {
        writeObject(123, 3)
    }.check {
        read {
            readObject(123, 3)
        } shouldEqualTo qqJce {
            read(123 as Any, 3, true)
        }
    }

}