/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.starters.local.GeneratorAsset
import com.intellij.ide.starters.local.GeneratorEmptyDirectory
import com.intellij.ide.starters.local.GeneratorTemplateFile
import com.intellij.openapi.project.ProjectManager
import net.mamoe.mirai.console.intellij.assets.FT

sealed class ProjectCreator(
    val model: MiraiProjectModel,
) {
    private val manager get() = FileTemplateManager.getInstance(ProjectManager.getInstance().defaultProject)

    fun getTemplate(name: String): FileTemplate = manager.getCodeTemplate(name)

    abstract fun collectAssets(
        collect: (GeneratorAsset) -> Unit,
    )
}

sealed class GradleProjectCreator(
    model: MiraiProjectModel,
) : ProjectCreator(model) {
    override fun collectAssets(
        collect: (GeneratorAsset) -> Unit
    ) {
        collect(GeneratorEmptyDirectory("src/main/${model.languageType.sourceSetDirName}"))
        collect(GeneratorEmptyDirectory("src/main/resources"))

        collect(GeneratorEmptyDirectory("src/test/${model.languageType.sourceSetDirName}"))
        collect(GeneratorEmptyDirectory("src/test/resources"))

        collect(GeneratorTemplateFile(model.languageType.pluginMainClassFile(this)))

        collect(GeneratorTemplateFile(".gitignore", getTemplate(FT.Gitignore)))
        collect(GeneratorTemplateFile("gradle.properties", getTemplate(FT.GradleProperties)))
    }

}

private fun GeneratorTemplateFile(targetFileName: NamedFile): GeneratorTemplateFile {
    return GeneratorTemplateFile(targetFileName.path, targetFileName.template)
}


class GradleKotlinProjectCreator(
    model: MiraiProjectModel,
) : GradleProjectCreator(
    model,
) {
    override fun collectAssets(
        collect: (GeneratorAsset) -> Unit
    ) {
        super.collectAssets(collect)
        collect(GeneratorTemplateFile("build.gradle.kts", getTemplate(FT.BuildGradleKts)))
        collect(GeneratorTemplateFile("settings.gradle.kts", getTemplate(FT.SettingsGradleKts)))
    }
}

class GradleGroovyProjectCreator(
    model: MiraiProjectModel,
) : GradleProjectCreator(
    model,
) {
    override fun collectAssets(
        collect: (GeneratorAsset) -> Unit
    ) {
        super.collectAssets(collect)
        collect(GeneratorTemplateFile("build.gradle", getTemplate(FT.BuildGradle)))
        collect(GeneratorTemplateFile("settings.gradle", getTemplate(FT.SettingsGradle)))
    }
}