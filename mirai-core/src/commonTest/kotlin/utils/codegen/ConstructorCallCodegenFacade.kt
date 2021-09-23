/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.codegen

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.cast
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.typeOf

object ConstructorCallCodegenFacade {
    /**
     * Analyze [value] and give its correspondent [ValueDesc].
     */
    fun analyze(value: Any?, type: KType): ValueDesc {
        if (value == null) return PlainValueDesc(null, "null", null)

        val clazz = value::class

        if (clazz.isData || clazz.hasAnnotation<Serializable>()) {
            val primaryConstructor =
                clazz.primaryConstructor ?: error("$value does not have a primary constructor.")
            val properties = clazz.declaredMemberProperties

            val map = mutableMapOf<KParameter, ValueDesc>()

            for (valueParameter in primaryConstructor.valueParameters) {
                val prop = properties.find { it.name == valueParameter.name }
                    ?: error("Could not find corresponding property for parameter ${clazz.qualifiedName}.${valueParameter.name}")

                prop.cast<KProperty1<Any, Any?>>()
                map[valueParameter] = analyze(prop.get(value), prop.returnType)
            }
            return ClassValueDesc(null, value, map)
        }

        ArrayValueDesc.createOrNull(value, type, null)?.let { return it }
        if (value is Collection<*>) {
            return CollectionValueDesc(null, value, arrayType = type, elementType = type.arguments.first().type!!)
        } else if (value is Map<*, *>) {
            return MapValueDesc(
                null,
                value.cast(),
                value.cast(),
                type,
                type.arguments.first().type!!,
                type.arguments[1].type!!
            )
        }

        return when (value) {
            is CharSequence -> {
                PlainValueDesc(null, '"' + value.toString() + '"', value)
            }
            is Char -> {
                PlainValueDesc(null, "'$value'", value)
            }
            else -> PlainValueDesc(null, value.toString(), value)
        }
    }

    /**
     * Generate source code to construct the value represented by [desc].
     */
    fun generate(desc: ValueDesc, context: CodegenContext = CodegenContext()): String {
        if (context.configuration.removeDefaultValues) {
            val def = AnalyzeDefaultValuesMappingVisitor()
            desc.accept(def)
            desc.accept(RemoveDefaultValuesVisitor(def.mappings))
        }

        ValueCodegen(context).generate(desc)
        return context.getResult()
    }

    fun analyzeAndGenerate(value: Any?, type: KType, context: CodegenContext = CodegenContext()): String {
        return generate(analyze(value, type), context)
    }
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> ConstructorCallCodegenFacade.analyze(value: T): ValueDesc {
    return analyze(value, typeOf<T>())
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> ConstructorCallCodegenFacade.analyzeAndGenerate(
    value: T,
    context: CodegenContext = CodegenContext()
): String {
    return analyzeAndGenerate(value, typeOf<T>(), context)
}

