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
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.protobuf.ProtoId


@OptIn(InternalSerializationApi::class) // 将来 kotlinx 修改后再复制过来 mirai.
private class JceDecoder(
    val jce: JceInput, override val context: SerialModule
) : TaggedDecoder<JceTag>() {
    override val updateMode: UpdateMode
        get() = UpdateMode.BANNED

    override fun SerialDescriptor.getTag(index: Int): JceTag {
        val annotations = this.getElementAnnotations(index)

        val id = (annotations.asSequence().filterIsInstance<JceId>().firstOrNull()?.id
            ?: annotations.asSequence().filterIsInstance<ProtoId>().firstOrNull()?.id) // 旧版本兼容
            ?: error("cannot find @JceId or @ProtoId for ${this.getElementName(index)} in ${this.serialName}")

        return JceTag(
            id,
            this.getElementDescriptor(index).isNullable
        )
    }


    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        TODO("Not yet implemented")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeTaggedInt(tag: JceTag): Int =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceIntValue(it) }

    override fun decodeTaggedByte(tag: JceTag): Byte =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceByteValue(it) }

    override fun decodeTaggedBoolean(tag: JceTag): Boolean =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceBooleanValue(it) }

    override fun decodeTaggedFloat(tag: JceTag): Float =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceFloatValue(it) }

    override fun decodeTaggedDouble(tag: JceTag): Double =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceDoubleValue(it) }

    override fun decodeTaggedShort(tag: JceTag): Short =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceShortValue(it) }

    override fun decodeTaggedLong(tag: JceTag): Long =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceLongValue(it) }

    override fun decodeTaggedString(tag: JceTag): String =
        jce.skipToTagAndUseIfPossibleOrFail(tag.id) { jce.readJceStringValue(it) }

    override fun decodeTaggedEnum(tag: JceTag, enumDescription: SerialDescriptor): Int {
        return super.decodeTaggedEnum(tag, enumDescription)
    }

    override fun decodeTaggedChar(tag: JceTag): Char {
        return super.decodeTaggedChar(tag)
    }

    override fun decodeTaggedNotNullMark(tag: JceTag): Boolean {
        println("!! decodeTaggedNotNullMark: $tag")
        return super.decodeTaggedNotNullMark(tag)
    }
}
