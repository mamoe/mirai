/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.line.marker

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.intellij.Icons
import net.mamoe.mirai.console.intellij.diagnostics.resolveMiraiPluginDeclaration
import net.mamoe.mirai.console.intellij.resolve.getElementForLineMark
import org.jetbrains.kotlin.psi.KtReferenceExpression

class PluginMainLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is KtReferenceExpression) return null
        val main = element.resolveMiraiPluginDeclaration() ?: return null
        return Info(getElementForLineMark(main))
    }

    @Suppress("DEPRECATION")
    class Info(
        callElement: PsiElement,
    ) : LineMarkerInfo<PsiElement>(
        callElement,
        callElement.textRange,
        Icons.PluginMainDeclaration,
        Pass.LINE_MARKERS,
        {
            "Mirai Console Plugin"
        },
        null,
        GutterIconRenderer.Alignment.CENTER
    ) {
        override fun createGutterRenderer(): GutterIconRenderer {
            return object : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(this) {
                override fun getClickAction(): AnAction? = null
            }
        }
    }
}