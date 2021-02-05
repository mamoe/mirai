/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics.fix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.projectRoots.JavaVersionService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression
import net.mamoe.mirai.console.intellij.diagnostics.ResourceNotClosedInspectionProcessors.KtExtensionProcessor.SEND_AS_IMAGE_TO
import net.mamoe.mirai.console.intellij.diagnostics.ResourceNotClosedInspectionProcessors.KtExtensionProcessor.UPLOAD_AS_IMAGE
import net.mamoe.mirai.console.intellij.diagnostics.replaceExpressionAndShortenReferences
import net.mamoe.mirai.console.intellij.diagnostics.resolveCalleeFunction
import net.mamoe.mirai.console.intellij.resolve.hasSignature
import org.jetbrains.kotlin.idea.intentions.SelfTargetingIntention
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * @since 2.4
 */
class WrapWithResourceUseCallIntention : SelfTargetingIntention<KtDotQualifiedExpression>(KtDotQualifiedExpression::class.java, { "转换为 .use" }) {
    override fun applyTo(element: KtDotQualifiedExpression, editor: Editor?) {
        val selectorExpression = element.selectorExpression ?: return
        selectorExpression.replaceExpressionAndShortenReferences("use { it.${selectorExpression.text} }")
    }

    override fun isApplicableTo(element: KtDotQualifiedExpression, caretOffset: Int): Boolean {
        val callee = element.selectorExpression?.referenceExpression()?.resolveCalleeFunction() ?: return false
        if (!callee.hasSignature(UPLOAD_AS_IMAGE) && !callee.hasSignature(SEND_AS_IMAGE_TO)) return false
        val receiver = element.receiverExpression
        return receiver is KtSimpleNameExpression
    }
}

// https://github.com/mamoe/mirai-console/issues/284
/**
 *
 * to be supported by 2.5
 * @since 2.4
 */
class WrapWithResourceUseCallJavaIntention : SelfTargetingIntention<PsiMethodCallExpression>(PsiMethodCallExpression::class.java, { "转换为 .use" }) {
    override fun applyTo(element: PsiMethodCallExpression, editor: Editor?) {
        // val selectorExpression = element.methodExpression

    }

    override fun isApplicableTo(element: PsiMethodCallExpression, caretOffset: Int): Boolean {
        return false
        // if (!element.isJavaVersionAtLeast(JavaSdkVersion.JDK_1_9)) return false

    }
}

fun PsiElement.isJavaVersionAtLeast(version: JavaSdkVersion): Boolean {
    return this.module?.getService(JavaVersionService::class.java)?.isAtLeast(this, JavaSdkVersion.JDK_1_9) == true
}