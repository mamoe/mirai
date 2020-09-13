package net.mamoe.mirai.console.intellij.line.marker

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment
import net.mamoe.mirai.console.intellij.Icons
import net.mamoe.mirai.console.intellij.Plugin_FQ_NAME
import org.jetbrains.kotlin.idea.core.util.getLineNumber
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtReferenceExpression

class PluginMainLineMarkerProvider : LineMarkerProvider {
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


            val containingFile = element.containingFile
            if (containingFile is PsiJavaCodeReferenceCodeFragment) {
                continue
            }




            when {
                element is KtReferenceExpression // constructor call
                -> {
                    val objectDeclaration =
                        element.parents.filterIsInstance<KtObjectDeclaration>().firstOrNull() ?: continue

                    val resolved = element.resolve() as? KtConstructor<*> ?: continue

                    val kotlinPluginClass = resolved.parent as? KtClass ?: continue

                    if (kotlinPluginClass.allSuperNames.none { it == Plugin_FQ_NAME }) continue

                    val lineNumber = objectDeclaration.getLineNumber()
                    if (lineNumber in markedLineNumbers) continue

                    markedLineNumbers += lineNumber
                    result += Info(getElementForLineMark(objectDeclaration))
                }
            }

            // if (!element.hasBridgeCalls()) continue

        }
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
        GutterIconRenderer.Alignment.RIGHT
    ) {
        override fun createGutterRenderer(): GutterIconRenderer? {
            return object : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(this) {
                override fun getClickAction(): AnAction? = null
            }
        }
    }
}