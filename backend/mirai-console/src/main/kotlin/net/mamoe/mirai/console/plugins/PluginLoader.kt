package net.mamoe.mirai.console.plugins

/**
 * 插件加载器
 *
 * @see JarPluginLoader 内建的 Jar (JVM) 插件加载器.
 */
interface PluginLoader<P : Plugin> {
    val list: List<P>

    fun loadAll() = list.forEach(::load)
    fun enableAll() = list.forEach(::enable)
    fun unloadAll() = list.forEach(::unload)
    fun reloadAll() = list.forEach(::reload)

    val isUnloadSupported: Boolean
        get() = false

    fun load(plugin: P)
    fun enable(plugin: P)
    fun unload(plugin: P) {
        error("NotImplemented")
    }

    fun reload(plugin: P) {
        unload(plugin)
        load(plugin)
    }
}
