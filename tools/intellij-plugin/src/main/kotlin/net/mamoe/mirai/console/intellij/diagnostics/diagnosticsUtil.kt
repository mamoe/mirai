/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.intellij.resolve.getResolvedCallOrResolveToCall
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

fun DeclarationCheckerContext.report(diagnostic: Diagnostic) {
    return this.trace.report(diagnostic)
}

val DeclarationCheckerContext.bindingContext get() = this.trace.bindingContext

fun KtElement?.getResolvedCallOrResolveToCall(
    context: DeclarationCheckerContext,
): ResolvedCall<out CallableDescriptor>? {
    return this.getResolvedCallOrResolveToCall(context.bindingContext)
}