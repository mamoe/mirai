/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.line.marker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.compiler.common.resolve.PLUGIN_FQ_NAME
import net.mamoe.mirai.console.intellij.assets.Icons
import net.mamoe.mirai.console.intellij.resolve.allSuperNames
import net.mamoe.mirai.console.intellij.resolve.getElementForLineMark
import net.mamoe.mirai.console.intellij.util.runIgnoringErrors

class PluginMainLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        runIgnoringErrors { // not showing icons is better than throwing exception every time doing inspection
            if (element.allSuperNames.any { it == PLUGIN_FQ_NAME }) return Info(getElementForLineMark(element))
        }

        return null
    }


    @Suppress("DEPRECATION")
    class Info(callElement: PsiElement) : LineMarkerInfo<PsiElement>(
        // this constructor is deprecated but is not going to be removed. The newer ones are since 203 which is too high.

        callElement,
        callElement.textRange,
        Icons.PluginMainDeclaration,
        tooltipProvider,
        null, GutterIconRenderer.Alignment.RIGHT
    ) {
        companion object {
            val tooltipProvider = { _: PsiElement ->
                "Mirai Console Plugin"
            }
        }
    }
}