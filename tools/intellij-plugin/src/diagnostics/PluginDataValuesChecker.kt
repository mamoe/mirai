/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.compiler.common.SERIALIZABLE_FQ_NAME
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.compiler.common.resolve.*
import net.mamoe.mirai.console.intellij.resolve.resolveAllCallsWithElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlinx.serialization.compiler.resolve.*


class PluginDataValuesChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        val bindingContext = context.bindingContext
        declaration.resolveAllCallsWithElement(bindingContext)
            .filter { (call) -> call.isCalling(PLUGIN_DATA_VALUE_FUNCTIONS_FQ_FQ_NAME) }
            .filter { (call) ->
                call.resultingDescriptor.resolveContextKinds?.contains(ResolveContextKind.RESTRICTED_NO_ARG_CONSTRUCTOR) == true
            }.flatMap { (call, element) ->
                call.typeArguments.entries.associateWith { element }.asSequence()
            }.filter { (e, _) ->
                val (p, t) = e
                (p.isReified || p.resolveContextKinds?.contains(ResolveContextKind.RESTRICTED_NO_ARG_CONSTRUCTOR) == true)
                    && t is SimpleType
            }.forEach { (e, callExpr) ->
                val (_, type) = e
                val classDescriptor = type.constructor.declarationDescriptor?.castOrNull<ClassDescriptor>() ?: return@forEach

                if (canBeSerializedInternally(classDescriptor)) return@forEach

                val inspectionTarget = kotlin.run {
                    val fqName = type.fqName ?: return@run null
                    callExpr.typeArguments.find { it.typeReference?.isReferencing(fqName) == true }
                } ?: return@forEach

                if (!classDescriptor.hasNoArgConstructor())
                    return@forEach context.report(MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE.on(
                        inspectionTarget,
                        callExpr,
                        type.fqName?.asString().toString())
                    )

                if (!classDescriptor.hasAnnotation(SERIALIZABLE_FQ_NAME))
                    return@forEach context.report(MiraiConsoleErrors.UNSERIALIZABLE_TYPE.on(
                        inspectionTarget,
                        classDescriptor
                    ))
            }
    }
}

private fun canBeSerializedInternally(descriptor: ClassDescriptor): Boolean {
    @Suppress("UNUSED_VARIABLE") val name = when (descriptor.defaultType.getJetTypeFqName(false)) {
        "kotlin.Unit" -> "UnitSerializer"
        "Z", "kotlin.Boolean" -> "BooleanSerializer"
        "B", "kotlin.Byte" -> "ByteSerializer"
        "S", "kotlin.Short" -> "ShortSerializer"
        "I", "kotlin.Int" -> "IntSerializer"
        "J", "kotlin.Long" -> "LongSerializer"
        "F", "kotlin.Float" -> "FloatSerializer"
        "D", "kotlin.Double" -> "DoubleSerializer"
        "C", "kotlin.Char" -> "CharSerializer"
        "kotlin.String" -> "StringSerializer"
        "kotlin.Pair" -> "PairSerializer"
        "kotlin.Triple" -> "TripleSerializer"
        "kotlin.collections.Collection", "kotlin.collections.List",
        "kotlin.collections.ArrayList", "kotlin.collections.MutableList",
        -> "ArrayListSerializer"
        "kotlin.collections.Set", "kotlin.collections.LinkedHashSet", "kotlin.collections.MutableSet" -> "LinkedHashSetSerializer"
        "kotlin.collections.HashSet" -> "HashSetSerializer"
        "kotlin.collections.Map", "kotlin.collections.LinkedHashMap", "kotlin.collections.MutableMap" -> "LinkedHashMapSerializer"
        "kotlin.collections.HashMap" -> "HashMapSerializer"
        "kotlin.collections.Map.Entry" -> "MapEntrySerializer"
        "kotlin.ByteArray" -> "ByteArraySerializer"
        "kotlin.ShortArray" -> "ShortArraySerializer"
        "kotlin.IntArray" -> "IntArraySerializer"
        "kotlin.LongArray" -> "LongArraySerializer"
        "kotlin.CharArray" -> "CharArraySerializer"
        "kotlin.FloatArray" -> "FloatArraySerializer"
        "kotlin.DoubleArray" -> "DoubleArraySerializer"
        "kotlin.BooleanArray" -> "BooleanArraySerializer"
        "java.lang.Boolean" -> "BooleanSerializer"
        "java.lang.Byte" -> "ByteSerializer"
        "java.lang.Short" -> "ShortSerializer"
        "java.lang.Integer" -> "IntSerializer"
        "java.lang.Long" -> "LongSerializer"
        "java.lang.Float" -> "FloatSerializer"
        "java.lang.Double" -> "DoubleSerializer"
        "java.lang.Character" -> "CharSerializer"
        "java.lang.String" -> "StringSerializer"
        "java.util.Collection", "java.util.List", "java.util.ArrayList" -> "ArrayListSerializer"
        "java.util.Set", "java.util.LinkedHashSet" -> "LinkedHashSetSerializer"
        "java.util.HashSet" -> "HashSetSerializer"
        "java.util.Map", "java.util.LinkedHashMap" -> "LinkedHashMapSerializer"
        "java.util.HashMap" -> "HashMapSerializer"
        "java.util.Map.Entry" -> "MapEntrySerializer"
        else -> return false
    }
    return true
}

