/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import net.mamoe.mirai.console.intellij.resolve.*
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.search.getKotlinFqName
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.idea.util.ImportInsertHelper
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import kotlin.contracts.contract

/*
private val bundle by lazy {
    BundleUtil.loadLanguageBundle(PluginMainServiceNotConfiguredInspection::class.java.classLoader, "messages.InspectionGadgetsBundle")!!
}*/


/**
 * @since 2.4
 */
class ResourceNotClosedInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitCallExpression(callExpression: KtCallExpression) {
                for (processor in ResourceNotClosedInspectionProcessors.processors) {
                    processor.visitKtExpr(holder, isOnTheFly, callExpression)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (element is PsiCallExpression) {
                    for (processor in ResourceNotClosedInspectionProcessors.processors) {
                        processor.visitPsiExpr(holder, isOnTheFly, element)
                    }
                }
            }
        }
    }
}

val CONTACT_FQ_NAME = FqName("net.mamoe.mirai.contact.Contact")
val CONTACT_COMPANION_FQ_NAME = FqName("net.mamoe.mirai.contact.Contact.Companion")

fun KtReferenceExpression.resolveCalleeFunction(): KtNamedFunction? {
    val originalCallee = getCalleeExpressionIfAny()?.referenceExpression()?.resolve() ?: return null
    if (originalCallee !is KtNamedFunction) return null

    return originalCallee
}

fun KtNamedFunction.isNamedMemberFunctionOf(
    className: String,
    functionName: String,
    extensionReceiver: String? = null
): Boolean {
    if (extensionReceiver != null) {
        if (this.receiverTypeReference?.resolveReferencedType()?.getKotlinFqName()
                ?.toString() != extensionReceiver
        ) return false
    }
    return this.name == functionName && this.containingClassOrObject?.allSuperTypes?.any {
        it.getKotlinFqName()?.toString() == className
    } == true
}

@Suppress("DialogTitleCapitalization")
object ResourceNotClosedInspectionProcessors {
    val processors = arrayOf(
        FirstArgumentProcessor,
        KtExtensionProcessor
    )

    interface Processor {
        fun visitKtExpr(holder: ProblemsHolder, isOnTheFly: Boolean, callExpr: KtCallExpression)
        fun visitPsiExpr(holder: ProblemsHolder, isOnTheFly: Boolean, expr: PsiCallExpression)
    }

    object KtExtensionProcessor : Processor {
        // net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImage(net.mamoe.mirai.utils.ExternalResource, C, kotlin.coroutines.Continuation<? super net.mamoe.mirai.message.MessageReceipt<? extends C>>)
        val SEND_AS_IMAGE_TO = FunctionSignature {
            name("sendAsImageTo")
            dispatchReceiver("net.mamoe.mirai.utils.ExternalResource.Companion")
            extensionReceiver("net.mamoe.mirai.utils.ExternalResource")
        }
        val UPLOAD_AS_IMAGE = FunctionSignature {
            name("uploadAsImage")
            dispatchReceiver("net.mamoe.mirai.utils.ExternalResource.Companion")
            extensionReceiver("net.mamoe.mirai.utils.ExternalResource")
        }

        override fun visitKtExpr(holder: ProblemsHolder, isOnTheFly: Boolean, callExpr: KtCallExpression) {
            val parent = callExpr.parent
            if (parent !is KtDotQualifiedExpression) return
            val callee = callExpr.resolveCalleeFunction() ?: return

            if (!parent.receiverExpression.isCallingExternalResourceCreators()) return

            class Fix(private val functionName: String) :
                KotlinCrossLanguageQuickFixAction<KtDotQualifiedExpression>(parent), KotlinUniversalQuickFix {
                override fun getFamilyName(): String = FAMILY_NAME
                override fun getText(): String = "修复 $functionName"

                override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
                    if (editor == null) return
                    val uploadImageExpression = element ?: return
                    val toExternalExpression = uploadImageExpression.receiverExpression

                    val toExternalReceiverExpression = toExternalExpression.dotReceiverExpression() ?: return

                    toExternalExpression.replace(toExternalReceiverExpression)
                }
            }

            when {
                callee.hasSignature(SEND_AS_IMAGE_TO) -> {
                    // RECEIVER.sendAsImageTo
                    holder.registerResourceNotClosedProblem(
                        parent.receiverExpression,
                        Fix("sendAsImageTo"),
                    )
                }
                callee.hasSignature(UPLOAD_AS_IMAGE) -> {
                    holder.registerResourceNotClosedProblem(
                        parent.receiverExpression,
                        Fix("uploadAsImage"),
                    )
                }
            }
        }

        override fun visitPsiExpr(holder: ProblemsHolder, isOnTheFly: Boolean, expr: PsiCallExpression) {
        }

    }

    object FirstArgumentProcessor : Processor {
        val CONTACT_UPLOAD_IMAGE = FunctionSignature {
            name("uploadImage")
            dispatchReceiver(CONTACT_FQ_NAME)
            parameters("net.mamoe.mirai.utils.ExternalResource")
        }
        val CONTACT_UPLOAD_IMAGE_STATIC = FunctionSignature {
            name("uploadImage")
            extensionReceiver(CONTACT_FQ_NAME)
            dispatchReceiver(CONTACT_COMPANION_FQ_NAME)
            parameters("net.mamoe.mirai.utils.ExternalResource")
        }
        val CONTACT_COMPANION_UPLOAD_IMAGE = FunctionSignature {
            name("uploadImage")
            extensionReceiver(CONTACT_FQ_NAME)
            parameters("net.mamoe.mirai.utils.ExternalResource")
        }

        val CONTACT_COMPANION_SEND_IMAGE = FunctionSignature {
            name("sendImage")
            extensionReceiver(CONTACT_FQ_NAME)
            parameters("net.mamoe.mirai.utils.ExternalResource")
        }

        private val signatures = arrayOf(
            CONTACT_UPLOAD_IMAGE,
            CONTACT_COMPANION_UPLOAD_IMAGE,
            CONTACT_COMPANION_SEND_IMAGE
        )

        override fun visitKtExpr(holder: ProblemsHolder, isOnTheFly: Boolean, callExpr: KtCallExpression) {
            val callee = callExpr.resolveCalleeFunction() ?: return
            if (signatures.none { callee.hasSignature(it) }) return

            val firstArgument = callExpr.valueArguments.firstOrNull() ?: return
            val firstArgumentExpr = firstArgument.getArgumentExpression()
            if (firstArgumentExpr?.isCallingExternalResourceCreators() != true) return

            holder.registerResourceNotClosedProblem(
                firstArgument,
                LocalQuickFix("修复", firstArgumentExpr) {
                    fun tryAddImport() {
                        if (file !is KtFile) return
                        val companion = callee.descriptor?.containingDeclaration?.companionObjectDescriptor() ?: return
                        val toImport = companion.findMemberFunction(callee.nameAsName ?: return) ?: return

                        // net.mamoe.mirai.contact.Contact.Companion
                        ImportInsertHelper.getInstance(project).importDescriptor(file, toImport)
                    }

                    val newArgumentText = element.dotReceiverExpression()?.text ?: return@LocalQuickFix
                    callExpr.replace(KtPsiFactory(project).createExpression(buildString {
                        append(callee.name)
                        append('(')
                        append(newArgumentText)
                        append(')')
                    }))
                    tryAddImport()
                }
            )
        }

        override fun visitPsiExpr(holder: ProblemsHolder, isOnTheFly: Boolean, expr: PsiCallExpression) {
            if (expr !is PsiMethodCallExpression) return
            val callee = expr.resolveMethod() ?: return

            val arguments = expr.argumentList.expressions
            when {
                callee.hasSignature(CONTACT_UPLOAD_IMAGE) -> {
                    createFixImpl(
                        expr = expr,
                        holder = holder,
                        argument = arguments.firstOrNull() ?: return,
                        fileTypeArgument = arguments.getOrNull(1)
                    ) { it.methodExpression.qualifierExpression?.text ?: "this" }
                }
                callee.hasSignature(CONTACT_UPLOAD_IMAGE_STATIC) -> {
                    createFixImpl(
                        expr = expr,
                        holder = holder,
                        argument = arguments.getOrNull(1) ?: return,
                        fileTypeArgument = arguments.getOrNull(2)
                    ) { arguments.getOrNull(0)?.text ?: "this" }
                }
            }
        }

        private fun createFixImpl(
            expr: PsiMethodCallExpression,
            holder: ProblemsHolder,
            argument: PsiExpression,
            fileTypeArgument: PsiExpression?,
            replaceForFirstArgument: (expr: PsiMethodCallExpression) -> String,
        ) {
            if (!argument.isCallingExternalResourceCreators()) return

            holder.registerResourceNotClosedProblem(
                argument,
                LocalQuickFix("修复", argument) {
                    /*
                            useImage(Contact.uploadImage(contact, ExternalResource.create(file))); // before
                            useImage(Contact.uploadImage(contact, file)); // after
                             */
                    val factory = project.psiElementFactory ?: return@LocalQuickFix
                    val reference = factory.createExpressionFromText(
                        if (fileTypeArgument == null) {
                            "$CONTACT_FQ_NAME.uploadImage(${replaceForFirstArgument(expr)}, ${argument.argumentList?.expressions?.firstOrNull()?.text ?: ""})"
                        } else {
                            "$CONTACT_FQ_NAME.uploadImage(${replaceForFirstArgument(expr)}, ${argument.argumentList?.expressions?.firstOrNull()?.text ?: ""}, ${fileTypeArgument.text})"
                        },
                        expr.context
                    )
                    expr.replace(reference)
                }
            )
        }
    }

    private fun ProblemsHolder.registerResourceNotClosedProblem(target: PsiElement, vararg fixes: LocalQuickFix) {
        registerProblem(
            target,
            @Suppress("DialogTitleCapitalization") "资源未关闭",
            ProblemHighlightType.WARNING,
            *fixes
        )
    }
}

private val EXTERNAL_RESOURCE_CREATE = FunctionSignature {
    name("create")
    dispatchReceiver("net.mamoe.mirai.utils.ExternalResource.Companion")
}
private val TO_EXTERNAL_RESOURCE = FunctionSignature {
    name("toExternalResource")
    dispatchReceiver("net.mamoe.mirai.utils.ExternalResource.Companion")
}

fun KtExpression.isCallingExternalResourceCreators(): Boolean {
    val callExpr = resolveToCall(BodyResolveMode.PARTIAL)?.resultingDescriptor ?: return false
    return callExpr.hasSignature(EXTERNAL_RESOURCE_CREATE) || callExpr.hasSignature(TO_EXTERNAL_RESOURCE)
}

fun PsiExpression.isCallingExternalResourceCreators(): Boolean {
    contract { returns() implies (this@isCallingExternalResourceCreators is PsiCallExpression) }
    if (this !is PsiCallExpression) return false
    val callee = resolveMethod() ?: return false
    return callee.hasSignature(EXTERNAL_RESOURCE_CREATE)
}

private const val FAMILY_NAME = "Mirai console"