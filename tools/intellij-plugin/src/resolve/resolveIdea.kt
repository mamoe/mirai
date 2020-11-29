/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.resolve

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsWithSelf
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.resolve.COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.allChildrenWithSelf
import net.mamoe.mirai.console.compiler.common.resolve.findParent
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
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
            yieldAll((list.typeAsUserType?.referenceExpression?.resolve()?.parentsWithSelf?.filterIsInstance<KtClass>()
                ?.firstOrNull())?.allSuperTypes.orEmpty())
        }
    }

val PsiClass.allSuperTypes: Sequence<PsiClass>
    get() = sequence {
        interfaces.forEach {
            yield(it)
            yieldAll(it.allSuperTypes)
        }
        val superClass = superClass
        if (superClass != null) {
            yield(superClass)
            yieldAll(superClass.allSuperTypes)
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

fun KtClassOrObject.hasSuperType(fqName: FqName): Boolean = allSuperNames.contains(fqName)
fun KtClass.hasSuperType(fqName: FqName): Boolean = allSuperNames.contains(fqName)

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.LowPriorityInOverloadResolution
fun PsiElement.hasSuperType(fqName: FqName): Boolean = allSuperNames.contains(fqName)

val KtClassOrObject.allSuperNames: Sequence<FqName> get() = allSuperTypes.mapNotNull { it.getKotlinFqName() }
val PsiClass.allSuperNames: Sequence<FqName> get() = allSuperTypes.mapNotNull { clazz -> clazz.qualifiedName?.let { FqName(it) } }

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.LowPriorityInOverloadResolution
val PsiElement.allSuperNames: Sequence<FqName>
    get() {
        return when (this) {
            is KtClassOrObject -> allSuperNames
            is PsiClass -> allSuperNames
            else -> emptySequence()
        }
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

fun KtDeclaration.resolveAllCalls(bindingContext: BindingContext): Sequence<ResolvedCall<*>> {
    return allChildrenWithSelf
        .filterIsInstance<KtCallExpression>()
        .mapNotNull { it.calleeExpression?.getResolvedCall(bindingContext) }
}

fun KtDeclaration.resolveAllCallsWithElement(bindingContext: BindingContext): Sequence<Pair<ResolvedCall<out CallableDescriptor>, KtCallExpression>> {
    return allChildrenWithSelf
        .filterIsInstance<KtCallExpression>()
        .mapNotNull {
            val callee = it.calleeExpression ?: return@mapNotNull null
            val resolved = callee.getResolvedCall(bindingContext) ?: return@mapNotNull null

            resolved to it
        }
}

fun ResolvedCall<*>.valueParametersWithArguments(): List<Pair<ValueParameterDescriptor, ValueArgument>> {
    return this.valueParameters.zip(this.valueArgumentsByIndex?.mapNotNull { it.arguments.firstOrNull() }.orEmpty())
}

fun ValueArgument.resolveStringConstantValues(): Sequence<String>? {
    return this.getArgumentExpression()?.resolveStringConstantValues()
}

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

fun KtElement?.getResolvedCall(
    context: BindingContext,
): ResolvedCall<out CallableDescriptor>? {
    return this?.getCall(context)?.getResolvedCall(context)
}

val ResolvedCall<out CallableDescriptor>.valueParameters: List<ValueParameterDescriptor> get() = this.resultingDescriptor.valueParameters

fun ConstantValue<*>.selfOrChildrenConstantStrings(): Sequence<String> {
    return when (this) {
        is StringValue -> sequenceOf(value)
        is ArrayValue -> sequence {
            yieldAll(this@selfOrChildrenConstantStrings.selfOrChildrenConstantStrings())
        }
        else -> emptySequence()
    }
}

fun KtExpression.resolveStringConstantValues(): Sequence<String> {
    when (this) {
        is KtNameReferenceExpression -> {
            when (val reference = references.firstIsInstance<KtSimpleNameReference>().resolve()) {
                is KtDeclaration -> {
                    val descriptor = reference.resolveToDescriptorIfAny(BodyResolveMode.FULL).castOrNull<VariableDescriptor>() ?: return emptySequence()
                    val compileTimeConstant = descriptor.compileTimeInitializer ?: return emptySequence()
                    return compileTimeConstant.selfOrChildrenConstantStrings()
                }
                is PsiDeclarationStatement -> {
                    // TODO: 2020/9/18 compile-time constants from Java
                }
            }
        }
        is KtStringTemplateExpression -> {
            if (hasInterpolation()) return emptySequence()
            return sequenceOf(entries.joinToString("") { it.text })
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
    }
    return emptySequence()
}