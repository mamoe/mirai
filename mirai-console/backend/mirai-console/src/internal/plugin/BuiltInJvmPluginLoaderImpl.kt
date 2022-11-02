/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
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
import net.mamoe.mirai.utils.*
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

    fun pluginsFilesSequence(
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


    internal val jvmPluginLoadingCtx: JvmPluginsLoadingCtx by lazy {
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


    @Suppress("EXTENSION_SHADOWED_BY_MEMBER") // doesn't matter
    override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription = plugin.description

    private val pluginFileToInstanceMap: MutableMap<File, JvmPlugin> = ConcurrentHashMap()

    override fun Sequence<File>.extractPlugins(): List<JvmPlugin> {
        ensureActive()

        fun Sequence<Map.Entry<File, JvmPluginClassLoaderN>>.findAllInstances(): Sequence<Map.Entry<File, JvmPlugin>> {
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
            }.map { (f, pluginClassLoader) ->
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

        val filePlugins = this.filterNot {
            pluginFileToInstanceMap.containsKey(it)
        }.associateWith {
            JvmPluginClassLoaderN.newLoader(it, jvmPluginLoadingCtx)
        }.onEach { (_, classLoader) ->
            classLoaders.add(classLoader)
        }.asSequence().findAllInstances().onEach {
            //logger.verbose { "Successfully initialized JvmPlugin ${loaded}." }
        }.onEach { (file, plugin) ->
            pluginFileToInstanceMap[file] = plugin
        } + pluginFileToInstanceMap.asSequence()

        return filePlugins.toSet().map { it.value }
    }

    private val loadedPlugins = ConcurrentHashMap<JvmPlugin, Unit>()

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

        if (loadedPlugins.put(plugin, Unit) != null) {
            error("Plugin '${plugin.name}' is already loaded and cannot be reloaded.")
        }
        logger.verbose { "Loading plugin ${plugin.description.smartToString()}" }
        runCatching {
            // move nameFolder in config and data to idFolder
            PluginManager.pluginsDataPath.moveNameFolder(plugin)
            PluginManager.pluginsConfigPath.moveNameFolder(plugin)
            check(plugin is JvmPluginInternal) { "A JvmPlugin must extend AbstractJvmPlugin to be loaded by JvmPluginLoader.BuiltIn" }
            // region Link dependencies
            plugin.javaClass.classLoader.safeCast<JvmPluginClassLoaderN>()?.let { jvmPluginClassLoaderN ->
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
            // endregion
            plugin.internalOnLoad()
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
