/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.internal.plugin

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.setting.cast
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.utils.info
import java.io.File
import java.util.concurrent.locks.ReentrantLock

internal object PluginManagerImpl : PluginManager {
    override val pluginsDir = File(MiraiConsole.rootDir, "plugins").apply { mkdir() }
    override val pluginsDataFolder = File(MiraiConsole.rootDir, "data").apply { mkdir() }

    @Suppress("ObjectPropertyName")
    private val _pluginLoaders: MutableList<PluginLoader<*, *>> = mutableListOf()
    private val loadersLock: ReentrantLock = ReentrantLock()
    private val logger = MiraiConsole.newLogger("PluginManager")

    @JvmField
    internal val resolvedPlugins: MutableList<Plugin> = mutableListOf()
    override val plugins: List<Plugin>
        get() = resolvedPlugins.toList()
    override val builtInLoaders: List<PluginLoader<*, *>>
        get() = MiraiConsole.builtInPluginLoaders
    override val pluginLoaders: List<PluginLoader<*, *>>
        get() = _pluginLoaders.toList()

    override val Plugin.description: PluginDescription
        get() = resolvedPlugins.firstOrNull { it == this }
            ?.loader?.cast<PluginLoader<Plugin, PluginDescription>>()
            ?.getDescription(this)
            ?: error("Plugin is unloaded")

    override fun registerPluginLoader(loader: PluginLoader<*, *>): Boolean = loadersLock.withLock {
        if (_pluginLoaders.any { it::class == loader }) {
            return false
        }
        _pluginLoaders.add(loader)
    }

    override fun unregisterPluginLoader(loader: PluginLoader<*, *>) = loadersLock.withLock {
        _pluginLoaders.remove(loader)
    }


    // region LOADING

    private fun <P : Plugin, D : PluginDescription> PluginLoader<P, D>.loadPluginNoEnable(description: D): P {
        return kotlin.runCatching {
            this.load(description).also { resolvedPlugins.add(it) }
        }.fold(
            onSuccess = {
                logger.info { "Successfully loaded plugin ${description.name}" }
                it
            },
            onFailure = {
                logger.info { "Cannot load plugin ${description.name}" }
                throw it
            }
        )
    }

    private fun <P : Plugin, D : PluginDescription> PluginLoader<P, D>.enablePlugin(plugin: Plugin) {
        kotlin.runCatching {
            @Suppress("UNCHECKED_CAST")
            this.enable(plugin as P)
        }.fold(
            onSuccess = {
                logger.info { "Successfully enabled plugin ${plugin.description.name}" }
            },
            onFailure = {
                logger.info { "Cannot enable plugin ${plugin.description.name}" }
                throw it
            }
        )
    }

    /**
     * STEPS:
     * 1. 遍历插件列表, 使用 [builtInLoaders] 加载 [PluginKind.LOADER] 类型的插件
     * 2. [启动][PluginLoader.enable] 所有 [PluginKind.LOADER] 的插件
     * 3. 使用内建和所有插件提供的 [PluginLoader] 加载全部除 [PluginKind.LOADER] 外的插件列表.
     * 4. 解决依赖并排序
     * 5. 依次 [PluginLoader.load]
     * 但不 [PluginLoader.enable]
     *
     * @return [builtInLoaders] 可以加载的插件. 已经完成了 [PluginLoader.load], 但没有 [PluginLoader.enable]
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(PluginMissingDependencyException::class)
    internal fun loadEnablePlugins() {
        (loadAndEnableLoaderProviders() + _pluginLoaders.listAllPlugins().flatMap { it.second })
            .sortByDependencies().loadAndEnableAllInOrder()
    }

    private fun List<PluginDescriptionWithLoader>.loadAndEnableAllInOrder() {
        return this.map { (loader, desc) ->
            loader to loader.loadPluginNoEnable(desc)
        }.forEach { (loader, plugin) ->
            loader.enablePlugin(plugin)
        }
    }

    /**
     * @return [builtInLoaders] 可以加载的插件. 已经完成了 [PluginLoader.load], 但没有 [PluginLoader.enable]
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(PluginMissingDependencyException::class)
    private fun loadAndEnableLoaderProviders(): List<PluginDescriptionWithLoader> {
        val allDescriptions =
            builtInLoaders.listAllPlugins()
                .asSequence()
                .onEach { (loader, descriptions) ->
                    loader as PluginLoader<Plugin, PluginDescription>

                    descriptions.filter { it.kind == PluginKind.LOADER }.sortByDependencies().loadAndEnableAllInOrder()
                }
                .flatMap { it.second.asSequence() }

        return allDescriptions.toList()
    }

    private fun List<PluginLoader<*, *>>.listAllPlugins(): List<Pair<PluginLoader<*, *>, List<PluginDescriptionWithLoader>>> {
        return associateWith { loader -> loader.listPlugins().map { desc -> desc.wrapWith(loader) } }.toList()
    }

    @Throws(PluginMissingDependencyException::class)
    private fun <D : PluginDescription> List<D>.sortByDependencies(): List<D> {
        val resolved = ArrayList<D>(this.size)

        fun D.canBeLoad(): Boolean = this.dependencies.all { it.isOptional || it in resolved }

        fun List<D>.consumeLoadable(): List<D> {
            val (canBeLoad, cannotBeLoad) = this.partition { it.canBeLoad() }
            resolved.addAll(canBeLoad)
            return cannotBeLoad
        }

        fun List<PluginDependency>.filterIsMissing(): List<PluginDependency> =
            this.filterNot { it.isOptional || it in resolved }

        tailrec fun List<D>.doSort() {
            if (this.isEmpty()) return

            val beforeSize = this.size
            this.consumeLoadable().also { resultPlugins ->
                check(resultPlugins.size < beforeSize) {
                    throw PluginMissingDependencyException(resultPlugins.joinToString("\n") { badPlugin ->
                        "Cannot load plugin ${badPlugin.name}, missing dependencies: ${
                            badPlugin.dependencies.filterIsMissing()
                                .joinToString()
                        }"
                    })
                }
            }.doSort()
        }

        this.doSort()
        return resolved
    }

    // endregion
}

internal data class PluginDescriptionWithLoader(
    @JvmField val loader: PluginLoader<*, PluginDescription>, // easier type
    @JvmField val delegate: PluginDescription
) : PluginDescription by delegate

@Suppress("UNCHECKED_CAST")
internal fun <D : PluginDescription> PluginDescription.unwrap(): D =
    if (this is PluginDescriptionWithLoader) this.delegate as D else this as D

@Suppress("UNCHECKED_CAST")
internal fun PluginDescription.wrapWith(loader: PluginLoader<*, *>): PluginDescriptionWithLoader =
    PluginDescriptionWithLoader(
        loader as PluginLoader<*, PluginDescription>, this
    )

internal operator fun List<PluginDescription>.contains(dependency: PluginDependency): Boolean =
    any { it.name == dependency.name }
