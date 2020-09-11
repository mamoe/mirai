package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.InstanceExtension
import net.mamoe.mirai.console.plugin.PluginLoader

/**
 * 提供扩展 [PluginLoader]
 */
public interface PluginLoaderProvider : InstanceExtension<PluginLoader<*, *>> {
    public companion object ExtensionPoint : AbstractExtensionPoint<PluginLoaderProvider>(PluginLoaderProvider::class)
}