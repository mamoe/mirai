/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("PrivatePropertyName")

package net.mamoe.mirai.qqandroid.io.serialization.jce

import kotlinx.serialization.*
import kotlinx.serialization.builtins.AbstractDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.SerialModule


@OptIn(InternalSerializationApi::class) // 将来 kotlinx 修改后再复制过来 mirai.
internal class JceDecoder(
    val jce: JceInput, override val context: SerialModule
) : TaggedDecoder<JceTag>() {
    override val updateMode: UpdateMode
        get() = UpdateMode.BANNED

    override fun SerialDescriptor.getTag(index: Int): JceTag {
        val annotations = this.getElementAnnotations(index)

        val id = annotations.filterIsInstance<JceId>().single().id
        // ?: error("cannot find @JceId or @ProtoId for ${this.getElementName(index)} in ${this.serialName}")
        //println("getTag: ${this.getElementName(index)}=$id")

        return JceTagCommon(id)
    }

    private fun SerialDescriptor.getJceTagId(index: Int): Int {
        //println("getTag: ${getElementName(index)}")
        return getElementAnnotations(index).filterIsInstance<JceId>().singleOrNull()?.id
            ?: error("missing @JceId for ${getElementName(index)} in ${this.serialName}")
    }

    private val SimpleByteArrayReader: SimpleByteArrayReaderImpl = SimpleByteArrayReaderImpl()

    private inner class SimpleByteArrayReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true

        override fun endStructure(descriptor: SerialDescriptor) {
            this@JceDecoder.endStructure(descriptor)
        }

        override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            this@JceDecoder.pushTag(JceTagListElement)
            return this@JceDecoder.beginStructure(descriptor, *typeParams)
        }

        override fun decodeByte(): Byte = jce.input.readByte()
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
            return jce.currentHead.let { jce.readJceIntValue(it) }
        }
    }

    private val ListReader: ListReaderImpl = ListReaderImpl()

    private inner class ListReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = error("should not be reached")
        override fun endStructure(descriptor: SerialDescriptor) {
            this@JceDecoder.endStructure(descriptor)
        }

        override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            this@JceDecoder.pushTag(JceTagListElement)

            return this@JceDecoder.beginStructure(descriptor, *typeParams)
        }

        override fun decodeByte(): Byte = jce.useHead { jce.readJceByteValue(it) }
        override fun decodeShort(): Short = jce.useHead { jce.readJceShortValue(it) }
        override fun decodeInt(): Int = jce.useHead { jce.readJceIntValue(it) }
        override fun decodeLong(): Long = jce.useHead { jce.readJceLongValue(it) }
        override fun decodeFloat(): Float = jce.useHead { jce.readJceFloatValue(it) }
        override fun decodeDouble(): Double = jce.useHead { jce.readJceDoubleValue(it) }
        override fun decodeBoolean(): Boolean = jce.useHead { jce.readJceBooleanValue(it) }
        override fun decodeChar(): Char = decodeByte().toChar()
        override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeInt()
        override fun decodeString(): String = jce.useHead { jce.readJceStringValue(it) }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            //println("decodeCollectionSize: ${descriptor.serialName}")
            // 不读下一个 head
            return jce.useHead { jce.readJceIntValue(it) }
        }
    }


    private val MapReader: MapReaderImpl = MapReaderImpl()

    private inner class MapReaderImpl : AbstractDecoder() {
        override fun decodeSequentially(): Boolean = true
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = error("stub")

        override fun endStructure(descriptor: SerialDescriptor) {
            this@JceDecoder.endStructure(descriptor)
        }

        private var state: Boolean = true

        override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            this@JceDecoder.pushTag(if (jce.currentHead.tag == 0) JceTagMapEntryKey else JceTagMapEntryValue)
            state = !state
            return this@JceDecoder.beginStructure(descriptor, *typeParams)
        }

        override fun decodeByte(): Byte = jce.useHead { jce.readJceByteValue(it) }
        override fun decodeShort(): Short = jce.useHead { jce.readJceShortValue(it) }
        override fun decodeInt(): Int = jce.useHead { jce.readJceIntValue(it) }
        override fun decodeLong(): Long = jce.useHead { jce.readJceLongValue(it) }
        override fun decodeFloat(): Float = jce.useHead { jce.readJceFloatValue(it) }
        override fun decodeDouble(): Double = jce.useHead { jce.readJceDoubleValue(it) }

        override fun decodeBoolean(): Boolean = jce.useHead { jce.readJceBooleanValue(it) }
        override fun decodeChar(): Char = decodeByte().toChar()
        override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeInt()
        override fun decodeString(): String = jce.useHead { jce.readJceStringValue(it) }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            //println("decodeCollectionSize in MapReader: ${descriptor.serialName}")
            // 不读下一个 head
            return jce.useHead { jce.readJceIntValue(it) }
        }
    }


    override fun endStructure(descriptor: SerialDescriptor) {
        //println("endStructure: $descriptor")
        if (currentTagOrNull?.isSimpleByteArray == true) {
            jce.prepareNextHead() // read to next head
        }
        if (descriptor.kind == StructureKind.CLASS) {
            if (currentTagOrNull == null) {
                return
            }
            while (true) {
                val currentHead = jce.currentHeadOrNull ?: return
                if (currentHead.type == Jce.STRUCT_END) {
                    jce.prepareNextHead()
                    //println("current end")
                    break
                }
                //println("current $currentHead")
                jce.skipField(currentHead.type)
                jce.prepareNextHead()
            }
            // pushTag(JceTag(0, true))
            // skip STRUCT_END
            // popTag()
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        //println()
        //println("beginStructure: ${descriptor.serialName}")
        return when (descriptor.kind) {
            is PrimitiveKind -> this@JceDecoder

            StructureKind.MAP -> {
                //println("!! MAP")
                val tag = popTag()
                return jce.skipToHeadAndUseIfPossibleOrFail(tag.id) {
                    it.checkType(Jce.MAP, "beginStructure", tag, descriptor)
                    MapReader
                }
            }
            StructureKind.LIST -> {
                //println("!! ByteArray")
                //println("decoderTag: $currentTagOrNull")
                //println("jceHead: " + jce.currentHeadOrNull)
                return jce.skipToHeadAndUseIfPossibleOrFail(currentTag.id) {
                    // don't check type. it's polymorphic

                    //println("listHead: $it")
                    when (it.type) {
                        Jce.SIMPLE_LIST -> {
                            currentTag.isSimpleByteArray = true
                            jce.prepareNextHead() // 无用的元素类型
                            SimpleByteArrayReader
                        }
                        Jce.LIST -> ListReader
                        else -> error("type mismatch. Expected SIMPLE_LIST or LIST, got ${it.type} instead")
                    }
                }
            }
            StructureKind.CLASS -> {
                currentTagOrNull ?: return this@JceDecoder // outermost

                //println("!! CLASS")
                //println("decoderTag: $currentTag")
                //println("jceHead: " + jce.currentHeadOrNull)
                val tag = popTag()
                return jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jceHead ->
                    jceHead.checkType(Jce.STRUCT_BEGIN, "beginStructure", tag, descriptor)

                    repeat(descriptor.elementsCount) {
                        pushTag(descriptor.getTag(descriptor.elementsCount - it - 1)) // better performance
                    }
                    this // independent tag stack
                }
            }

            StructureKind.OBJECT -> error("unsupported StructureKind.OBJECT: ${descriptor.serialName}")
            is UnionKind -> error("unsupported UnionKind: ${descriptor.serialName}")
            is PolymorphicKind -> error("unsupported PolymorphicKind: ${descriptor.serialName}")
        }
    }

    override fun decodeSequentially(): Boolean = false
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val jceHead = jce.currentHeadOrNull ?: return CompositeDecoder.READ_DONE
        if (jceHead.type == Jce.STRUCT_END) {
            return CompositeDecoder.READ_DONE
        }

        repeat(descriptor.elementsCount) {
            val tag = descriptor.getJceTagId(it)
            if (tag == jceHead.tag) {
                return it
            }
        }

        return CompositeDecoder.READ_DONE // optional support
    }

    override fun decodeTaggedInt(tag: JceTag): Int =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceIntValue(it) }

    override fun decodeTaggedByte(tag: JceTag): Byte =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceByteValue(it) }

    override fun decodeTaggedBoolean(tag: JceTag): Boolean =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceBooleanValue(it) }

    override fun decodeTaggedFloat(tag: JceTag): Float =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceFloatValue(it) }

    override fun decodeTaggedDouble(tag: JceTag): Double =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceDoubleValue(it) }

    override fun decodeTaggedShort(tag: JceTag): Short =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceShortValue(it) }

    override fun decodeTaggedLong(tag: JceTag): Long =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceLongValue(it) }

    override fun decodeTaggedString(tag: JceTag): String =
        jce.skipToHeadAndUseIfPossibleOrFail(tag.id) { jce.readJceStringValue(it) }

    override fun decodeTaggedNotNullMark(tag: JceTag): Boolean {
        return jce.skipToHeadOrNull(tag.id) != null
    }
}
