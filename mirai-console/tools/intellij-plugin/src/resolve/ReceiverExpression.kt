/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.resolve

import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.util.getFactoryForImplicitReceiverWithSubtypeOf
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory

sealed class ReceiverExpression {
    abstract val receiverExpression: KtExpression
    abstract val receiverText: String

    operator fun component1(): KtExpression = receiverExpression
    operator fun component2(): String = receiverText

    class Explicit(
        override val receiverExpression: KtExpression
    ) : ReceiverExpression() {
        override val receiverText: String
            get() = receiverExpression.text
    }

    class Implicit(
        receiverExpression: Lazy<KtExpression>,
        override val receiverText: String,
    ) : ReceiverExpression() {
        override val receiverExpression: KtExpression by receiverExpression
    }
}

fun KtCallExpression.siblingDotReceiverExpression(): KtExpression? {
    val dotQualifiedExpression = parent
    if (dotQualifiedExpression is KtDotQualifiedExpression) {
        return dotQualifiedExpression.receiverExpression
    }
    return null
}

fun KtExpression.dotReceiverExpression(): KtExpression? {
    return if (this is KtDotQualifiedExpression) receiverExpression else null
}

/**
 * Find:
 * - explicit receiver: `a` for `a.foo()`
 * - implicit labeled receiver: `this@run` in `a.run { foo() }`
 *
 * @receiver identifier reference in a call. e.g. `foo` in `a.foo()` and `foo()`
 */
fun KtExpression.receiverExpression(psiFactory: KtPsiFactory): ReceiverExpression? {
    val dotQualifiedExpr = parent
    if (dotQualifiedExpr is KtDotQualifiedExpression) {
        return ReceiverExpression.Explicit(dotQualifiedExpr.receiverExpression)
    } else {
        val context = analyze()
        val scope = getResolutionScope(context) ?: return null

        val descriptor = getResolvedCall(context)?.resultingDescriptor ?: return null
        val receiverDescriptor = descriptor.extensionReceiverParameter
            ?: descriptor.dispatchReceiverParameter
            ?: return null

        val expressionFactory = scope.getFactoryForImplicitReceiverWithSubtypeOf(receiverDescriptor.type) ?: return null
        val receiverText = if (expressionFactory.isImmediate) "this" else expressionFactory.expressionText
        return ReceiverExpression.Implicit(lazy { expressionFactory.createExpression(psiFactory, true) }, receiverText)
    }
}

fun KtExpression.implicitExpressionText(): String? {
    val dotQualifiedExpr = parent
    if (dotQualifiedExpr is KtDotQualifiedExpression) {
        return null
    } else {
        val context = analyze()
        val scope = getResolutionScope(context) ?: return null

        val descriptor = getResolvedCall(context)?.resultingDescriptor ?: return null
        val receiverDescriptor = descriptor.extensionReceiverParameter
            ?: descriptor.dispatchReceiverParameter
            ?: return null

        val expressionFactory = scope.getFactoryForImplicitReceiverWithSubtypeOf(receiverDescriptor.type) ?: return null
        return if (expressionFactory.isImmediate) "this" else expressionFactory.expressionText
    }
}