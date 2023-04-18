/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.wizard

import com.intellij.ide.starters.local.StarterContextProvider
import com.intellij.ide.starters.local.wizard.StarterInitialStep
import com.intellij.ide.starters.shared.JAVA_STARTER_LANGUAGE
import com.intellij.ide.starters.shared.KOTLIN_STARTER_LANGUAGE
import com.intellij.ide.starters.shared.ValidationFunctions
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.observable.util.trim
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.dsl.builder.*
import net.mamoe.mirai.console.intellij.wizard.MiraiProjectWizardBundle.message
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.runAsync
import org.jetbrains.kotlin.tools.projectWizard.Versions

private val log = logger<MiraiProjectWizardInitialStep>()

class MiraiProjectWizardInitialStep(contextProvider: StarterContextProvider) : StarterInitialStep(contextProvider) {
    private val pluginVersionProperty = propertyGraph.property("0.1.0")
    private val pluginNameProperty = propertyGraph.lazyProperty { "" }
    private val pluginIdProperty = propertyGraph.lazyProperty { "" }
    private val pluginAuthorProperty = propertyGraph.lazyProperty { System.getProperty("user.name") }
    private val pluginDependenciesProperty = propertyGraph.lazyProperty { "" }
    private val pluginInfoProperty = propertyGraph.lazyProperty { "" }
    private val miraiVersionKindProperty = propertyGraph.property(MiraiVersionKind.Stable)
    private val miraiVersionProperty = propertyGraph.property("0.1.0")
    private val useProxyRepoProperty = propertyGraph.property(true)

    var pluginVersion by pluginVersionProperty.trim()
    var pluginName by pluginNameProperty.trim()
    var pluginId by pluginIdProperty.trim()
    var pluginAuthor by pluginAuthorProperty.trim()
    var pluginDependencies by pluginDependenciesProperty.trim()
    var pluginInfo by pluginInfoProperty.trim()

    var miraiVersionKind by miraiVersionKindProperty
    var miraiVersion by miraiVersionProperty

    var kotlinStdlibVersion = Versions.KOTLIN.text

    private lateinit var miraiVersionCell: Cell<ComboBox<String>>

    var useProxyRepo by useProxyRepoProperty

    override fun addFieldsAfter(layout: Panel) {
        layout.group(message("title.plugin.description")) {
            row(message("label.plugin.id")) {
                textField()
                    .withSpecialValidation(
                        ValidationFunctions.CHECK_NOT_EMPTY,
                    )
                    .bindText(pluginIdProperty)
                rowComment(message("comment.plugin.id"))

                pluginIdProperty.dependsOn(groupIdProperty) { "$groupId.$artifactId" }
                pluginIdProperty.dependsOn(artifactIdProperty) { "$groupId.$artifactId" }
            }

            row(message("label.plugin.name")) {
                textField()
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
                textField()
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

                miraiVersionCell = comboBox(listOf<String>())
                    .enabled(false)
                    .bindItem(miraiVersionProperty)

                miraiVersionProperty.afterChange {
                    kotlinStdlibVersion = "Loading"
                    runAsync {
                        KotlinStdlibVersionFetcher.getKotlinStdlibVersion(miraiVersion)
                    }.onError {
                        log.error(it)
                        kotlinStdlibVersion = Versions.KOTLIN.text
                    }.onProcessed {
                        kotlinStdlibVersion = it
                    }
                }

                miraiVersionKindProperty.afterChange {
                    if (!miraiVersionCell.component.isEnabled) return@afterChange

                    updateVersionItems(miraiVersionKindCell, miraiVersionCell)
                }

                updateVersionItems(miraiVersionKindCell, miraiVersionCell)
                rowComment(message("comment.mirai.version"))
            }
            row {
                checkBox(message("text.use.proxy.repo")).enabled(true).bindSelected(useProxyRepoProperty)
            }
        }

        // Update default values

        languageProperty.set(KOTLIN_STARTER_LANGUAGE)
        projectTypeProperty.set(MiraiModuleBuilder.GRADLE_KTS_PROJECT)
        pluginIdProperty.set("$groupId.$artifactId")
        pluginNameProperty.set(artifactId.adjustToPresentationName())
    }

    override fun updateDataModel() {
        super.updateDataModel()

        starterContext.putUserData(
            /* key = */ MiraiModuleBuilder.MIRAI_PROJECT_MODEL_KEY,
            /* value = */ MiraiProjectModel(
                projectCoordinates = ProjectCoordinates(
                    groupId = groupId.trim(),
                    artifactId = artifactId.trim(),
                    version = pluginVersion.trim(),
                    moduleName = entityName
                ),
                pluginCoordinates = PluginCoordinates(
                    id = pluginId.trim(),
                    name = pluginName.trim(),
                    author = pluginAuthor.trim(),
                    info = pluginInfo.trim(),
                    dependsOn = pluginDependencies.trim()
                ),
                miraiVersion = miraiVersion,
                kotlinVersion = kotlinStdlibVersion,
                buildSystemType = when (val projectType = projectTypeProperty.get()) {
                    MiraiModuleBuilder.GRADLE_KTS_PROJECT -> BuildSystemType.GradleKt
                    MiraiModuleBuilder.GRADLE_GROOVY_PROJECT -> BuildSystemType.GradleGroovy
                    else -> error("Unsupported project type: $projectType")
                },
                languageType = when (val language = languageProperty.get()) {
                    KOTLIN_STARTER_LANGUAGE -> LanguageType.Kotlin
                    JAVA_STARTER_LANGUAGE -> LanguageType.Java
                    else -> error("Unsupported language type: $language")
                },
                useProxyRepo = useProxyRepo
            )
        )
    }

    override fun validate(): Boolean {
        if (miraiVersion == message("label.mirai.version.loading") || kotlinStdlibVersion == "Loading") {
            JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(
                    message("error.please.wait.for.mirai.version"),
                    MessageType.WARNING, null
                )
                .setFadeoutTime(3000)
                .createBalloon()
                .show(
                    RelativePoint.getSouthWestOf(
                        miraiVersionCell.component
                    ), Balloon.Position.atLeft
                )
            return false
        }
        return super.validate()
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
                JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(
                        message("error.failed.to.download.mirai.version"),
                        MessageType.ERROR, null
                    )
                    .setFadeoutTime(2000)
                    .createBalloon()
                    .show(
                        RelativePoint.getSouthOf(
                            miraiVersionCell.component
                        ), Balloon.Position.below
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

private fun String.adjustToPresentationName(): String {
    val result = buildString {
        var doCapitalization = true

        fun Char.isAllowed() = isLetterOrDigit() || this in "_- "

        for (char in this@adjustToPresentationName) {
            if (!char.isAllowed()) continue

            if (doCapitalization) {
                when {
                    char.isLetter() -> append(char.uppercase())
                    char == '_' -> {}
                    char == '-' -> {}
                    else -> append(char)
                }
                doCapitalization = false
            } else {
                if (char in "_- ") {
                    doCapitalization = true
                    append(' ')
                } else {
                    append(char)
                }
            }
        }
    }.trim()

    return result
}
