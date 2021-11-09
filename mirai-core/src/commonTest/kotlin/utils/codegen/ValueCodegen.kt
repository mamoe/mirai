/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.codegen

import net.mamoe.mirai.utils.toUHexString

class ValueCodegen(
    val context: CodegenContext
) {
    fun generate(desc: ValueDesc) {
        when (desc) {
            is PlainValueDesc -> generate(desc)
            is ObjectArrayValueDesc -> generate(desc)
            is PrimitiveArrayValueDesc -> generate(desc)
            is CollectionValueDesc -> generate(desc)
            is ClassValueDesc<*> -> generate(desc)
            is MapValueDesc -> generate(desc)
        }
    }

    fun generate(desc: PlainValueDesc) {
        context.append(desc.value)
    }

    fun generate(desc: MapValueDesc) {
        context.run {
            appendLine("mutableMapOf(")
            for ((key, value) in desc.elements) {
                generate(key)
                append(" to ")
                generate(value)
                appendLine(",")
            }
            append(")")
        }
    }

    fun <T : Any> generate(desc: ClassValueDesc<T>) {
        context.run {
            appendLine("${desc.type.qualifiedName}(")
            for ((param, valueDesc) in desc.properties) {
                append(param.name)
                append("=")
                generate(valueDesc)
                appendLine(",")
            }
            append(")")
        }
    }

    fun generate(desc: ArrayValueDesc) {
        val array = desc.value

        fun impl(funcName: String, elements: List<ValueDesc>) {
            context.run {
                append(funcName)
                append('(')
                val list = elements.toList()
                list.forEachIndexed { index, desc ->
                    generate(desc)
                    if (index != list.lastIndex) append(", ")
                }
                append(')')
            }
        }

        return when (array) {
            is Array<*> -> impl("arrayOf", desc.elements)
            is IntArray -> impl("intArrayOf", desc.elements)
            is ByteArray -> {
                if (array.size == 0) {
                    context.append("net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY") // let IDE to shorten references.
                    return
                } else {
                    if (array.decodeToString().all { Character.isUnicodeIdentifierPart(it) || it.isWhitespace() }) {
                        // prefers to show readable string
                        context.append(
                            "\"${
                                array.decodeToString().escapeQuotation()
                            }\".toByteArray() /* ${array.toUHexString()} */"
                        )
                    } else {
                        context.append("\"${array.toUHexString()}\".hexToBytes()")
                    }
                    return
                }
            }
            is ShortArray -> impl("shortArrayOf", desc.elements)
            is CharArray -> impl("charArrayOf", desc.elements)
            is LongArray -> impl("longArrayOf", desc.elements)
            is FloatArray -> impl("floatArrayOf", desc.elements)
            is DoubleArray -> impl("doubleArrayOf", desc.elements)
            is BooleanArray -> impl("booleanArrayOf", desc.elements)
            is List<*> -> impl("mutableListOf", desc.elements)
            is Set<*> -> impl("mutableSetOf", desc.elements)
            else -> error("$array is not an array.")
        }
    }
}

class CodegenContext(
    val sb: StringBuilder = StringBuilder(),
    val configuration: CodegenConfiguration = CodegenConfiguration()
) : Appendable by sb {
    fun getResult(): String {
        return sb.toString()
    }
}

class CodegenConfiguration(
    var removeDefaultValues: Boolean = true,
)


private fun String.escapeQuotation(): String = buildString { this@escapeQuotation.escapeQuotationTo(this) }

private fun String.escapeQuotationTo(out: StringBuilder) {
    for (i in 0 until length) {
        when (val ch = this[i]) {
            '\\' -> out.append("\\\\")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            '\t' -> out.append("\\t")
            '\"' -> out.append("\\\"")
            else -> out.append(ch)
        }
    }
}

