/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.intellij.assets.Icons
import net.mamoe.mirai.console.intellij.creator.steps.BuildSystemStep
import net.mamoe.mirai.console.intellij.creator.steps.OptionsStep
import net.mamoe.mirai.console.intellij.creator.steps.PluginCoordinatesStep
import net.mamoe.mirai.console.intellij.creator.tasks.CreateProjectTask
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MiraiModuleBuilder : JavaModuleBuilder() {
    override fun getPresentableName() = MiraiModuleType.NAME
    override fun getNodeIcon() = Icons.MainIcon
    override fun getGroupName() = MiraiModuleType.NAME
    override fun getWeight() = BUILD_SYSTEM_WEIGHT - 1
    override fun getBuilderId() = ID
    override fun getModuleType(): ModuleType<*> = JavaModuleType.getModuleType()
    override fun getParentGroup() = MiraiModuleType.NAME

    override fun setupRootModel(rootModel: ModifiableRootModel) {
        val project = rootModel.project
        val (root, vFile) = createAndGetRoot()
        rootModel.addContentEntry(vFile)

        if (moduleJdk != null) {
            rootModel.sdk = moduleJdk
        } else {
            rootModel.inheritSdk()
        }

        val r = DumbAwareRunnable {
            ProgressManager.getInstance().run(CreateProjectTask(root, rootModel.module, model))
        }

        if (project.isDisposed) return

        if (
            ApplicationManager.getApplication().isUnitTestMode ||
            ApplicationManager.getApplication().isHeadlessEnvironment
        ) {
            r.run()
            return
        }

        if (!project.isInitialized) {
            StartupManager.getInstance(project).registerPostStartupActivity(r)
            return
        }

        DumbService.getInstance(project).runWhenSmart(r)
    }

    private fun createAndGetRoot(): Pair<Path, VirtualFile> {
        val temp = contentEntryPath ?: throw IllegalStateException("Failed to get content entry path")

        val pathName = FileUtil.toSystemIndependentName(temp)

        val path = Paths.get(pathName)
        Files.createDirectories(path)
        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(pathName)
            ?: throw IllegalStateException("Failed to refresh and file file: $path")

        return path to vFile
    }

    private val scope = CoroutineScope(SupervisorJob())
    private val model = MiraiProjectModel.create(scope)

    override fun cleanup() {
        super.cleanup()
        scope.cancel()
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return arrayOf(
            BuildSystemStep(model),
            PluginCoordinatesStep(model),
        )
    }

    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?): ModuleWizardStep =
        OptionsStep()

    companion object {
        const val ID = "MIRAI_MODULE"
    }
}