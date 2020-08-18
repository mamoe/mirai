/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.io.serialization.tars.internal

import kotlinx.io.charsets.Charset
import kotlinx.io.charsets.Charsets
import kotlinx.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapEntrySerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.MapLikeSerializer
import kotlinx.serialization.internal.TaggedEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.BYTE
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.DOUBLE
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.FLOAT
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.INT
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.LIST
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.LONG
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.MAP
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.SHORT
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.SIMPLE_LIST
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.STRING1
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.STRING4
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.STRUCT_BEGIN
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.STRUCT_END
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.Tars_MAX_STRING_LENGTH
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars.Companion.ZERO_TYPE
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified A : Annotation> SerialDescriptor.findAnnotation(elementIndex: Int): A? {
    val candidates = getElementAnnotations(elementIndex).filterIsInstance<A>()
    return when (candidates.size) {
        0 -> null
        1 -> candidates[0]
        else -> throw IllegalStateException("There are duplicate annotations of type ${A::class} in the descriptor $this")
    }
}

internal fun getSerialId(desc: SerialDescriptor, index: Int): Int? = desc.findAnnotation<TarsId>(index)?.id

//@Suppress("DEPRECATION_ERROR")
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class TarsOld internal constructor(private val charset: Charset, override val serializersModule: SerializersModule = EmptySerializersModule) :
    SerialFormat, BinaryFormat {

    private inner class ListWriter(
        private val count: Int,
        private val tag: Int,
        private val parentEncoder: TarsEncoder
    ) : TarsEncoder(BytePacketBuilder()) {
        override fun SerialDescriptor.getTag(index: Int): Int {
            return 0
        }

        override fun endEncode(descriptor: SerialDescriptor) {
            parentEncoder.writeHead(LIST, this.tag)
            parentEncoder.encodeTaggedInt(0, count)
            parentEncoder.output.writePacket(this.output.build())
        }
    }

    private inner class TarsMapWriter(
        output: BytePacketBuilder
    ) : TarsEncoder(output) {
        override fun SerialDescriptor.getTag(index: Int): Int {
            return if (index % 2 == 0) 0 else 1
        }

        /*
        override fun endEncode(desc: SerialDescriptor) {
            parentEncoder.writeHead(MAP, this.tag)
            parentEncoder.encodeTaggedInt(Int.STUB_FOR_PRIMITIVE_NUMBERS_GBK, count)
            // println(this.output.toByteArray().toUHexString())
            parentEncoder.output.write(this.output.toByteArray())
        }*/

    }

    /**
     * From: com.qq.taf.Tars.TarsOutputStream
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    @OptIn(ExperimentalIoApi::class)
    private open inner class TarsEncoder(
        val output: BytePacketBuilder
    ) : TaggedEncoder<Int>() {
        override val serializersModule get() = this@TarsOld.serializersModule

        override fun SerialDescriptor.getTag(index: Int): Int {
            return getSerialId(this, index) ?: error("cannot find @SerialId")
        }

        /**
         * 序列化最开始的时候的
         */
        override fun beginStructure(
            descriptor: SerialDescriptor
        ): CompositeEncoder =
            when (descriptor.kind) {
                StructureKind.LIST -> this
                StructureKind.MAP -> this
                StructureKind.CLASS, StructureKind.OBJECT -> this
                is PolymorphicKind -> this
                else -> throw SerializationException("Primitives are not supported at top-level")
            }

        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) = when {
            serializer.descriptor.kind == StructureKind.MAP -> {
                try {
                    val entries = (value as Map<*, *>).entries
                    val serializer = (serializer as MapLikeSerializer<Any?, Any?, T, *>)
                    val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)

                    this.writeHead(MAP, currentTag)
                    this.encodeTaggedInt(0, entries.count())
                    SetSerializer(mapEntrySerial).serialize(TarsMapWriter(this.output), entries)
                } catch (e: Exception) {
                    super.encodeSerializableValue(serializer, value)
                }
            }
            serializer.descriptor.kind == StructureKind.LIST
                && value is ByteArray -> encodeTaggedByteArray(popTag(), value as ByteArray)
            serializer.descriptor.kind == StructureKind.LIST
                && serializer.descriptor.getElementDescriptor(0) is PrimitiveKind -> {
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
            serializer.descriptor.kind == StructureKind.LIST && value is Array<*> -> {
                if (serializer.descriptor.getElementDescriptor(0).kind is PrimitiveKind.BYTE) {
                    encodeTaggedByteArray(popTag(), (value as Array<Byte>).toByteArray())
                } else
                    serializer.serialize(
                        ListWriter((value as Array<*>).size, popTag(), this),
                        value
                    )
            }
            serializer.descriptor.kind == StructureKind.LIST -> {
                serializer.serialize(
                    ListWriter((value as Collection<*>).size, popTag(), this),
                    value
                )
            }
            serializer.descriptor.kind == StructureKind.CLASS -> {
                if (currentTagOrNull == null) {
                    serializer.serialize(this, value)
                } else {
                    this.writeHead(STRUCT_BEGIN, popTag())
                    serializer.serialize(TarsEncoder(this.output), value)
                    this.writeHead(STRUCT_END, 0)
                }
            }
            else -> {
                serializer.serialize(this, value)
            }
        }

        public override fun encodeTaggedByte(tag: Int, value: Byte) {
            if (value.toInt() == 0) {
                writeHead(ZERO_TYPE, tag)
            } else {
                writeHead(BYTE, tag)
                output.writeByte(value)
            }
        }

        public override fun encodeTaggedShort(tag: Int, value: Short) {
            if (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
                encodeTaggedByte(tag, value.toByte())
            } else {
                writeHead(SHORT, tag)
                output.writeShort(value)
            }
        }

        public override fun encodeTaggedInt(tag: Int, value: Int) {
            if (value in Short.MIN_VALUE..Short.MAX_VALUE) {
                encodeTaggedShort(tag, value.toShort())
            } else {
                writeHead(INT, tag)
                output.writeInt(value)
            }
        }

        public override fun encodeTaggedFloat(tag: Int, value: Float) {
            writeHead(FLOAT, tag)
            output.writeFloat(value)
        }

        public override fun encodeTaggedDouble(tag: Int, value: Double) {
            writeHead(DOUBLE, tag)
            output.writeDouble(value)
        }

        public override fun encodeTaggedLong(tag: Int, value: Long) {
            if (value in Int.MIN_VALUE..Int.MAX_VALUE) {
                encodeTaggedInt(tag, value.toInt())
            } else {
                writeHead(LONG, tag)
                output.writeLong(value)
            }
        }

        public override fun encodeTaggedBoolean(tag: Int, value: Boolean) {
            encodeTaggedByte(tag, if (value) 1 else 0)
        }

        public override fun encodeTaggedChar(tag: Int, value: Char) {
            encodeTaggedByte(tag, value.toByte())
        }

        public override fun encodeTaggedEnum(tag: Int, enumDescriptor: SerialDescriptor, ordinal: Int) {
            encodeTaggedInt(tag, ordinal)
        }

        public override fun encodeTaggedNull(tag: Int) {
        }

        fun encodeTaggedByteArray(tag: Int, bytes: ByteArray) {
            writeHead(SIMPLE_LIST, tag)
            writeHead(BYTE, 0)
            encodeTaggedInt(0, bytes.size)
            output.writeFully(bytes)
        }

        public override fun encodeTaggedString(tag: Int, value: String) {
            require(value.length <= Tars_MAX_STRING_LENGTH) { "string is too long for tag $tag" }
            val array = value.toByteArray(charset)
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
                is Unit -> {
                }
                else -> error("unsupported type: ${value.getClassName()}")
            }
        }

        fun writeHead(type: Byte, tag: Int) {
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

    @Suppress("MemberVisibilityCanBePrivate")
    companion object : BinaryFormat by TarsOld(Charsets.UTF_8) {

        private fun Any?.getClassName(): String =
            (if (this == null) Unit::class else this::class).simpleName?.split(".")?.takeLast(2)?.joinToString(".")
                ?: "<unnamed class>"
    }

    fun <T> dumpAsPacket(serializer: SerializationStrategy<T>, obj: T): ByteReadPacket {
        val encoder = BytePacketBuilder()
        val dumper = TarsEncoder(encoder)
        dumper.encodeSerializableValue(serializer, obj)
        return encoder.build()
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return dumpAsPacket(serializer, value).readBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        error("Use TarsNew.")
    }
}

internal fun Input.readString(length: Int, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length), charset = charset)
