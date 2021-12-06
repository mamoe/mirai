/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import net.mamoe.mirai.console.intellij.creator.tasks.readChildText
import net.mamoe.mirai.console.intellij.creator.tasks.writeChild
import org.jetbrains.kotlin.idea.core.isAndroidModule
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.idea.util.module


class ConfigurePluginMainServiceFix(
    element: PsiElement,
    private val fqName: String,
) : KotlinCrossLanguageQuickFixAction<PsiElement>(element), KotlinUniversalQuickFix, LocalQuickFix {

    override fun getFamilyName(): String = "Mirai Console"
    override fun getText(): String = "配置插件主类服务"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        val elementFqName = fqName
        val sourceRoots = file.module?.rootManager?.sourceRoots ?: return

        val sourceRoot = sourceRoots.find { it.name.endsWith("resources") }
            ?: sourceRoots.find { it.name.endsWith("res") }
            ?: sourceRoots.find { it.name.contains("resources") }
            ?: sourceRoots.find { it.name.contains("res") }
            ?: sourceRoots.last().run {
                project.executeWriteCommand(name, groupId = null) {
                    parent.createChildDirectory(
                        this@ConfigurePluginMainServiceFix,
                        if (file.module?.isAndroidModule() == true) "res" else "resources"
                    )
                }
            }

        project.executeWriteCommand(name) {
            val filepath = "META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin"

            fun computeContent(): String {
                val origin = sourceRoot.readChildText(filepath) ?: ""
                return when {
                    origin.isBlank() -> elementFqName
                    origin.endsWith("\n") -> origin + elementFqName
                    else -> "$origin\n$elementFqName"
                }
            }

            sourceRoot.writeChild(filepath, computeContent())
            VfsUtil.markDirtyAndRefresh(true, false, false, sourceRoot.findChild(filepath))
        }
    }
}
