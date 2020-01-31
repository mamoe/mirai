package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.charsets.Charset
import kotlinx.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.utils.io.readString
import net.mamoe.mirai.utils.io.toReadPacket

@PublishedApi
internal val CharsetGBK = Charset.forName("GBK")
@PublishedApi
internal val CharsetUTF8 = Charset.forName("UTF8")

enum class JceCharset(val kotlinCharset: Charset) {
    GBK(Charset.forName("GBK")),
    UTF8(Charset.forName("UTF8"))
}

internal fun getSerialId(desc: SerialDescriptor, index: Int): Int? = desc.findAnnotation<SerialId>(index)?.id

/**
 * Jce 数据结构序列化和反序列化工具, 能将 kotlinx.serialization 通用的注解标记格式的 `class` 序列化为 [ByteArray]
 */
class Jce private constructor(private val charset: JceCharset, context: SerialModule = EmptyModule) : AbstractSerialFormat(context), BinaryFormat {

    private inner class ListWriter(
        private val count: Int,
        private val tag: Int,
        private val parentEncoder: JceEncoder
    ) : JceEncoder(BytePacketBuilder()) {
        override fun SerialDescriptor.getTag(index: Int): Int {
            return 0
        }

        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(LIST, this.tag)
            parentEncoder.encodeTaggedInt(0, count)
            parentEncoder.output.writePacket(this.output.build())
        }
    }

    private inner class JceMapWriter(
        output: BytePacketBuilder
    ) : JceEncoder(output) {
        override fun SerialDescriptor.getTag(index: Int): Int {
            return if (index % 2 == 0) 0 else 1
        }

        /*
        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(MAP, this.tag)
            parentEncoder.encodeTaggedInt(Int.STUB_FOR_PRIMITIVE_NUMBERS_GBK, count)
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
        internal val output: BytePacketBuilder
    ) : TaggedEncoder<Int>() {
        override val context get() = this@Jce.context

        override fun SerialDescriptor.getTag(index: Int): Int {
            return getSerialId(this, index) ?: error("cannot find @SerialId")
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

        @UseExperimental(ImplicitReflectionSerializer::class)
        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) = when (serializer.descriptor) {
            is MapLikeDescriptor -> {
                val entries = (value as Map<*, *>).entries
                val serializer = (serializer as MapLikeSerializer<Any?, Any?, T, *>)
                val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)

                this.writeHead(MAP, currentTag)
                this.encodeTaggedInt(0, entries.count())
                HashSetSerializer(mapEntrySerial).serialize(JceMapWriter(this.output), entries)
            }
            ByteArraySerializer.descriptor -> encodeTaggedByteArray(popTag(), value as ByteArray)
            is PrimitiveArrayDescriptor -> {
                serializer.serialize(
                    ListWriter(
                        when (value) {
                            is ShortArray -> value.size
                            is IntArray -> value.size
                            is LongArray -> value.size
                            is FloatArray -> value.size
                            is DoubleArray -> value.size
                            is CharArray -> value.size
                            is ByteArray -> value.size
                            is BooleanArray -> value.size
                            else -> error("unknown array type: ${value.getClassName()}")
                        }, popTag(), this
                    ),
                    value
                )
            }
            is ArrayClassDesc -> {
                val descriptor = serializer.descriptor as ReferenceArraySerializer<Any, Any?>
                if (descriptor.typeParams.isNotEmpty() && descriptor.typeParams[0] is ByteSerializer) {
                    encodeTaggedByteArray(popTag(), (value as Array<Byte>).toByteArray())
                } else
                    serializer.serialize(
                        ListWriter((value as Array<*>).size, popTag(), this),
                        value
                    )
            }
            is ListLikeDescriptor -> {
                serializer.serialize(
                    ListWriter((value as Collection<*>).size, popTag(), this),
                    value
                )
            }
            else -> {
                if (value is JceStruct) {
                    if (currentTagOrNull == null) {
                        serializer.serialize(this, value)
                    } else {
                        this.writeHead(STRUCT_BEGIN, popTag())
                        serializer.serialize(JceEncoder(this.output), value)
                        this.writeHead(STRUCT_END, 0)
                    }
                } else if (value is ProtoBuf) {
                    this.encodeTaggedByteArray(popTag(), ProtoBufWithNullableSupport.dump(value))
                } else {
                    serializer.serialize(this, value)
                }
            }
        }

        override fun encodeTaggedByte(tag: Int, value: Byte) {
            if (value.toInt() == 0) {
                writeHead(ZERO_TYPE, tag)
            } else {
                writeHead(BYTE, tag)
                output.writeByte(value)
            }
        }

        override fun encodeTaggedShort(tag: Int, value: Short) {
            if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
                encodeTaggedByte(tag, value.toByte())
            } else {
                writeHead(SHORT, tag)
                output.writeShort(value)
            }
        }

        override fun encodeTaggedInt(tag: Int, value: Int) {
            if (value in Short.MIN_VALUE..Short.MAX_VALUE) {
                encodeTaggedShort(tag, value.toShort())
            } else {
                writeHead(INT, tag)
                output.writeInt(value)
            }
        }

        override fun encodeTaggedFloat(tag: Int, value: Float) {
            writeHead(FLOAT, tag)
            output.writeFloat(value)
        }

        override fun encodeTaggedDouble(tag: Int, value: Double) {
            writeHead(DOUBLE, tag)
            output.writeDouble(value)
        }

        override fun encodeTaggedLong(tag: Int, value: Long) {
            if (value in Int.MIN_VALUE..Int.MAX_VALUE) {
                encodeTaggedInt(tag, value.toInt())
            } else {
                writeHead(LONG, tag)
                output.writeLong(value)
            }
        }

        override fun encodeTaggedBoolean(tag: Int, value: Boolean) {
            encodeTaggedByte(tag, if (value) 1 else 0)
        }

        override fun encodeTaggedChar(tag: Int, value: Char) {
            encodeTaggedByte(tag, value.toByte())
        }

        override fun encodeTaggedEnum(tag: Int, enumDescription: SerialDescriptor, ordinal: Int) {
            TODO()
        }

        override fun encodeTaggedNull(tag: Int) {
        }

        override fun encodeTaggedUnit(tag: Int) {
            encodeTaggedNull(tag)
        }

        fun encodeTaggedByteArray(tag: Int, bytes: ByteArray) {
            writeHead(SIMPLE_LIST, tag)
            writeHead(BYTE, 0)
            encodeTaggedInt(0, bytes.size)
            output.writeFully(bytes)
        }

        override fun encodeTaggedString(tag: Int, value: String) {
            require(value.length <= JCE_MAX_STRING_LENGTH) { "string is too long for tag $tag" }
            val array = value.toByteArray(charset.kotlinCharset)
            if (array.size > 255) {
                writeHead(STRING4, tag)
                output.writeInt(array.size)
                output.writeFully(array)
            } else {
                writeHead(STRING1, tag)
                output.writeByte(array.size.toByte()) // one byte
                output.writeFully(array)
            }
        }

        override fun encodeTaggedValue(tag: Int, value: Any) {
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
        internal fun writeHead(type: Byte, tag: Int) {
            if (tag < 15) {
                this.output.writeByte(((tag shl 4) or type.toInt()).toByte())
                return
            }
            if (tag < 256) {
                this.output.writeByte((type.toInt() or 0xF0).toByte())
                this.output.writeByte(tag.toByte())
                return
            }
            error("tag is too large: $tag")
        }
    }

    private open inner class JceMapReader(
        val size: Int,
        input: JceInput
    ) : JceDecoder(input) {
        override fun decodeCollectionSize(desc: SerialDescriptor): Int {
            return size
        }

        override fun SerialDescriptor.getTag(index: Int): Int {
            // 奇数 0, 即 key; 偶数 1, 即 value
            return if (index % 2 == 0) 0 else 1
        }
    }

    private open inner class JceListReader(
        val size: Int,
        input: JceInput
    ) : JceDecoder(input) {
        override fun decodeCollectionSize(desc: SerialDescriptor): Int {
            return size
        }

        override fun SerialDescriptor.getTag(index: Int): Int {
            return 0
        }
    }

    private open inner class JceStructReader(
        input: JceInput
    ) : JceDecoder(input) {
        override fun endStructure(desc: SerialDescriptor) {
            while (input.input.canRead() && input.peakHeadOrNull()?.type != STRUCT_END) {
                input.readHeadOrNull() ?: return
            }
            input.readHeadOrNull()
        }
    }

    private open inner class NullReader(
        input: JceInput
    ) : JceDecoder(input)

    private open inner class JceDecoder(
        internal val input: JceInput
    ) : TaggedDecoder<Int>() {
        override fun SerialDescriptor.getTag(index: Int): Int {
            return getSerialId(this, index) ?: error("cannot find tag with index $index")
        }

        override fun decodeTaggedByte(tag: Int): Byte = input.readByte(tag)
        override fun decodeTaggedShort(tag: Int): Short = input.readShort(tag)
        override fun decodeTaggedInt(tag: Int): Int = input.readInt(tag)
        override fun decodeTaggedLong(tag: Int): Long = input.readLong(tag)
        override fun decodeTaggedFloat(tag: Int): Float = input.readFloat(tag)
        override fun decodeTaggedDouble(tag: Int): Double = input.readDouble(tag)
        override fun decodeTaggedChar(tag: Int): Char = input.readByte(tag).toChar()
        override fun decodeTaggedString(tag: Int): String = input.readString(tag)
        override fun decodeTaggedBoolean(tag: Int): Boolean = input.readBoolean(tag)


        override fun decodeTaggedEnum(tag: Int, enumDescription: SerialDescriptor): Int =
            TODO()

        /**
         * 在 [KSerializer.serialize] 前
         */
        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            //println("beginStructure: desc=${desc.getClassName()}, typeParams: ${typeParams.contentToString()}")
            when (desc) {
                // 由于 Byte 的数组有两种方式写入, 需特定读取器
                ByteArraySerializer.descriptor -> {
                    // ByteArray, 交给 decodeSerializableValue 进行处理
                    return this
                }
                is ListLikeDescriptor -> {
                    if (typeParams.isNotEmpty() && typeParams[0] is ByteSerializer) {
                        // Array<Byte>
                        return this // 交给 decodeSerializableValue
                    }

                    val tag = currentTagOrNull
                    @Suppress("SENSELESS_COMPARISON") // 推断 bug
                    if (tag != null && input.skipToTagOrNull(tag) {
                            popTag()
                            if (it.type == SIMPLE_LIST) {
                                input.readHead() // list 里面元素类型, 没必要知道
                            }
                            return when (it.type) {
                                SIMPLE_LIST, LIST -> JceListReader(input.readInt(0), this.input)
                                MAP -> JceMapReader(input.readInt(0), this.input)
                                else -> error("type mismatch")
                            }
                        } == null && desc.isNullable) {
                        return NullReader(this.input)
                    }
                }

                is MapLikeDescriptor -> {
                    val tag = currentTagOrNull
                    if (tag != null) {
                        popTag()
                    }
                    return JceMapReader(input.readInt(0), this.input)
                }
            }

            val tag = currentTagOrNull
            val jceHead = input.peakHeadOrNull()
            if (tag != null && (jceHead == null || jceHead.tag > tag)) {
                return NullReader(this.input)
            }

            return super.beginStructure(desc, *typeParams)
        }

        override fun decodeTaggedNull(tag: Int): Nothing? {
            return null
        }

        override fun decodeTaggedNotNullMark(tag: Int): Boolean {
            return !isTagOptional(tag)
        }

        fun isTagOptional(tag: Int): Boolean {
            val head = input.peakHeadOrNull()
            return input.isEndOfInput || head == null || head.tag > tag
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
            // println("decodeNullableSerializableValue: ${deserializer::class.qualifiedName}")
            if (deserializer is NullReader) {
                return null
            }
            when (deserializer.descriptor) {
                ByteArraySerializer.descriptor -> {
                    val tag = popTag()
                    return if (isTagOptional(tag)) input.readByteArrayOrNull(tag) as? T
                    else input.readByteArray(tag) as T
                }
                is ListLikeDescriptor -> {
                    if (deserializer is ReferenceArraySerializer<*, *>
                        && (deserializer as ListLikeSerializer<Any?, T, Any?>).typeParams.isNotEmpty()
                        && (deserializer as ListLikeSerializer<Any?, T, Any?>).typeParams[0] is ByteSerializer
                    ) {
                        val tag = popTag()
                        return if (isTagOptional(tag)) input.readByteArrayOrNull(tag)?.toTypedArray() as? T
                        else input.readByteArray(tag).toTypedArray() as T
                    } else if (deserializer is ArrayListSerializer<*>
                        && (deserializer as ArrayListSerializer<*>).typeParams.isNotEmpty()
                        && (deserializer as ArrayListSerializer<*>).typeParams[0] is ByteSerializer
                    ) {
                        val tag = popTag()
                        return if (isTagOptional(tag)) input.readByteArrayOrNull(tag)?.toMutableList() as? T
                        else input.readByteArray(tag).toMutableList() as T
                    }
                    val tag = currentTag
//                    println(tag)
                    @Suppress("SENSELESS_COMPARISON") // false positive
                    if (input.skipToTagOrNull(tag) {
                            return deserializer.deserialize(JceListReader(input.readInt(0), input))
                        } == null) {
                        if (isTagOptional(tag)) {
                            return null
                        } else error("property is notnull but cannot find tag $tag")
                    }
                    error("UNREACHABLE CODE")
                }
                is MapLikeDescriptor -> {
                    val tag = popTag()
                    @Suppress("SENSELESS_COMPARISON")
                    if (input.skipToTagOrNull(tag) {
                            check(it.type == MAP) { "type mismatch: ${it.type}" }
                            // 将 mapOf(k1 to v1, k2 to v2, ...) 转换为 listOf(k1, v1, k2, v2, ...) 以便于写入.
                            val serializer = (deserializer as MapLikeSerializer<Any?, Any?, T, *>)
                            val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)
                            val setOfEntries = HashSetSerializer(mapEntrySerial).deserialize(JceMapReader(input.readInt(0), input))
                            return setOfEntries.associateBy({ it.key }, { it.value }) as T
                        } == null) {
                        if (isTagOptional(tag)) {
                            return null
                        } else error("property is notnull but cannot find tag $tag")
                    }
                    error("UNREACHABLE CODE")
                }
            }

            if (deserializer.descriptor.kind == StructureKind.CLASS || deserializer.descriptor.kind == UnionKind.OBJECT) {
                val tag = currentTagOrNull
                if (tag != null) {
                    @Suppress("SENSELESS_COMPARISON") // 推断 bug
                    if (input.skipToTagOrNull(tag) {
                            check(it.type == STRUCT_BEGIN) { "type mismatch: ${it.type}" }
                            //popTag()
                            return deserializer.deserialize(JceStructReader(input))
                        } == null && isTagOptional(tag)) {
                        return null
                    } else error("cannot find tag $tag")
                }

                return deserializer.deserialize(JceDecoder(this.input))
            }

            val tag = currentTagOrNull ?: return deserializer.deserialize(JceDecoder(this.input))
            return if (this.decodeTaggedNotNullMark(tag)) {
                deserializer.deserialize(this)
            } else {
                // popTag()
                null
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
            return decodeNullableSerializableValue(deserializer as DeserializationStrategy<Any?>) as? T
                ?: error("value with tag $currentTagOrNull(by ${deserializer.getClassName()}) is not optional but cannot find. currentJceHead = ${input.currentJceHead}")
        }
    }


    @UseExperimental(ExperimentalUnsignedTypes::class)
    internal inner class JceInput(
        @PublishedApi
        internal val input: ByteReadPacket,
        maxReadSize: Long = input.remaining
    ) : Closeable {
        internal val leastRemaining = input.remaining - maxReadSize
        internal val isEndOfInput: Boolean get() = input.remaining <= leastRemaining

        internal var currentJceHead: JceHead? = input.doReadHead().also { println("first jce head = $it") }

        override fun close() = input.close()

        internal fun peakHeadOrNull(): JceHead? = currentJceHead ?: readHeadOrNull()
        internal fun peakHead(): JceHead = peakHeadOrNull() ?: error("no enough data to read head")

        @PublishedApi
        internal fun readHead(): JceHead = readHeadOrNull() ?: error("no enough data to read head")

        @PublishedApi
        internal fun readHeadOrNull(): JceHead? = input.doReadHead()

        /**
         * 读取下一个 head 存储到 [currentJceHead]
         */
        private fun ByteReadPacket.doReadHead(): JceHead? {
            if (isEndOfInput) {
                currentJceHead = null
                println("doReadHead: endOfInput")
                return null
            }
            val var2 = readUByte()
            val type = var2 and 15u
            var tag = var2.toUInt() shr 4
            if (tag == 15u) {
                if (isEndOfInput) {
                    currentJceHead = null
                    println("doReadHead: endOfInput2")
                    return null
                }
                tag = readUByte().toUInt()
            }
            currentJceHead = JceHead(tag = tag.toInt(), type = type.toByte())
            println("doReadHead: $currentJceHead")
            return currentJceHead
        }

        fun readBoolean(tag: Int): Boolean = readBooleanOrNull(tag) ?: error("cannot find tag $tag")
        fun readByte(tag: Int): Byte = readByteOrNull(tag) ?: error("cannot find tag $tag")
        fun readShort(tag: Int): Short = readShortOrNull(tag) ?: error("cannot find tag $tag")
        fun readInt(tag: Int): Int = readIntOrNull(tag) ?: error("cannot find tag $tag")
        fun readLong(tag: Int): Long = readLongOrNull(tag) ?: error("cannot find tag $tag")
        fun readFloat(tag: Int): Float = readFloatOrNull(tag) ?: error("cannot find tag $tag")
        fun readDouble(tag: Int): Double = readDoubleOrNull(tag) ?: error("cannot find tag $tag")

        fun readString(tag: Int): String = readStringOrNull(tag) ?: error("cannot find tag $tag")

        fun readByteArray(tag: Int): ByteArray = readByteArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readShortArray(tag: Int): ShortArray = readShortArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readLongArray(tag: Int): LongArray = readLongArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readFloatArray(tag: Int): FloatArray = readFloatArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readDoubleArray(tag: Int): DoubleArray = readDoubleArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readIntArray(tag: Int): IntArray = readIntArrayOrNull(tag) ?: error("cannot find tag $tag")
        fun readBooleanArray(tag: Int): BooleanArray = readBooleanArrayOrNull(tag) ?: error("cannot find tag $tag")


        fun readShortArrayOrNull(tag: Int): ShortArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            ShortArray(readInt(0)) { readShort(0) }
        }

        fun readDoubleArrayOrNull(tag: Int): DoubleArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            DoubleArray(readInt(0)) { readDouble(0) }
        }

        fun readFloatArrayOrNull(tag: Int): FloatArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            FloatArray(readInt(0)) { readFloat(0) }
        }

        fun readIntArrayOrNull(tag: Int): IntArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            IntArray(readInt(0)) { readInt(0) }
        }

        fun readLongArrayOrNull(tag: Int): LongArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            LongArray(readInt(0)) { readLong(0) }
        }

        fun readBooleanArrayOrNull(tag: Int): BooleanArray? = skipToTagOrNull(tag) {
            require(it.type.toInt() == 9) { "type mismatch" }
            BooleanArray(readInt(0)) { readBoolean(0) }
        }

        fun readByteArrayOrNull(tag: Int): ByteArray? = skipToTagOrNull(tag) {
            when (it.type) {
                LIST -> ByteArray(readInt(0)) { readByte(0) }
                SIMPLE_LIST -> {
                    val head = readHead()
                    readHead()
                    check(head.type.toInt() == 0) { "type mismatch" }
                    input.readBytes(readInt(0).also { println("list size=$it") })
                }
                else -> error("type mismatch")
            }
        }

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun <T> readObject(default: T, tag: Int): T = when (default) {
            is Byte -> readByte(tag)
            is Boolean -> readBoolean(tag)
            is Short -> readShort(tag)
            is Int -> readInt(tag)
            is Long -> readLong(tag)
            is Float -> readFloat(tag)
            is Double -> readDouble(tag)
            is String -> readString(tag)
            else -> error("unsupported type")
        } as T

        fun readStringOrNull(tag: Int): String? = skipToTagOrNull(tag) { head ->
            return when (head.type) {
                STRING1 -> input.readString(input.readUByte().toInt(), charset = charset.kotlinCharset)
                STRING4 -> input.readString(
                    input.readUInt().toInt().also { require(it in 1 until 104857600) { "bad string length: $it" } },
                    charset = charset.kotlinCharset
                )
                else -> error("type mismatch: ${head.type}, expecting 6 or 7 (for string)")
            }
        }

        fun readLongOrNull(tag: Int): Long? = skipToTagOrNull(tag) {
            return when (it.type) {
                ZERO_TYPE -> 0
                BYTE -> input.readByte().toLong()
                SHORT -> input.readShort().toLong()
                INT -> input.readInt().toLong()
                LONG -> input.readLong()
                else -> error("type mismatch ${it.type} when reading tag $tag")
            }
        }

        fun readShortOrNull(tag: Int): Short? = skipToTagOrNull(tag) {
            return when (it.type.toInt()) {
                12 -> 0
                0 -> input.readByte().toShort()
                1 -> input.readShort()
                else -> error("type mismatch: ${it.type}")
            }
        }

        fun readIntOrNull(tag: Int): Int? = skipToTagOrNull(tag) {
            return when (it.type.toInt()) {
                12 -> 0
                0 -> input.readByte().toInt()
                1 -> input.readShort().toInt()
                2 -> input.readInt()
                else -> error("type mismatch: ${it.type}")
            }
        }

        fun readByteOrNull(tag: Int): Byte? = skipToTagOrNull(tag) {
            return when (it.type.toInt()) {
                12 -> 0
                0 -> input.readByte()
                else -> error("type mismatch")
            }
        }

        fun readFloatOrNull(tag: Int): Float? = skipToTagOrNull(tag) {
            return when (it.type.toInt()) {
                12 -> 0f
                4 -> input.readFloat()
                else -> error("type mismatch: ${it.type}")
            }
        }

        fun readDoubleOrNull(tag: Int): Double? = skipToTagOrNull(tag) {
            return when (it.type.toInt()) {
                12 -> 0.0
                4 -> input.readFloat().toDouble()
                5 -> input.readDouble()
                else -> error("type mismatch: ${it.type}")
            }
        }

        fun readBooleanOrNull(tag: Int): Boolean? = this.readByteOrNull(tag)?.let { it.toInt() != 0 }


        private fun skipField() {
            skipField(readHead().type)
        }

        private fun skipToStructEnd() {
            var head: JceHead
            do {
                head = readHead()
                skipField(head.type)
            } while (head.type.toInt() != 11)
        }

        @UseExperimental(ExperimentalUnsignedTypes::class)
        @PublishedApi
        internal fun skipField(type: Byte) = when (type.toInt()) {
            0 -> this.input.discardExact(1)
            1 -> this.input.discardExact(2)
            2 -> this.input.discardExact(4)
            3 -> this.input.discardExact(8)
            4 -> this.input.discardExact(4)
            5 -> this.input.discardExact(8)
            6 -> this.input.discardExact(this.input.readUByte().toInt())
            7 -> this.input.discardExact(this.input.readInt())
            8 -> { // map
                repeat(this.readInt(0) * 2) {
                    skipField()
                }
            }
            9 -> { // list
                repeat(this.readInt(0)) {
                    skipField()
                }
            }
            10 -> this.skipToStructEnd()
            11, 12 -> {

            }
            13 -> {
                val head = readHead()
                check(head.type.toInt() == 0) { "skipField with invalid type, type value: " + type + ", " + head.type }
                this.input.discardExact(this.readInt(0))
            }
            else -> error("invalid type: $type")
        }

    }

    companion object {
        val UTF8 = Jce(JceCharset.UTF8)
        val GBK = Jce(JceCharset.GBK)

        fun byCharSet(c: JceCharset): Jce {
            return if (c == JceCharset.UTF8) {
                UTF8
            } else {
                GBK
            }
        }

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

        private fun Any?.getClassName(): String =
            (if (this == null) Unit::class else this::class).qualifiedName?.split(".")?.takeLast(2)?.joinToString(".") ?: "<unnamed class>"
    }

    fun <T> dumpAsPacket(serializer: SerializationStrategy<T>, obj: T): ByteReadPacket {
        val encoder = BytePacketBuilder()
        val dumper = JceEncoder(encoder)
        dumper.encode(serializer, obj)
        return encoder.build()
    }

    override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray {
        return dumpAsPacket(serializer, obj).readBytes()
    }

    /**
     * 注意 close [packet]!!
     */
    fun <T> load(deserializer: DeserializationStrategy<T>, packet: ByteReadPacket, length: Int = packet.remaining.toInt()): T {
        return JceDecoder(JceInput(packet, length.toLong())).decode(deserializer)
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        return bytes.toReadPacket().use {
            val decoder = JceDecoder(JceInput(it))
            decoder.decode(deserializer)
        }
    }
}

internal inline fun <R> Jce.JceInput.skipToTagOrNull(tag: Int, block: (JceHead) -> R): R? {
    println("skipping to $tag start")
    while (true) {
        if (isEndOfInput) { // 读不了了
            currentJceHead = null
            println("skipping to $tag: endOfInput")
            return null
        }

        var head = currentJceHead
        if (head == null) { // 没有新的 head 了
            head = readHeadOrNull() ?: return null
        }

        if (head.tag > tag) {
            println("skipping to $tag: head.tag > tag")
            return null
        }
        // readHead()
        if (head.tag == tag) {
            // readHeadOrNull()
            currentJceHead = null
            println("skipping to $tag: run block")
            return block(head)
        } else {
            println("skipping to $tag: tag not matching")
        }
        println("skipping to $tag: skipField")
        this.skipField(head.type)
        currentJceHead = readHeadOrNull()
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
inline class JceHead(private val value: Long) {
    constructor(tag: Int, type: Byte) : this(tag.toLong().shl(32) or type.toLong())

    val tag: Int get() = (value ushr 32).toInt()
    val type: Byte get() = value.toUInt().toByte()

    override fun toString(): String {
        return "JceHead(tag=$tag, type=$type)"
    }
}