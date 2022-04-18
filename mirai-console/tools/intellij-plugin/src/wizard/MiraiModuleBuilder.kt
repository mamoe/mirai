/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.starters.local.*
import com.intellij.ide.starters.local.wizard.StarterInitialStep
import com.intellij.ide.starters.shared.*
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.java.LanguageLevel
import com.intellij.util.lang.JavaVersion
import net.mamoe.mirai.console.intellij.assets.Icons
import net.mamoe.mirai.console.intellij.creator.MiraiProjectModel
import net.mamoe.mirai.console.intellij.creator.tasks.CreateProjectTask
import org.jetbrains.kotlin.tools.composeProjectWizard.ComposeModuleBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors

class MiraiModuleBuilder : StarterModuleBuilder() {
    override fun getBuilderId() = "MiraiModuleBuilder"
    override fun getPresentableName() = MiraiProjectWizardBundle.message("module.presentation.name")
    override fun getWeight() = KOTLIN_WEIGHT - 2
    override fun getNodeIcon() = Icons.MainIcon
    override fun getDescription(): String = MiraiProjectWizardBundle.message("module.description")

    override fun getProjectTypes(): List<StarterProjectType> = listOf(GRADLE_PROJECT)
    override fun getTestFrameworks(): List<StarterTestRunner> = listOf(JUNIT_TEST_RUNNER)
    override fun getMinJavaVersion(): JavaVersion = LanguageLevel.JDK_1_8.toJavaVersion()

    override fun getStarterPack(): StarterPack {
        return StarterPack(
            "mirai", listOf(
                Starter("mirai", "Mirai Console", getDependencyConfig("/starters/compose.pom"), emptyList())
            )
        )
    }

    override fun getLanguages(): List<StarterLanguage> = listOf(KOTLIN_STARTER_LANGUAGE, JAVA_STARTER_LANGUAGE)


    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> = emptyArray()

    override fun createOptionsStep(contextProvider: StarterContextProvider): StarterInitialStep {
        return MiraiProjectWizardInitialStep(contextProvider)
    }

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

    //    private val scope = CoroutineScope(SupervisorJob())
    private val scope = Executors.newFixedThreadPool(2)
    private val model = MiraiProjectModel.create(scope)

    override fun cleanup() {
        super.cleanup()
        scope.shutdownNow()
    }

    override fun getAssets(starter: Starter): List<GeneratorAsset> {
        val manager = FileTemplateManager.getInstance(ProjectManager.getInstance().defaultProject)
        val standardAssetsProvider = StandardAssetsProvider()

        val configType = starterContext.getUserData(ComposeModuleBuilder.COMPOSE_CONFIG_TYPE_KEY)
        val platform = starterContext.getUserData(ComposeModuleBuilder.COMPOSE_PLATFORM_KEY)
        val packagePath = starterContext.group.replace('.', '/')

        val assets = mutableListOf<GeneratorAsset>()

//        assets.add(
//            GeneratorTemplateFile("")
//        )
        return listOf(GeneratorEmptyDirectory(""))
    }

}