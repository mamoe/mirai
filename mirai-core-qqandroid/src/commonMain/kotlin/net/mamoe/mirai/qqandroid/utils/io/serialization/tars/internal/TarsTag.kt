/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.io.serialization.tars.internal

import kotlinx.io.core.Output
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.Tars


internal abstract class TarsTag {
    abstract val id: Int
    internal var isSimpleByteArray: Boolean = false
}

internal object TarsTagListElement : TarsTag() {
    override val id: Int get() = 0
    override fun toString(): String {
        return "TarsTagListElement"
    }
}

internal object TarsTagMapEntryKey : TarsTag() {
    override val id: Int get() = 0
    override fun toString(): String {
        return "TarsTagMapEntryKey"
    }
}

internal object TarsTagMapEntryValue : TarsTag() {
    override val id: Int get() = 1
    override fun toString(): String {
        return "TarsTagMapEntryValue"
    }
}

internal data class TarsTagCommon(
    override val id: Int
) : TarsTag()

@OptIn(ExperimentalSerializationApi::class)
internal fun TarsHead.checkType(type: Byte, message: String, tag: TarsTag, descriptor: SerialDescriptor) {
    check(this.type == type) {
        "type mismatch. " +
            "Expected ${TarsHead.findTarsTypeName(type)}, " +
            "actual ${TarsHead.findTarsTypeName(this.type)} for $message. " +
            "Tag info: " +
            "id=${tag.id}, " +
            "name=${descriptor.getElementName(tag.id)} " +
            "in ${descriptor.serialName}"
    }
}

@PublishedApi
internal fun Output.writeTarsHead(type: Byte, tag: Int) {
    if (tag < 15) {
        writeByte(((tag shl 4) or type.toInt()).toByte())
        return
    }
    if (tag < 256) {
        writeByte((type.toInt() or 0xF0).toByte())
        writeByte(tag.toByte())
        return
    }
    error("tag is too large: $tag")
}

@OptIn(ExperimentalUnsignedTypes::class)
internal class TarsHead(private val value: Long) {
    constructor(tag: Int, type: Byte) : this(tag.toLong().shl(32) or type.toLong())

    val tag: Int get() = (value ushr 32).toInt()
    val type: Byte get() = value.toUInt().toByte()

    override fun toString(): String {
        return "TarsHead(tag=$tag, type=$type(${findTarsTypeName(type)}))"
    }

    companion object {
        fun findTarsTypeName(type: Byte): String {
            return when (type) {
                Tars.BYTE -> "Byte"
                Tars.DOUBLE -> "Double"
                Tars.FLOAT -> "Float"
                Tars.INT -> "Int"
                Tars.LIST -> "List"
                Tars.LONG -> "Long"
                Tars.MAP -> "Map"
                Tars.SHORT -> "Short"
                Tars.SIMPLE_LIST -> "SimpleList"
                Tars.STRING1 -> "String1"
                Tars.STRING4 -> "String4"
                Tars.STRUCT_BEGIN -> "StructBegin"
                Tars.STRUCT_END -> "StructEnd"
                Tars.ZERO_TYPE -> "Zero"
                else -> error("illegal Tars type: $type")
            }
        }
    }
}