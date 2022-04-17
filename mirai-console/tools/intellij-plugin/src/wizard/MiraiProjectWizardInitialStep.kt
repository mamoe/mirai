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
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.SegmentedButton
import com.intellij.ui.dsl.builder.bindText
import net.mamoe.mirai.console.intellij.creator.MiraiVersion
import net.mamoe.mirai.console.intellij.creator.MiraiVersionKind
import net.mamoe.mirai.console.intellij.creator.steps.Validation
import net.mamoe.mirai.console.intellij.wizard.MiraiProjectWizardBundle.message
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.runAsync

private val log = logger<MiraiProjectWizardInitialStep>()

class MiraiProjectWizardInitialStep(contextProvider: StarterContextProvider) : StarterInitialStep(contextProvider) {
    private val miraiVersionKindProperty = propertyGraph.property(MiraiVersionKind.Stable)
    private val pluginVersionProperty = propertyGraph.property("0.1.0")
    private val pluginNameProperty = propertyGraph.lazyProperty { wizardContext.projectName }
    private val pluginIdProperty = propertyGraph.lazyProperty { "$groupId.$artifactId" }
    private val pluginAuthorProperty = propertyGraph.lazyProperty { System.getProperty("user.name") }

    var miraiVersionKind by miraiVersionKindProperty
    var pluginVersion by pluginVersionProperty
    var pluginName by pluginNameProperty
    var pluginId by pluginIdProperty
    val pluginAuthor by pluginAuthorProperty

    override fun addFieldsAfter(layout: Panel) {
        layout.group(message("title.plugin.description")) {
            row(message("label.plugin.id")) {
                textField().bindText(pluginNameProperty)
            }
            row(message("label.plugin.name")) {
                textField().bindText(pluginNameProperty)
            }
            row(message("label.plugin.version")) {
                textField().bindText(pluginVersionProperty)
            }
            row(message("label.plugin.author")) {
                textField().bindText(pluginAuthorProperty)
            }
            row(message("label.mirai.version")) {
                val miraiVersionKindCell = segmentedButton(MiraiVersionKind.values().toList()) { kind ->
                    when (kind) {
                        MiraiVersionKind.Stable -> message("label.version.stable")
                        MiraiVersionKind.Prerelease -> message("label.version.prerelease")
                        MiraiVersionKind.Nightly -> message("label.version.nightly")
                    }
                }.bind(miraiVersionKindProperty)
                val miraiVersionCell = comboBox(listOf<String>()).enabled(false)

                miraiVersionKindProperty.afterChange {
                    if (!miraiVersionCell.component.isEnabled) return@afterChange

                    updateVersionItems(miraiVersionKindCell, miraiVersionCell)
                }

                updateVersionItems(miraiVersionKindCell, miraiVersionCell)
            }
        }
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