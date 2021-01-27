package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_DECLARATION_RECEIVER
import net.mamoe.mirai.console.compiler.common.resolve.COMMAND_SENDER_FQ_NAME
import net.mamoe.mirai.console.intellij.resolve.hasSuperType
import net.mamoe.mirai.console.intellij.resolve.isCompositeCommandSubCommand
import net.mamoe.mirai.console.intellij.resolve.isSimpleCommandHandler
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

class CommandDeclarationChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtNamedFunction) return

        // exclusive checks
        when {
            declaration.isSimpleCommandHandler() -> {
            }

            declaration.isCompositeCommandSubCommand() -> {
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