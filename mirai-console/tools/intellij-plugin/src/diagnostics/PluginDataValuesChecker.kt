/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.compiler.common.SERIALIZABLE_FQ_NAME
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.compiler.common.resolve.*
import net.mamoe.mirai.console.intellij.resolve.bodyCalls
import net.mamoe.mirai.console.intellij.resolve.hasSuperType
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.idea.project.builtIns
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType

class PluginDataValuesChecker : DeclarationChecker {
    /**
     * [KtObjectDeclaration], [KtParameter], [KtPrimaryConstructor], [KtClass], [KtNamedFunction], [KtProperty]
     */
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        val bindingContext = context.bindingContext

        //println(declaration::class.qualifiedName + "\t:" + declaration.text.take(10))

        if (declaration is KtProperty) {
            checkReadOnly(declaration, context)
        }

        val calls = declaration.bodyCalls(bindingContext) ?: return

        for ((call, expr) in calls) {
            check(call, expr, context)
        }
    }

    /**
     * Check `PluginData.value` calls
     */
    fun check(call: ResolvedCall<out CallableDescriptor>, expr: KtExpression, context: DeclarationCheckerContext) {
        if (!call.isCalling(PLUGIN_DATA_VALUE_FUNCTIONS_FQ_FQ_NAME)) return

        if (expr is KtCallExpression)
            checkConstructableAndSerializable(call, expr, context)
    }

    private fun KtProperty.isInsideOrExtensionOfReadOnlyPluginData(): Boolean {
        return containingClassOrObject?.hasSuperType(READ_ONLY_PLUGIN_DATA_FQ_NAME) == true // inside
                || receiverTypeReference?.hasSuperType(READ_ONLY_PLUGIN_DATA_FQ_NAME) == true // extension
    }

    private fun checkReadOnly(property: KtProperty, context: DeclarationCheckerContext) {
        // first parent is KtPropertyDelegate, next is KtProperty

        if (property.isVar // var
            && property.delegateExpression?.getResolvedCall(context)
                ?.isCalling(PLUGIN_DATA_VALUE_FUNCTIONS_FQ_FQ_NAME) == true // by value()
            && property.isInsideOrExtensionOfReadOnlyPluginData() // extensionReceiver is ReadOnlyPluginData or null
        ) {
            context.report(MiraiConsoleErrors.READ_ONLY_VALUE_CANNOT_BE_VAR.on(property.valOrVarKeyword))
        }
    }

    private fun checkConstructableAndSerializable(
        call: ResolvedCall<out CallableDescriptor>,
        expr: KtCallExpression,
        context: DeclarationCheckerContext
    ) {
        if (call.resultingDescriptor.resolveContextKinds?.contains(ResolveContextKind.RESTRICTED_NO_ARG_CONSTRUCTOR) != true) return

        for ((typeParameterDescriptor, kotlinType) in call.typeArguments.entries) {
            if ((typeParameterDescriptor.isReified || typeParameterDescriptor.resolveContextKinds?.contains(
                    ResolveContextKind.RESTRICTED_NO_ARG_CONSTRUCTOR
                ) == true)
                && kotlinType is SimpleType
            ) {

                checkConstructableAndSerializable(kotlinType, expr, context)
                checkFixType(kotlinType, expr, context)
            }
        }
    }

    private fun checkFixType(type: KotlinType, callExpr: KtCallExpression, context: DeclarationCheckerContext) {
        val inspectionTarget = retrieveInspectionTarget(type, callExpr) ?: return
        val classDescriptor = type.classDescriptor() ?: return
        val jetTypeFqn = type.getJetTypeFqName(false)

        val builtIns = callExpr.builtIns
        val factory = when {
            jetTypeFqn == "java.util.concurrent.ConcurrentHashMap" -> MiraiConsoleErrors.USING_DERIVED_CONCURRENT_MAP_TYPE

            classDescriptor.isSubclassOf(builtIns.list) && jetTypeFqn != "kotlin.collections.List" -> {
                if (classDescriptor.isSubclassOf(builtIns.mutableList)) {
                    if (jetTypeFqn != "kotlin.collections.MutableList" && jetTypeFqn != "java.util.List") {
                        MiraiConsoleErrors.USING_DERIVED_MUTABLE_LIST_TYPE
                    } else null
                } else MiraiConsoleErrors.USING_DERIVED_LIST_TYPE
            }

            classDescriptor.isSubclassOf(builtIns.map) && jetTypeFqn != "kotlin.collections.Map" -> {
                if (classDescriptor.isSubclassOf(builtIns.mutableMap)) {
                    if (jetTypeFqn != "kotlin.collections.MutableMap" && jetTypeFqn != "java.util.Map") {
                        MiraiConsoleErrors.USING_DERIVED_MUTABLE_MAP_TYPE
                    } else null
                } else MiraiConsoleErrors.USING_DERIVED_MAP_TYPE
            }

            else -> return
        } ?: return

        context.report(factory.on(inspectionTarget, callExpr, jetTypeFqn.substringAfterLast('.')))
    }

    private fun checkConstructableAndSerializable(
        type: KotlinType,
        callExpr: KtCallExpression,
        context: DeclarationCheckerContext
    ) {
        val classDescriptor = type.classDescriptor() ?: return

        if (canBeSerializedInternally(classDescriptor)) return

        val inspectionTarget = retrieveInspectionTarget(type, callExpr) ?: return

        if (!classDescriptor.hasNoArgConstructor())
            return context.report(
                MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE.on(
                    inspectionTarget,
                    callExpr,
                    type.fqName?.asString().toString()
                )
            )

        if (!classDescriptor.hasAnnotation(SERIALIZABLE_FQ_NAME))
            return context.report(
                MiraiConsoleErrors.UNSERIALIZABLE_TYPE.on(
                    inspectionTarget,
                    classDescriptor
                )
            )
    }

    private fun KotlinType.classDescriptor() = constructor.declarationDescriptor?.castOrNull<ClassDescriptor>()

    private fun retrieveInspectionTarget(type: KotlinType, callExpr: KtCallExpression): KtTypeProjection? {
        val fqName = type.fqName ?: return null
        return callExpr.typeArguments.find { it.typeReference?.isReferencing(fqName) == true }
    }
}

private fun canBeSerializedInternally(descriptor: ClassDescriptor): Boolean {
    @Suppress("UNUSED_VARIABLE") val name = when (descriptor.defaultType.getJetTypeFqName(false)) {
        // kotlinx.serialization
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

        // mirai
        "java.util.concurrent.ConcurrentMap",
        "java.util.concurrent.ConcurrentHashMap",
        -> "ConcurrentMap" // dummy name
        else -> return false
    }
    return true
}

