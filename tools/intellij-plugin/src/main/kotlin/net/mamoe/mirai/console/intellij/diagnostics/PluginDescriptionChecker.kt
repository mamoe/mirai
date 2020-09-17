/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.compiler.common.resolve.ResolveContextKind
import net.mamoe.mirai.console.compiler.common.resolve.resolveContextKind
import net.mamoe.mirai.console.intellij.resolve.findChildren
import net.mamoe.mirai.console.intellij.resolve.resolveStringConstantValue
import net.mamoe.mirai.console.intellij.resolve.valueParameters
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import java.util.*
import kotlin.contracts.contract

/**
 * Checks:
 * - plugin id
 * - plugin name
 */
class PluginDescriptionChecker : DeclarationChecker {
    companion object {
        private val ID_REGEX: Regex = Regex("""([a-zA-Z]+(?:\.[a-zA-Z0-9]+)*)\.([a-zA-Z]+(?:-[a-zA-Z0-9]+)*)""")
        private val FORBIDDEN_ID_NAMES: Array<String> = arrayOf("main", "console", "plugin", "config", "data")

        fun checkPluginId(inspectionTarget: PsiElement, value: String): Diagnostic? {
            if (value.isBlank()) return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "Plugin id cannot be blank")
            if (value.none { it == '.' }) return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget,
                "'$value' is illegal. Plugin id must consist of both domain and name. ")

            val lowercaseId = value.toLowerCase()

            if (ID_REGEX.matchEntire(value) == null) {
                return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "Plugin does not match regex '${ID_REGEX.pattern}'.")
            }

            FORBIDDEN_ID_NAMES.firstOrNull { it == lowercaseId }?.let { illegal ->
                return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "Plugin id contains illegal word: '$illegal'.")
            }
            return null
        }

        fun checkPluginName(inspectionTarget: PsiElement, value: String): Diagnostic? {
            if (value.isBlank()) return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "Plugin name cannot be blank")
            val lowercaseName = value.toLowerCase()
            FORBIDDEN_ID_NAMES.firstOrNull { it == lowercaseName }?.let { illegal ->
                return MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "Plugin name is illegal: '$illegal'.")
            }
            return null
        }

        fun checkPluginVersion(inspectionTarget: PsiElement, value: String): Diagnostic? {
            return null // TODO: 2020/9/18  checkPluginVersion
        }
    }

    fun PsiElement.shouldPerformCheck(): Boolean {
        contract {
            returns(true) implies (this@shouldPerformCheck is KtCallExpression)
        }
        return when (this) {
            is KtCallExpression,
            -> true
            else -> true
        }
    }

    private val checkersMap: EnumMap<ResolveContextKind, (declaration: PsiElement, value: String) -> Diagnostic?> =
        EnumMap<ResolveContextKind, (declaration: PsiElement, value: String) -> Diagnostic?>(ResolveContextKind::class.java).apply {
            put(ResolveContextKind.PLUGIN_NAME, ::checkPluginName)
            put(ResolveContextKind.PLUGIN_ID, ::checkPluginId)
            put(ResolveContextKind.PLUGIN_VERSION, ::checkPluginVersion)
        }

    fun check(
        expression: KtCallExpression,
        context: DeclarationCheckerContext,
    ) {
        val call = expression.calleeExpression.getResolvedCallOrResolveToCall(context) ?: return // unresolved
        call.valueArgumentsByIndex?.forEach { resolvedValueArgument ->
            for ((parameter, argument) in call.valueParameters.zip(resolvedValueArgument.arguments)) {
                val parameterContextKind = parameter.resolveContextKind
                if (checkersMap.containsKey(parameterContextKind)) {
                    val value = argument.getArgumentExpression()
                        ?.resolveStringConstantValue(context.bindingContext) ?: continue
                    for ((kind, fn) in checkersMap) {
                        if (parameterContextKind == kind) fn(argument.asElement(), value)?.let { context.report(it) }
                    }
                }
            }
        }
    }

    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
    ) {
        println("${declaration::class.qualifiedName}   $declaration")
        when (declaration) {
            is KtObjectDeclaration -> {
                // check super type constructor
                val superTypeCallEntry = declaration.findChildren<KtSuperTypeList>()?.findChildren<KtSuperTypeCallEntry>() ?: return
                // val constructorCall = superTypeCallEntry.findChildren<KtConstructorCalleeExpression>()?.resolveToCall() ?: return
                val valueArgumentList = superTypeCallEntry.findChildren<KtValueArgumentList>() ?: return
                valueArgumentList.arguments.asSequence().mapNotNull(KtValueArgument::getArgumentExpression).forEach {
                    if (it.shouldPerformCheck()) {
                        check(it as KtCallExpression, context)
                    }
                }

            }
            is KtClassOrObject -> {
                // check constructor

                val superTypeCallEntry = declaration.findChildren<KtSuperTypeList>()?.findChildren<KtSuperTypeCallEntry>() ?: return

                val constructorCall = superTypeCallEntry.findChildren<KtConstructorCalleeExpression>()?.resolveToCall() ?: return
                val valueArgumentList = superTypeCallEntry.findChildren<KtValueArgumentList>() ?: return


            }
            else -> {
                declaration.children.filter { it.shouldPerformCheck() }.forEach { element ->
                    if (element is KtDeclaration) {
                        val desc = element.descriptor ?: return@forEach
                        check(element, desc, context)
                    }
                }
            }
        }
    }
}