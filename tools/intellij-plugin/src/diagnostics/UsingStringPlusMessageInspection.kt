/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import net.mamoe.mirai.console.intellij.resolve.*
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny

/*
private val bundle by lazy {
    BundleUtil.loadLanguageBundle(PluginMainServiceNotConfiguredInspection::class.java.classLoader, "messages.InspectionGadgetsBundle")!!
}*/

class UsingStringPlusMessageInspection : AbstractKotlinInspection() {
    companion object {
        const val DESCRIPTION = "使用 String + Message 会导致 Message 被转换为 String 再相加"
        private const val MESSAGE_FQ_NAME_STR = "net.mamoe.mirai.message.data.Message"
        private const val CONVERT_TO_PLAIN_TEXT = "将 String 转换为 PlainText"


        fun KtReferenceExpression.isCallingStringPlus(): Boolean {
            val callee = this.referenceExpression()?.resolve() ?: return false
            if (callee !is KtNamedFunction) return false

            val className = callee.containingClassOrObject?.fqName?.asString()
            if (className != "kotlin.String") return false
            if (callee.name != "plus") return false
            return true
        }
    }

    private class Visitor(
        val holder: ProblemsHolder
    ) : KtVisitorVoid() {
        class BinaryExprFix(left: KtExpression) : ConvertToPlainTextFix<KtExpression>(left) {
            override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
                if (editor == null || file !is KtFile) return
                val element = element ?: return

                val referenceExpr = element.referenceExpression()
                if (referenceExpr == null || element.parent is KtBinaryExpression) {
                    // `+ operator`, e.g. `str + msg`
                    // or
                    // complex expressions, e.g. `str.toString().plus(msg)`, `"".also {  }.plus(msg)`
                    val replaced =
                        element.replace(KtPsiFactory(project).createExpression("net.mamoe.mirai.message.data.PlainText(${element.text})"))
                                as? KtElement ?: return
                    ShortenReferences.DEFAULT.process(replaced)
                    return
                }

                if (element is KtNameReferenceExpression) {
                    val receiver = element.getQualifiedExpressionForReceiver() ?: return
                    val replaced = receiver
                        .replace(KtPsiFactory(project).createExpression("net.mamoe.mirai.message.data.PlainText(${receiver.text})"))
                            as? KtElement ?: return

                    ShortenReferences.DEFAULT.process(replaced)
                }
            }
        }

        override fun visitBinaryExpression(binaryExpression: KtBinaryExpression) {
            if (binaryExpression.operationToken != KtTokens.PLUS) return
            if (binaryExpression.left?.getCalleeExpressionIfAny()?.typeFqName()?.toString() != "kotlin.String") return

            val rightType = binaryExpression.right?.type() ?: return
            if (!rightType.hasSuperType(MESSAGE_FQ_NAME_STR)) return

            val left = binaryExpression.left ?: return

            holder.registerProblem(
                left,
                DESCRIPTION,
                ProblemHighlightType.WARNING,
                BinaryExprFix(left)
            )
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            if (!expression.isCallingStringPlus()) return
            val argumentType = expression.valueArguments.singleOrNull()?.type() ?: return

            if (!argumentType.hasSuperType(MESSAGE_FQ_NAME_STR)) return

            val explicitReceiverExpr = expression.siblingDotReceiverExpression()
            if (explicitReceiverExpr != null) {
                holder.registerProblem(
                    explicitReceiverExpr,
                    DESCRIPTION,
                    ProblemHighlightType.WARNING,
                    LocalQuickFix(CONVERT_TO_PLAIN_TEXT, explicitReceiverExpr) {
                        element.replaceExpressionAndShortenReferences("net.mamoe.mirai.message.data.PlainText(${element.text})")
                    }
                )
            } else {
                val nameReferenceExpression =
                    expression.findChild<KtNameReferenceExpression>() ?: expression.calleeExpression ?: expression
                holder.registerProblem(
                    nameReferenceExpression,
                    DESCRIPTION,
                    ProblemHighlightType.WARNING,
                    LocalQuickFix(CONVERT_TO_PLAIN_TEXT, expression) {
                        val callExpression = this.element.calleeExpression ?: return@LocalQuickFix
                        val implicitReceiverText = this.element.implicitExpressionText() ?: return@LocalQuickFix

                        this.element.replaceExpressionAndShortenReferences(
                            "net.mamoe.mirai.message.data.PlainText(${implicitReceiverText}).${callExpression.text}"
                        )
                    }
                )
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder)

    abstract class ConvertToPlainTextFix<T : PsiElement>(element: T) : KotlinCrossLanguageQuickFixAction<T>(element),
        KotlinUniversalQuickFix {
        @Suppress("DialogTitleCapitalization")
        override fun getFamilyName(): String = "Mirai Console"

        @Suppress("DialogTitleCapitalization")
        override fun getText(): String = "将 String 转换为 PlainText"
    }
}


fun KtElement.replaceExpressionAndShortenReferences(expression: String) {
    val replaced = replace(KtPsiFactory(this.project).createExpression(expression)) as? KtElement ?: return
    ShortenReferences.DEFAULT.process(replaced)
}