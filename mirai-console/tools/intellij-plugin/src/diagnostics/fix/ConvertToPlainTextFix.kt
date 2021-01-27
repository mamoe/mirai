/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics.fix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.util.getFactoryForImplicitReceiverWithSubtypeOf
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

class ConvertToPlainTextFix(
    /**
     * Maybe:
     *
     * - [KtNameReferenceExpression]: if implicit receiver
     * - [KtExpression]
     */
    element: KtExpression,
) : KotlinCrossLanguageQuickFixAction<KtExpression>(element), KotlinUniversalQuickFix {

    override fun getFamilyName(): String = "Mirai Console"
    override fun getText(): String = "将 String 转换为 PlainText"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        if (editor == null) return
        if (file !is KtFile) return
        val element = element ?: return

        val psiFactory = KtPsiFactory(project)

        val referenceExpr = element.referenceExpression()
        if (referenceExpr == null || element.parent is KtBinaryExpression) {
            // + operator, e.g. 'str + msg'
            // or
            // complex expressions, e.g. 'str.toString().plus(msg)', '"".also {  }.plus(msg)'
            val replaced = element.replace(psiFactory.createExpression("net.mamoe.mirai.message.data.PlainText(${element.text})"))
                as? KtElement ?: return
            ShortenReferences.DEFAULT.process(replaced)
            return
        }

        val resolved = referenceExpr.resolve()
        if (resolved !is KtDeclaration) return
        // 'plus' function
        // perform fix on receiver
        val dotQualifiedExpr = element.parent
        if (dotQualifiedExpr is KtDotQualifiedExpression) {
            // got explicit receiver
            val replaced = dotQualifiedExpr.receiverExpression
                .replace(psiFactory.createExpression("net.mamoe.mirai.message.data.PlainText(${dotQualifiedExpr.receiverExpression.text})"))
                as? KtElement ?: return

            ShortenReferences.DEFAULT.process(replaced)
        } else {
            // implicit receiver
            val context = element.analyze()
            val scope = element.getResolutionScope(context) ?: return

            val descriptor = element.resolveToCall()?.resultingDescriptor ?: return
            val receiverDescriptor = descriptor.extensionReceiverParameter
                ?: descriptor.dispatchReceiverParameter
                ?: return
            val receiverType = receiverDescriptor.type

            val expressionFactory = scope.getFactoryForImplicitReceiverWithSubtypeOf(receiverType) ?: return
            val receiverText = if (expressionFactory.isImmediate) "this" else expressionFactory.expressionText

            // element.parent is 'plus(msg)'
            // replace it with a dot qualified expr
            val replaced =
                element.parent.replace(psiFactory.createExpression("net.mamoe.mirai.message.data.PlainText($receiverText).${element.parent.text}"))
                    as? KtElement ?: return
            ShortenReferences.DEFAULT.process(replaced)

        }
    }
}
