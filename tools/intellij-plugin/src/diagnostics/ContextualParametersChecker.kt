/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import net.mamoe.mirai.console.compiler.common.CheckerConstants
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_ID
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAME
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PERMISSION_NAMESPACE
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_PLUGIN_DESCRIPTION
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.ILLEGAL_VERSION_REQUIREMENT
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.compiler.common.resolve.CONSOLE_COMMAND_OWNER_FQ_NAME
import net.mamoe.mirai.console.compiler.common.resolve.ResolveContextKind
import net.mamoe.mirai.console.compiler.common.resolve.resolveContextKinds
import net.mamoe.mirai.console.intellij.resolve.bodyCalls
import net.mamoe.mirai.console.intellij.resolve.resolveStringConstantValues
import net.mamoe.mirai.console.intellij.util.RequirementHelper
import net.mamoe.mirai.console.intellij.util.RequirementParser
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.inspections.collections.isCalling
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import java.util.*
import kotlin.reflect.KFunction2

/**
 * Checks parameters with [ResolveContextKind]
 */
class ContextualParametersChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
    ) {
        val calls = declaration.bodyCalls(context.bindingContext) ?: return

        for ((call, _) in calls) {
            for ((parameter, resolvedArgument) in call.valueArguments) {
                for (valueArgument in resolvedArgument.arguments) {
                    checkArgument(parameter, valueArgument, context)
                }
            }
        }
    }

    private fun checkArgument(
        parameter: ValueParameterDescriptor,
        argument: ValueArgument,
        context: DeclarationCheckerContext,
    ) {
        val elementCheckers = parameter.resolveContextKinds?.mapNotNull(checkersMap::get) ?: return
        if (elementCheckers.isEmpty()) return

        val resolvedConstants = argument.resolveStringConstantValues(context.bindingContext)?.toList() ?: return

        for (elementChecker in elementCheckers) {
            if (resolvedConstants.isEmpty()) {
                elementChecker(context, argument.asElement(), argument, null)?.let { context.report(it) }
            } else {
                for (resolvedConstant in resolvedConstants) {
                    elementChecker(context, argument.asElement(), argument, resolvedConstant)?.let { context.report(it) }
                }
            }
        }
    }

    companion object {
        private val ID_REGEX: Regex = CheckerConstants.PLUGIN_ID_REGEX
        private val FORBIDDEN_ID_NAMES: Array<String> = CheckerConstants.PLUGIN_FORBIDDEN_NAMES

        private const val syntax = """类似于 "net.mamoe.mirai.example-plugin", 其中 "net.mamoe.mirai" 为 groupId, "example-plugin" 为插件名"""

        const val SEMANTIC_VERSIONING_PATTERN =
            """^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?${'$'}"""

        /**
         * https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
         */
        private val SEMANTIC_VERSIONING_REGEX = Regex(SEMANTIC_VERSIONING_PATTERN)

        fun checkPluginId(inspectionTarget: KtElement, value: String): Diagnostic? {
            if (value.isBlank()) return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "插件 Id 不能为空. \n插件 Id$syntax")
            if (value.none { it == '.' }) return ILLEGAL_PLUGIN_DESCRIPTION.on(
                inspectionTarget,
                "插件 Id '$value' 无效. 插件 Id 必须同时包含 groupId 和插件名称. $syntax"
            )

            val lowercaseId = value.toLowerCase()

            if (ID_REGEX.matchEntire(value) == null) {
                return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "插件 Id 无效. 正确的插件 Id 应该满足正则表达式 '${ID_REGEX.pattern}', \n$syntax")
            }

            FORBIDDEN_ID_NAMES.firstOrNull { it == lowercaseId }?.let { illegal ->
                return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "'$illegal' 不允许作为插件 Id. 确保插件 Id 不完全是这个名称")
            }
            return null
        }

        fun checkPluginName(inspectionTarget: KtElement, value: String): Diagnostic? {
            if (value.isBlank()) return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "插件名不能为空")
            val lowercaseName = value.toLowerCase()
            FORBIDDEN_ID_NAMES.firstOrNull { it == lowercaseName }?.let { illegal ->
                return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "'$illegal' 不允许作为插件名. 确保插件名不完全是这个名称")
            }
            return null
        }

        fun checkPluginVersion(inspectionTarget: KtElement, value: String): Diagnostic? {
            if (!SEMANTIC_VERSIONING_REGEX.matches(value)) {
                return ILLEGAL_PLUGIN_DESCRIPTION.on(inspectionTarget, "版本号无效: '$value'. \nhttps://semver.org/lang/zh-CN/")
            }
            return null
        }

        fun checkCommandName(inspectionTarget: KtElement, value: String): Diagnostic? {
            return when {
                value.isBlank() -> ILLEGAL_COMMAND_NAME.on(inspectionTarget, value, "指令名不能为空")
                value.any { it.isWhitespace() } -> ILLEGAL_COMMAND_NAME.on(inspectionTarget, value, "暂时不允许指令名中存在空格")
                value.contains(':') -> ILLEGAL_COMMAND_NAME.on(inspectionTarget, value, "指令名不允许包含 ':'")
                value.contains('.') -> ILLEGAL_COMMAND_NAME.on(inspectionTarget, value, "指令名不允许包含 '.'")
                else -> null
            }
        }

        fun checkPermissionNamespace(inspectionTarget: KtElement, value: String): Diagnostic? {
            return when {
                value.isBlank() -> ILLEGAL_PERMISSION_NAMESPACE.on(inspectionTarget, value, "权限命名空间不能为空")
                value.any { it.isWhitespace() } -> ILLEGAL_PERMISSION_NAMESPACE.on(inspectionTarget, value, "不允许权限命名空间中存在空格")
                value.contains(':') -> ILLEGAL_PERMISSION_NAMESPACE.on(inspectionTarget, value, "权限命名空间不允许包含 ':'")
                else -> null
            }
        }

        fun checkPermissionName(inspectionTarget: KtElement, value: String): Diagnostic? {
            return when {
                value.isBlank() -> ILLEGAL_PERMISSION_NAME.on(inspectionTarget, value, "权限名称不能为空")
                value.any { it.isWhitespace() } -> ILLEGAL_PERMISSION_NAME.on(inspectionTarget, value, "不允许权限名称中存在空格")
                value.contains(':') -> ILLEGAL_PERMISSION_NAME.on(inspectionTarget, value, "权限名称不允许包含 ':'")
                else -> null
            }
        }

        fun checkPermissionId(inspectionTarget: KtElement, value: String): Diagnostic? {
            return when {
                value.isBlank() -> ILLEGAL_PERMISSION_ID.on(inspectionTarget, value, "权限 Id 不能为空")
                value.any { it.isWhitespace() } -> ILLEGAL_PERMISSION_ID.on(inspectionTarget, value, "暂时不允许权限 Id 中存在空格")
                value.count { it == ':' } != 1 -> ILLEGAL_PERMISSION_ID.on(inspectionTarget, value, "权限 Id 必须为 \"命名空间:名称\". 且命名空间和名称均不能包含 ':'")
                else -> null
            }
        }

        @Suppress("UNUSED_PARAMETER")
        fun checkVersionRequirement(inspectionTarget: KtElement, value: String): Diagnostic? {
            return try {
                RequirementHelper.RequirementChecker.processLine(RequirementParser.TokenReader(value))
                null
            } catch (err: Throwable) {
                ILLEGAL_VERSION_REQUIREMENT.on(inspectionTarget, value, err.message ?: err.toString())
            }
        }

        fun checkConsoleCommandOwner(context: DeclarationCheckerContext, inspectionTarget: KtElement, argument: ValueArgument): Diagnostic? {
            val expr = argument.getArgumentExpression() ?: return null

            if (expr is KtReferenceExpression) {
                if (expr.getResolvedCall(context)?.isCalling(CONSOLE_COMMAND_OWNER_FQ_NAME) == true) {
                    return RESTRICTED_CONSOLE_COMMAND_OWNER.on(inspectionTarget)
                }
            }

            return null
        }
    }

    fun interface ElementChecker {
        operator fun invoke(context: DeclarationCheckerContext, declaration: KtElement, valueArgument: ValueArgument, value: String?): Diagnostic?
    }

    @Suppress("unused")
    private val checkersMap: EnumMap<ResolveContextKind, ElementChecker> =
        EnumMap<ResolveContextKind, ElementChecker>(ResolveContextKind::class.java).apply {

            fun put(key: ResolveContextKind, value: KFunction2<KtElement, String, Diagnostic?>): ElementChecker? {
                return put(key) { _, d, _, v ->
                    if (v != null) value(d, v)
                    else null
                }
            }

            fun put(key: ResolveContextKind, value: KFunction2<KtElement, ValueArgument, Diagnostic?>): ElementChecker? {
                return put(key) { _, d, v, _ ->
                    value(d, v)
                }
            }

            fun put(key: ResolveContextKind, value: (DeclarationCheckerContext, KtElement, ValueArgument) -> Diagnostic?): ElementChecker? {
                return put(key) { c, d, v, _ ->
                    value(c, d, v)
                }
            }

            put(ResolveContextKind.PLUGIN_NAME, ::checkPluginName)
            put(ResolveContextKind.PLUGIN_ID, ::checkPluginId)
            put(ResolveContextKind.SEMANTIC_VERSION, ::checkPluginVersion)
            put(ResolveContextKind.COMMAND_NAME, ::checkCommandName)
            put(ResolveContextKind.PERMISSION_NAME, ::checkPermissionName)
            put(ResolveContextKind.PERMISSION_NAMESPACE, ::checkPermissionNamespace)
            put(ResolveContextKind.PERMISSION_ID, ::checkPermissionId)
            put(ResolveContextKind.VERSION_REQUIREMENT, ::checkVersionRequirement)
            put(ResolveContextKind.RESTRICTED_CONSOLE_COMMAND_OWNER, ::checkConsoleCommandOwner)
        }

}