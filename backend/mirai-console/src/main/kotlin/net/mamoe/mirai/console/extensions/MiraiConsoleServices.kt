package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.internal.util.PluginServiceHelper
import kotlin.reflect.KClass

// not public now
internal object ServiceContainer {
    private val instances: MutableMap<KClass<*>, List<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @Synchronized
    fun <T : Any> getService(clazz: KClass<T>): List<T> {
        instances[clazz]?.let { return it as List<T> }
        PluginServiceHelper.loadAllServicesFromMemoryAndPluginClassLoaders(clazz).let {
            instances[clazz] = it
            return it
        }
    }

    inline fun <reified T : Any> getService(): List<T> = getService(T::class)
}