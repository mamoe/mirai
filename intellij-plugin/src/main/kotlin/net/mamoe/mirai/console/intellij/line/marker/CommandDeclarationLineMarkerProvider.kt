package net.mamoe.mirai.console.intellij.line.marker

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.intellij.Icons
import net.mamoe.mirai.console.intellij.resolve.isSimpleCommandHandlerOrCompositeCommandSubCommand
import org.jetbrains.kotlin.psi.KtNamedFunction

class CommandDeclarationLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is KtNamedFunction) return null
        if (!element.isSimpleCommandHandlerOrCompositeCommandSubCommand()) return null
        return Info(getElementForLineMark(element.funKeyword ?: element.identifyingElement ?: element))
    }

    @Suppress("DEPRECATION")
    class Info(
        callElement: PsiElement,
    ) : LineMarkerInfo<PsiElement>(
        callElement,
        callElement.textRange,
        Icons.CommandDeclaration,
        Pass.LINE_MARKERS,
        {
            "子指令定义"
        },
        null,
        GutterIconRenderer.Alignment.RIGHT
    ) {
        override fun createGutterRenderer(): GutterIconRenderer? {
            return object : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(this) {
                override fun getClickAction(): AnAction? = null
            }
        }
    }
}