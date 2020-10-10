package net.mamoe.mirai.console.intellij.diagnostics.fix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import net.mamoe.mirai.console.compiler.common.SERIALIZABLE_FQ_NAME
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.idea.inspections.KotlinUniversalQuickFix
import org.jetbrains.kotlin.idea.quickfix.KotlinCrossLanguageQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * @see MiraiConsoleErrors.UNSERIALIZABLE_TYPE
 */
class AddSerializerFix(
    element: KtClassOrObject,
) : KotlinCrossLanguageQuickFixAction<KtModifierListOwner>(element), KotlinUniversalQuickFix {

    override fun getFamilyName(): String = "添加注解"
    override fun getText(): String = "添加 @Serializable"

    override fun invokeImpl(project: Project, editor: Editor?, file: PsiFile) {
        element?.addAnnotation(SERIALIZABLE_FQ_NAME) ?: return
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val classDescriptor = diagnostic.castOrNull<DiagnosticWithParameters1<*, *>>()?.a?.castOrNull<ClassDescriptor>() ?: return null
            val ktClassOrObject = classDescriptor.findPsi()?.castOrNull<KtClassOrObject>() ?: return null
            return AddSerializerFix(ktClassOrObject)
        }

        override fun isApplicableForCodeFragment(): Boolean = false
    }
}
