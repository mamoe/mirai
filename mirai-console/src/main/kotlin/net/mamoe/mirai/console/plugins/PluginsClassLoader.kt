package net.mamoe.mirai.console.plugins

import java.io.File
import java.net.URLClassLoader

internal class PluginsClassLoader(private val parent: ClassLoader) {
    private val pluginLoaders = mutableMapOf<String, PluginClassLoader>()

    /**
     * 加载多个插件
     */
    fun loadPlugins(pluginsLocation: Map<String, File>) {
        for ((key, value) in pluginsLocation) {
            pluginLoaders[key] = PluginClassLoader(value, this)
        }
    }

    /**
     * 清除所有插件加载器
     */
    fun clear() {
        pluginLoaders.values.forEach {
            it.close()
        }
        pluginLoaders.clear()
    }

    /**
     * 移除单个插件加载器
     */
    fun remove(pluginName: String): Boolean {
        pluginLoaders[pluginName]?.close() ?: return false
        pluginLoaders.remove(pluginName)
        return true
    }

    fun loadClass(name: String): Class<*>? {
        var c: Class<*>? = null
        // 循环插件classloader loadClass
        pluginLoaders.values.forEach {
            try{
                c = it.loadClass(name)
                return@forEach
            }catch (e : ClassNotFoundException){/*nothing*/}
        }
        if(c == null){
            throw ClassNotFoundException("PluginsClassLoader can't load this class:${name}")
        }
        return c
    }

    fun loadDependClass(name: String): Class<*>? {
        var c: Class<*>? = null

        // 依赖问题先交给MiraiClassLoader来处理
        try {
            c = parent.loadClass(name)
        }catch (e : ClassNotFoundException){/*nothing*/}

        // 然后再交给插件的classloader来加载依赖
        if (c == null) {
            pluginLoaders.values.forEach {
                try {
                    c = it.loadDependClass(name)
                    return@forEach
                }catch (e : ClassNotFoundException){/*nothing*/}
            }
        }
        return c
    }
}

internal class PluginClassLoader(files: File,private val parent: PluginsClassLoader) :
    URLClassLoader(arrayOf((files.toURI().toURL()))) {

    override fun loadClass(name: String): Class<*>? {
        synchronized(getClassLoadingLock(name)) {
            // 看缓存中是否加载过此类
            var c = findLoadedClass(name)
            if (c == null) {
                c = try {
                    // 父类去加载本插件的依赖
                    parent.loadDependClass(name)
                } catch (e: ClassNotFoundException) {
                    // 自己加载本插件，按理说只有本插件的ClassName会到达这里
                    this.findClass(name)//ClassNotFoundException or java.lang.LinkageError
                }
            }
            return c
        }
    }

    fun loadDependClass(name: String): Class<*>? {
        synchronized(getClassLoadingLock(name)) {
            var c = findLoadedClass(name)
            if (c == null) {
                // 加载依赖，没有则丢出异常
                c = this.findClass(name)    // ClassNotFoundException
            }
            return c
        }
    }

}

