/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.visitors

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.ClassValueDesc
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.ValueDesc
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitorUnit
import net.mamoe.mirai.utils.cast
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class DefaultValuesMapping(
    val forClass: KClass<*>,
    val mapping: MutableMap<String, Any?> = mutableMapOf()
) {
    operator fun get(property: KProperty<*>): Any? = mapping[property.name]
}

class AnalyzeDefaultValuesMappingVisitor : ValueDescVisitorUnit {
    val mappings: MutableList<DefaultValuesMapping> = mutableListOf()

    override fun visitValue(desc: ValueDesc, data: Nothing?) {
        desc.acceptChildren(this, data)
    }

    override fun <T : Any> visitClass(desc: ClassValueDesc<T>, data: Nothing?) {
        super.visitClass(desc, data)

        if (mappings.any { it.forClass == desc.type }) return

        val defaultInstance =
            createInstanceWithMostDefaultValues(desc.type, desc.properties.mapValues { it.value.origin })

        val optionalParameters = desc.type.primaryConstructor!!.parameters.filter { it.isOptional }

        mappings.add(
            DefaultValuesMapping(
                desc.type,
                optionalParameters.associateTo(mutableMapOf()) { param ->
                    val value = findCorrespondingProperty(desc, param).get(defaultInstance)
                    param.name!! to value
                }
            )
        )
    }


    private fun <T : Any> findCorrespondingProperty(
        desc: ClassValueDesc<T>,
        param: KParameter
    ) = desc.type.memberProperties.single { it.name == param.name }.cast<KProperty1<Any, Any>>()

    private fun <T : Any> createInstanceWithMostDefaultValues(clazz: KClass<T>, arguments: Map<KParameter, Any?>): T {
        val primaryConstructor = clazz.primaryConstructor ?: error("Type $clazz does not have primary constructor.")
        return primaryConstructor.callBy(arguments.filter { !it.key.isOptional })
    }
}