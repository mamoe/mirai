/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "NO_REFLECTION_IN_CLASS_PATH")

package net.mamoe.mirai.utils.cryptor

import net.mamoe.mirai.utils.MiraiDebugAPI

// ProtoBuf utilities


@Suppress("FunctionName", "SpellCheckingInspection")
/*
 * Type	Meaning	Used For
 * 0	Varint	int32, int64, uint32, uint64, sint32, sint64, bool, enum
 * 1	64-bit	fixed64, sfixed64, double
 * 2	Length-delimi	string, bytes, embedded messages, packed repeated fields
 * 3	Start group	Groups (deprecated)
 * 4	End group	Groups (deprecated)
 * 5	32-bit	fixed32, sfixed32, float
 *
 * https://www.jianshu.com/p/f888907adaeb
 */
@MiraiDebugAPI
fun ProtoFieldId(serializedId: UInt): ProtoFieldId =
    ProtoFieldId(
        protoFieldNumber(serializedId),
        protoType(serializedId)
    )

@MiraiDebugAPI
data class ProtoFieldId(
    val fieldNumber: Int,
    val type: ProtoType
) {
    override fun toString(): String = "$type $fieldNumber"
}

@Suppress("SpellCheckingInspection")
@MiraiDebugAPI
enum class ProtoType(val value: Byte, private val typeName: String) {
    /**
     * int32, int64, uint32, uint64, sint32, sint64, bool, enum
     */
    VAR_INT(0x00, "varint"),

    /**
     * fixed64, sfixed64, double
     */
    BIT_64(0x01, " 64bit"),

    /**
     * string, bytes, embedded messages, packed repeated fields
     */
    LENGTH_DELIMI(0x02, "delimi"),

    /**
     * Groups (deprecated)
     */
    START_GROUP(0x03, "startg"),

    /**
     * Groups (deprecated)
     */
    END_GROUP(0x04, "  endg"),

    /**
     * fixed32, sfixed32, float
     */
    BIT_32(0x05, " 32bit"),
    ;

    override fun toString(): String = this.typeName

    companion object {
        fun valueOf(value: Byte): ProtoType = values().firstOrNull { it.value == value } ?: error("Unknown ProtoType $value")
    }
}

/**
 * 由 ProtoBuf 序列化后的 id 得到类型
 *
 * serializedId = (fieldNumber << 3) | wireType
 */
@MiraiDebugAPI
fun protoType(number: UInt): ProtoType =
    ProtoType.valueOf(number.toInt().shl(29).ushr(29).toByte())

/**
 * ProtoBuf 序列化后的 id 转为序列前标记的 id
 *
 * serializedId = (fieldNumber << 3) | wireType
 */
@MiraiDebugAPI
fun protoFieldNumber(number: UInt): Int = number.toInt().ushr(3)
