/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.visitors

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.*
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitor

class RendererContext(
    val indenter: Indenter,
    val classFormatter: ClassFormatter = ClassFormatter()
)

fun ValueDesc.renderToString(
    renderer: ValueDescToStringRenderer = ValueDescToStringRenderer(),
    rendererContext: RendererContext = RendererContext(WordingIndenter.spacing(4))
): String {
    return accept(
        renderer,
        rendererContext
    )
}

open class ValueDescToStringRenderer : ValueDescVisitor<RendererContext, String> {
    private inline val visitor get() = this

    private inline fun Appendable.withIndent(indenter: Indenter, block: Appendable.() -> Unit) {
        append(
            buildString(block)
                .lineSequence()
                .map { indenter.indentize(it) }
                .joinToString("\n")
                .trimEnd { it != '\n' && it.isWhitespace() }
        )
    }

    override fun visitValue(desc: ValueDesc, data: RendererContext): String {
        return desc.toString() // tentative fallback
    }

    override fun visitPlain(desc: PlainValueDesc, data: RendererContext): String {
        return desc.value
    }

    override fun visitArray(desc: CollectionLikeValueDesc, data: RendererContext): String = buildString {
        val array = desc.value

        fun impl(funcName: String, elements: List<ValueDesc>) {
            if (elements.any { it !is PlainValueDesc }) {
                // complex types
                append(funcName)
                append('(')
                appendLine()
                withIndent(data.indenter) {
                    val list = elements.toList()
                    list.forEach { desc ->
                        append(desc.accept(visitor, data))
                        appendLine(", ")
                    }
                }
                append(')')
            } else {
                // primitive types
                append(funcName)
                append('(')
                val list = elements.toList()
                list.forEachIndexed { index, desc ->
                    append(desc.accept(visitor, data))
                    if (index != list.lastIndex) append(", ")
                }
                append(')')
            }
        }

        when (array) {
            is Array<*> -> impl("arrayOf", desc.elements)
            is IntArray -> impl("intArrayOf", desc.elements)
            is ByteArray -> impl("byteArrayOf", desc.elements)
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

    override fun visitMap(desc: MapValueDesc, data: RendererContext): String = buildString {
        appendLine("mutableMapOf(")
        for ((key, value) in desc.elements) {
            withIndent(data.indenter) {
                append(key.accept(visitor, data))
                append(" to ")
                append(value.accept(visitor, data))
                appendLine(",")
            }
        }
        append(")")
    }

    override fun <T : Any> visitClass(desc: ClassValueDesc<T>, data: RendererContext): String = buildString {
        val classFormatterContext = ClassFormatterContext(desc, visitor, data)
        appendLine("${data.classFormatter.formatClassName(classFormatterContext)}(")
        for ((param, valueDesc) in desc.properties) {
            withIndent(data.indenter) {
                append(
                    data.classFormatter.formatClassProperty(
                        classFormatterContext,
                        param.name,
                        valueDesc.accept(visitor, data)
                    )
                )
            }
            appendLine(",")
        }
        append(")")
    }

//    open fun renderClassName(desc: ClassValueDesc<*>): String {
//        val name = desc.type.qualifiedName ?: desc.type.java.name
//        return wrapBacktickIfNecessary(name)
//    }

    companion object {
        protected fun wrapBacktickIfNecessary(name: String) = if (name.contains(' ') || name.contains('$')) {
            "`$name`"
        } else name
    }
}