/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue

fun Annotated.hasAnnotation(fqName: FqName) = this.annotations.hasAnnotation(fqName)
fun Annotated.findAnnotation(fqName: FqName) = this.annotations.findAnnotation(fqName)


val PsiElement.allChildrenWithSelfSequence: Sequence<PsiElement>
    get() = sequence {
        yield(this@allChildrenWithSelfSequence)
        for (child in children) {
            yieldAll(child.allChildrenWithSelfSequence)
        }
    }


val PsiElement.childrenWithSelf: List<PsiElement>
    get() = listOf(this, *children)


inline fun <reified E> PsiElement.findParent(): E? = this.parents.filterIsInstance<E>().firstOrNull()


val PsiElement.parents: Sequence<PsiElement>
    get() {
        val seed = if (this is PsiFile) null else parent
        return generateSequence(seed) { if (it is PsiFile) null else it.parent }
    }


fun ClassDescriptor.findNoArgConstructor(): ClassConstructorDescriptor? {
    return constructors.find { desc ->
        desc.valueParameters.all { it.hasDefaultValue() }
    }
}

fun ClassDescriptor.hasNoArgConstructor(): Boolean = this.findNoArgConstructor() != null