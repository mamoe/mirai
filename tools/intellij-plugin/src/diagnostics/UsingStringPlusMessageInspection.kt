/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.intellij.diagnostics.fix.ConvertToPlainTextFix
import net.mamoe.mirai.console.intellij.resolve.findChild
import net.mamoe.mirai.console.intellij.resolve.hasSuperType
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.search.declarationsSearch.findDeepestSuperMethodsKotlinAware
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import java.util.*

/*
private val bundle by lazy {
    BundleUtil.loadLanguageBundle(PluginMainServiceNotConfiguredInspection::class.java.classLoader, "messages.InspectionGadgetsBundle")!!
}*/

class UsingStringPlusMessageInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return referenceExpressionVisitor visitor@{ expression ->
            val originalCallee = expression.resolve() ?: return@visitor
            if (originalCallee !is KtNamedFunction) return@visitor

            val callee = findDeepestSuperMethodsKotlinAware(originalCallee).lastOrNull() as? KtNamedFunction ?: originalCallee

            val className = callee.containingClassOrObject?.fqName?.asString()
            if (className != "kotlin.String") return@visitor
            if (callee.name != "plus") return@visitor

            val parent = expression.parent

            val inspectionTarget = when (parent) {
                is KtBinaryExpression -> {
                    val right = parent.right?.referenceExpression()?.resolve() as? KtDeclaration ?: return@visitor
                    val rightType = right.type() ?: return@visitor
                    if (!rightType.hasSuperType("net.mamoe.mirai.message.data.Message")) return@visitor
                    parent.left
                }
                is KtCallExpression -> {
                    val argumentType = parent
                        .valueArguments.singleOrNull()
                        ?.findChild<KtReferenceExpression>()
                        ?.resolve()?.castOrNull<KtDeclaration>()?.type()
                        ?: return@visitor
                    if (!argumentType.hasSuperType("net.mamoe.mirai.message.data.Message")) return@visitor

                    parent.parent?.castOrNull<KtDotQualifiedExpression>()?.receiverExpression // explicit receiver, inspection on it.
                        ?: parent.findChild<KtNameReferenceExpression>() // implicit receiver, inspection on 'plus'
                }
                else -> null
            } ?: return@visitor

            println(expression::class.qualifiedName + "    " + callee::class.qualifiedName + "    " + callee.text)

            holder.registerProblem(
                inspectionTarget,
                "使用 String + Message 会导致 Message 被转换为 String 再相加",
                ProblemHighlightType.WARNING,
                ConvertToPlainTextFix(inspectionTarget)
            )
        }
    }
}