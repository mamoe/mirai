/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils.cryptor

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.io.core.readULong
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
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

@MiraiDebugAPI
class ProtoMap(map: MutableMap<ProtoFieldId, Any>) : MutableMap<ProtoFieldId, Any> by map {
    companion object {
        @JvmStatic
        internal val indent: String = "    "
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

/**
 * 将所有元素加入转换为多行的字符串表示.
 */
@MiraiDebugAPI
fun <T> Sequence<T>.joinToStringPrefixed(prefix: String, transform: (T) -> CharSequence): String {
    return this.joinToString(prefix = "$prefix${ProtoMap.indent}", separator = "\n$prefix${ProtoMap.indent}", transform = transform)
}

/**
 * 将内容格式化为较可读的字符串输出.
 *
 * 各数字类型极其无符号类型: 十六进制表示 + 十进制表示. e.g. `0x1000(4096)`
 * [ByteArray] 和 [UByteaArray]: 十六进制表示, 通过 [ByteArray.toUHexString]
 * [ProtoMap]: 调用 [ProtoMap.toStringPrefixed]
 * [Iterable], [Iterator], [Sequence]: 调用各自的 joinToString.
 * [Map]: 多行输出. 每行显示一个值. 递归调用 [contentToString]. 嵌套结构将会以缩进表示
 * `data class`: 调用其 [toString]
 * 其他类型: 反射获取它和它的所有来自 Mirai 的 super 类型的所有自有属性并递归调用 [contentToString]. 嵌套结构将会以缩进表示
 */
@MiraiDebugAPI("Extremely slow")
fun Any?.contentToString(prefix: String = ""): String = when (this) {
    is Unit -> "Unit"
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

    is ByteArray -> {
        if (this.size == 0) "<Empty ByteArray>"
        else this.toUHexString()
    }
    is UByteArray -> {
        if (this.size == 0) "<Empty UByteArray>"
        else this.toUHexString()
    }
    is ShortArray -> {
        if (this.size == 0) "<Empty ShortArray>"
        else this.iterator().contentToString()
    }
    is IntArray -> {
        if (this.size == 0) "<Empty IntArray>"
        else this.iterator().contentToString()
    }
    is LongArray -> {
        if (this.size == 0) "<Empty LongArray>"
        else this.iterator().contentToString()
    }
    is FloatArray -> {
        if (this.size == 0) "<Empty FloatArray>"
        else this.iterator().contentToString()
    }
    is DoubleArray -> {
        if (this.size == 0) "<Empty DoubleArray>"
        else this.iterator().contentToString()
    }
    is UShortArray -> {
        if (this.size == 0) "<Empty ShortArray>"
        else this.iterator().contentToString()
    }
    is UIntArray -> {
        if (this.size == 0) "<Empty IntArray>"
        else this.iterator().contentToString()
    }
    is ULongArray -> {
        if (this.size == 0) "<Empty LongArray>"
        else this.iterator().contentToString()
    }
    is Array<*> -> {
        if (this.size == 0) "<Empty Array>"
        else this.iterator().contentToString()
    }
    is BooleanArray -> {
        if (this.size == 0) "<Empty BooleanArray>"
        else this.iterator().contentToString()
    }

    is ProtoMap -> "ProtoMap(size=$size){\n" + this.toStringPrefixed("$prefix${ProtoMap.indent}${ProtoMap.indent}") + "\n$prefix${ProtoMap.indent}}"
    is Iterable<*> -> this.joinToString(prefix = "[", postfix = "]") { it.contentToString(prefix) }
    is Iterator<*> -> this.asSequence().joinToString(prefix = "[", postfix = "]") { it.contentToString(prefix) }
    is Sequence<*> -> this.joinToString(prefix = "[", postfix = "]") { it.contentToString(prefix) }
    is Map<*, *> -> this.entries.joinToString(prefix = "{", postfix = "}") { it.key.contentToString(prefix) + "=" + it.value.contentToString(prefix) }
    else -> {
        if (this == null) "null"
        else if (this::class.isData) this.toString()
        else {
            if (this::class.qualifiedName?.startsWith("net.mamoe.mirai.") == true) {
                this.contentToStringReflectively(prefix + ProtoMap.indent)
            } else this.toString()
            /*
            (this::class.simpleName ?: "<UnnamedClass>") + "#" + this::class.hashCode() + "{\n" +
                    this::class.members.asSequence().filterIsInstance<KProperty<*>>().filter { !it.isSuspend && it.visibility == KVisibility.PUBLIC }
                        .joinToStringPrefixed(
                            prefix = ProtoMap.indent
                        ) { it.name + "=" + kotlin.runCatching { it.call(it).contentToString(ProtoMap.indent) }.getOrElse { "<!>" } }
             */
        }
    }
}

@MiraiExperimentalAPI("Extremely slow")
@MiraiDebugAPI("Extremely slow")
expect fun Any.contentToStringReflectively(prefix: String = "", filter: ((String, Any?) -> Boolean)? = null): String

@MiraiDebugAPI
@Suppress("UNCHECKED_CAST")
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
                if (map[id] !is MutableList<*>) map[id] = mutableListOf(map[id]!!)
                (map[id] as MutableList<Any>) += readValue()
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