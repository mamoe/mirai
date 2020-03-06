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
    private inner class SimpleByteArrayReader : CompositeDecoder by this {
        override fun decodeSequentially(): Boolean = true
        override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
            println("decodeByteElement: $index")
            return jce.input.readByte().also { println("read: $it") }
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            error("should not be reached")
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            // 不要读下一个 head
            return jce.currentHead.let { jce.readJceIntValue(it) }.also { println("simpleListSize=$it") }
        }
    }

    // TODO: 2020/3/6 can be object
    private inner class ListReader : CompositeDecoder by this {
        override fun decodeSequentially(): Boolean = true
        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            error("should not be reached")
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            println("endStructure: ${descriptor.serialName}")
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            return jce.useHead { jce.readJceIntValue(it) }
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (descriptor == ByteArraySerializer.descriptor) {
            jce.prepareNextHead() // list 里面没读 head
        }
        super.endStructure(descriptor)
    }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        println("beginStructure: ${descriptor.serialName}")
        if (descriptor == ByteArraySerializer.descriptor) {
            println("!! ByteArray")
            println("decoderTag: $currentTagOrNull")
            println("jceHead: " + jce.currentHeadOrNull)
            return jce.skipToHeadAndUseIfPossibleOrFail(popTag().id) {
                println("listHead: $it")
                when (it.type) {
                    Jce.SIMPLE_LIST -> SimpleByteArrayReader().also { jce.prepareNextHead() } // 无用的元素类型
                    Jce.LIST -> error(""); //ListReader()
                    else -> error("type mismatch. Expected SIMPLE_LIST or LIST, got ${it.type} instead")
                }
            }
        }
        return when (descriptor.kind) {
            StructureKind.MAP -> {
                error("map")
            }
            StructureKind.LIST -> ListReader()

            else -> this@JceDecoder
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        println(
            "decodeSerializableValue: ${deserializer.descriptor.toString().substringBefore('(')
                .substringAfterLast('.')}"
        )
        return super.decodeSerializableValue(deserializer)
    }

    override fun decodeSequentially(): Boolean = false
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val jceHead = jce.currentHeadOrNull ?: return CompositeDecoder.READ_DONE
        repeat(descriptor.elementsCount) {
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

    override fun decodeTaggedNotNullMark(tag: JceTag): Boolean {
        return jce.skipToHeadOrNull(tag.id) != null
    }
}
