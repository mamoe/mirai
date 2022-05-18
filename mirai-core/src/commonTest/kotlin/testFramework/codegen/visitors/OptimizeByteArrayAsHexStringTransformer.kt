/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.internal.testFramework.codegen.visitors

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.*
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescTransformerNotNull
import net.mamoe.mirai.utils.toUHexString
import kotlin.reflect.typeOf

/**
 * If the byte array was from [String.toByteArray], transform it to the [String].
 * Otherwise, transform it as hex string.
 */
open class OptimizeByteArrayAsHexStringTransformer : ValueDescTransformerNotNull<Nothing?>() {
    open fun transform(desc: CollectionLikeValueDesc, value: ByteArray): ValueDesc {
        return if (isReadableString(value)) {
            // prefers to show readable string
            PlainValueDesc(
                desc.parent,
                value = "\"${
                    value.decodeToString().escapeQuotation()
                }\".toByteArray() /* ${value.toUHexString()} */",
                origin = desc.origin
            )
        } else {
            PlainValueDesc(
                desc.parent,
                value = "\"${value.toUHexString()}\".hexToBytes()",
                origin = desc.origin
            )
        }

    }

    protected fun isReadableString(value: ByteArray) =
        value.decodeToString().all { (it.isDefined() || it.isWhitespace()) && !it.isISOControl() }

    override fun visitValue(desc: ValueDesc, data: Nothing?): ValueDesc {
        desc.acceptChildren(this, data)
        return super.visitValue(desc, data)
    }

    override fun visitObjectArray(desc: ObjectArrayValueDesc, data: Nothing?): ValueDesc {
        if (desc.arrayType == arrayOfByteType) {
            val array = desc.elements.mapNotNull { (it as? PlainValueDesc)?.value?.toByteOrNull() }.toByteArray()
            if (array.size != desc.elements.size) return desc

            return transform(desc, array)
        }
        return super.visitObjectArray(desc, data)
    }

    override fun visitPrimitiveArray(desc: PrimitiveArrayValueDesc, data: Nothing?): ValueDesc {
        if (desc.value is ByteArray) {
            return transform(desc, desc.value as ByteArray)
        }
        return super.visitPrimitiveArray(desc, data)
    }

    companion object {
        private val arrayOfByteType = typeOf<Array<Byte>>()
        private fun String.escapeQuotation(): String = buildString { this@escapeQuotation.escapeQuotationTo(this) }

        private fun String.escapeQuotationTo(out: StringBuilder) {
            for (element in this) {
                when (element) {
                    '\\' -> out.append("\\\\")
                    '\n' -> out.append("\\n")
                    '\r' -> out.append("\\r")
                    '\t' -> out.append("\\t")
                    '\"' -> out.append("\\\"")
                    else -> out.append(element)
                }
            }
        }

    }
}