/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.resolve.READ_ONLY_PLUGIN_DATA_FQ_NAME
import net.mamoe.mirai.console.intellij.resolve.getResolvedCall
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlinx.serialization.compiler.resolve.toClassDescriptor

fun KotlinType.isSubtypeOfReadOnlyPluginData(): Boolean {
    return this.toClassDescriptor?.getAllSuperClassifiers()
        ?.any { it.fqNameOrNull() == READ_ONLY_PLUGIN_DATA_FQ_NAME } == true
}

fun DeclarationCheckerContext.report(diagnostic: Diagnostic) {
    return this.trace.report(diagnostic)
}

val DeclarationCheckerContext.bindingContext get() = this.trace.bindingContext

fun KtElement.getResolvedCall(
    context: DeclarationCheckerContext,
): ResolvedCall<out CallableDescriptor>? {
    return this.getResolvedCall(context.bindingContext)
}

fun KtTypeReference.isReferencing(fqName: FqName): Boolean {
    return resolveReferencedType()?.getKotlinFqName() == fqName
}

val KtTypeReference.referencedUserType: KtUserType? get() = this.typeElement.castOrNull()

fun KtTypeReference.resolveReferencedType(): PsiElement? {
    val resolved = referencedUserType?.referenceExpression?.mainReference?.resolve()
    if (resolved is KtTypeParameter) {
        val bound = resolved.extendsBound ?: return resolved
        if (bound.name == resolved.name) return null // <C: C> bad type, avoid infinite run
        return bound.resolveReferencedType()
    }
    return resolved
}