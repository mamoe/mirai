package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.extension.SingletonExtensionPoint
import net.mamoe.mirai.console.permission.ExperimentalPermission
import net.mamoe.mirai.console.permission.PermissionService

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 当插件注册 [PermissionService] 后, 默认会使用插件的 [PermissionService].
 */
@ExperimentalPermission
public interface PermissionServiceProvider : SingletonExtension<PermissionService<*>> {
    public companion object ExtensionPoint :
        AbstractExtensionPoint<PermissionServiceProvider>(PermissionServiceProvider::class),
        SingletonExtensionPoint<PermissionServiceProvider>
}

@ExperimentalPermission
public class PermissionServiceProviderImpl(override val instance: PermissionService<*>) : PermissionServiceProvider

@ExperimentalPermission
public class LazyPermissionServiceProviderImpl(initializer: () -> PermissionService<*>) : PermissionServiceProvider {
    override val instance: PermissionService<*> by lazy(initializer)
}