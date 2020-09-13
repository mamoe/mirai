package net.mamoe.mirai.console.internal.extension

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.extension.Extension
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.internal.data.kClassQualifiedName
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.utils.info
import kotlin.reflect.KClass

internal object BuiltInSingletonExtensionSelector : SingletonExtensionSelector {

    internal val config: SaveData = SaveData()

    internal class SaveData : AutoSavePluginConfig() {
        override val saveName: String get() = "ExtensionSelector"

        val value: MutableMap<String, String> by value()
    }

    override fun <T : Extension> selectSingleton(
        extensionType: KClass<T>,
        candidates: Collection<SingletonExtensionSelector.Registry<T>>,
    ): T? = when {
        candidates.isEmpty() -> null
        candidates.size == 1 -> candidates.single().extension
        else -> kotlin.run {
            val target = config.value[extensionType.qualifiedName!!]
                ?: return promptForSelectionAndSave(extensionType, candidates)

            val found = candidates.firstOrNull { it.extension::class.qualifiedName == target }?.extension
                ?: return promptForSelectionAndSave(extensionType, candidates)

            found
        }
    }

    private fun <T : Extension> promptForSelectionAndSave(
        extensionType: KClass<T>,
        candidates: Collection<SingletonExtensionSelector.Registry<T>>,
    ) = promptForManualSelection(extensionType, candidates)
        .also { config.value[extensionType.qualifiedName!!] = it.extension.kClassQualifiedName!! }.extension

    private fun <T : Extension> promptForManualSelection(
        extensionType: KClass<T>,
        candidates: Collection<SingletonExtensionSelector.Registry<T>>,
    ): SingletonExtensionSelector.Registry<T> {
        MiraiConsole.mainLogger.info { "There are multiple ${extensionType.simpleName}s, please select one:" }

        val candidatesList = candidates.toList()

        for ((index, candidate) in candidatesList.withIndex()) {
            MiraiConsole.mainLogger.info { "${index + 1}. '${candidate.extension}' from '${candidate.plugin.name}'" }
        }

        MiraiConsole.mainLogger.info { "Please choose a number from 1 to ${candidatesList.count()}" }

        val choiceStr = runBlocking { ConsoleInput.requestInput("Your choice: ") }

        val choice = choiceStr.toIntOrNull() ?: error("Bad choice")

        return candidatesList[choice - 1]
    }
}