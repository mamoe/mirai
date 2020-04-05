package net.mamoe.mirai.console.plugins

import java.io.File
import java.net.URLClassLoader

class PluginsClassLoader(parent: ClassLoader) : ClassLoader(parent) {
    private val pluginLoaders = mutableMapOf<String,PluginClassLoader>()

    /**
     * 加载多个插件
     */
    fun loadPlugins(pluginsLocation: Map<String, File>) {
        for((key, value) in pluginsLocation){
            pluginLoaders[key] = PluginClassLoader(value,this)
        }
    }


    /**
     * 清除所有插件加载器
     */
    fun clear() {
        pluginLoaders.values.map {
            it.close()
        }
        pluginLoaders.clear()
    }

    /**
     * 移除单个插件加载器
     */
    fun remove(pluginName: String){
        pluginLoaders[pluginName]!!.close()
        pluginLoaders.remove(pluginName)
    }


    override fun loadClass(name: String): Class<*>? {
        var c : Class<*>? = null
        // 循环插件classloader loadClass
        pluginLoaders.map {
            try {
                c = it.value.loadClass(name)
                return@map
            }catch (e: Throwable){
            }
        }
        // 如果为null，交给mirai的classloader进行加载
        if(c==null){
            c = parent.loadClass(name) // 如果无法加载这个类，这里会抛异常
        }
        return c
    }

    fun loadDependClass(name: String) : Class<*>? {
        var c : Class<*>? = null
        // 依赖问题先交给mirai ClassLoader来处理
         try {
            c = parent.loadClass(name)
         }catch (e : Throwable){
         }
        // 如果mirai加载不了依赖则交给插件的classloader进行加载
        if(c==null){
            pluginLoaders.map {
                try {
                    c = it.value.loadDependClass(name)
                    return@map
                }catch (e: Throwable){
                }

            }
        }
        return c
    }




}

class PluginClassLoader(files: File, parent: PluginsClassLoader?) :
    URLClassLoader(arrayOf((files.toURI().toURL())),parent) {

    override fun loadClass(name: String): Class<*>? {
        synchronized(getClassLoadingLock(name)) {
            // 看缓存中是否加载过此类
            var c = findLoadedClass(name)
            if (c == null) {
                c = try {
                    // 自己加载
                    this.findClass(name)        //ClassNotFoundException
                } catch (e: ClassNotFoundException) {
                    // 交给父类去加载依赖
                    (this.parent as PluginsClassLoader).loadDependClass(name)
                }
            }
            return c
        }
    }

    fun loadDependClass(name: String): Class<*>? {
        synchronized(getClassLoadingLock(name)) {
            var c = findLoadedClass(name)
            if (c == null) {
                // 加载依赖类，没有返回null
                c = this.findClass(name)    // 这里会丢ClassNotFoundException
            }
            return c
        }
    }

}

