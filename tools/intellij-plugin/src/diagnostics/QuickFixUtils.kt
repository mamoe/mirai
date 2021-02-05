/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction


fun <T: PsiElement> LocalQuickFix(text: String, element: T, invokeAction: QuickFixInvoke<T>.() -> Unit): LocalQuickFix {
    return object:  KotlinCrossLanguageQuickFixAction<T>(element), KotlinUniversalQuickFix {
        @Suppress("DialogTitleCapitalization")
        override fun getFamilyName(): String = "Mirai console"
        override fun getText(): String = text
        override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
            invokeAction(QuickFixInvoke(project, editor ?: return, file, this.element ?: return))
        }
    }
}

class QuickFixInvoke<T>(
    val project: Project,
    val editor: Editor,
    val file: PsiFile,
    val element: T,
)