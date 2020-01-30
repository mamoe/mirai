/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.*
import kotlinx.serialization.*
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.internal.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import kotlinx.serialization.protobuf.ProtobufDecodingException
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport.Varint.decodeSignedVarintInt
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport.Varint.decodeSignedVarintLong
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport.Varint.decodeVarint
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport.Varint.encodeVarint

internal typealias ProtoDesc = Pair<Int, ProtoNumberType>

internal fun extractParameters(desc: SerialDescriptor, index: Int, zeroBasedDefault: Boolean = false): ProtoDesc {
    val idx = getSerialId(desc, index) ?: (if (zeroBasedDefault) index else index + 1)
    val format = desc.findAnnotation<ProtoType>(index)?.type
        ?: ProtoNumberType.DEFAULT
    return idx to format
}


class ProtoBufWithNullableSupport(context: SerialModule = EmptyModule) : AbstractSerialFormat(context), BinaryFormat {

    internal open inner class ProtobufWriter(val encoder: ProtobufEncoder) : TaggedEncoder<ProtoDesc>() {
        public override val context
            get() = this@ProtoBufWithNullableSupport.context

        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when (desc.kind) {
            StructureKind.LIST -> RepeatedWriter(encoder, currentTag)
            StructureKind.CLASS, UnionKind.OBJECT, is PolymorphicKind -> ObjectWriter(currentTagOrNull, encoder)
            StructureKind.MAP -> MapRepeatedWriter(currentTagOrNull, encoder)
            else -> throw SerializationException("Primitives are not supported at top-level")
        }

        override fun encodeTaggedInt(tag: ProtoDesc, value: Int) = encoder.writeInt(value, tag.first, tag.second)
        override fun encodeTaggedByte(tag: ProtoDesc, value: Byte) = encoder.writeInt(value.toInt(), tag.first, tag.second)
        override fun encodeTaggedShort(tag: ProtoDesc, value: Short) = encoder.writeInt(value.toInt(), tag.first, tag.second)
        override fun encodeTaggedLong(tag: ProtoDesc, value: Long) = encoder.writeLong(value, tag.first, tag.second)
        override fun encodeTaggedFloat(tag: ProtoDesc, value: Float) = encoder.writeFloat(value, tag.first)
        override fun encodeTaggedDouble(tag: ProtoDesc, value: Double) = encoder.writeDouble(value, tag.first)
        override fun encodeTaggedBoolean(tag: ProtoDesc, value: Boolean) = encoder.writeInt(if (value) 1 else 0, tag.first, ProtoNumberType.DEFAULT)
        override fun encodeTaggedChar(tag: ProtoDesc, value: Char) = encoder.writeInt(value.toInt(), tag.first, tag.second)
        override fun encodeTaggedString(tag: ProtoDesc, value: String) = encoder.writeString(value, tag.first)
        override fun encodeTaggedEnum(
            tag: ProtoDesc,
            enumDescription: SerialDescriptor,
            ordinal: Int
        ) = encoder.writeInt(
            extractParameters(enumDescription, ordinal, zeroBasedDefault = true).first,
            tag.first,
            ProtoNumberType.DEFAULT
        )

        override fun SerialDescriptor.getTag(index: Int) = this.getProtoDesc(index)

        // MIRAI MODIFY START:
        override fun encodeTaggedNull(tag: ProtoDesc) {

        }

        override fun <T : Any> encodeNullableSerializableValue(serializer: SerializationStrategy<T>, value: T?) {
            if (value == null) {
                encodeTaggedNull(popTag())
            } else encodeSerializableValue(serializer, value)
        }
        // MIRAI MODIFY END

        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) = when {
            // encode maps as collection of map entries, not merged collection of key-values
            serializer.descriptor is MapLikeDescriptor -> {
                val serializer = (serializer as MapLikeSerializer<Any?, Any?, T, *>)
                val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)
                HashSetSerializer(mapEntrySerial).serialize(this, (value as Map<*, *>).entries)
            }
            serializer.descriptor == ByteArraySerializer.descriptor -> encoder.writeBytes(value as ByteArray, popTag().first)
            else -> serializer.serialize(this, value)
        }
    }

    internal open inner class ObjectWriter(
        val parentTag: ProtoDesc?, private val parentEncoder: ProtobufEncoder,
        private val stream: ByteArrayOutputStream = ByteArrayOutputStream()
    ) : ProtobufWriter(ProtobufEncoder(stream)) {
        override fun endEncode(desc: SerialDescriptor) {
            if (parentTag != null) {
                parentEncoder.writeBytes(stream.toByteArray(), parentTag.first)
            } else {
                parentEncoder.out.write(stream.toByteArray())
            }
        }
    }

    internal inner class MapRepeatedWriter(parentTag: ProtoDesc?, parentEncoder: ProtobufEncoder) : ObjectWriter(parentTag, parentEncoder) {
        override fun SerialDescriptor.getTag(index: Int): ProtoDesc =
            if (index % 2 == 0) 1 to (parentTag?.second ?: ProtoNumberType.DEFAULT)
            else 2 to (parentTag?.second ?: ProtoNumberType.DEFAULT)
    }

    internal inner class RepeatedWriter(encoder: ProtobufEncoder, val curTag: ProtoDesc) : ProtobufWriter(encoder) {
        override fun SerialDescriptor.getTag(index: Int) = curTag
    }

    internal class ProtobufEncoder(val out: ByteArrayOutputStream) {

        fun writeBytes(bytes: ByteArray, tag: Int) {
            val header = encode32((tag shl 3) or SIZE_DELIMITED)
            val len = encode32(bytes.size)
            out.write(header)
            out.write(len)
            out.write(bytes)
        }

        fun writeInt(value: Int, tag: Int, format: ProtoNumberType) {
            val wireType = if (format == ProtoNumberType.FIXED) i32 else VARINT
            val header = encode32((tag shl 3) or wireType)
            val content = encode32(value, format)
            out.write(header)
            out.write(content)
        }

        fun writeLong(value: Long, tag: Int, format: ProtoNumberType) {
            val wireType = if (format == ProtoNumberType.FIXED) i64 else VARINT
            val header = encode32((tag shl 3) or wireType)
            val content = encode64(value, format)
            out.write(header)
            out.write(content)
        }

        fun writeString(value: String, tag: Int) {
            val bytes = value.toUtf8Bytes()
            writeBytes(bytes, tag)
        }

        fun writeDouble(value: Double, tag: Int) {
            val header = encode32((tag shl 3) or i64)
            val content = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value).array()
            out.write(header)
            out.write(content)
        }

        fun writeFloat(value: Float, tag: Int) {
            val header = encode32((tag shl 3) or i32)
            val content = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array()
            out.write(header)
            out.write(content)
        }

        private fun encode32(number: Int, format: ProtoNumberType = ProtoNumberType.DEFAULT): ByteArray =
            when (format) {
                ProtoNumberType.FIXED -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(number).array()
                ProtoNumberType.DEFAULT -> encodeVarint(number.toLong())
                ProtoNumberType.SIGNED -> encodeVarint(((number shl 1) xor (number shr 31)))
            }


        private fun encode64(number: Long, format: ProtoNumberType = ProtoNumberType.DEFAULT): ByteArray =
            when (format) {
                ProtoNumberType.FIXED -> ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(number).array()
                ProtoNumberType.DEFAULT -> encodeVarint(number)
                ProtoNumberType.SIGNED -> encodeVarint((number shl 1) xor (number shr 63))
            }
    }

    private open inner class ProtobufReader(val decoder: ProtobufDecoder) : TaggedDecoder<ProtoDesc>() {
        override val context: SerialModule
            get() = this@ProtoBufWithNullableSupport.context

        private val indexByTag: MutableMap<Int, Int> = mutableMapOf()
        private fun findIndexByTag(desc: SerialDescriptor, serialId: Int, zeroBasedDefault: Boolean = false): Int =
            (0 until desc.elementsCount).firstOrNull {
                extractParameters(
                    desc,
                    it,
                    zeroBasedDefault
                ).first == serialId
            } ?: -1

        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = when (desc.kind) {
            StructureKind.LIST -> RepeatedReader(decoder, currentTag)
            StructureKind.CLASS, UnionKind.OBJECT, is PolymorphicKind ->
                ProtobufReader(makeDelimited(decoder, currentTagOrNull))
            StructureKind.MAP -> MapEntryReader(makeDelimited(decoder, currentTagOrNull), currentTagOrNull)
            else -> throw SerializationException("Primitives are not supported at top-level")
        }

        override fun decodeTaggedBoolean(tag: ProtoDesc): Boolean = when (val i = decoder.nextInt(ProtoNumberType.DEFAULT)) {
            0 -> false
            1 -> true
            else -> throw ProtobufDecodingException("Expected boolean value (0 or 1), found $i")
        }

        override fun decodeTaggedByte(tag: ProtoDesc): Byte = decoder.nextInt(tag.second).toByte()
        override fun decodeTaggedShort(tag: ProtoDesc): Short = decoder.nextInt(tag.second).toShort()
        override fun decodeTaggedInt(tag: ProtoDesc): Int = decoder.nextInt(tag.second)
        override fun decodeTaggedLong(tag: ProtoDesc): Long = decoder.nextLong(tag.second)
        override fun decodeTaggedFloat(tag: ProtoDesc): Float = decoder.nextFloat()
        override fun decodeTaggedDouble(tag: ProtoDesc): Double = decoder.nextDouble()
        override fun decodeTaggedChar(tag: ProtoDesc): Char = decoder.nextInt(tag.second).toChar()
        override fun decodeTaggedString(tag: ProtoDesc): String = decoder.nextString()
        override fun decodeTaggedEnum(tag: ProtoDesc, enumDescription: SerialDescriptor): Int =
            findIndexByTag(enumDescription, decoder.nextInt(ProtoNumberType.DEFAULT), zeroBasedDefault = true)

        @Suppress("UNCHECKED_CAST")
        override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T = when {
            // encode maps as collection of map entries, not merged collection of key-values
            deserializer.descriptor is MapLikeDescriptor -> {
                val serializer = (deserializer as MapLikeSerializer<Any?, Any?, T, *>)
                val mapEntrySerial = MapEntrySerializer(serializer.keySerializer, serializer.valueSerializer)
                val setOfEntries = HashSetSerializer(mapEntrySerial).deserialize(this)
                setOfEntries.associateBy({ it.key }, { it.value }) as T
            }
            deserializer.descriptor == ByteArraySerializer.descriptor -> decoder.nextObject() as T
            else -> deserializer.deserialize(this)
        }

        override fun SerialDescriptor.getTag(index: Int) = this.getProtoDesc(index)

        override fun decodeElementIndex(desc: SerialDescriptor): Int {
            while (true) {
                if (decoder.curId == -1) // EOF
                    return READ_DONE
                val ind = indexByTag.getOrPut(decoder.curId) { findIndexByTag(desc, decoder.curId) }
                if (ind == -1) // not found
                    decoder.skipElement()
                else return ind
            }
        }
    }

    private inner class RepeatedReader(decoder: ProtobufDecoder, val targetTag: ProtoDesc) : ProtobufReader(decoder) {
        private var ind = -1

        override fun decodeElementIndex(desc: SerialDescriptor) = if (decoder.curId == targetTag.first) ++ind else READ_DONE
        override fun SerialDescriptor.getTag(index: Int): ProtoDesc = targetTag
    }

    private inner class MapEntryReader(decoder: ProtobufDecoder, val parentTag: ProtoDesc?) : ProtobufReader(decoder) {
        override fun SerialDescriptor.getTag(index: Int): ProtoDesc =
            if (index % 2 == 0) 1 to (parentTag?.second ?: ProtoNumberType.DEFAULT)
            else 2 to (parentTag?.second ?: ProtoNumberType.DEFAULT)
    }

    internal class ProtobufDecoder(val inp: ByteArrayInputStream) {
        val curId
            get() = curTag.first
        private var curTag: Pair<Int, Int> = -1 to -1

        init {
            readTag()
        }

        private fun readTag(): Pair<Int, Int> {
            val header = decode32(eofAllowed = true)
            curTag = if (header == -1) {
                -1 to -1
            } else {
                val wireType = header and 0b111
                val fieldId = header ushr 3
                fieldId to wireType
            }
            return curTag
        }

        fun skipElement() {
            when (curTag.second) {
                VARINT -> nextInt(ProtoNumberType.DEFAULT)
                i64 -> nextLong(ProtoNumberType.FIXED)
                SIZE_DELIMITED -> nextObject()
                i32 -> nextInt(ProtoNumberType.FIXED)
                else -> throw ProtobufDecodingException("Unsupported start group or end group wire type")
            }
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun assertWireType(expected: Int) {
            if (curTag.second != expected) throw ProtobufDecodingException("Expected wire type $expected, but found ${curTag.second}")
        }

        fun nextObject(): ByteArray {
            assertWireType(SIZE_DELIMITED)
            val len = decode32()
            check(len >= 0)
            val ans = inp.readExactNBytes(len)
            readTag()
            return ans
        }

        fun nextInt(format: ProtoNumberType): Int {
            val wireType = if (format == ProtoNumberType.FIXED) i32 else VARINT
            assertWireType(wireType)
            val ans = decode32(format)
            readTag()
            return ans
        }

        fun nextLong(format: ProtoNumberType): Long {
            val wireType = if (format == ProtoNumberType.FIXED) i64 else VARINT
            assertWireType(wireType)
            val ans = decode64(format)
            readTag()
            return ans
        }

        fun nextFloat(): Float {
            assertWireType(i32)
            val ans = inp.readToByteBuffer(4).order(ByteOrder.LITTLE_ENDIAN).getFloat()
            readTag()
            return ans
        }

        fun nextDouble(): Double {
            assertWireType(i64)
            val ans = inp.readToByteBuffer(8).order(ByteOrder.LITTLE_ENDIAN).getDouble()
            readTag()
            return ans
        }

        fun nextString(): String {
            val bytes = this.nextObject()
            return stringFromUtf8Bytes(bytes)
        }

        private fun decode32(format: ProtoNumberType = ProtoNumberType.DEFAULT, eofAllowed: Boolean = false): Int = when (format) {
            ProtoNumberType.DEFAULT -> decodeVarint(inp, 64, eofAllowed).toInt()
            ProtoNumberType.SIGNED -> decodeSignedVarintInt(inp)
            ProtoNumberType.FIXED -> inp.readToByteBuffer(4).order(ByteOrder.LITTLE_ENDIAN).getInt()
        }

        private fun decode64(format: ProtoNumberType = ProtoNumberType.DEFAULT): Long = when (format) {
            ProtoNumberType.DEFAULT -> decodeVarint(inp, 64)
            ProtoNumberType.SIGNED -> decodeSignedVarintLong(inp)
            ProtoNumberType.FIXED -> inp.readToByteBuffer(8).order(ByteOrder.LITTLE_ENDIAN).getLong()
        }
    }

    /**
     *  Source for all varint operations:
     *  https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/util/Varint.java
     */
    internal object Varint {
        internal fun encodeVarint(inp: Int): ByteArray {
            var value = inp
            val byteArrayList = ByteArray(10)
            var i = 0
            while (value and 0xFFFFFF80.toInt() != 0) {
                byteArrayList[i++] = ((value and 0x7F) or 0x80).toByte()
                value = value ushr 7
            }
            byteArrayList[i] = (value and 0x7F).toByte()
            val out = ByteArray(i + 1)
            while (i >= 0) {
                out[i] = byteArrayList[i]
                i--
            }
            return out
        }

        internal fun encodeVarint(inp: Long): ByteArray {
            var value = inp
            val byteArrayList = ByteArray(10)
            var i = 0
            while (value and 0x7FL.inv() != 0L) {
                byteArrayList[i++] = ((value and 0x7F) or 0x80).toByte()
                value = value ushr 7
            }
            byteArrayList[i] = (value and 0x7F).toByte()
            val out = ByteArray(i + 1)
            while (i >= 0) {
                out[i] = byteArrayList[i]
                i--
            }
            return out
        }

        internal fun decodeVarint(inp: InputStream, bitLimit: Int = 32, eofOnStartAllowed: Boolean = false): Long {
            var result = 0L
            var shift = 0
            var b: Int
            do {
                if (shift >= bitLimit) {
                    // Out of range
                    throw ProtobufDecodingException("Varint too long: exceeded $bitLimit bits")
                }
                // Get 7 bits from next byte
                b = inp.read()
                if (b == -1) {
                    if (eofOnStartAllowed && shift == 0) return -1
                    else throw IOException("Unexpected EOF")
                }
                result = result or (b.toLong() and 0x7FL shl shift)
                shift += 7
            } while (b and 0x80 != 0)
            return result
        }

        internal fun decodeSignedVarintInt(inp: InputStream): Int {
            val raw = decodeVarint(inp, 32).toInt()
            val temp = raw shl 31 shr 31 xor raw shr 1
            // This extra step lets us deal with the largest signed values by treating
            // negative results from read unsigned methods as like unsigned values.
            // Must re-flip the top bit if the original read value had it set.
            return temp xor (raw and (1 shl 31))
        }

        internal fun decodeSignedVarintLong(inp: InputStream): Long {
            val raw = decodeVarint(inp, 64)
            val temp = raw shl 63 shr 63 xor raw shr 1
            // This extra step lets us deal with the largest signed values by treating
            // negative results from read unsigned methods as like unsigned values
            // Must re-flip the top bit if the original read value had it set.
            return temp xor (raw and (1L shl 63))

        }
    }

    companion object : BinaryFormat {
        public override val context: SerialModule get() = plain.context

        // todo: make more memory-efficient
        private fun makeDelimited(decoder: ProtobufDecoder, parentTag: ProtoDesc?): ProtobufDecoder {
            if (parentTag == null) return decoder
            val bytes = decoder.nextObject()
            return ProtobufDecoder(ByteArrayInputStream(bytes))
        }

        private fun SerialDescriptor.getProtoDesc(index: Int): ProtoDesc {
            return extractParameters(this, index)
        }

        internal const val VARINT = 0
        internal const val i64 = 1
        internal const val SIZE_DELIMITED = 2
        internal const val i32 = 5

        val plain = ProtoBufWithNullableSupport()

        override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray = plain.dump(serializer, obj)
        override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T = plain.load(deserializer, bytes)
        override fun install(module: SerialModule) = throw IllegalStateException("You should not install anything to global instance")
    }

    override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray {
        val encoder = ByteArrayOutputStream()
        val dumper = ProtobufWriter(ProtobufEncoder(encoder))
        dumper.encode(serializer, obj)
        return encoder.toByteArray()
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val stream = ByteArrayInputStream(bytes)
        val reader = ProtobufReader(ProtobufDecoder(stream))
        return reader.decode(deserializer)
    }

}

internal fun InputStream.readExactNBytes(bytes: Int): ByteArray {
    val array = ByteArray(bytes)
    var read = 0
    while (read < bytes) {
        val i = this.read(array, read, bytes - read)
        if (i == -1) throw IOException("Unexpected EOF")
        read += i
    }
    return array
}

internal fun InputStream.readToByteBuffer(bytes: Int): ByteBuffer {
    val arr = readExactNBytes(bytes)
    val buf = ByteBuffer.allocate(bytes)
    buf.put(arr).flip()
    return buf
}