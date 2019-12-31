package net.mamoe.mirai.qqandroid.network.io

import kotlinx.io.charsets.Charset
import kotlinx.io.core.*
import kotlin.reflect.KClass

private val CharsetGBK = Charset.forName("GBK")

/**
 *
 * From: com.qq.taf.jce.JceOutputStream
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@UseExperimental(ExperimentalIoApi::class)
class JceOutput(
    private val stringCharset: Charset = CharsetGBK,
    private val output: Output = BytePacketBuilder()
) {

    fun close() = output.close()
    fun flush() = output.flush()

    fun writeByte(v: Byte, tag: Int) {
        if (v.toInt() == 0) {
            writeHead(ZERO_TAG, tag)
        } else {
            writeHead(BYTE, tag)
            output.writeByte(v)
        }
    }

    fun writeDouble(v: Double, tag: Int) {
        writeHead(DOUBLE, tag)
        output.writeDouble(v)
    }

    fun writeFloat(v: Float, tag: Int) {
        writeHead(FLOAT, tag)
        output.writeFloat(v)
    }

    fun writeFully(src: ByteArray, tag: Int) {
        writeHead(SIMPLE_LIST, tag)
        writeHead(BYTE, 0)
        writeInt(src.size, 0)
        output.writeFully(src)
    }

    fun writeFully(src: DoubleArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeDouble(it, 0)
        }
    }

    fun writeFully(src: FloatArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeFloat(it, 0)
        }
    }

    fun writeFully(src: IntArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeInt(it, 0)
        }
    }

    fun writeFully(src: LongArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeLong(it, 0)
        }
    }

    fun writeFully(src: ShortArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeShort(it, 0)
        }
    }

    fun writeFully(src: BooleanArray, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeBoolean(it, 0)
        }
    }

    fun <T> writeFully(src: Array<T>, tag: Int) {
        writeHead(LIST, tag)
        writeInt(src.size, 0)
        src.forEach {
            writeObject(it, 0)
        }
    }

    fun writeInt(v: Int, tag: Int) {
        if (v in Short.MIN_VALUE..Short.MAX_VALUE) {
            writeShort(v.toShort(), tag)
        } else {
            writeHead(INT, tag)
            output.writeInt(v)
        }
    }

    fun writeLong(v: Long, tag: Int) {
        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
            writeInt(v.toInt(), tag)
        } else {
            writeHead(LONG, tag)
            output.writeLong(v)
        }
    }

    fun writeShort(v: Short, tag: Int) {
        if (v in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            writeByte(v.toByte(), tag)
        } else {
            writeHead(BYTE, tag)
            output.writeShort(v)
        }
    }

    fun writeBoolean(v: Boolean, tag: Int) {
        this.writeByte(if (v) 1 else 0, tag)
    }

    fun writeString(v: String, tag: Int) {
        val array = v.toByteArray(stringCharset)
        if (array.size > 255) {
            writeHead(STRING4, tag)
            output.writeInt(array.size)
            output.writeFully(array)
        } else {
            writeHead(STRING1, tag)
            output.writeByte(array.size.toByte())
            output.writeFully(array)
        }
    }

    fun <K, V> writeMap(map: Map<K, V>, tag: Int) {
        writeHead(MAP, tag)
        if (map.isEmpty()) {
            writeInt(0, 0)
        } else {
            writeInt(map.size, 0)
            map.forEach { (key, value) ->
                writeObject(key, 0)
                writeObject(value, 0)
            }
        }
    }

    fun writeCollection(collection: Collection<*>?, tag: Int) {
        writeHead(LIST, tag)
        if (collection == null || collection.isEmpty()) {
            writeInt(0, 0)
        } else {
            writeInt(collection.size, 0)
            collection.forEach {
                writeObject(it, 0)
            }
        }
    }

    fun writeJceStruct(v: JceStruct, tag: Int) {
        writeHead(STRUCT_BEGIN, tag)
        v.writeTo(this)
        writeHead(STRUCT_END, 0)
    }

    fun <T> writeObject(v: T, tag: Int) {
        when (v) {
            is Byte -> writeByte(v, tag)
            is Boolean -> writeBoolean(v, tag)
            is Short -> writeShort(v, tag)
            is Int -> writeInt(v, tag)
            is Long -> writeLong(v, tag)
            is Float -> writeFloat(v, tag)
            is Double -> writeDouble(v, tag)
            is Map<*, *> -> writeMap(v, tag)
            is Collection<*> -> writeCollection(v, tag)
            is JceStruct -> writeJceStruct(v, tag)
            is ByteArray -> writeFully(v, tag)
            is IntArray -> writeFully(v, tag)
            is ShortArray -> writeFully(v, tag)
            is BooleanArray -> writeFully(v, tag)
            is LongArray -> writeFully(v, tag)
            is FloatArray -> writeFully(v, tag)
            is DoubleArray -> writeFully(v, tag)
            is Array<*> -> writeFully(v, tag)
            else -> error("unsupported type: ${v.getClassName()}")
        }
    }

    @PublishedApi
    internal companion object {
        const val BYTE: Int = 0
        const val DOUBLE: Int = 5
        const val FLOAT: Int = 4
        const val INT: Int = 2
        const val JCE_MAX_STRING_LENGTH = 104857600
        const val LIST: Int = 9
        const val LONG: Int = 3
        const val MAP: Int = 8
        const val SHORT: Int = 1
        const val SIMPLE_LIST: Int = 13
        const val STRING1: Int = 6
        const val STRING4: Int = 7
        const val STRUCT_BEGIN: Int = 10
        const val STRUCT_END: Int = 11
        const val ZERO_TAG: Int = 12

        private fun Any?.getClassName(): KClass<out Any> = if (this == null) Unit::class else this::class
    }

    @PublishedApi
    internal fun writeHead(type: Int, tag: Int) {
        if (tag < 15) {
            this.output.writeByte((tag shl 4 or type).toByte())
            return
        }
        if (tag < 256) {
            this.output.writeByte((type or 0xF0).toByte())
            this.output.writeByte(tag.toByte())
            return
        }
        throw JceEncodeException("tag is too large: $tag")
    }
}

class JceEncodeException(message: String) : RuntimeException(message)