/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.SerialModule
import net.mamoe.mirai.qqandroid.io.serialization.Jce


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
        println("getTag: ${this.getElementName(index)}=$id")

        return JceTag(
            id,
            this.getElementDescriptor(index).isNullable
        )
    }
    fun SerialDescriptor.getJceTagId(index: Int): Int {
        return getElementAnnotations(index).filterIsInstance<JceId>().single().id
    }


    private val ByteArraySerializer = ByteArraySerializer()

    // TODO: 2020/3/6 can be object
    private inner class SimpleByteArrayReader() : CompositeDecoder by this {
        override fun decodeSequentially(): Boolean = true
        override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
            return jce.input.readByte()
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return 0
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            return jce.useHead { jce.readJceIntValue(it) }
        }
    }

    // TODO: 2020/3/6 can be object
    private inner class ListReader() : CompositeDecoder by this {
        override fun decodeSequentially(): Boolean = false
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return 0
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            return jce.useHead { jce.readJceIntValue(it) }
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (descriptor == ByteArraySerializer.descriptor) {
            return jce.skipToHeadAndUseIfPossibleOrFail(popTag().id) {
                when (it.type) {
                    Jce.SIMPLE_LIST -> SimpleByteArrayReader()
                    Jce.LIST -> ListReader()
                    else -> error("type mismatch. Expected SIMPLE_LIST or LIST, got ${it.type} instead")
                }
            }
        }
        return when (descriptor.kind) {
            StructureKind.MAP -> {
                error("map")
            }
            StructureKind.LIST -> ListReader()

            else -> this
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        println("decodeSerializableValue: ${deserializer.descriptor}")
        return super.decodeSerializableValue(deserializer)
    }

    private var currentIndex = 0


    override fun decodeSequentially(): Boolean = false
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val jceHead = jce.currentHeadOrNull ?: return CompositeDecoder.READ_DONE
        repeat(descriptor.elementsCount){
            val tag = descriptor.getJceTagId(it)
            if (tag == jceHead.tag) {
                return it
            }
        }

        return CompositeDecoder.READ_DONE // optional support
    }

    override fun decodeTaggedNull(tag: JceTag): Nothing? {
        println("decodeTaggedNull")
        return super.decodeTaggedNull(tag)
    }

    override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
        return super.decodeNullableSerializableValue(deserializer)
    }

    override fun decodeTaggedValue(tag: JceTag): Any {
        println("decodeTaggedValue")
        return super.decodeTaggedValue(tag)
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

    override fun decodeTaggedEnum(tag: JceTag, enumDescription: SerialDescriptor): Int {
        return super.decodeTaggedEnum(tag, enumDescription)
    }

    override fun decodeTaggedChar(tag: JceTag): Char {
        return super.decodeTaggedChar(tag)
    }

    override fun decodeTaggedNotNullMark(tag: JceTag): Boolean {
        return jce.skipToHeadOrNull(tag.id) != null
    }
}
