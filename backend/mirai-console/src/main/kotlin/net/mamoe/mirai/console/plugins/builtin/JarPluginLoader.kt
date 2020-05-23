package net.mamoe.mirai.console.plugins.builtin

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugins.PluginLoadException
import net.mamoe.mirai.console.plugins.PluginsLoader
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.createInstance

/**
 * 内建的 Jar (JVM) 插件加载器
 */
object JarPluginLoader : AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>("jar"),
    CoroutineScope {
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

    private val classLoader: PluginsLoader =
        PluginsLoader(this.javaClass.classLoader)

    override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription = plugin.description

    override fun Sequence<File>.mapToDescription(): List<JvmPluginDescription> {
        TODO(
            """
            CHECK IS JAR FILE AND CAN BE READ
            READ JAR FILE, EXTRACT PLUGIN DESCRIPTION
            SET JvmPluginDescription._file
            RETURN PLUGIN 
        """.trimIndent()
        )
    }

    @Throws(PluginLoadException::class)
    override fun load(description: JvmPluginDescription): JvmPlugin = description.runCatching {
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
        }

        TODO(
            """
            FIND PLUGIN MAIN, THEN LOAD
            SET JvmPluginImpl._description
            SET JvmPluginImpl._intrinsicCoroutineContext
        """.trimIndent()
        )
        // no need to check dependencies
    }.getOrElse {
        throw PluginLoadException(
            "Exception while loading ${description.name}",
            it
        )
    }

    override fun enable(plugin: JvmPlugin) = plugin.onEnable()
    override fun disable(plugin: JvmPlugin) = plugin.onDisable()
}