/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class, MiraiInternalApi::class)

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.util.PluginServiceHelper.findServices
import net.mamoe.mirai.console.internal.util.PluginServiceHelper.loadAllServices
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.dependencies
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.plugin.loader.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoadException
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.*
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

internal val JvmPluginLoader.implOrNull get() = this.castOrNull<BuiltInJvmPluginLoaderImpl>()

internal class BuiltInJvmPluginLoaderImpl(
    parentCoroutineContext: CoroutineContext
) : AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>(".jar"),
    CoroutineScope by parentCoroutineContext.childScope("JvmPluginLoader", CoroutineExceptionHandler { _, throwable ->
        logger.error("Unhandled Jar plugin exception: ${throwable.message}", throwable)
    }),
    JvmPluginLoader {

    companion object {
        internal val logger: MiraiLogger =
            MiraiLogger.Factory.create(JvmPluginLoader::class)
    }

    private fun pluginsFilesSequence(
        files: Sequence<File> = PluginManager.pluginsFolder.listFiles().orEmpty().asSequence()
    ): Sequence<File> {
        val raw = files
            .filter { it.isFile && it.name.endsWith(fileSuffix, ignoreCase = true) }
            .toMutableList()

        val mirai2List = raw.filter { it.name.endsWith(".mirai2.jar", ignoreCase = true) }
        for (mirai2Plugin in mirai2List) {
            val name = mirai2Plugin.name.substringBeforeLast('.').substringBeforeLast('.') // without ext.
            raw.removeAll {
                it !== mirai2Plugin && it.name.substringBeforeLast('.').substringBeforeLast('.') == name
            } // remove those with .mirai.jar
        }

        return raw.asSequence()
    }

    override fun listPlugins(): List<JvmPlugin> {
        return pluginsFilesSequence().extractPlugins()
    }

    override val configStorage: PluginDataStorage
        get() = MiraiConsoleImplementation.getInstance().configStorageForJvmPluginLoader

    override val dataStorage: PluginDataStorage
        get() = MiraiConsoleImplementation.getInstance().dataStorageForJvmPluginLoader


    private val jvmPluginLoadingCtx: JvmPluginsLoadingCtx by lazy {
        val legacyCompatibilityLayerClassLoader = LegacyCompatibilityLayerClassLoader.newInstance(
            BuiltInJvmPluginLoaderImpl::class.java.classLoader,
        )

        val classLoader = DynLibClassLoader.newInstance(
            legacyCompatibilityLayerClassLoader, "GlobalShared", "global-shared"
        )
        val ctx = JvmPluginsLoadingCtx(
            legacyCompatibilityLayerClassLoader,
            classLoader,
            mutableListOf(),
            JvmPluginDependencyDownloader(logger),
        )
        logger.debug { "Downloading legacy compatibility modules....." }
        ctx.downloader.resolveDependencies(
            sequenceOf(
                "client-core",
                "client-core-jvm",
                "client-okhttp",
                "utils",
                "utils-jvm",
            ).map { "io.ktor:ktor-$it:1.6.8" }.asIterable()
        ).let { rsp ->
            rsp.artifactResults.forEach {
                legacyCompatibilityLayerClassLoader.addLib(it.artifact.file)
            }
            if (logger.isVerboseEnabled) {
                logger.verbose("Legacy compatibility modules:")
                rsp.artifactResults.forEach { art ->
                    logger.verbose(" `- ${art.artifact}  -> ${art.artifact.file}")
                }
            }
        }

        logger.verbose { "Plugin shared libraries: " + PluginManager.pluginSharedLibrariesFolder }
        PluginManager.pluginSharedLibrariesFolder.listFiles()?.asSequence().orEmpty()
            .onEach { logger.debug { "Peek $it in shared libraries" } }
            .filter { file ->
                if (file.isDirectory) {
                    return@filter true
                }
                if (!file.exists()) {
                    logger.debug { "Skipped $file because file not exists" }
                    return@filter false
                }
                if (file.isFile) {
                    if (file.extension == "jar") {
                        return@filter true
                    }
                    logger.debug { "Skipped $file because extension <${file.extension}> != jar" }
                    return@filter false
                }
                logger.debug { "Skipped $file because unknown error" }
                return@filter false
            }
            .filter { it.isDirectory || (it.isFile && it.extension == "jar") }
            .forEach { pt ->
                classLoader.addLib(pt)
                logger.debug { "Linked static shared library: $pt" }
            }
        val libraries = PluginManager.pluginSharedLibrariesFolder.resolve("libraries.txt")
        if (libraries.isFile) {
            logger.verbose { "Linking static shared libraries...." }
            val libs = libraries.useLines { lines ->
                lines.filter { it.isNotBlank() }
                    .filterNot { it.startsWith("#") }
                    .onEach { logger.verbose { "static lib queued: $it" } }
                    .toMutableList()
            }
            val staticLibs = ctx.downloader.resolveDependencies(libs)
            staticLibs.artifactResults.forEach { artifactResult ->
                if (artifactResult.isResolved) {
                    ctx.sharedLibrariesLoader.addLib(artifactResult.artifact.file)
                    ctx.sharedLibrariesDependencies.add(artifactResult.artifact.depId())
                    logger.debug { "Linked static shared library: ${artifactResult.artifact}" }
                    logger.verbose { "Linked static shared library: ${artifactResult.artifact.file}" }
                }
            }
        } else {
            libraries.createNewFile()
        }
        ctx
    }

    override val classLoaders: MutableList<JvmPluginClassLoaderN> get() = jvmPluginLoadingCtx.pluginClassLoaders

    override fun findLoadedClass(name: String): Class<*>? {
        return classLoaders.firstNotNullOfOrNull { it.loadedClass(name) }
    }


    override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription = plugin.description

    private val pluginFileToInstanceMap: MutableMap<File, JvmPlugin> = ConcurrentHashMap()

    override fun Sequence<File>.extractPlugins(): List<JvmPlugin> {
        ensureActive()

        fun Sequence<Map.Entry<File, JvmPluginClassLoaderN>>.initialize(): Sequence<Map.Entry<File, JvmPluginClassLoaderN>> {
            return onEach { (_, pluginClassLoader) ->
                val exportManagers = pluginClassLoader.findServices(
                    ExportManager::class
                ).loadAllServices()
                if (exportManagers.isEmpty()) {
                    val rules = pluginClassLoader.getResourceAsStream("export-rules.txt")
                    if (rules == null)
                        pluginClassLoader.declaredFilter = StandardExportManagers.AllExported
                    else rules.bufferedReader(Charsets.UTF_8).useLines {
                        pluginClassLoader.declaredFilter = ExportManagerImpl.parse(it.iterator())
                    }
                } else {
                    pluginClassLoader.declaredFilter = exportManagers[0]
                }
            }
        }

        fun Sequence<Map.Entry<File, JvmPluginClassLoaderN>>.findAllInstances(): Sequence<Map.Entry<File, JvmPlugin>> {
            return map { (f, pluginClassLoader) ->
                f to pluginClassLoader.findServices(
                    JvmPlugin::class,
                    KotlinPlugin::class,
                    JavaPlugin::class
                ).loadAllServices().also { plugins ->
                    plugins.firstOrNull()?.logger?.let { pluginClassLoader.linkedLogger = it }
                }
            }.flatMap { (f, list) ->

                list.associateBy { f }.asSequence()
            }
        }

        fun Map.Entry<File, JvmPluginClassLoaderN>.loadWithoutPluginDescription(): Sequence<Pair<File, JvmPlugin>> {
            return sequenceOf(this).initialize().findAllInstances().map { (k, v) -> k to v }
        }

        fun Map.Entry<File, JvmPluginClassLoaderN>.loadWithPluginDescription(description: JvmPluginDescription): Sequence<Pair<File, JvmPlugin>> {
            val pluginClassLoader = this.value
            val pluginFile = this.key
            pluginClassLoader.pluginDescriptionFromPluginResource = description

            val pendingPlugin = object : NotYetLoadedJvmPlugin(
                description = description,
                classLoaderN = pluginClassLoader,
            ) {
                private val plugin by lazy {
                    val services = pluginClassLoader.findServices(
                        JvmPlugin::class,
                        KotlinPlugin::class,
                        JavaPlugin::class
                    ).loadAllServices()
                    if (services.isEmpty()) {
                        error("No plugin instance found in $pluginFile")
                    }
                    if (services.size > 1) {
                        error(
                            "Only one plugin can exist at the same time when using plugin.yml:\n\nPlugins found:\n" + services.joinToString(
                                separator = "\n"
                            ) { it.javaClass.name + " (from " + it.javaClass.classLoader + ")" }
                        )
                    }

                    return@lazy services[0]
                }

                override fun resolve(): JvmPlugin = plugin
            }
            pluginClassLoader.linkedLogger = pendingPlugin.logger


            return sequenceOf(pluginFile to pendingPlugin)
        }

        val filePlugins = this.filterNot {
            pluginFileToInstanceMap.containsKey(it)
        }.associateWith {
            JvmPluginClassLoaderN.newLoader(it, jvmPluginLoadingCtx)
        }.onEach { (_, classLoader) ->
            classLoaders.add(classLoader)
        }.asSequence().flatMap { entry ->
            val (file, pluginClassLoader) = entry

            val pluginDescriptionDefine = pluginClassLoader.getResourceAsStream("plugin.yml")
            if (pluginDescriptionDefine == null) {
                entry.loadWithoutPluginDescription()
            } else {
                val desc = kotlin.runCatching {
                    pluginDescriptionDefine.bufferedReader().use { resource ->
                        Yaml.decodeFromString(
                            SimpleJvmPluginDescription.SerialData.serializer(),
                            resource.readText()
                        ).toJvmPluginDescription()
                    }
                }.onFailure { err ->
                    throw PluginLoadException("Invalid plugin.yml in " + file.absolutePath, err)
                }.getOrThrow()

                entry.loadWithPluginDescription(desc)
            }
        }.onEach {
            logger.verbose { "Successfully initialized JvmPlugin ${it.second}." }
        }.onEach { (file, plugin) ->
            pluginFileToInstanceMap[file] = plugin
        }

        return filePlugins.toSet().map { it.second }
    }

    private val loadedPlugins = ConcurrentHashMap<String, JvmPlugin>()

    private fun Path.moveNameFolder(plugin: JvmPlugin) {
        val nameFolder = this.resolve(plugin.description.name).toFile()
        if (plugin.description.name != plugin.description.id && nameFolder.exists()) {
            // need move
            val idFolder = this.resolve(plugin.description.id).toFile()
            val moveDescription =
                "移动 ${plugin.description.smartToString()} 的数据文件目录(${nameFolder.path})到 ${idFolder.path}"
            if (idFolder.exists()) {
                if (idFolder.listFiles()?.size != 0) {
                    logger.error("$moveDescription 失败, 原因:数据文件目录(${idFolder.path})被占用")
                    logger.error("Mirai Console 将自动关闭, 请删除或移动该目录后再启动")
                    MiraiConsole.job.cancel()
                } else
                    idFolder.delete()
            }
            kotlin.runCatching {
                logger.info(moveDescription)
                if (!nameFolder.renameTo(idFolder)) {
                    logger.error("$moveDescription 失败")
                    logger.error("Mirai Console 将自动关闭, 请手动移动该文件夹后再启动")
                    MiraiConsole.job.cancel()
                }
            }.onFailure {
                logger.error("$moveDescription 失败, 原因:\n", it)
                logger.error("Mirai Console 将自动关闭, 请解决该错误后再启动")
                MiraiConsole.job.cancel()
            }
            logger.info("$moveDescription 完成")
        }
    }

    @Throws(PluginLoadException::class)
    override fun load(plugin: JvmPlugin) {
        ensureActive()

        if (loadedPlugins.put(plugin.id, plugin) != null) {
            error("Plugin '${plugin.id}' is already loaded and cannot be reloaded.")
        }
        logger.verbose { "Loading plugin ${plugin.description.smartToString()}" }
        runCatching {
            // move nameFolder in config and data to idFolder
            PluginManager.pluginsDataPath.moveNameFolder(plugin)
            PluginManager.pluginsConfigPath.moveNameFolder(plugin)

            check(plugin is JvmPluginInternal || plugin is NotYetLoadedJvmPlugin) {
                "A JvmPlugin must extend AbstractJvmPlugin to be loaded by JvmPluginLoader.BuiltIn"
            }


            // region Link dependencies
            when (plugin) {
                is NotYetLoadedJvmPlugin -> plugin.classLoaderN
                else -> plugin.javaClass.classLoader
            }.safeCast<JvmPluginClassLoaderN>()?.let { jvmPluginClassLoaderN ->
                // Link plugin dependencies
                plugin.description.dependencies.asSequence().mapNotNull { dependency ->
                    plugin.logger.verbose { "Linking dependency: ${dependency.id}" }
                    PluginManager.plugins.firstOrNull { it.id == dependency.id }
                }.mapNotNull { it.javaClass.classLoader.safeCast<JvmPluginClassLoaderN>() }.forEach { dependency ->
                    plugin.logger.debug { "Linked  dependency: $dependency" }
                    jvmPluginClassLoaderN.dependencies.add(dependency)
                    jvmPluginClassLoaderN.pluginSharedCL.dependencies.cast<MutableList<DynLibClassLoader>>().add(
                        dependency.pluginSharedCL
                    )
                }
                jvmPluginClassLoaderN.linkPluginLibraries(plugin.logger)
            }

            val realPlugin = when (plugin) {
                is NotYetLoadedJvmPlugin -> plugin.resolve().also { realPlugin ->
                    check(plugin.description === realPlugin.description) {
                        "A JvmPlugin loaded by plugin.yml must has same description reference"
                    }
                }
                else -> plugin
            }

            check(realPlugin is JvmPluginInternal) { "A JvmPlugin must extend AbstractJvmPlugin to be loaded by JvmPluginLoader.BuiltIn" }
            // endregion
            realPlugin.internalOnLoad()
        }.getOrElse {
            throw PluginLoadException("Exception while loading ${plugin.description.smartToString()}", it)
        }
    }

    override fun enable(plugin: JvmPlugin) {
        if (plugin.isEnabled) error("Plugin '${plugin.name}' is already enabled and cannot be re-enabled.")
        ensureActive()
        runCatching {
            logger.verbose { "Enabling plugin ${plugin.description.smartToString()}" }

            val loadedPlugins = PluginManager.plugins
            val failedDependencies = plugin.dependencies.asSequence().mapNotNull { dep ->
                loadedPlugins.firstOrNull { it.id == dep.id }
            }.filterNot { it.isEnabled }.toList()
            if (failedDependencies.isNotEmpty()) {
                logger.error("Failed to enable '${plugin.name}' because dependencies not enabled: " + failedDependencies.joinToString { "'${it.name}'" })
                return
            }

            if (plugin is JvmPluginInternal) {
                plugin.internalOnEnable()
            } else plugin.onEnable()

            // Extra space for logging align
            logger.verbose { "Enabled  plugin ${plugin.description.smartToString()}" }
        }.getOrElse {
            throw PluginLoadException("Exception while enabling ${plugin.description.name}", it)
        }
    }

    override fun disable(plugin: JvmPlugin) {
        if (!plugin.isEnabled) error("Plugin '${plugin.name}' is not already disabled and cannot be re-disabled.")

        if (MiraiConsole.isActive)
            ensureActive()

        if (plugin is JvmPluginInternal) {
            plugin.internalOnDisable()
        } else plugin.onDisable()
    }
}
