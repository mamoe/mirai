/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("PrivatePropertyName")

package net.mamoe.mirai.qqandroid.utils.io.serialization.tars.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class TarsDecoder(
    val input: TarsInput, override val serializersModule: SerializersModule
) : TaggedDecoder<TarsTag>() {
    override fun SerialDescriptor.getTag(index: Int): TarsTag {
        val annotations = this.getElementAnnotations(index)

        val id = annotations.filterIsInstance<TarsId>().single().id
        // ?: error("cannot find @TarsId or @ProtoNumber for ${this.getElementName(index)} in ${this.serialName}")
        //println("getTag: ${this.getElementName(index)}=$id")

        return TarsTagCommon(id)
    }

    private fun SerialDescriptor.getTarsTagId(index: Int): Int {
        // higher performance, don't use filterIsInstance
        val annotation = getElementAnnotations(index).firstOrNull { it is TarsId }
            ?: error("missing @TarsId for ${getElementName(index)} in ${this.serialName}")
        return (annotation as TarsId).id

    }

    private val SimpleByteArrayReader: SimpleByteArrayReaderImpl = SimpleByteArrayReaderImpl()

    private inner class SimpleByteArrayReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true

        override fun endStructure(descriptor: SerialDescriptor) {
            this@TarsDecoder.endStructure(descriptor)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            this@TarsDecoder.pushTag(TarsTagListElement)
            return this@TarsDecoder.beginStructure(descriptor)
        }

        override fun decodeByte(): Byte = input.input.readByte()
        override fun decodeShort(): Short = error("illegal access")
        override fun decodeInt(): Int = error("illegal access")
        override fun decodeLong(): Long = error("illegal access")
        override fun decodeFloat(): Float = error("illegal access")
        override fun decodeDouble(): Double = error("illegal access")
        override fun decodeBoolean(): Boolean = error("illegal access")
        override fun decodeChar(): Char = error("illegal access")
        override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = error("illegal access")
        override fun decodeString(): String = error("illegal access")

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            error("should not be reached")
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            // 不要读下一个 head
            return input.currentHead.let { input.readTarsIntValue(it) }
        }
    }

    private val ListReader: ListReaderImpl = ListReaderImpl()

    private inner class ListReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = error("should not be reached")
        override fun endStructure(descriptor: SerialDescriptor) {
            this@TarsDecoder.endStructure(descriptor)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            this@TarsDecoder.pushTag(TarsTagListElement)

            return this@TarsDecoder.beginStructure(descriptor)
        }

        override fun decodeByte(): Byte = input.useHead { input.readTarsByteValue(it) }
        override fun decodeShort(): Short = input.useHead { input.readTarsShortValue(it) }
        override fun decodeInt(): Int = input.useHead { input.readTarsIntValue(it) }
        override fun decodeLong(): Long = input.useHead { input.readTarsLongValue(it) }
        override fun decodeFloat(): Float = input.useHead { input.readTarsFloatValue(it) }
        override fun decodeDouble(): Double = input.useHead { input.readTarsDoubleValue(it) }
        override fun decodeBoolean(): Boolean = input.useHead { input.readTarsBooleanValue(it) }
        override fun decodeChar(): Char = decodeByte().toChar()
        override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeInt()
        override fun decodeString(): String = input.useHead { input.readTarsStringValue(it) }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            //println("decodeCollectionSize: ${descriptor.serialName}")
            // 不读下一个 head
            return input.useHead { input.readTarsIntValue(it) }
        }
    }


    private val MapReader: MapReaderImpl = MapReaderImpl()

    private inner class MapReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = error("stub")

        override fun endStructure(descriptor: SerialDescriptor) {
            this@TarsDecoder.endStructure(descriptor)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            println { "MapReader.beginStructure: ${input.currentHead}" }
            this@TarsDecoder.pushTag(
                when (input.currentHead.tag) {
                    0 -> TarsTagMapEntryKey
                    1 -> TarsTagMapEntryValue
                    else -> error("illegal map entry head: ${input.currentHead.tag}")
                }
            )
            return this@TarsDecoder.beginStructure(descriptor)
        }

        override fun decodeByte(): Byte = input.useHead { input.readTarsByteValue(it) }
        override fun decodeShort(): Short = input.useHead { input.readTarsShortValue(it) }
        override fun decodeInt(): Int = input.useHead { input.readTarsIntValue(it) }
        override fun decodeLong(): Long = input.useHead { input.readTarsLongValue(it) }
        override fun decodeFloat(): Float = input.useHead { input.readTarsFloatValue(it) }
        override fun decodeDouble(): Double = input.useHead { input.readTarsDoubleValue(it) }

        override fun decodeBoolean(): Boolean = input.useHead { input.readTarsBooleanValue(it) }
        override fun decodeChar(): Char = decodeByte().toChar()
        override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeInt()
        override fun decodeString(): String = input.useHead { input.readTarsStringValue(it) }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            println { "decodeCollectionSize in MapReader: ${descriptor.serialName}" }
            // 不读下一个 head
            return input.useHead { input.readTarsIntValue(it) }
        }
    }


    override fun endStructure(descriptor: SerialDescriptor) {
        structureHierarchy--
        println { "endStructure: ${descriptor.serialName}" }
        if (currentTagOrNull?.isSimpleByteArray == true) {
            input.prepareNextHead() // read to next head
        }
        if (descriptor.kind == StructureKind.CLASS) {
            if (currentTagOrNull == null) {
                return
            }
            while (true) {
                val currentHead = input.currentHeadOrNull ?: return
                if (currentHead.type == Tars.STRUCT_END) {
                    input.prepareNextHead()
                    //println("current end")
                    break
                }
                //println("current $currentHead")
                input.skipField(currentHead.type)
                input.prepareNextHead()
            }
            // pushTag(TarsTag(0, true))
            // skip STRUCT_END
            // popTag()
        }
    }


    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        var debuggingMode: Boolean = false

        var structureHierarchy: Int = 0

        inline fun println(value: () -> String) {
            if (debuggingMode) {
                kotlin.io.println("    ".repeat(structureHierarchy) + value())
            }
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun println(value: Any? = "") {
            if (debuggingMode) {
                kotlin.io.println("    ".repeat(structureHierarchy) + value)
            }
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        println()
        println { "beginStructure: ${descriptor.serialName}" }
        structureHierarchy++
        return when (descriptor.kind) {
            is PrimitiveKind -> this@TarsDecoder

            StructureKind.MAP -> {
                //println("!! MAP")
                val tag = popTag()
                return input.skipToHeadAndUseIfPossibleOrFail(tag.id) {
                    it.checkType(Tars.MAP, "beginStructure", tag, descriptor)
                    MapReader
                }
            }
            StructureKind.LIST -> {
                //println("!! ByteArray")
                //println("decoderTag: $currentTagOrNull")
                //println("TarsHead: " + Tars.currentHeadOrNull)
                return input.skipToHeadAndUseIfPossibleOrFail(currentTag.id) {
                    // don't check type. it's polymorphic

                    //println("listHead: $it")
                    when (it.type) {
                        Tars.SIMPLE_LIST -> {
                            currentTag.isSimpleByteArray = true
                            input.nextHead() // 无用的元素类型
                            SimpleByteArrayReader
                        }
                        Tars.LIST -> ListReader
                        else -> error("type mismatch. Expected SIMPLE_LIST or LIST, got $it instead")
                    }
                }
            }
            StructureKind.CLASS -> {
                currentTagOrNull ?: return this@TarsDecoder // outermost

                //println("!! CLASS")
                //println("decoderTag: $currentTag")
                //println("TarsHead: " + Tars.currentHeadOrNull)
                val tag = popTag()
                return input.skipToHeadAndUseIfPossibleOrFail(tag.id) { TarsHead ->
                    TarsHead.checkType(Tars.STRUCT_BEGIN, "beginStructure", tag, descriptor)

                    repeat(descriptor.elementsCount) {
                        pushTag(descriptor.getTag(descriptor.elementsCount - it - 1)) // better performance
                    }
                    this // independent tag stack
                }
            }

            StructureKind.OBJECT -> error("unsupported StructureKind.OBJECT: ${descriptor.serialName}")
            is SerialKind.ENUM -> error("unsupported UnionKind: ${descriptor.serialName}")
            is PolymorphicKind -> error("unsupported PolymorphicKind: ${descriptor.serialName}")
            is SerialKind.CONTEXTUAL -> error("unsupported PolymorphicKind: ${descriptor.serialName}")
        }
    }

    override fun decodeSequentially(): Boolean = false
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        var tarsHead = input.currentHeadOrNull ?: kotlin.run {
            println("decodeElementIndex: currentHead == null")
            return CompositeDecoder.DECODE_DONE
        }

        println { "decodeElementIndex: ${input.currentHead}" }
        while (!input.input.endOfInput) {
            if (tarsHead.type == Tars.STRUCT_END) {
                println { "decodeElementIndex: ${input.currentHead}" }
                return CompositeDecoder.DECODE_DONE
            }

            repeat(descriptor.elementsCount) {
                val tag = descriptor.getTarsTagId(it)
                if (tag == tarsHead.tag) {
                    println {
                        "name=" + descriptor.getElementName(
                            it
                        )
                    }
                    return it
                }
            }

            input.skipField(tarsHead.type)
            if (!input.prepareNextHead()) {
                println { "decodeElementIndex EOF" }
                break
            }
            tarsHead = input.currentHead
            println { "next! $tarsHead" }
        }

        return CompositeDecoder.DECODE_DONE // optional support
    }

    override fun decodeTaggedInt(tag: TarsTag): Int =
        kotlin.runCatching { input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsIntValue(it) } }.getOrElse {
            throw IllegalStateException("$tag", it)
        }

    override fun decodeTaggedByte(tag: TarsTag): Byte =
        kotlin.runCatching { input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsByteValue(it) } }.getOrElse {
            throw IllegalStateException("$tag", it)
        }

    override fun decodeTaggedBoolean(tag: TarsTag): Boolean =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsBooleanValue(it) }

    override fun decodeTaggedFloat(tag: TarsTag): Float =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsFloatValue(it) }

    override fun decodeTaggedDouble(tag: TarsTag): Double =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsDoubleValue(it) }

    override fun decodeTaggedShort(tag: TarsTag): Short =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsShortValue(it) }

    override fun decodeTaggedLong(tag: TarsTag): Long =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsLongValue(it) }

    override fun decodeTaggedString(tag: TarsTag): String =
        input.skipToHeadAndUseIfPossibleOrFail(tag.id) { input.readTarsStringValue(it) }

    override fun decodeTaggedNotNullMark(tag: TarsTag): Boolean {
        return input.skipToHeadOrNull(tag.id) != null
    }
}
