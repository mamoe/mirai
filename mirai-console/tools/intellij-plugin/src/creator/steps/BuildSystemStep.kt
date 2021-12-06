/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator.steps

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import net.mamoe.mirai.console.intellij.creator.MiraiProjectModel
import net.mamoe.mirai.console.intellij.creator.ProjectCoordinates
import net.mamoe.mirai.console.intellij.creator.tasks.PACKAGE_PATTERN
import net.mamoe.mirai.console.intellij.diagnostics.ContextualParametersChecker.Companion.SEMANTIC_VERSIONING_PATTERN
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @see MiraiProjectModel.projectCoordinates
 * @see MiraiProjectModel.languageType
 * @see MiraiProjectModel.buildSystemType
 */
class BuildSystemStep(
    private val model: MiraiProjectModel
) : ModuleWizardStep() {

    private lateinit var panel: JPanel

    @field:Validation.NotBlank("Group ID")
    @field:Validation.Pattern("Group ID", PACKAGE_PATTERN)
    private lateinit var groupIdField: JTextField

    @field:Validation.NotBlank("Artifact ID")
    @field:Validation.Pattern("Artifact ID", PACKAGE_PATTERN)
    private lateinit var artifactIdField: JTextField

    @field:Validation.NotBlank("Version")
    @field:Validation.Pattern("Version", SEMANTIC_VERSIONING_PATTERN)
    private lateinit var versionField: JTextField

    private lateinit var buildSystemBox: JComboBox<BuildSystemType>
    private lateinit var languageBox: JComboBox<LanguageType>

    override fun getComponent() = panel

    override fun updateStep() {
        buildSystemBox.removeAllItems()
        buildSystemBox.isEnabled = true
        BuildSystemType.values().forEach { buildSystemBox.addItem(it) }
        buildSystemBox.selectedItem = BuildSystemType.DEFAULT
        buildSystemBox.toolTipText = """
            Gradle Kotlin DSL: build.gradle.kts <br/>
            Gradle Groovy DSL: build.gradle
        """.trimIndent()

        languageBox.removeAllItems()
        languageBox.isEnabled = true
        LanguageType.values().forEach { languageBox.addItem(it) }
        languageBox.selectedItem = LanguageType.DEFAULT
        buildSystemBox.toolTipText = """
            Language for main class.
        """.trimIndent()
    }

    override fun updateDataModel() {
        model.buildSystemType = this.buildSystemBox.selectedItem as BuildSystemType
        model.languageType = this.languageBox.selectedItem as LanguageType
        model.projectCoordinates = ProjectCoordinates(
            groupId = groupIdField.text,
            artifactId = artifactIdField.text,
            version = versionField.text
        )
    }

    override fun validate() = Validation.doValidation(this)
}
