/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.impl.CancellationCheck.Companion.runWithCancellationCheck
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import net.mamoe.mirai.console.compiler.common.resolve.AUTO_SERVICE
import net.mamoe.mirai.console.compiler.common.resolve.PLUGIN_FQ_NAME
import net.mamoe.mirai.console.intellij.diagnostics.fix.ConfigurePluginMainServiceFix
import net.mamoe.mirai.console.intellij.resolve.allSuperNames
import net.mamoe.mirai.console.intellij.resolve.hasAnnotation
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.debugger.readAction
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.classOrObjectVisitor
import java.util.*

/*
private val bundle by lazy {
    BundleUtil.loadLanguageBundle(PluginMainServiceNotConfiguredInspection::class.java.classLoader, "messages.InspectionGadgetsBundle")!!
}*/

class PluginMainServiceNotConfiguredInspection : AbstractKotlinInspection() {
    companion object {
        private val SERVICE_FILE_NAMES = arrayOf(
            "net.mamoe.mirai.console.plugin.jvm.JvmPlugin",
            "net.mamoe.mirai.console.plugin.jvm.KotlinPlugin",
            "net.mamoe.mirai.console.plugin.jvm.JavaPlugin",
        )
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val ktVisitor = classOrObjectVisitor visitor@{ element ->
            if (element !is KtObjectDeclaration) return@visitor
            if (element.allSuperNames.none { it == PLUGIN_FQ_NAME }) return@visitor
            val fqName = element.fqName?.asString() ?: return@visitor

            val found = isServiceConfiguredWithAutoService(element)
                    || isServiceConfiguredWithResource(element, fqName)

            if (!found) {
                registerProblemImpl(holder, element, fqName)
            }
        }

        val javaVisitor = object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is PsiClass) return
                if (element.allSuperNames.none { it == PLUGIN_FQ_NAME }) return

                if (element.hasAnnotation(AUTO_SERVICE.asString())) return
                val fqName = element.qualifiedName ?: return
                if (isServiceConfiguredWithResource(element, fqName)) return

                registerProblemImpl(holder, element, fqName)
            }
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is KtClassOrObject) ktVisitor.visitClassOrObject(element)
                else javaVisitor.visitElement(element)
            }
        }
    }

    private fun registerProblemImpl(holder: ProblemsHolder, element: PsiNameIdentifierOwner, fqName: String) {
        holder.registerProblem(
            element.nameIdentifier ?: element.identifyingElement ?: element,
            @Suppress("DialogTitleCapitalization") "插件主类服务未配置",
            ProblemHighlightType.WARNING,
            ConfigurePluginMainServiceFix(element, fqName)
        )
    }

    private fun isServiceConfiguredWithAutoService(
        ktClass: KtClassOrObject,
    ): Boolean {
        return ktClass.hasAnnotation(AUTO_SERVICE)
    }

    private fun isServiceConfiguredWithResource(
        psiOrKtClass: PsiElement,
        fqName: String,
    ): Boolean {
        return runWithCancellationCheck {
            val sourceRoots: Array<com.intellij.openapi.vfs.VirtualFile> =
                psiOrKtClass.module?.rootManager?.sourceRoots ?: return@runWithCancellationCheck false
            val services = sourceRoots.asSequence().flatMap { file ->
                SERVICE_FILE_NAMES.asSequence().mapNotNull { serviceFileName ->
                    file.findFileByRelativePath("META-INF/services/$serviceFileName")
                }
            }
            return@runWithCancellationCheck services.any { serviceFile ->
                serviceFile.readAction { f ->
                    f.inputStream.bufferedReader().use { reader -> reader.lineSequence().any { it == fqName } }
                }
            }
        }
    }
}