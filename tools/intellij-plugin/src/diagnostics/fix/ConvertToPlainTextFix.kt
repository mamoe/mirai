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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.imports.canBeAddedToImport
import org.jetbrains.kotlin.idea.imports.importableFqName
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.idea.references.resolveToDescriptors
import org.jetbrains.kotlin.idea.util.ImportDescriptorResult
import org.jetbrains.kotlin.idea.util.ImportInsertHelper
import org.jetbrains.kotlin.idea.util.getFactoryForImplicitReceiverWithSubtypeOf
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedElementSelector
import org.jetbrains.kotlin.psi.psiUtil.getReceiverExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.utils.checkWithAttachment

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

    fun <TDeclaration : KtDeclaration> KtPsiFactory.createAnalyzableDeclaration(@NonNls text: String, context: PsiElement): TDeclaration {
        val file = createAnalyzableFile("Dummy.kt", text, context)
        val declarations = file.declarations
        checkWithAttachment(declarations.size == 1, { "unexpected ${declarations.size} declarations" }) {
            it.withAttachment("text.kt", text)
            for (d in declarations.withIndex()) {
                it.withAttachment("declaration${d.index}.kt", d.value.text)
            }
        }
        @Suppress("UNCHECKED_CAST")
        return declarations.first() as TDeclaration
    }

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        if (editor == null) return
        if (file !is KtFile) return
        val element = element ?: return

        val psiFactory = KtPsiFactory(project)

        if (element.parent is KtBinaryExpression) {
            // 'str + msg'

            val replaced = element.replace(psiFactory.createExpression("net.mamoe.mirai.message.data.PlainText(${element.text})"))
                as? KtElement ?: return
            ShortenReferences.DEFAULT.process(replaced)
            return
        }

        val resolved = element.referenceExpression()?.resolve() ?: return
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

    fun applyImport(targetElement: KtElement) {
        val targets = targetElement.resolveMainReferenceToDescriptors()
        if (targets.isEmpty()) return

        val fqName = targets.map { it.importableFqName!! }.single()

        val file = targetElement.containingKtFile
        val helper = ImportInsertHelper.getInstance(targetElement.project)
        if (helper.importDescriptor(file, targets.first()) == ImportDescriptorResult.FAIL) return

        val qualifiedExpressions = file.collectDescendantsOfType<KtDotQualifiedExpression> { qualifiedExpression ->
            val selector = qualifiedExpression.getQualifiedElementSelector() as? KtNameReferenceExpression
            selector?.getReferencedNameAsName() == fqName.shortName() && target(qualifiedExpression)?.importableFqName == fqName
        }
        val userTypes = file.collectDescendantsOfType<KtUserType> { userType ->
            val selector = userType.getQualifiedElementSelector() as? KtNameReferenceExpression
            selector?.getReferencedNameAsName() == fqName.shortName() && target(userType)?.importableFqName == fqName
        }

        //TODO: not deep
        ShortenReferences.DEFAULT.process(qualifiedExpressions + userTypes)
    }

    private fun target(qualifiedElement: KtElement): DeclarationDescriptor? {
        val nameExpression = qualifiedElement.getQualifiedElementSelector() as? KtNameReferenceExpression ?: return null
        val receiver = nameExpression.getReceiverExpression() ?: return null
        val bindingContext = qualifiedElement.analyze(BodyResolveMode.PARTIAL)
        if (bindingContext[BindingContext.QUALIFIER, receiver] == null) return null

        val targets = nameExpression.mainReference.resolveToDescriptors(bindingContext)
        if (targets.isEmpty()) return null
        if (!targets.all { it.canBeAddedToImport() }) return null
        return targets.singleOrNull()
    }
}
