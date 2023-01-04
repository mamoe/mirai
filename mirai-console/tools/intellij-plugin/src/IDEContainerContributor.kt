/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij

import com.intellij.psi.PsiElement
import net.mamoe.mirai.console.compiler.common.castOrNull
import net.mamoe.mirai.console.intellij.diagnostics.CommandDeclarationChecker
import net.mamoe.mirai.console.intellij.diagnostics.ContextualParametersChecker
import net.mamoe.mirai.console.intellij.diagnostics.PluginDataValuesChecker
import net.mamoe.mirai.console.intellij.util.DEBUG_ENABLED
import net.mamoe.mirai.console.intellij.util.runIgnoringErrors
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.idea.base.projectStructure.unwrapModuleSourceInfo
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import java.io.File

class IDEContainerContributor : StorageComponentContainerContributor {
    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: org.jetbrains.kotlin.platform.TargetPlatform,
        moduleDescriptor: ModuleDescriptor,
    ) {
        if (moduleDescriptor.hasMiraiConsoleDependency()) {
            container.useInstance(ContextualParametersChecker().wrapIgnoringExceptionIfNotDebug())
            container.useInstance((PluginDataValuesChecker() as CallChecker).wrapIgnoringExceptionIfNotDebug())
            container.useInstance((PluginDataValuesChecker() as DeclarationChecker).wrapIgnoringExceptionIfNotDebug())
            container.useInstance(CommandDeclarationChecker().wrapIgnoringExceptionIfNotDebug())
        }
    }

    private fun DeclarationChecker.wrapIgnoringExceptionIfNotDebug(): DeclarationChecker {
        if (DEBUG_ENABLED) {
            return this
        }
        return DeclarationCheckerIgnoringExceptions(this)
    }

    private fun CallChecker.wrapIgnoringExceptionIfNotDebug(): CallChecker {
        if (DEBUG_ENABLED) {
            return this
        }
        return CallCheckerIgnoringExceptions(this)
    }

    class CallCheckerIgnoringExceptions(
        private val delegate: CallChecker
    ) : CallChecker {
        override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
            runIgnoringErrors { delegate.check(resolvedCall, reportOn, context) }
        }

    }

    class DeclarationCheckerIgnoringExceptions(
        private val delegate: DeclarationChecker
    ) : DeclarationChecker {
        override fun check(
            declaration: KtDeclaration,
            descriptor: DeclarationDescriptor,
            context: DeclarationCheckerContext
        ) {
            runIgnoringErrors { delegate.check(declaration, descriptor, context) }
        }

    }
}

fun ModuleDescriptor.hasMiraiConsoleDependency(): Boolean {
    // /.m2/repository/net/mamoe/kotlin-jvm-blocking-bridge-compiler-embeddable/1.4.0/kotlin-jvm-blocking-bridge-compiler-embeddable-1.4.0.jar
    val pluginJpsJarName = "mirai-console"
    val module =
        getCapability(ModuleInfo.Capability)?.unwrapModuleSourceInfo()?.module
            ?: return false
    val facet = KotlinFacet.get(module) ?: return false
    val pluginClasspath =
        facet.configuration.settings.compilerArguments?.castOrNull<K2JVMCompilerArguments>()?.classpathAsList0
            ?: return false

    if (pluginClasspath.none { path -> path.name.contains(pluginJpsJarName) }) return false
    return true
}

private var K2JVMCompilerArguments.classpathAsList0: List<File>
    get() = classpath.orEmpty().split(File.pathSeparator).map(::File)
    set(value) {
        classpath = value.joinToString(separator = File.pathSeparator, transform = { it.path })
    }
