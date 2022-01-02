/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.ClassValueDesc
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.ValueDesc
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.accept
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitorUnit
import net.mamoe.mirai.internal.testFramework.codegen.visitors.AnalyzeDefaultValuesMappingVisitor
import net.mamoe.mirai.internal.testFramework.codegen.visitors.DefaultValuesMapping
import kotlin.reflect.KParameter


fun ValueDesc.removeDefaultValues(): ValueDesc {
    val def = AnalyzeDefaultValuesMappingVisitor()
    this.accept(def)
    this.accept(RemoveDefaultValuesVisitor(def.mappings))
    return this
}

class RemoveDefaultValuesVisitor(
    private val mappings: MutableList<DefaultValuesMapping>,
) : ValueDescVisitorUnit {
    override fun visitValue(desc: ValueDesc, data: Nothing?) {
        desc.acceptChildren(this, data)
    }

    override fun <T : Any> visitClass(desc: ClassValueDesc<T>, data: Nothing?) {
        super.visitClass(desc, data)
        val mapping = mappings.find { it.forClass == desc.type }?.mapping ?: return

        // remove properties who have the same values as their default values, this would significantly reduce code size.
        mapping.forEach { (name, defaultValue) ->
            if (desc.properties.entries.removeIf {
                    it.key.name == name && isDefaultOrEmpty(it.key, it.value, defaultValue)
                }) {
                return@forEach // by removing one property, there will not by any other matches
            }
        }
    }

    private fun isDefaultOrEmpty(key: KParameter, value: ValueDesc, defaultValue: Any?): Boolean {
        if (!key.isOptional) return false
        if (equals(value.origin, defaultValue)) return true

        if (value is ClassValueDesc<*>
            && value.properties.all { it.key.isOptional && isDefaultOrEmpty(it.key, it.value, defaultValue) }
        ) {
            return true
        }

        return false
    }

    private fun Any?.isNullOrZeroOrEmpty(): Boolean {
        when (this) {
            null,
            0.toByte(), 0.toShort(), 0, 0L, 0.toFloat(), 0.toDouble(), 0.toChar(),
            "", listOf<Any>(), setOf<Any>(), mapOf<Any, Any>(),
            -> return true
        }

        check(this != null)

        when {
            this is Array<*> && this.isEmpty() -> return true
            this is IntArray && this.isEmpty() -> return true
            this is ByteArray && this.isEmpty() -> return true
            this is ShortArray && this.isEmpty() -> return true
            this is LongArray && this.isEmpty() -> return true
            this is CharArray && this.isEmpty() -> return true
            this is FloatArray && this.isEmpty() -> return true
            this is DoubleArray && this.isEmpty() -> return true
            this is BooleanArray && this.isEmpty() -> return true
        }

        return false
    }

    fun equals(a: Any?, b: Any?): Boolean {
        if (a.isNullOrZeroOrEmpty() && b.isNullOrZeroOrEmpty()) return true
        return when {
            a === b -> true
            a == b -> true
            a is Array<*>? && b is Array<*>? -> a.contentEquals(b)
            a is IntArray? && b is IntArray? -> a.contentEquals(b)
            a is ByteArray? && b is ByteArray? -> a.contentEquals(b)
            a is ShortArray? && b is ShortArray? -> a.contentEquals(b)
            a is LongArray? && b is LongArray? -> a.contentEquals(b)
            a is CharArray? && b is CharArray? -> a.contentEquals(b)
            a is FloatArray? && b is FloatArray? -> a.contentEquals(b)
            a is DoubleArray? && b is DoubleArray? -> a.contentEquals(b)
            a is BooleanArray? && b is BooleanArray? -> a.contentEquals(b)
            else -> false
        }
    }
}