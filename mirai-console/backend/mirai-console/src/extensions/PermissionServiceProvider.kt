/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractInstanceExtensionPoint
import net.mamoe.mirai.console.extension.InstanceExtension
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.utils.DeprecatedSinceMirai

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 当插件注册 [PermissionService] 后, 默认会使用插件的 [PermissionService].
 */
public interface PermissionServiceProvider : InstanceExtension<PermissionService<*>> {

    public companion object ExtensionPoint :
        AbstractInstanceExtensionPoint<PermissionServiceProvider, PermissionService<*>>(PermissionServiceProvider::class)
    // ! BREAKING CHANGE MADE IN 2.11: supertype changed from AbstractSingletonExtensionPoint to AbstractInstanceExtensionPoint
}

/**
 * @see PermissionServiceProvider
 */
@Deprecated("Please implement your own PermissionServiceProvider.")
@DeprecatedSinceMirai(warningSince = "2.11") // for hidden.
public class PermissionServiceProviderImpl(override val instance: PermissionService<*>) : PermissionServiceProvider

/**
 * @see PermissionServiceProvider
 */
@Deprecated("Please implement your own PermissionServiceProvider.")
@DeprecatedSinceMirai(warningSince = "2.11") // for hidden.
public class PermissionServiceProviderImplLazy(initializer: () -> PermissionService<*>) : PermissionServiceProvider {
    override val instance: PermissionService<*> by lazy(initializer)
}
