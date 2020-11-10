/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.util.castSafelyTo
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.resolve.PLUGIN_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.parents
import net.mamoe.mirai.console.intellij.resolve.allSuperNames
import net.mamoe.mirai.console.intellij.resolve.getResolvedCall
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

fun DeclarationCheckerContext.report(diagnostic: Diagnostic) {
    return this.trace.report(diagnostic)
}

val DeclarationCheckerContext.bindingContext get() = this.trace.bindingContext

fun KtElement?.getResolvedCall(
    context: DeclarationCheckerContext,
): ResolvedCall<out CallableDescriptor>? {
    return this.getResolvedCall(context.bindingContext)
}

fun KtTypeReference.isReferencing(fqName: FqName): Boolean {
    return resolveReferencedType()?.getKotlinFqName() == fqName
}

val KtTypeReference.referencedUserType: KtUserType? get() = this.typeElement.castOrNull()

fun KtTypeReference.resolveReferencedType() = referencedUserType?.referenceExpression?.mainReference?.resolve()

fun KtReferenceExpression.resolveMiraiPluginDeclaration(): KtClassOrObject? {
    val main =
        parents.filterIsInstance<KtClassOrObject>().firstOrNull() ?: return null
    val kotlinPluginClass =
        resolve().castSafelyTo<KtConstructor<*>>()?.parent?.castSafelyTo<KtClass>() ?: return null
    if (kotlinPluginClass.allSuperNames.none { it == PLUGIN_FQ_NAME }) return null
    return main
}
