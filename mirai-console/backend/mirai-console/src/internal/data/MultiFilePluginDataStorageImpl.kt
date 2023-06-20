/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.PluginDataHolder
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.logging.lazyInitMiraiLogger
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.nio.file.Path

@Suppress("RedundantVisibilityModifier") // might be public in the future
internal open class MultiFilePluginDataStorageImpl(
    public final override val directoryPath: Path,
    private val logger: MiraiLogger = lazyInitMiraiLogger { MiraiLogger.Factory.create(MultiFilePluginDataStorageImpl::class) },
) : PluginDataStorage, MultiFilePluginDataStorage {
    init {
        directoryPath.mkdir()
    }

    public override fun load(holder: PluginDataHolder, instance: PluginData) {
        instance.onInit(holder, this)

        // 0xFEFF is BOM, handle UTF8-BOM
        val file = getPluginDataFile(holder, instance)
        val text = file.readText().removePrefix("\uFEFF")
        if (text.isNotBlank()) {
            try {
                when (instance.saveType) {
                    PluginData.SaveType.YAML -> {
                        val yaml = createYaml(instance)
                        yaml.decodeFromString(instance.updaterSerializer, text)
                    }

                    PluginData.SaveType.JSON -> {
                        val json = createJson(instance)
                        json.decodeFromString(instance.updaterSerializer, text)
                    }
                }
            } catch (cause: Throwable) {
                // backup data file
                file.copyTo(file.resolveSibling("${file.name}.${currentTimeMillis()}.bak"))
                throw cause
            }
        } else {
            this.store(holder, instance) // save an initial copy
        }
//        logger.verbose { "Successfully loaded PluginData: ${instance.saveName} (containing ${instance.castOrNull<AbstractPluginData>()?.valueNodes?.size} properties)" }
    }

    internal fun getPluginDataFileInternal(holder: PluginDataHolder, instance: PluginData): File {
        return getPluginDataFile(holder, instance)
    }

    protected open fun getPluginDataFile(holder: PluginDataHolder, instance: PluginData): File {
        val name = instance.saveName

        val dir = directoryPath.resolve(holder.dataHolderName)
        if (dir.isFile) {
            error("Target directory $dir for holder $holder is occupied by a file therefore data ${instance::class.qualifiedNameOrTip} can't be saved.")
        }
        dir.mkdir()

        val file = dir.resolve("$name.${instance.saveType.extension}")
        if (file.isDirectory) {
            error("Target File $file is occupied by a directory therefore data ${instance::class.qualifiedNameOrTip} can't be saved.")
        }
//        logger.verbose { "File allocated for ${instance.saveName}: $file" }
        return file.toFile().also {
            it.parentFile?.mkdirs()
            it.createNewFile()
        }
    }

    @ConsoleExperimentalApi
    public override fun store(holder: PluginDataHolder, instance: PluginData) {
        getPluginDataFile(holder, instance).writeText(
            kotlin.runCatching {
                when (instance.saveType) {
                    PluginData.SaveType.YAML -> {
                        val yaml = createYaml(instance)
                        yaml.encodeToString(instance.updaterSerializer, Unit).also {
                            yaml.decodeAnyFromString(it) // test yaml
                        }
                    }

                    PluginData.SaveType.JSON -> {
                        val json = createJson(instance)
                        json.encodeToString(instance.updaterSerializer, Unit).also {
                            json.decodeFromString(instance.updaterSerializer, it) // test json
                        }
                    }
                }
            }.recoverCatching {
                logger.warning(
                    "Could not save ${instance.saveName} in ${instance.saveType.name} format due to exception in ${instance.saveType.name} encoder. " +
                            "Please report this exception and relevant configurations to https://github.com/mamoe/mirai/issues/new/choose",
                    it
                )

                if (instance.saveType == PluginData.SaveType.JSON) {
                    throw it
                }

                val json = createJson(instance)
                json.encodeToString(instance.updaterSerializer, Unit).also { string ->
                    json.decodeFromString(instance.updaterSerializer, string) // test json
                }
            }.getOrElse {
                throw IllegalStateException(
                    "Exception while saving $instance, saveName=${instance.saveName} in json format",
                    it
                )
            }
        )
//        logger.verbose { "Successfully saved PluginData: ${instance.saveName} (containing ${instance.castOrNull<AbstractPluginData>()?.valueNodes?.size} properties)" }
    }

    private fun createYaml(instance: PluginData): Yaml {
        return Yaml {
            this.serializersModule =
                MessageSerializers.serializersModule + instance.serializersModule // MessageSerializers.serializersModule is dynamic
        }
    }

    private fun createJson(instance: PluginData): Json {
        return Json {
            serializersModule =
                MessageSerializers.serializersModule + instance.serializersModule // MessageSerializers.serializersModule is dynamic

            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
            allowStructuredMapKeys = true
            encodeDefaults = true

            classDiscriminator = "#class"
        }
    }
}

internal fun Path.mkdir(): Boolean = this.toFile().mkdir()
internal val Path.isFile: Boolean get() = this.toFile().isFile
internal val Path.isDirectory: Boolean get() = this.toFile().isDirectory