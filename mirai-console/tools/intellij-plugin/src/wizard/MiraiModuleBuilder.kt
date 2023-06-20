/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.Key
import com.intellij.pom.java.LanguageLevel
import com.intellij.util.lang.JavaVersion
import net.mamoe.mirai.console.intellij.assets.FT
import net.mamoe.mirai.console.intellij.assets.Icons

class MiraiModuleBuilder : StarterModuleBuilder() {
    companion object {
        val MIRAI_PROJECT_MODEL_KEY = Key.create<MiraiProjectModel>("mirai.project.model")

        val GRADLE_GROOVY_PROJECT: StarterProjectType = StarterProjectType("gradleGroovy", "Gradle Groovy DSL")
        val GRADLE_KTS_PROJECT: StarterProjectType = StarterProjectType("gradleKts", "Gradle Kotlin DSL")
    }

    override fun getBuilderId() = "MiraiModuleBuilder"
    override fun getPresentableName() = MiraiProjectWizardBundle.message("module.presentation.name")
    override fun getWeight() = KOTLIN_WEIGHT - 2
    override fun getNodeIcon() = Icons.MainIcon
    override fun getDescription(): String = MiraiProjectWizardBundle.message("module.description")

    override fun getProjectTypes(): List<StarterProjectType> = listOf(GRADLE_GROOVY_PROJECT, GRADLE_KTS_PROJECT)
    override fun getTestFrameworks(): List<StarterTestRunner> = listOf(JUNIT_TEST_RUNNER)
    override fun getMinJavaVersion(): JavaVersion = LanguageLevel.JDK_1_8.toJavaVersion()

    override fun getStarterPack(): StarterPack {
        return StarterPack(
            "mirai", listOf(
                Starter("mirai", "Mirai Console", getDependencyConfig("/starters/compose.pom"), emptyList())
            )
        )
    }

    override fun getLanguages(): List<StarterLanguage> = listOf(JAVA_STARTER_LANGUAGE, KOTLIN_STARTER_LANGUAGE)


    override fun createWizardSteps(
        context: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> = emptyArray()

    override fun createOptionsStep(contextProvider: StarterContextProvider): StarterInitialStep {
        return MiraiProjectWizardInitialStep(contextProvider)
    }

    override fun setupModule(module: Module) {
        // manually set, we do not show the second page with libraries
        starterContext.starter = starterContext.starterPack.starters.first()
        starterContext.starterDependencyConfig = loadDependencyConfig()[starterContext.starter?.id]

        super.setupModule(module)
    }

    override fun getTemplateProperties(): Map<String, Any> {
        val model = starterContext.getUserData(MIRAI_PROJECT_MODEL_KEY)!!
        model.run {
            val projectCoordinates = projectCoordinates
            val pluginCoordinates = pluginCoordinates
            return mapOf<String, Any>(
                "KOTLIN_VERSION" to kotlinVersion,
                "MIRAI_VERSION" to miraiVersion,
                "GROUP_ID" to projectCoordinates.groupId,
                "VERSION" to projectCoordinates.version,
                "PROJECT_NAME" to starterContext,
                "USE_PROXY_REPO" to useProxyRepo,
                "ARTIFACT_ID" to projectCoordinates.artifactId,
                "MODULE_NAME" to projectCoordinates.moduleName,

                "PLUGIN_ID" to pluginCoordinates.id,
                "PLUGIN_NAME" to languageType.escapeString(pluginCoordinates.name),
                "PLUGIN_AUTHOR" to languageType.escapeString(pluginCoordinates.author),
                "PLUGIN_INFO" to languageType.escapeRawString(pluginCoordinates.info),
                "PLUGIN_DEPENDS_ON" to pluginCoordinates.dependsOn,
                "PLUGIN_VERSION" to projectCoordinates.version,

                "PACKAGE_NAME" to packageName,
                "CLASS_NAME" to mainClassSimpleName,

                "LANGUAGE_TYPE" to languageType.toString(),
            )
        }
    }

    override fun getAssets(starter: Starter): List<GeneratorAsset> {
        val ftManager = FileTemplateManager.getInstance(ProjectManager.getInstance().defaultProject)
        val assets = mutableListOf<GeneratorAsset>()

        val model = starterContext.getUserData(MIRAI_PROJECT_MODEL_KEY)!!

        val standardAssetsProvider = StandardAssetsProvider()
        assets.add(GeneratorTemplateFile(
            standardAssetsProvider.gradleWrapperPropertiesLocation,
            ftManager.getCodeTemplate(FT.GradleWrapperProperties)
        ))
        assets.addAll(standardAssetsProvider.getGradlewAssets())

        model.buildSystemType.createBuildSystem(model)
            .collectAssets { assets.add(it) }

        assets.add(
            GeneratorTemplateFile(
                "src/main/resources/META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin",
                ftManager.getCodeTemplate(FT.PluginMainService)
            )
        )

        assets.add(GeneratorEmptyDirectory("debug-sandbox"))
        assets.add(GeneratorEmptyDirectory("debug-sandbox/plugins"))
        assets.add(GeneratorEmptyDirectory("debug-sandbox/data"))
        assets.add(GeneratorEmptyDirectory("debug-sandbox/config"))
        assets.add(
            GeneratorTemplateFile(
                "debug-sandbox/account.properties",
                ftManager.getCodeTemplate(FT.AccountProperties)
            )
        )

        assets.add(GeneratorTemplateFile(".run/RunTerminal.run.xml", ftManager.getCodeTemplate(FT.RunTerminalRun)))

        return assets
    }

}