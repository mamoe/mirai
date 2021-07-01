/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_DECLARATION_RECEIVER
import net.mamoe.mirai.console.compiler.common.resolve.COMMAND_SENDER_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.hasAnnotation
import net.mamoe.mirai.console.intellij.resolve.hasSuperType
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

class CommandDeclarationChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        if (declaration !is KtNamedFunction) return

        // exclusive checks or return
        when {
            descriptor.hasAnnotation(SIMPLE_COMMAND_HANDLER_COMMAND_FQ_NAME) -> {
            }
            descriptor.hasAnnotation(COMPOSITE_COMMAND_SUB_COMMAND_FQ_NAME) -> {
            }
            else -> return
        }

        // common checks
        checkCommandReceiverParameter(declaration)?.let { context.report(it) }
    }

    companion object {
        fun checkCommandReceiverParameter(declaration: KtNamedFunction): Diagnostic? {
            val receiverTypeRef = declaration.receiverTypeReference ?: return null // no receiver, accept.
            val receiver = receiverTypeRef.resolveReferencedType() ?: return null // unresolved type
            if (!receiver.hasSuperType(COMMAND_SENDER_FQ_NAME)) {
                return ILLEGAL_COMMAND_DECLARATION_RECEIVER.on(receiverTypeRef)
            }

            return null
        }
    }
}