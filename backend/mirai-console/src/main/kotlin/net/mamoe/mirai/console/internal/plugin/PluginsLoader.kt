/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.MiraiConsole
import java.io.File
import java.net.URLClassLoader

internal class PluginsLoader(private val parentClassLoader: ClassLoader) {
    private val loggerName = "PluginsLoader"
    private val pluginLoaders = linkedMapOf<String, PluginClassLoader>()
    private val classesCache = mutableMapOf<String, Class<*>>()
    private val logger = MiraiConsole.newLogger(loggerName)

    /**
     * 清除所有插件加载器
     */
    fun clear() {
        val iterator = pluginLoaders.iterator()
        while (iterator.hasNext()) {
            val plugin = iterator.next()
            var cl = ""
            try {
                cl = plugin.value.toString()
                plugin.value.close()
                iterator.remove()
            } catch (e: Throwable) {
                logger.error("Plugin(${plugin.key}) can't not close its ClassLoader(${cl})", e)
            }
        }
        classesCache.clear()
    }

    /**
     * 移除单个插件加载器
     */
    fun remove(pluginName: String): Boolean {
        pluginLoaders[pluginName]?.close() ?: return false
        pluginLoaders.remove(pluginName)
        return true
    }

    fun loadPluginMainClassByJarFile(pluginName: String, mainClass: String, jarFile: File): Class<*> {
        try {
            if (!pluginLoaders.containsKey(pluginName)) {
                pluginLoaders[pluginName] =
                    PluginClassLoader(
                        jarFile,
                        this,
                        parentClassLoader
                    )
            }
            return pluginLoaders[pluginName]!!.loadClass(mainClass)
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException(
                "PluginsClassLoader(${pluginName}) can't load this pluginMainClass:${mainClass}",
                e
            )
        } catch (e: Throwable) {
            throw Throwable("init or load class error", e)
        }
    }

    /**
     *  尝试加载插件的依赖,无则返回null
     */
    fun findClassByName(name: String): Class<*>? {
        return classesCache[name] ?: pluginLoaders.values.asSequence().mapNotNull {
            kotlin.runCatching {
                it.findClass(name, false)
            }.getOrNull()
        }.firstOrNull()
    }

    fun addClassCache(name: String, clz: Class<*>) {
        synchronized(classesCache) {
            if (!classesCache.containsKey(name)) {
                classesCache[name] = clz
            }
        }
    }
}


/**
 * A Adapted URL Class Loader that supports Android and JVM for single URL(File) Class Load
 */

internal open class AdaptiveURLClassLoader(file: File, parent: ClassLoader) : ClassLoader() {

    private val internalClassLoader: ClassLoader by lazy {
        kotlin.runCatching {
            val loaderClass = Class.forName("dalvik.system.PathClassLoader")
            loaderClass.getConstructor(String::class.java, ClassLoader::class.java)
                .newInstance(file.absolutePath, parent) as ClassLoader
        }.getOrElse {
            URLClassLoader(arrayOf((file.toURI().toURL())), parent)
        }
    }

    override fun loadClass(name: String?): Class<*> {
        return internalClassLoader.loadClass(name)
    }


    private val internalClassCache = mutableMapOf<String, Class<*>>()

    internal val classesCache: Map<String, Class<*>>
        get() = internalClassCache

    internal fun addClassCache(string: String, clazz: Class<*>) {
        synchronized(internalClassCache) {
            internalClassCache[string] = clazz
        }
    }


    fun close() {
        if (internalClassLoader is URLClassLoader) {
            (internalClassLoader as URLClassLoader).close()
        }
        internalClassCache.clear()
    }

}

internal class PluginClassLoader(
    file: File,
    private val pluginsLoader: PluginsLoader,
    parent: ClassLoader
) : AdaptiveURLClassLoader(file, parent) {

    override fun findClass(name: String): Class<*> {
        return findClass(name, true)
    }

    fun findClass(name: String, global: Boolean = true): Class<*> {
        return classesCache[name] ?: kotlin.run {
            var clazz: Class<*>? = null
            if (global) {
                clazz = pluginsLoader.findClassByName(name)
            }
            if (clazz == null) {
                clazz = loadClass(name)//这里应该是find, 如果不行就要改
            }
            pluginsLoader.addClassCache(name, clazz)
            this.addClassCache(name, clazz)
            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
            clazz!! // compiler bug
        }
    }
}
