/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics.fix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.SimpleDiagnostic
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * @see MiraiConsoleErrors.READ_ONLY_VALUE_CANNOT_BE_VAR
 */
class ConvertToValFix(
    element: PsiElement,
) : KotlinCrossLanguageQuickFixAction<PsiElement>(element), KotlinUniversalQuickFix {

    override fun getFamilyName(): String = "Mirai Console"
    override fun getText(): String = "转换为 val"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        val element = element ?: return
        project.executeWriteCommand(name) {
            element.replace(KtPsiFactory(project).createValKeyword())
        }
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val diagnostic1 = diagnostic.castOrNull<SimpleDiagnostic<PsiElement>>() ?: return null
            return ConvertToValFix(diagnostic1.psiElement)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}
