/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass

@Suppress("RedundantVisibilityModifier") // might be public in the future
internal open class MultiFilePluginDataStorageImpl(
    public final override val directoryPath: Path
) : PluginDataStorage, MultiFilePluginDataStorage {
    init {
        directoryPath.mkdir()
    }

    public override fun <T : PluginData> load(holder: PluginDataHolder, dataClass: Class<T>): T =
        with(dataClass.kotlin) {
            @Suppress("UNCHECKED_CAST")
            val instance = objectInstance ?: this.createInstanceOrNull() ?: kotlin.run {
                require(dataClass == PluginData::class.java) {
                    "Cannot create PluginData instance. Make sure dataClass is PluginData::class.java or a Kotlin's object, " +
                            "or has a constructor which either has no parameters or all parameters of which are optional"
                }
                if (holder is AutoSavePluginDataHolder) {
                    AutoSavePluginData(holder, this) as T?
                } else null
            } ?: throw IllegalArgumentException(
                "Cannot create PluginData instance. Make sure 'holder' is a AutoSavePluginDataHolder, " +
                        "or 'data' is an object or has a constructor which either has no parameters or all parameters of which are optional"
            )

            val file = getPluginDataFile(holder, this)
            file.createNewFile()
            check(file.exists() && file.isFile && file.canRead()) { "${file.absolutePath} cannot be read" }
            val text = file.readText()
            if (text.isNotBlank()) {
                Yaml.default.decodeFromString(instance.updaterSerializer, file.readText())
            }
            instance
        }.also { it.setStorage(this) }

    protected open fun getPluginDataFile(holder: PluginDataHolder, clazz: KClass<*>): File = with(clazz) {
        val name = findValueName()

        val dir = directoryPath.resolve(holder.name)
        if (dir.isFile) {
            error("Target directory $dir for holder $holder is occupied by a file therefore data $qualifiedNameOrTip can't be saved.")
        }
        dir.mkdir()

        val file = directoryPath.resolve(name)
        if (file.isDirectory) {
            error("Target file $file is occupied by a directory therefore data $qualifiedNameOrTip can't be saved.")
        }
        return file.toFile()
    }

    @ConsoleExperimentalAPI
    public override fun store(holder: PluginDataHolder, pluginData: PluginData) {
        val file =
            getPluginDataFile(
                holder,
                if (pluginData is AutoSavePluginData) pluginData.originPluginDataClass else pluginData::class
            )

        if (file.exists() && file.isFile && file.canRead()) {
            file.writeText(Yaml.default.encodeToString(pluginData.updaterSerializer, Unit))
        }
    }
}

internal fun Path.mkdir(): Boolean = this.toFile().mkdir()
internal val Path.isFile: Boolean get() = this.toFile().isFile
internal val Path.isDirectory: Boolean get() = this.toFile().isDirectory