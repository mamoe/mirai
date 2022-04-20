/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
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


    class Info(callElement: PsiElement) : LineMarkerInfo<PsiElement>(
        callElement,
        callElement.textRange,
        Icons.PluginMainDeclaration,
        { "Mirai Console Plugin" },
        null, GutterIconRenderer.Alignment.RIGHT,
        { "Mirai Console Plugin" }
    ) {
    }
}