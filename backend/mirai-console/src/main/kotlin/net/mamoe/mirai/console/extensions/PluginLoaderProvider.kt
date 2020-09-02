package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.plugin.PluginLoader

/**
 * 提供扩展 [PluginLoader]
 */
public interface PluginLoaderProvider {
    public val instance: PluginLoader<*, *>

    public companion object ExtensionPoint : AbstractExtensionPoint<PluginLoaderProvider>(PluginLoaderProvider::class)
}