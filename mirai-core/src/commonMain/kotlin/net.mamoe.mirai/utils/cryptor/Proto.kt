@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils.cryptor

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.io.core.readULong
import net.mamoe.mirai.utils.io.*
import kotlin.jvm.JvmStatic

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
fun ProtoFieldId(serializedId: UInt): ProtoFieldId =
    ProtoFieldId(
        protoFieldNumber(serializedId),
        protoType(serializedId)
    )

data class ProtoFieldId(
    val fieldNumber: Int,
    val type: ProtoType
) {
    override fun toString(): String = "$type $fieldNumber"
}

@Suppress("SpellCheckingInspection")
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
fun protoType(number: UInt): ProtoType =
    ProtoType.valueOf(number.toInt().shl(29).ushr(29).toByte())

/**
 * ProtoBuf 序列化后的 id 转为序列前标记的 id
 *
 * serializedId = (fieldNumber << 3) | wireType
 */
fun protoFieldNumber(number: UInt): Int = number.toInt().ushr(3)


class ProtoMap(map: MutableMap<ProtoFieldId, Any>) : MutableMap<ProtoFieldId, Any> by map {
    companion object {
        @JvmStatic
        val indent: String = "    "
    }

    override fun toString(): String {
        return this.entries.joinToString(prefix = "ProtoMap(size=$size){\n$indent", postfix = "\n}", separator = "\n$indent") {
            "${it.key}=" + it.value.contentToString()
        }
    }

    fun toStringPrefixed(prefix: String): String {
        return this.entries.joinToString(prefix = "$prefix$indent", separator = "\n$prefix$indent") {
            "${it.key}=" + it.value.contentToString(prefix)
        }
    }
    /*
    override fun put(key: ProtoFieldId, value: Any): Any? {
        println("${key}=" + value.contentToString())
        return null
    }*/
}

internal fun Any.contentToString(prefix: String = ""): String = when (this) {
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

    is ProtoMap -> "ProtoMap(size=$size){\n" + this.toStringPrefixed("$prefix${ProtoMap.indent}${ProtoMap.indent}") + "\n$prefix${ProtoMap.indent}}"
    else -> this.toString()
}

fun ByteReadPacket.readProtoMap(length: Long = this.remaining): ProtoMap {
    val map = ProtoMap(mutableMapOf())


    val expectingRemaining = this.remaining - length
    while (this.remaining != expectingRemaining) {
        require(this.remaining > expectingRemaining) { "Expecting to read $length bytes, but read ${expectingRemaining + length - this.remaining}" }

        try {
            val id = ProtoFieldId(readUVarInt())

            fun readValue(): Any = when (id.type) {
                ProtoType.VAR_INT -> UVarInt(readUVarInt())
                ProtoType.BIT_32 -> readUInt()
                ProtoType.BIT_64 -> readULong()
                ProtoType.LENGTH_DELIMI -> tryReadProtoMapOrByteArray(readUVarInt().toInt())

                ProtoType.START_GROUP -> Unit
                ProtoType.END_GROUP -> Unit
            }

            if (map.containsKey(id)) {
                if (map[id] is MutableList<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (map[id] as MutableList<Any>) += readValue()
                } else {
                    map[id] = mutableListOf(map[id]!!)
                    @Suppress("UNCHECKED_CAST")
                    (map[id] as MutableList<Any>) += readValue()
                }
            } else {
                map[id] = readValue()
            }
        } catch (e: IllegalStateException) {
            e.logStacktrace()
            return map
        }
    }
    return map
}

private fun ByteReadPacket.tryReadProtoMapOrByteArray(length: Int): Any {
    val bytes = this.readBytes(length)
    return try {
        bytes.toReadPacket().readProtoMap().apply { require(none { it.key.type == ProtoType.START_GROUP || it.key.type == ProtoType.END_GROUP }) }
    } catch (e: Exception) {
        bytes
    }
}