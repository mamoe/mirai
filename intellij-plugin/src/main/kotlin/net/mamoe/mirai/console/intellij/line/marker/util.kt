/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.line.marker

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceExpression
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*


internal val KtPureClassOrObject.allSuperTypes: Sequence<KtSuperTypeListEntry>
    get() = sequence {
        yieldAll(superTypeListEntries)
        for (list in superTypeListEntries.asSequence()) {
            yieldAll((list.typeAsUserType?.referenceExpression?.resolve() as? KtClass)?.allSuperTypes.orEmpty())
        }
    }

internal inline fun <reified E> PsiElement.findParent(): E? = this.parents.filterIsInstance<E>().firstOrNull()

internal val KtClassOrObject.allSuperNames: Sequence<FqName> get() = allSuperTypes.mapNotNull { it.getKotlinFqName() }

fun PsiReferenceExpression.hasBridgeCalls(): Boolean {
    val resolved = this.resolve() as? KtLightMethod ?: return false

    TODO()
}

val PsiElement.parents: Sequence<PsiElement>
    get() {
        val seed = if (this is PsiFile) null else parent
        return generateSequence(seed) { if (it is PsiFile) null else it.parent }
    }

internal fun getElementForLineMark(callElement: PsiElement): PsiElement =
    when (callElement) {
        is KtSimpleNameExpression -> callElement.getReferencedNameElement()
        else ->
            // a fallback,
            //but who knows what to reference in KtArrayAccessExpression ?
            generateSequence(callElement, { it.firstChild }).last()
    }

internal val KtAnnotationEntry.annotationClass: KtClass?
    get() = calleeExpression?.constructorReferenceExpression?.resolve()?.findParent<KtClass>()

internal fun KtAnnotated.hasAnnotation(fqName: FqName): Boolean =
    this.annotationEntries.any { it.annotationClass?.getKotlinFqName() == fqName }