package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.extension.SingletonExtensionPoint
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.description.PluginKind

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 此扩展可由 [PluginKind.LOADER] 和 [PluginKind.HIGH_PRIORITY_EXTENSIONS] 插件提供
 */
@ExperimentalPermission
public interface PermissionServiceProvider : SingletonExtension<PermissionService<*>> {
    public companion object ExtensionPoint :
        AbstractExtensionPoint<PermissionServiceProvider>(PermissionServiceProvider::class),
        SingletonExtensionPoint<PermissionServiceProvider>
}