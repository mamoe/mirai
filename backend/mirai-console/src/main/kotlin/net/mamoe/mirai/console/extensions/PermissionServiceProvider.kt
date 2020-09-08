package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermissionService

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 此扩展
 */
@ExperimentalPermission
public interface PermissionServiceProvider : SingletonExtension<PermissionService<*>> {
    public companion object ExtensionPoint :
        AbstractExtensionPoint<PermissionServiceProvider>(PermissionServiceProvider::class)
}