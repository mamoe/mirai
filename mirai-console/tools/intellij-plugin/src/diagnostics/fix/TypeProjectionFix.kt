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
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeProjection

abstract class AbstractTypeProjectionFix(
    element: KtTypeProjection,
    private val newTypeFqn: String,
) : KotlinCrossLanguageQuickFixAction<KtTypeProjection>(element), KotlinUniversalQuickFix {
    override fun getFamilyName(): String = "Mirai console"
    override fun getText(): String = "转化为 ${newTypeFqn.substringAfterLast('.')}"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        val element = element ?: return
        project.executeWriteCommand(name) {
            val arguments = element.text.substringAfter('<', "")

            val e = element.replaced(
                KtPsiFactory(project).createTypeArgument(
                    if (arguments.isBlank()) {
                        newTypeFqn
                    } else "$newTypeFqn<$arguments"
                )
            )
            ShortenReferences.DEFAULT.process(e)
        }
    }
}

class ConvertToMutableMapFix(
    element: KtTypeProjection,
) : AbstractTypeProjectionFix(element, "kotlin.collections.MutableMap") {
    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            return ConvertToMutableMapFix(diagnostic.psiElement as? KtTypeProjection ?: return null)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}

class ConvertToMapFix(
    element: KtTypeProjection,
) : AbstractTypeProjectionFix(element, "kotlin.collections.Map") {
    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            return ConvertToMapFix(diagnostic.psiElement as? KtTypeProjection ?: return null)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}

class ConvertToConcurrentMapFix(
    element: KtTypeProjection,
) : AbstractTypeProjectionFix(element, "java.util.concurrent.ConcurrentHashMap") {
    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            return ConvertToConcurrentMapFix(diagnostic.psiElement as? KtTypeProjection ?: return null)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}

class ConvertToListFix(
    element: KtTypeProjection,
) : AbstractTypeProjectionFix(element, "kotlin.collections.List") {
    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            return ConvertToListFix(diagnostic.psiElement as? KtTypeProjection ?: return null)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}

class ConvertToMutableListFix(
    element: KtTypeProjection,
) : AbstractTypeProjectionFix(element, "kotlin.collections.MutableList") {
    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            return ConvertToListFix(diagnostic.psiElement as? KtTypeProjection ?: return null)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}
