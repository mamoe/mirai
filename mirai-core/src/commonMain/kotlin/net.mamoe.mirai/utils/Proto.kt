@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.io.core.readULong
import net.mamoe.mirai.utils.io.UVarInt
import net.mamoe.mirai.utils.io.readUVarInt
import net.mamoe.mirai.utils.io.toUHexString

// ProtoBuf utilities

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

@Suppress("FunctionName")
fun ProtoFieldId(serializedId: UInt): ProtoFieldId = ProtoFieldId(protoFieldNumber(serializedId), protoType(serializedId))

data class ProtoFieldId(
    val fieldNumber: Int,
    val type: ProtoType
) {
    override fun toString(): String = "$type $fieldNumber"
}

enum class ProtoType(val value: Byte, val typeName: String) {
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
    BIT_32(0x05, " 32bit"), ;

    override fun toString(): String = this.typeName

    companion object {
        fun valueOf(value: Byte): ProtoType = values().firstOrNull { it.value == value } ?: error("Unknown ProtoId $value")
    }
}

/**
 * 由 ProtoBuf 序列化后的 id 得到类型
 *
 * serializedId = (fieldNumber << 3) | wireType
 */
fun protoType(number: UInt): ProtoType = ProtoType.valueOf(number.toInt().shl(29).ushr(29).toByte())

/**
 * ProtoBuf 序列化后的 id 转为序列前标记的 id
 *
 * serializedId = (fieldNumber << 3) | wireType
 */
fun protoFieldNumber(number: UInt): Int = number.toInt().ushr(3)


class ProtoMap(map: MutableMap<ProtoFieldId, Any>) : MutableMap<ProtoFieldId, Any> by map {
    override fun toString(): String {
        return this.entries.joinToString(prefix = "ProtoMap(\n  ", postfix = "\n)", separator = "\n  ") {
            "${it.key}=" + it.value.contentToString().replace("\n", """\n""")
        }
    }

    /*
    override fun put(key: ProtoFieldId, value: Any): Any? {
        println("${key}=" + value.contentToString())
        return null
    }*/
}

fun Any.contentToString(): String = when (this) {
    is UInt -> "0x" + this.toUHexString("") + "($this)"
    is UByte -> "0x" + this.toUHexString() + "($this)"
    is UShort -> "0x" + this.toUHexString("") + "($this)"
    is ULong -> "0x" + this.toUHexString("") + "($this)"
    is Int -> "0x" + this.toUHexString("") + "($this)"
    is Byte -> "0x" + this.toUHexString() + "($this)"
    is Short -> "0x" + this.toUHexString("") + "($this)"
    is Long -> "0x" + this.toUHexString("") + "($this)"

    is UVarInt -> "0x" + this.toUHexString("") + "($this)"

    is Boolean -> if (this) "true" else "false"

    is ByteArray -> this.toUHexString()// + " (${this.encodeToString()})"
    else -> this.toString()
}

fun ByteReadPacket.readProtoMap(length: Long = this.remaining): ProtoMap {
    val map = ProtoMap(mutableMapOf())


    val expectingRemaining = this.remaining - length
    while (this.remaining != expectingRemaining) {
        val id = ProtoFieldId(readUVarInt())
        map[id] = when (id.type) {
            ProtoType.VAR_INT -> UVarInt(readUVarInt())
            ProtoType.BIT_32 -> readUInt()
            ProtoType.BIT_64 -> readULong()
            ProtoType.LENGTH_DELIMI -> readBytes(readUVarInt().toInt())

            ProtoType.START_GROUP -> error("unsupported")
            ProtoType.END_GROUP -> error("unsupported")
        }
    }
    return map
}