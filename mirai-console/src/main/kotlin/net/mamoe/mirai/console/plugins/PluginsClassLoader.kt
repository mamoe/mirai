package net.mamoe.mirai.console.plugins

import java.io.File
import java.net.URLClassLoader

internal class PluginsClassLoader(private val parent: ClassLoader)  {

    private var cl : URLClassLoader? = null

    /**
     * 加载多个插件
     */
    fun loadPlugins(pluginsLocation: Map<String, File>) {
        cl = URLClassLoader(
            pluginsLocation.values.map { it.toURI().toURL() }.toTypedArray(),
            parent)
    }

    /**
     * 清除所有插件加载器
     */
    fun clear() {
        cl?.close()
    }

    /**
     * 加载
     */

    fun loadPluginMainClass(name: String) : Class<*>{
        return cl?.loadClass(name) ?: error("PluginsClassLoader has not yet run the loadPlugins func.")
    }
}
