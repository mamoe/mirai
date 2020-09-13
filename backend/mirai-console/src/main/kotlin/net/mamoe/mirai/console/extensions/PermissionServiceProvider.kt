/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.extension.SingletonExtensionPoint
import net.mamoe.mirai.console.permission.PermissionService

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 当插件注册 [PermissionService] 后, 默认会使用插件的 [PermissionService].
 */
public interface PermissionServiceProvider : SingletonExtension<PermissionService<*>> {
    public companion object ExtensionPoint :
        AbstractExtensionPoint<PermissionServiceProvider>(PermissionServiceProvider::class),
        SingletonExtensionPoint<PermissionServiceProvider>
}

/**
 * @see PermissionServiceProvider
 */
public class PermissionServiceProviderImpl(override val instance: PermissionService<*>) : PermissionServiceProvider

/**
 * @see PermissionServiceProvider
 */
public class LazyPermissionServiceProviderImpl(initializer: () -> PermissionService<*>) : PermissionServiceProvider {
    override val instance: PermissionService<*> by lazy(initializer)
}