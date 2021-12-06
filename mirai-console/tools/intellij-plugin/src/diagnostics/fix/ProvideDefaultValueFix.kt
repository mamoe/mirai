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
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.intellij.resolve.findChild
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.idea.core.moveCaret
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * @see MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE
 */
class ProvideDefaultValueFix(
    element: KtCallExpression,
    private val typeProjection: SmartPsiElementPointer<KtTypeProjection>,
) : KotlinCrossLanguageQuickFixAction<KtCallExpression>(element), KotlinUniversalQuickFix {

    override fun getFamilyName(): String = "Mirai Console"
    override fun getText(): String = "添加默认值"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        val element = element ?: return
        if (file !is KtFile) return


        /*
        val refereeFqName = element.resolve()?.getKotlinFqName() ?: return
        val referee = file.resolveImportReference(refereeFqName).singleOrNull { it is ClassDescriptor } ?: return
        ImportInsertHelper.getInstance(project).importDescriptor(file, referee)
         */

        val typeName =
            typeProjection.element?.typeReference?.typeElement?.castOrNull<KtUserType>()?.referencedName ?: return
        val argumentList = element.findChild<KtValueArgumentList>() ?: return
        val offset = argumentList.leftParenthesis?.endOffset ?: return

        project.executeWriteCommand(name) {
            argumentList.addArgument(KtPsiFactory(project).createArgument("$typeName()"))
            editor?.moveCaret(offset + typeName.length + 1)
        }
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val diagnostic1 =
                diagnostic.castOrNull<DiagnosticWithParameters2<KtTypeProjection, KtCallExpression, *>>() ?: return null
            return ProvideDefaultValueFix(diagnostic1.a, SmartPointerManager.createPointer(diagnostic1.psiElement))
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}
