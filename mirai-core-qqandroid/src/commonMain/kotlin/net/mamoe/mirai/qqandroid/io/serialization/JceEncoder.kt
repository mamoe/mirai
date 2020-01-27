package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.ByteBuffer
import kotlinx.io.ByteOrder
import kotlinx.io.charsets.Charset
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.core.toByteArray
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.mamoe.mirai.qqandroid.io.CharsetUTF8
import net.mamoe.mirai.qqandroid.io.JceEncodeException
import net.mamoe.mirai.qqandroid.io.JceStruct
import kotlin.reflect.KClass

fun <T> ByteArray.loadAs(deserializer: DeserializationStrategy<T>, c: Charset): T {
    return Jce.byCharSet(c).load(deserializer, this)
}


enum class JceCharset(val kotlinCharset: Charset) {
    GBK(Charset.forName("GBK")),
    UTF8(Charset.forName("UTF8"))
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SerialCharset(val charset: JceCharset)

internal object JceType {
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
    const val ZERO_TYPE: Int = 12

    private fun Any?.getClassName(): KClass<out Any> = if (this == null) Unit::class else this::class
}


internal fun getSerialId(desc: SerialDescriptor, index: Int): Int? = desc.findAnnotation<SerialId>(index)?.id

internal data class JceDesc(
    val id: Int,
    val charset: JceCharset
) {
    companion object {
        val STUB_FOR_PRIMITIVE_NUMBERS_GBK = JceDesc(0, JceCharset.GBK)
    }
}

class Jce private constructor(private val charset: JceCharset, context: SerialModule = EmptyModule) : AbstractSerialFormat(context), BinaryFormat {

    private inner class ListWriter(
        defaultStringCharset: JceCharset,
        private val count: Int,
        private val tag: JceDesc,
        private val parentEncoder: JceEncoder
    ) : JceEncoder(defaultStringCharset, ByteArrayOutputStream()) {
        override fun SerialDescriptor.getTag(index: Int): JceDesc {
            return JceDesc(0, getCharset(index))
        }

        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(LIST, this.tag.id)
            parentEncoder.encodeTaggedInt(JceDesc.STUB_FOR_PRIMITIVE_NUMBERS_GBK, count)
            parentEncoder.output.write(this.output.toByteArray())
        }
    }

    private inner class JceStructWriter(
        defaultStringCharset: JceCharset,
        private val tag: JceDesc,
        private val parentEncoder: JceEncoder,
        private val stream: ByteArrayOutputStream = ByteArrayOutputStream()
    ) : JceEncoder(defaultStringCharset, stream) {
        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(STRUCT_BEGIN, this.tag.id)
            parentEncoder.output.write(stream.toByteArray())
            parentEncoder.writeHead(STRUCT_END, 0)
        }
    }

    private inner class JceMapWriter(
        defaultStringCharset: JceCharset,
        output: ByteArrayOutputStream
    ) : JceEncoder(defaultStringCharset, output) {
        override fun SerialDescriptor.getTag(index: Int): JceDesc {
            return if (index % 2 == 0) JceDesc(0, getCharset(index))
            else JceDesc(1, getCharset(index))
        }

        /*
        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(MAP, this.tag.id)
            parentEncoder.encodeTaggedInt(JceDesc.STUB_FOR_PRIMITIVE_NUMBERS_GBK, count)
            println(this.output.toByteArray().toUHexString())
            parentEncoder.output.write(this.output.toByteArray())
        }*/

        override fun beginCollection(desc: SerialDescriptor, collectionSize: Int, vararg typeParams: KSerializer<*>): CompositeEncoder {
            return this
        }

        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
            return this
        }
    }

    /**
     * From: com.qq.taf.jce.JceOutputStream
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    @UseExperimental(ExperimentalIoApi::class)
    private open inner class JceEncoder(
        /**
         * 标注在 class 上的 charset
         */
        private val defaultStringCharset: JceCharset,
        internal val output: ByteArrayOutputStream
    ) : TaggedEncoder<JceDesc>() {
        override val context get() = this@Jce.context

        protected fun SerialDescriptor.getCharset(index: Int): JceCharset {
            return findAnnotation<SerialCharset>(index)?.charset ?: defaultStringCharset
        }

        override fun SerialDescriptor.getTag(index: Int): JceDesc {
            return JceDesc(getSerialId(this, index) ?: error("cannot find @SerialId"), getCharset(index))
        }

        /**
         * 序列化最开始的时候的
         */
        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when (desc.kind) {
            StructureKind.LIST -> this
            StructureKind.MAP -> this
            StructureKind.CLASS, UnionKind.OBJECT -> this
            is PolymorphicKind -> this
            else -> throw SerializationException("Primitives are not supported at top-level")
        }

        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) = when (serializer.descriptor) {
            is MapLikeDescriptor -> {
                println("hello")
                val entries = (value as Map<*, *>).entries
                val serializer = (serializer as MapLikeSerializer<Any?, Any?, T, *>)
                val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)

                this.writeHead(MAP, currentTag.id)
                this.encodeTaggedInt(JceDesc.STUB_FOR_PRIMITIVE_NUMBERS_GBK, entries.count())
                HashSetSerializer(mapEntrySerial).serialize(JceMapWriter(charset, this.output), entries)
            }
            ByteArraySerializer.descriptor -> encodeTaggedByteArray(popTag(), value as ByteArray)
            is PrimitiveArrayDescriptor -> {
                if (value is ByteArray) {
                    this.encodeTaggedByteArray( popTag(), value)
                } else{
                    serializer.serialize(
                        ListWriter(charset, when(value){
                            is ShortArray -> value.size
                            is IntArray -> value.size
                            is LongArray -> value.size
                            is FloatArray -> value.size
                            is DoubleArray -> value.size
                            is CharArray -> value.size
                            else -> error("unknown array type: ${value.getClassName()}")
                        },  popTag(), this),
                        value
                    )
                }
            }
            is ArrayClassDesc-> {
                serializer.serialize(
                    ListWriter(charset, (value as Array<*>).size,  popTag(), this),
                    value
                )
            }
            is ListLikeDescriptor -> {
                serializer.serialize(
                    ListWriter(charset, (value as Collection<*>).size,  popTag(), this),
                    value
                )
            }
            else -> {
                if (value is JceStruct) {
                    if (currentTagOrNull == null) {
                        serializer.serialize(this, value)
                    } else {
                        this.writeHead(STRUCT_BEGIN, currentTag.id)
                        serializer.serialize(this, value)
                        this.writeHead(STRUCT_END, 0)
                    }
                } else serializer.serialize(this, value)
            }
        }

        override fun encodeTaggedByte(tag: JceDesc, value: Byte) {
            if (value.toInt() == 0) {
                writeHead(ZERO_TYPE, tag.id)
            } else {
                writeHead(BYTE, tag.id)
                output.write(value.toInt())
            }
        }

        override fun encodeTaggedShort(tag: JceDesc, value: Short) {
            if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
                encodeTaggedByte(tag, value.toByte())
            } else {
                writeHead(SHORT, tag.id)
                output.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array())
            }
        }

        override fun encodeTaggedInt(tag: JceDesc, value: Int) {
            if (value in Short.MIN_VALUE..Short.MAX_VALUE) {
                encodeTaggedShort(tag, value.toShort())
            } else {
                writeHead(INT, tag.id)
                output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
            }
        }

        override fun encodeTaggedFloat(tag: JceDesc, value: Float) {
            writeHead(FLOAT, tag.id)
            output.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(value).array())
        }

        override fun encodeTaggedDouble(tag: JceDesc, value: Double) {
            writeHead(DOUBLE, tag.id)
            output.write(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putDouble(value).array())
        }

        override fun encodeTaggedLong(tag: JceDesc, value: Long) {
            if (value in Int.MIN_VALUE..Int.MAX_VALUE) {
                encodeTaggedInt(tag, value.toInt())
            } else {
                writeHead(LONG, tag.id)
                output.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array())
            }
        }

        override fun encodeTaggedBoolean(tag: JceDesc, value: Boolean) {
            encodeTaggedByte(tag, if (value) 1 else 0)
        }

        override fun encodeTaggedChar(tag: JceDesc, value: Char) {
            encodeTaggedByte(tag, value.toByte())
        }

        override fun encodeTaggedEnum(tag: JceDesc, enumDescription: SerialDescriptor, ordinal: Int) {
            TODO()
        }

        override fun encodeTaggedNull(tag: JceDesc) {
        }

        override fun encodeTaggedUnit(tag: JceDesc) {
            encodeTaggedNull(tag)
        }

        fun encodeTaggedByteArray(tag: JceDesc, bytes: ByteArray) {
            writeHead(SIMPLE_LIST, tag.id)
            writeHead(BYTE, 0)
            encodeTaggedInt(JceDesc.STUB_FOR_PRIMITIVE_NUMBERS_GBK, bytes.size)
            output.write(bytes)
        }

        override fun encodeTaggedString(tag: JceDesc, value: String) {
            val array = value.toByteArray(defaultStringCharset.kotlinCharset)
            if (array.size > 255) {
                writeHead(STRING4, tag.id)
                output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(array.size).array())
                output.write(array)
            } else {
                writeHead(STRING1, tag.id)
                output.write(ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(array.size.toByte()).array())
                output.write(array)
            }
        }

        override fun encodeTaggedValue(tag: JceDesc, value: Any) {
            when (value) {
                is Byte -> encodeTaggedByte(tag, value)
                is Short -> encodeTaggedShort(tag, value)
                is Int -> encodeTaggedInt(tag, value)
                is Long -> encodeTaggedLong(tag, value)
                is Float -> encodeTaggedFloat(tag, value)
                is Double -> encodeTaggedDouble(tag, value)
                is Boolean -> encodeTaggedBoolean(tag, value)
                is String -> encodeTaggedString(tag, value)
                is Unit -> encodeTaggedUnit(tag)
                else -> error("unsupported type: ${value.getClassName()}")
            }
        }

        @PublishedApi
        internal fun writeHead(type: Int, tag: Int) {
            if (tag < 15) {
                this.output.write((tag shl 4) or type)
                return
            }
            if (tag < 256) {
                this.output.write(type or 0xF0)
                this.output.write(tag)
                return
            }
            throw JceEncodeException("tag is too large: $tag")
        }
    }

    companion object {
        val UTF8 = Jce(JceCharset.UTF8)
        val GBK = Jce(JceCharset.GBK)

        public fun byCharSet(c: Charset): Jce {
            return if (c === CharsetUTF8) {
                UTF8
            } else {
                GBK
            }
        }

        internal const val BYTE: Int = 0
        internal const val DOUBLE: Int = 5
        internal const val FLOAT: Int = 4
        internal const val INT: Int = 2
        internal const val JCE_MAX_STRING_LENGTH = 104857600
        internal const val LIST: Int = 9
        internal const val LONG: Int = 3
        internal const val MAP: Int = 8
        internal const val SHORT: Int = 1
        internal const val SIMPLE_LIST: Int = 13
        internal const val STRING1: Int = 6
        internal const val STRING4: Int = 7
        internal const val STRUCT_BEGIN: Int = 10
        internal const val STRUCT_END: Int = 11
        internal const val ZERO_TYPE: Int = 12

        private fun Any?.getClassName(): KClass<out Any> = if (this == null) Unit::class else this::class

        internal const val VARINT = 0
        internal const val i64 = 1
        internal const val SIZE_DELIMITED = 2
        internal const val i32 = 5
    }

    override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray {
        val encoder = ByteArrayOutputStream()

        val dumper = JceEncoder(charset, encoder)
        dumper.encode(serializer, obj)
        return encoder.toByteArray()
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        TODO()
    }

    override fun <T>

}
