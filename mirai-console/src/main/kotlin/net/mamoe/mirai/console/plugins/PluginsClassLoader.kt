package net.mamoe.mirai.console.plugins

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File
import java.io.IOException
import java.net.URLClassLoader

internal class PluginsLoader(private val parentClassLoader: ClassLoader) {
    private val loggerName = "PluginsLoader"
    private val pluginLoaders =  linkedMapOf<String,PluginClassLoader>()
    private val classesCache = mutableMapOf<String, Class<*>>()
    private val logger = SimpleLogger(loggerName) { p, message, e ->
        MiraiConsole.logger(p, "[${loggerName}]", 0, message)
        MiraiConsole.logger(p, "[${loggerName}]", 0, e)
    }

    /**
     * 清除所有插件加载器
     */
    fun clear() {
        val iterator = pluginLoaders.iterator()
        while(iterator.hasNext()){
            val plugin = iterator.next()
            var cl = ""
            try {
                cl = plugin.value.toString()
                plugin.value.close()
                iterator.remove()
            }catch (e: Throwable){
                logger.error("Plugin(${plugin.key}) can't not close its ClassLoader(${cl})",e)
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

    fun loadPluginMainClassByJarFile(pluginName:String, mainClass: String, jarFile:File): Class<*> {
        try {
            if(!pluginLoaders.containsKey(pluginName)){
                pluginLoaders[pluginName] = PluginClassLoader(pluginName,jarFile, this, parentClassLoader)
            }
            return Class.forName(mainClass,true,pluginLoaders[pluginName])
        }catch (e : ClassNotFoundException){
            throw ClassNotFoundException("PluginsClassLoader(${pluginName}) can't load this pluginMainClass:${mainClass}",e)
        }catch (e : Throwable){
            throw Throwable("init or load class error",e)
        }
    }

    /**
     *  尝试加载插件的依赖,无则返回null
     */
    fun loadDependentClass(name: String): Class<*>? {
        var c: Class<*>? = null
        // 尝试从缓存中读取
        if (classesCache.containsKey(name)) {
            c = classesCache[name]
        }
        // 然后再交给插件的classloader来加载依赖
        if (c == null) {
            pluginLoaders.values.forEach {
                try {
                    c = it.findClass(name, false)
                    return@forEach
                } catch (e: ClassNotFoundException) {/*nothing*/
                }
            }
        }
        return c
    }

    fun addClassCache(name: String, clz: Class<*>) {
        synchronized(classesCache) {
            if (!classesCache.containsKey(name)) {
                classesCache[name] = clz
            }
        }
    }
}

internal class PluginClassLoader(private val pluginName: String,files: File, private val pluginsLoader: PluginsLoader, parent: ClassLoader) :
    URLClassLoader(arrayOf((files.toURI().toURL())), parent) {
    private val classesCache = mutableMapOf<String, Class<*>?>()

    override fun findClass(name: String): Class<*>? {
        return this.findClass(name, true)
    }

    fun findClass(name: String, isSearchDependent: Boolean): Class<*>? {
        var clz: Class<*>? = null
        // 缓存中找
        if (classesCache.containsKey(name)) {

            return classesCache[name]
        }
        // 是否寻找依赖
        if (isSearchDependent) {
            clz = pluginsLoader.loadDependentClass(name)
        }
        // 交给super去findClass
        if (clz == null) {
            clz = super.findClass(name)
        }
        // 加入缓存
        if (clz != null) {
            pluginsLoader.addClassCache(name, clz)
        }
        // 加入缓存
        synchronized(classesCache) {
            classesCache[name] = clz
        }
        return clz
    }

    override fun close() {
        super.close()
        classesCache.clear()
    }
}