package net.mamoe.mirai.console.intellij

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiReferenceExpression
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.idea.core.util.getLineNumber
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

class MiraiConsoleLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        val markedLineNumbers = HashSet<Int>()

        for (element in elements) {
            ProgressManager.checkCanceled()

            if (element !is PsiReferenceExpression) continue

            val containingFile = element.containingFile
            if (containingFile !is PsiJavaFile || containingFile is PsiJavaCodeReferenceCodeFragment) {
                continue
            }

            val lineNumber = element.getLineNumber()
            if (lineNumber in markedLineNumbers) continue
            if (!element.hasBridgeCalls()) continue


            markedLineNumbers += lineNumber
            result += if (element is KtForExpression) {
                CommandDeclarationLineMarkerInfo(
                    getElementForLineMark(element.loopRange!!),
                    // KotlinBundle.message("highlighter.message.suspending.iteration")
                )
            } else {
                CommandDeclarationLineMarkerInfo(
                    getElementForLineMark(element),
                    //KotlinBundle.message("highlighter.message.suspend.function.call")
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    class CommandDeclarationLineMarkerInfo(
        callElement: PsiElement,
    ) : LineMarkerInfo<PsiElement>(
        callElement,
        callElement.textRange,
        Icons.CommandDeclaration,
        Pass.LINE_MARKERS,
        {
            "Mirai Console Command"
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

fun PsiReferenceExpression.hasBridgeCalls(): Boolean {
    val resolved = this.resolve() as? KtLightMethod ?: return false

    TODO()
}

internal fun getElementForLineMark(callElement: PsiElement): PsiElement =
    when (callElement) {
        is KtSimpleNameExpression -> callElement.getReferencedNameElement()
        else ->
            // a fallback,
            //but who knows what to reference in KtArrayAccessExpression ?
            generateSequence(callElement, { it.firstChild }).last()
    }