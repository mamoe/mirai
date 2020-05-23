package net.mamoe.mirai.console.plugins.builtin

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugins.PluginLoadException
import net.mamoe.mirai.console.plugins.PluginsLoader
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.createInstance

/**
 * 内建的 Jar (JVM) 插件加载器
 */
object JarPluginLoader : AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>("jar"), CoroutineScope {
    private val logger: MiraiLogger by lazy {
        MiraiConsole.newLogger(JarPluginLoader::class.simpleName!!)
    }

    override val coroutineContext: CoroutineContext by lazy {
        MiraiConsole.coroutineContext + SupervisorJob(
            MiraiConsole.coroutineContext[Job]
        ) + CoroutineExceptionHandler { _, throwable ->
            logger.error("Unhandled Jar plugin exception: ${throwable.message}", throwable)
        }
    }
    private val supervisor: Job = coroutineContext[Job]!!

    private val classLoader: PluginsLoader = PluginsLoader(this.javaClass.classLoader)

    init {
        supervisor.invokeOnCompletion {
            classLoader.clear()
        }
    }

    override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription = plugin.description

    override fun Sequence<File>.mapToDescription(): List<JvmPluginDescription> {
        return this.associateWith { URL("jar:${it.absolutePath}!/plugin.yml") }.mapNotNull { (file, url) ->
            kotlin.runCatching {
                url.readText()
            }.fold(
                onSuccess = { yaml ->
                    Yaml.nonStrict.parse(JvmPluginDescription.serializer(), yaml)
                },
                onFailure = {
                    logger.error("Cannot load plugin file ${file.name}", it)
                    null
                }
            )?.also { it._file = file }
        }
    }

    @Throws(PluginLoadException::class)
    override fun load(description: JvmPluginDescription): JvmPlugin = description.runCatching {
        ensureActive()
        val main = classLoader.loadPluginMainClassByJarFile(name, mainClassName, file).kotlin.run {
            objectInstance
                ?: kotlin.runCatching { createInstance() }.getOrNull()
                ?: (java.constructors + java.declaredConstructors)
                    .firstOrNull { it.parameterCount == 0 }
                    ?.apply { kotlin.runCatching { isAccessible = true } }
                    ?.newInstance()
        } ?: error("No Kotlin object or public no-arg constructor found")

        check(main is JvmPlugin) { "The main class of Jar plugin must extend JvmPlugin, recommending JavaPlugin or KotlinPlugin" }

        if (main is JvmPluginImpl) {
            main._description = description
            main.internalOnLoad()
        } else main.onLoad()
        main
    }.getOrElse {
        throw PluginLoadException("Exception while loading ${description.name}", it)
    }

    override fun enable(plugin: JvmPlugin) {
        ensureActive()
        if (plugin is JvmPluginImpl) {
            plugin.internalOnEnable()
        } else plugin.onEnable()
    }

    override fun disable(plugin: JvmPlugin) {
        if (plugin is JvmPluginImpl) {
            plugin.internalOnDisable()
        } else plugin.onDisable()
    }
}