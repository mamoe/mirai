/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import io.ktor.utils.io.core.Output
import kotlinx.serialization.SerialInfo
import net.mamoe.mirai.qqandroid.io.serialization.Jce


/**
 * 标注 JCE 序列化时使用的 ID
 */
@SerialInfo
annotation class JceId(val id: Int)

/**
 * 类中元素的 tag
 *
 * 保留这个结构, 为将来增加功能的兼容性.
 */
@PublishedApi
internal data class JceTag(
    val id: Int,
    val isNullable: Boolean
)


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
        val typeString = when (type) {
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
        return "JceHead(tag=$tag, type=$type($typeString))"
    }
}