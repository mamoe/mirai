/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.io.serialization.jce

import kotlinx.io.core.Output
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialInfo


/**
 * 标注 JCE 序列化时使用的 ID
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class JceId(val id: Int)

/**
 * 类中元素的 tag
 *
 * 保留这个结构, 为将来增加功能的兼容性.
 */
@PublishedApi
internal abstract class JceTag {
    abstract val id: Int

    internal var isSimpleByteArray: Boolean = false
}

internal object JceTagListElement : JceTag() {
    override val id: Int get() = 0
    override fun toString(): String {
        return "JceTagListElement"
    }
}

internal object JceTagMapEntryKey : JceTag() {
    override val id: Int get() = 0
    override fun toString(): String {
        return "JceTagMapEntryKey"
    }
}

internal object JceTagMapEntryValue : JceTag() {
    override val id: Int get() = 1
    override fun toString(): String {
        return "JceTagMapEntryValue"
    }
}

internal data class JceTagCommon(
    override val id: Int
) : JceTag()

internal fun JceHead.checkType(type: Byte, message: String, tag: JceTag, descriptor: SerialDescriptor) {
    check(this.type == type) {
        "type mismatch. " +
                "Expected ${JceHead.findJceTypeName(type)}, " +
                "actual ${JceHead.findJceTypeName(this.type)} for $message. " +
                "Tag info: " +
                "id=${tag.id}, " +
                "name=${descriptor.getElementName(tag.id)} " +
                "in ${descriptor.serialName}" }
}

@PublishedApi
internal fun Output.writeJceHead(type: Byte, tag: Int) {
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
inline class JceHead(private val value: Long) {
    constructor(tag: Int, type: Byte) : this(tag.toLong().shl(32) or type.toLong())

    val tag: Int get() = (value ushr 32).toInt()
    val type: Byte get() = value.toUInt().toByte()

    override fun toString(): String {
        return "JceHead(tag=$tag, type=$type(${findJceTypeName(type)}))"
    }

    companion object {
        fun findJceTypeName(type: Byte): String {
            return when (type) {
                Jce.BYTE -> "Byte"
                Jce.DOUBLE -> "Double"
                Jce.FLOAT -> "Float"
                Jce.INT -> "Int"
                Jce.LIST -> "List"
                Jce.LONG -> "Long"
                Jce.MAP -> "Map"
                Jce.SHORT -> "Short"
                Jce.SIMPLE_LIST -> "SimpleList"
                Jce.STRING1 -> "String1"
                Jce.STRING4 -> "String4"
                Jce.STRUCT_BEGIN -> "StructBegin"
                Jce.STRUCT_END -> "StructEnd"
                Jce.ZERO_TYPE -> "Zero"
                else -> error("illegal jce type: $type")
            }
        }
    }
}