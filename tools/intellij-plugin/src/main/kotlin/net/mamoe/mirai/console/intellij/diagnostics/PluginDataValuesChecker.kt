/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.compiler.common.SERIALIZABLE_FQ_NAME
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.compiler.common.resolve.*
import net.mamoe.mirai.console.intellij.resolve.resolveAllCallsWithElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.types.SimpleType


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
                val classDescriptor = type.constructor.declarationDescriptor?.castOrNull<ClassDescriptor>()

                val inspectionTarget: PsiElement by lazy {
                    callExpr.typeArguments.find { it.references.firstOrNull()?.canonicalText == type.fqName?.toString() } ?: callExpr
                }

                if (classDescriptor == null
                    || !classDescriptor.hasNoArgConstructor()
                ) return@forEach context.report(MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE.on(
                    inspectionTarget,
                    type.fqName?.asString().toString())
                )

                if (!classDescriptor.hasAnnotation(SERIALIZABLE_FQ_NAME)) // TODO: 2020/9/18 external serializers
                    return@forEach context.report(MiraiConsoleErrors.UNSERIALIZABLE_TYPE.on(
                        inspectionTarget,
                        classDescriptor
                    ))
            }
    }
}