/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import net.mamoe.mirai.console.compiler.common.resolve.AUTO_SERVICE
import net.mamoe.mirai.console.intellij.diagnostics.fix.ConfigurePluginMainServiceFix
import net.mamoe.mirai.console.intellij.resolve.hasAnnotation
import org.jetbrains.kotlin.idea.debugger.readAction
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.idea.util.rootManager
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.referenceExpressionVisitor
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
        return referenceExpressionVisitor visitor@{ referenceExpr ->
            val ktClass = referenceExpr.resolveMiraiPluginDeclaration() ?: return@visitor
            val fqName = ktClass.fqName?.asString() ?: return@visitor

            val found = isServiceConfiguredWithAutoService(ktClass)
                || isServiceConfiguredWithResource(ktClass, fqName)

            if (!found) {
                holder.registerProblem(
                    ktClass.nameIdentifier ?: ktClass.identifyingElement ?: ktClass,
                    "插件主类服务未配置",
                    ProblemHighlightType.WARNING,
                    ConfigurePluginMainServiceFix(ktClass)
                )
            }
        }
    }

    private fun isServiceConfiguredWithAutoService(
        ktClass: KtClassOrObject,
    ): Boolean = ktClass.hasAnnotation(AUTO_SERVICE)

    private fun isServiceConfiguredWithResource(
        ktClass: KtClassOrObject,
        fqName: String,
    ): Boolean {
        val sourceRoots = ktClass.module?.rootManager?.sourceRoots ?: return false
        val services = sourceRoots.asSequence().flatMap { file ->
            SERVICE_FILE_NAMES.asSequence().mapNotNull { serviceFileName ->
                file.findFileByRelativePath("META-INF/services/$serviceFileName")
            }
        }
        return services.any { serviceFile ->
            serviceFile.readAction { f -> f.inputStream.bufferedReader().use { it.readLine() }.trim() == fqName }
        }
    }
}