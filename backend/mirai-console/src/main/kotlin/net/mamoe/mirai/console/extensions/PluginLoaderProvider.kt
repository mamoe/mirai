package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.InstanceExtension
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.description.PluginLoadPriority

/**
 * 提供扩展 [PluginLoader]
 *
 * 此扩展可由 [PluginLoadPriority.BEFORE_EXTENSIONS] 插件提供
 */
public interface PluginLoaderProvider : InstanceExtension<PluginLoader<*, *>> {
    public companion object ExtensionPoint : AbstractExtensionPoint<PluginLoaderProvider>(PluginLoaderProvider::class)
}