/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.resolve

import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.resolve.COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance


/**
 * For CompositeCommand.SubCommand
 */
fun KtNamedFunction.isCompositeCommandSubCommand(): Boolean = this.hasAnnotation(COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME)

/**
 * SimpleCommand.Handler
 */
fun KtNamedFunction.isSimpleCommandHandler(): Boolean = this.hasAnnotation(SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME)

fun KtNamedFunction.isSimpleCommandHandlerOrCompositeCommandSubCommand(): Boolean =
    this.isSimpleCommandHandler() || this.isCompositeCommandSubCommand()


val KtPureClassOrObject.allSuperTypes: Sequence<KtSuperTypeListEntry>
    get() = sequence {
        yieldAll(superTypeListEntries)
        for (list in superTypeListEntries.asSequence()) {
            yieldAll((list.typeAsUserType?.referenceExpression?.resolve() as? KtClass)?.allSuperTypes.orEmpty())
        }
    }

fun KtConstructorCalleeExpression.getTypeAsUserType(): KtUserType? {
    val reference = typeReference
    if (reference != null) {
        val element = reference.typeElement
        if (element is KtUserType) {
            return element
        }
    }
    return null
}

inline fun <reified E> PsiElement.findParent(): E? = this.parents.filterIsInstance<E>().firstOrNull()

val KtClassOrObject.allSuperNames: Sequence<FqName> get() = allSuperTypes.mapNotNull { it.getKotlinFqName() }

val PsiElement.parents: Sequence<PsiElement>
    get() {
        val seed = if (this is PsiFile) null else parent
        return generateSequence(seed) { if (it is PsiFile) null else it.parent }
    }

fun getElementForLineMark(callElement: PsiElement): PsiElement =
    when (callElement) {
        is KtSimpleNameExpression -> callElement.getReferencedNameElement()
        else ->
            // a fallback,
            //but who knows what to reference in KtArrayAccessExpression ?
            generateSequence(callElement, { it.firstChild }).last()
    }

val KtAnnotationEntry.annotationClass: KtClass?
    get() = calleeExpression?.constructorReferenceExpression?.resolve()?.findParent<KtClass>()

fun KtAnnotated.hasAnnotation(fqName: FqName): Boolean =
    this.annotationEntries.any { it.annotationClass?.getKotlinFqName() == fqName }

val PsiElement.allChildrenFlat: Sequence<PsiElement>
    get() {
        return sequence {
            for (child in children) {
                yield(child)
                yieldAll(child.allChildrenFlat)
            }
        }
    }

inline fun <reified E> PsiElement.findChild(): E? = this.children.find { it is E } as E?

fun KtElement?.getResolvedCallOrResolveToCall(
    context: BindingContext,
    bodyResolveMode: BodyResolveMode = BodyResolveMode.PARTIAL,
): ResolvedCall<out CallableDescriptor>? {
    return this?.getCall(context)?.getResolvedCall(context) ?: this?.resolveToCall(bodyResolveMode)
}

val ResolvedCall<out CallableDescriptor>.valueParameters: List<ValueParameterDescriptor> get() = this.resultingDescriptor.valueParameters

fun KtExpression.resolveStringConstantValue(bindingContext: BindingContext): String? {
    when (this) {
        is KtNameReferenceExpression -> {
            when (val reference = references.firstIsInstance<KtSimpleNameReference>().resolve()) {
                is KtDeclaration -> {
                    val descriptor = reference.descriptor.castOrNull<VariableDescriptor>() ?: return null
                    val compileTimeConstant = descriptor.compileTimeInitializer ?: return null
                    return compileTimeConstant.castOrNull<StringValue>()?.value
                }
                is PsiDeclarationStatement -> {

                }
            }
        }
        is KtStringTemplateExpression -> {
            if (hasInterpolation()) return null
            return entries.joinToString("") { it.text }
        }
        /*
        is KtCallExpression -> {
            val callee = this.calleeExpression?.getResolvedCallOrResolveToCall(bindingContext)?.resultingDescriptor
            if (callee is VariableDescriptor) {
                val compileTimeConstant = callee.compileTimeInitializer ?: return null
                return compileTimeConstant.castOrNull<StringValue>()?.value
            }
            return null
        }*/
        is KtConstantExpression -> {
            // TODO: 2020/9/18  KtExpression.resolveStringConstantValue: KtConstantExpression
        }
        else -> return null
    }
    return null
}