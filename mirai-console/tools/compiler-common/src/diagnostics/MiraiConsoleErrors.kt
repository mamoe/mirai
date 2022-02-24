/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.console.compiler.common.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0.create
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1.create
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory2.create
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.diagnostics.Severity.WARNING
import org.jetbrains.kotlin.psi.*

/**
 * 如何增加一个错误:
 * 1. 在 [MiraiConsoleErrors] 添加
 * 2. 在 [MiraiConsoleErrorsRendering] 添加对应的 render
 */
object MiraiConsoleErrors {
    // plugin desc

    @JvmField
    val ILLEGAL_PLUGIN_DESCRIPTION = create<PsiElement, String>(ERROR)

    @JvmField
    val ILLEGAL_VERSION_REQUIREMENT = create<PsiElement, String, String>(ERROR)

    // plugin data

    @JvmField
    val NOT_CONSTRUCTABLE_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val USING_DERIVED_MUTABLE_MAP_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val USING_DERIVED_MAP_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val USING_DERIVED_LIST_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val USING_DERIVED_MUTABLE_LIST_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val USING_DERIVED_CONCURRENT_MAP_TYPE = create<KtTypeProjection, KtCallExpression, String>(ERROR)

    @JvmField
    val UNSERIALIZABLE_TYPE = create<PsiElement, ClassDescriptor>(ERROR)

    @JvmField
    val READ_ONLY_VALUE_CANNOT_BE_VAR = create<PsiElement>(ERROR)

    // command

    @JvmField
    val ILLEGAL_COMMAND_NAME = create<PsiElement, String, String>(ERROR)

    @JvmField
    val ILLEGAL_COMMAND_REGISTER_USE = create<PsiElement, KtNamedDeclaration, String>(ERROR)

    @JvmField
    val RESTRICTED_CONSOLE_COMMAND_OWNER = create<PsiElement>(WARNING)

    @JvmField
    val PROHIBITED_ABSTRACT_MESSAGE_KEYS = create<PsiElement>(WARNING)

    @JvmField
    val ILLEGAL_COMMAND_DECLARATION_RECEIVER = create<KtTypeReference>(ERROR)

    // permission

    @JvmField
    val ILLEGAL_PERMISSION_NAME = create<PsiElement, String, String>(ERROR)

    @JvmField
    val ILLEGAL_PERMISSION_ID = create<PsiElement, String, String>(ERROR)

    @JvmField
    val ILLEGAL_PERMISSION_NAMESPACE = create<PsiElement, String, String>(ERROR)

    @JvmField
    val ILLEGAL_PERMISSION_REGISTER_USE = create<PsiElement, KtNamedDeclaration, String>(ERROR)

//    @JvmField
//    val INAPPLICABLE_COMMAND_ANNOTATION = create<PsiElement, String>(ERROR)

    @Suppress("ObjectPropertyName", "unused")
    @JvmField
    @Deprecated("", level = DeprecationLevel.ERROR)
    val _init: Any = run {
        Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(
            MiraiConsoleErrors::class.java,
            MiraiConsoleErrorsRendering
        )
    }
}