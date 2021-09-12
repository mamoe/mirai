/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SilentLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.warning
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.nio.file.Path

@Suppress("RedundantVisibilityModifier") // might be public in the future
internal open class MultiFilePluginDataStorageImpl(
    public final override val directoryPath: Path,
    private val logger: MiraiLogger = SilentLogger,
) : PluginDataStorage, MultiFilePluginDataStorage {
    init {
        directoryPath.mkdir()
    }

    public override fun load(holder: PluginDataHolder, instance: PluginData) {
        instance.onInit(holder, this)

        // 0xFEFF is BOM, handle UTF8-BOM
        val text = getPluginDataFile(holder, instance).readText().removePrefix("\uFEFF")
        if (text.isNotBlank()) {
            logger.warning { "Deserializing $text" }
            Yaml.decodeFromString(instance.updaterSerializer, text)
        } else {
            this.store(holder, instance) // save an initial copy
        }
        logger.debug { "Successfully loaded PluginData: ${instance.saveName} (containing ${instance.castOrNull<AbstractPluginData>()?.valueNodes?.size} properties)" }
    }

    protected open fun getPluginDataFile(holder: PluginDataHolder, instance: PluginData): File {
        val name = instance.saveName

        val dir = directoryPath.resolve(holder.dataHolderName)
        if (dir.isFile) {
            error("Target directory $dir for holder $holder is occupied by a file therefore data ${instance::class.qualifiedNameOrTip} can't be saved.")
        }
        dir.mkdir()

        val file = dir.resolve("$name.yml")
        if (file.isDirectory) {
            error("Target File $file is occupied by a directory therefore data ${instance::class.qualifiedNameOrTip} can't be saved.")
        }
        logger.debug { "File allocated for ${instance.saveName}: $file" }
        return file.toFile().also { it.createNewFile() }
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        allowStructuredMapKeys = true
        encodeDefaults = true
    }

    private val yaml = Yaml

    @ConsoleExperimentalApi
    public override fun store(holder: PluginDataHolder, instance: PluginData) {
        getPluginDataFile(holder, instance).writeText(
            kotlin.runCatching {
                yaml.encodeToString(instance.updaterSerializer, Unit).also {
                    yaml.decodeAnyFromString(it) // test yaml
                }
            }.recoverCatching {
                logger.warning(
                    "Could not save ${instance.saveName} in YAML format due to exception in YAML encoder. " +
                            "Please report this exception and relevant configurations to https://github.com/mamoe/mirai-console/issues/new",
                    it
                )
                json.encodeToString(instance.updaterSerializer, Unit)
            }.getOrElse {
                throw IllegalStateException("Exception while saving $instance, saveName=${instance.saveName}", it)
            }
        )
        logger.debug { "Successfully saved PluginData: ${instance.saveName} (containing ${instance.castOrNull<AbstractPluginData>()?.valueNodes?.size} properties)" }
    }
}

internal fun Path.mkdir(): Boolean = this.toFile().mkdir()
internal val Path.isFile: Boolean get() = this.toFile().isFile
internal val Path.isDirectory: Boolean get() = this.toFile().isDirectory