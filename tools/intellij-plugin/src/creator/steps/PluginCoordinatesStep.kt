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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.compiler.common.CheckerConstants.PLUGIN_ID_PATTERN
import net.mamoe.mirai.console.intellij.creator.MiraiProjectModel
import net.mamoe.mirai.console.intellij.creator.MiraiVersionKind
import net.mamoe.mirai.console.intellij.creator.PluginCoordinates
import net.mamoe.mirai.console.intellij.creator.checkNotNull
import net.mamoe.mirai.console.intellij.creator.steps.Validation.NotBlank
import net.mamoe.mirai.console.intellij.creator.steps.Validation.Pattern
import net.mamoe.mirai.console.intellij.creator.tasks.QUALIFIED_CLASS_NAME_PATTERN
import net.mamoe.mirai.console.intellij.creator.tasks.adjustToClassName
import net.mamoe.mirai.console.intellij.diagnostics.ContextualParametersChecker
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.*

class PluginCoordinatesStep(
    private val model: MiraiProjectModel
) : ModuleWizardStep() {

    private lateinit var panel: JPanel

    @field:NotBlank("ID")
    @field:Pattern("ID", PLUGIN_ID_PATTERN)
    private lateinit var idField: JTextField

    @field:NotBlank("Main class")
    @field:Pattern("Main class", QUALIFIED_CLASS_NAME_PATTERN)
    private lateinit var mainClassField: JTextField
    private lateinit var nameField: JTextField
    private lateinit var authorField: JTextField
    private lateinit var dependsOnField: JTextField
    private lateinit var infoArea: JTextArea
    private lateinit var miraiVersionKindBox: JComboBox<MiraiVersionKind>

    @field:NotBlank("Mirai version")
    @field:Pattern("Mirai version", ContextualParametersChecker.SEMANTIC_VERSIONING_PATTERN)
    private lateinit var miraiVersionBox: JComboBox<String>

    override fun getComponent() = panel

    private val versionKindChangeListener: ItemListener = ItemListener { event ->
        if (event.stateChange != ItemEvent.SELECTED) return@ItemListener

        updateVersionItems()
    }

    override fun getPreferredFocusedComponent(): JComponent = idField

    override fun updateStep() {
        miraiVersionKindBox.removeAllItems()
        miraiVersionKindBox.isEnabled = true
        MiraiVersionKind.values().forEach { miraiVersionKindBox.addItem(it) }
        miraiVersionKindBox.selectedItem = MiraiVersionKind.DEFAULT
        miraiVersionKindBox.addItemListener(versionKindChangeListener) // when selected, change versions

        miraiVersionBox.removeAllItems()
        miraiVersionBox.addItem(VERSION_LOADING_PLACEHOLDER)
        miraiVersionBox.selectedItem = VERSION_LOADING_PLACEHOLDER

        model.availableMiraiVersionsOrFail.invokeOnCompletion {
            updateVersionItems()
        }

        if (idField.text.isNullOrEmpty()) {
            model.projectCoordinates.checkNotNull("projectCoordinates").run {
                idField.text = "$groupId.$artifactId"
            }
        }

        if (mainClassField.text.isNullOrEmpty()) {
            model.projectCoordinates.checkNotNull("projectCoordinates").run {
                mainClassField.text = "$groupId.${artifactId.adjustToClassName()}"
            }
        }
    }

    private fun updateVersionItems() {
        GlobalScope.launch(Dispatchers.Main + CoroutineName("updateVersionItems")) {
            if (!model.availableMiraiVersionsOrFail.isCompleted) return@launch
            miraiVersionBox.removeAllItems()
            val expectingKind = miraiVersionKindBox.selectedItem as? MiraiVersionKind ?: MiraiVersionKind.DEFAULT
            kotlin.runCatching { model.availableMiraiVersionsOrFail.await() }
                .fold(
                    onSuccess = { versions ->
                        versions.sortedDescending()
                            .filter { v -> expectingKind.isThatKind(v) }
                            .forEach { v -> miraiVersionBox.addItem(v) }
                    },
                    onFailure = { e ->
                        Validation.popup(
                            "Failed to download version list, please select a version by yourself. \nCause: ${e.cause ?: e}",
                            miraiVersionBox
                        )
                    }
                )

            miraiVersionBox.isEnabled = true
        }
    }

    override fun updateDataModel() {
        model.pluginCoordinates = PluginCoordinates(
            id = idField.text.trim(),
            author = authorField.text,
            name = nameField.text?.trim(),
            info = infoArea.text?.trim(),
            dependsOn = dependsOnField.text?.trim(),
        )
        model.miraiVersion = miraiVersionBox.selectedItem?.toString()?.trim() ?: "+"
        model.packageName = mainClassField.text.substringBeforeLast('.')
        model.mainClassSimpleName = mainClassField.text.substringAfterLast('.')
        model.mainClassQualifiedName = mainClassField.text
    }

    override fun validate(): Boolean {
        if (miraiVersionBox.selectedItem?.toString() == VERSION_LOADING_PLACEHOLDER) {
            Validation.popup("请等待获取版本号", miraiVersionBox)
            return false
        }
        if (!Validation.doValidation(this)) return false
        if (!mainClassField.text.contains('.')) {
            Validation.popup("Main class 需要包含包名", mainClassField)
            return false
        }
        return true
    }

    companion object {
        const val VERSION_LOADING_PLACEHOLDER = "Loading..."
    }
}
