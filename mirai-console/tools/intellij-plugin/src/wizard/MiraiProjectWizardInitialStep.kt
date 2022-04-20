/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.starters.local.StarterContextProvider
import com.intellij.ide.starters.local.wizard.StarterInitialStep
import com.intellij.ide.starters.shared.KOTLIN_STARTER_LANGUAGE
import com.intellij.ide.starters.shared.ValidationFunctions
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.observable.util.trim
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.SegmentedButton
import com.intellij.ui.dsl.builder.bindText
import net.mamoe.mirai.console.intellij.creator.MiraiVersion
import net.mamoe.mirai.console.intellij.creator.MiraiVersionKind
import net.mamoe.mirai.console.intellij.creator.steps.Validation
import net.mamoe.mirai.console.intellij.creator.tasks.adjustToPresentationName
import net.mamoe.mirai.console.intellij.wizard.MiraiProjectWizardBundle.message
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.runAsync

private val log = logger<MiraiProjectWizardInitialStep>()

class MiraiProjectWizardInitialStep(contextProvider: StarterContextProvider) : StarterInitialStep(contextProvider) {
    private val miraiVersionKindProperty = propertyGraph.property(MiraiVersionKind.Stable)
    private val pluginVersionProperty = propertyGraph.property("0.1.0")
    private val pluginNameProperty = propertyGraph.lazyProperty { "" }
    private val pluginIdProperty = propertyGraph.lazyProperty { "" }
    private val pluginAuthorProperty = propertyGraph.lazyProperty { System.getProperty("user.name") }
    private val pluginDependenciesProperty = propertyGraph.lazyProperty { "" }
    private val pluginInfoProperty = propertyGraph.lazyProperty { "" }

    var miraiVersionKind by miraiVersionKindProperty
    var pluginVersion by pluginVersionProperty.trim()
    var pluginName by pluginNameProperty.trim()
    var pluginId by pluginIdProperty.trim()
    var pluginAuthor by pluginAuthorProperty.trim()
    var pluginDependencies by pluginDependenciesProperty.trim()
    var pluginInfo by pluginInfoProperty.trim()

    override fun addFieldsAfter(layout: Panel) {
        lateinit var idCell: Cell<JBTextField>
        lateinit var nameCell: Cell<JBTextField>
        lateinit var versionCell: Cell<JBTextField>

        layout.group(message("title.plugin.description")) {
            row(message("label.plugin.id")) {
                idCell = textField()
                    .withSpecialValidation(
                        ValidationFunctions.CHECK_NOT_EMPTY,
                    )
                    .bindText(pluginIdProperty)
                rowComment(message("comment.plugin.id"))

                pluginIdProperty.dependsOn(groupIdProperty) { "$groupId.$artifactId" }
                pluginIdProperty.dependsOn(artifactIdProperty) { "$groupId.$artifactId" }
            }

            row(message("label.plugin.name")) {
                nameCell = textField()
                    .withSpecialValidation(
                        ValidationFunctions.CHECK_NOT_EMPTY,
                        MiraiValidations.CHECK_FORBIDDEN_PLUGIN_NAME,
                    )
                    .bindText(pluginNameProperty)

                pluginNameProperty.dependsOn(artifactIdProperty) {
                    artifactId.adjustToPresentationName()
                }

                rowComment(message("comment.plugin.name"))
            }

            row(message("label.plugin.version")) {
                versionCell = textField()
                    .withSpecialValidation(
                        ValidationFunctions.CHECK_NOT_EMPTY,
                        MiraiValidations.CHECK_ILLEGAL_VERSION_LINE
                    )
                    .bindText(pluginVersionProperty)
                rowComment(message("comment.plugin.version"))
            }
            row(message("label.plugin.author")) {
                textField().bindText(pluginAuthorProperty)
            }
            row(message("label.plugin.dependencies")) {
                expandableTextField()
                    .withSpecialValidation(MiraiValidations.CHECK_PLUGIN_DEPENDENCIES_SEGMENT)
                    .bindText(pluginDependenciesProperty)
                    .component.emptyText.setText(message("text.hint.plugin.dependencies"), GRAYED_ITALIC_ATTRIBUTES)
                rowComment(message("comment.plugin.dependencies"))
            }
            row(message("label.plugin.info")) {
                expandableTextField().bindText(pluginInfoProperty)
                    .component.emptyText.setText(message("text.hint.plugin.info"), GRAYED_ITALIC_ATTRIBUTES)
                rowComment(message("comment.plugin.info"))
            }
            row(message("label.mirai.version")) {
                val miraiVersionKindCell = segmentedButton(MiraiVersionKind.values().toList()) { kind ->
                    when (kind) {
                        MiraiVersionKind.Stable -> message("label.version.stable")
                        MiraiVersionKind.Prerelease -> message("label.version.prerelease")
                        MiraiVersionKind.Nightly -> message("label.version.nightly")
                    }
                }.bind(miraiVersionKindProperty)

                val miraiVersionCell = comboBox(listOf<String>())
                    .enabled(false)

                miraiVersionKindProperty.afterChange {
                    if (!miraiVersionCell.component.isEnabled) return@afterChange

                    updateVersionItems(miraiVersionKindCell, miraiVersionCell)
                }

                updateVersionItems(miraiVersionKindCell, miraiVersionCell)
                rowComment(message("comment.mirai.version"))
            }
        }

        // Update default values

        languageProperty.set(KOTLIN_STARTER_LANGUAGE)
        pluginIdProperty.set("$groupId.$artifactId")
        pluginNameProperty.set(artifactId.adjustToPresentationName())
    }

    private fun updateVersionItems(
        miraiVersionKindCell: SegmentedButton<MiraiVersionKind>,
        miraiVersionCell: Cell<ComboBox<MiraiVersion>>
    ): Promise<Set<MiraiVersion>?> {
        miraiVersionCell.component.isEditable = false
        miraiVersionKindCell.enabled(false) // disable the kind selector until the async operation finishes
        miraiVersionCell.enabled(false)

        miraiVersionCell.component.removeAllItems()
        miraiVersionCell.component.addItem(message("label.mirai.version.loading"))

        return runAsync {
            try {
                val list = MiraiVersionKind.getMiraiVersionList()
                miraiVersionCell.component.removeAllItems()
                list.filter { miraiVersionKind.isThatKind(it) }
                    .forEach { v -> miraiVersionCell.component.addItem(v) }
                list
            } catch (e: Throwable) {
                Validation.popup(
                    message("error.failed.to.download.mirai.version"),
                    miraiVersionCell.component
                )
                null
            }
        }.onError { log.error(it) }
            .onProcessed { versions ->
                miraiVersionCell.component.isEditable = versions == null
                miraiVersionKindCell.enabled(true)
                miraiVersionCell.enabled(true)
            }
    }
}