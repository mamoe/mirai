/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.console.compiler.common.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1.create
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory2.create
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.psi.KtNamedDeclaration

object MiraiConsoleErrors {
    @JvmStatic
    val ILLEGAL_PLUGIN_DESCRIPTION = create<PsiElement, String>(ERROR)

    @JvmStatic
    val NOT_CONSTRUCTABLE_TYPE = create<PsiElement, String>(ERROR)

    @JvmStatic
    val UNSERIALIZABLE_TYPE = create<PsiElement, ClassDescriptor>(ERROR)

    @JvmStatic
    val ILLEGAL_COMMAND_NAME = create<PsiElement, String, String>(ERROR)

    @JvmStatic
    val ILLEGAL_PERMISSION_NAME = create<PsiElement, String, String>(ERROR)

    @JvmStatic
    val ILLEGAL_PERMISSION_ID = create<PsiElement, String, String>(ERROR)

    @JvmStatic
    val ILLEGAL_PERMISSION_NAMESPACE = create<PsiElement, String, String>(ERROR)

    @JvmStatic
    val ILLEGAL_COMMAND_REGISTER_USE = create<PsiElement, KtNamedDeclaration, String>(ERROR)

    @JvmStatic
    val ILLEGAL_PERMISSION_REGISTER_USE = create<PsiElement, KtNamedDeclaration, String>(ERROR)

    @Suppress("ObjectPropertyName", "unused")
    @JvmStatic
    @Deprecated("", level = DeprecationLevel.ERROR)
    val _init: Any = object : Any() {
        init {
            Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(
                MiraiConsoleErrors::class.java,
                MiraiConsoleErrorsRendering
            )
        }
    }
}