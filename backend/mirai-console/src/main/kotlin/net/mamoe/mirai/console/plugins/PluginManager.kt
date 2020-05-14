package net.mamoe.mirai.console.plugins


object PluginManager {
    private val _loaders: MutableSet<PluginLoader<*>> = mutableSetOf()

    val loaders: Set<PluginLoader<*>> get() = _loaders

    fun registerPluginLoader(loader: PluginLoader<*>) {
        _loaders.add(loader)
    }

    fun unregisterPluginLoader(loader: PluginLoader<*>) {
        _loaders.remove(loader)
    }

    fun loadPlugins() {
        loaders.forEach(PluginLoader<*>::loadAll)
    }

    fun enablePlugins() {
        loaders.forEach(PluginLoader<*>::enableAll)
    }
}