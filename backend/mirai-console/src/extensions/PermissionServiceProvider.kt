/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractSingletonExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * [权限服务][PermissionService] 提供器.
 *
 * 当插件注册 [PermissionService] 后, 默认会使用插件的 [PermissionService].
 */
public interface PermissionServiceProvider : SingletonExtension<PermissionService<*>> {
    public companion object ExtensionPoint :
        AbstractSingletonExtensionPoint<PermissionServiceProvider, PermissionService<*>>(PermissionServiceProvider::class, BuiltInPermissionService) {
        internal var permissionServiceOk = false

        @ConsoleExperimentalApi
        public val providerPlugin: Plugin? by lazy {
            GlobalComponentStorage.run {
                val instance = PermissionService.INSTANCE
                if (instance is BuiltInPermissionService) return@lazy null
                PermissionServiceProvider.getExtensions().find { it.extension.instance === instance }?.plugin
            }
        }

        @ConsoleExperimentalApi
        override val selectedInstance: PermissionService<*>
            get() {
                if (!permissionServiceOk) {
                    error("PermissionService not yet loaded")
                }
                return super.selectedInstance
            }
    }
}

/**
 * @see PermissionServiceProvider
 */
public class PermissionServiceProviderImpl(override val instance: PermissionService<*>) : PermissionServiceProvider

/**
 * @see PermissionServiceProvider
 */
public class PermissionServiceProviderImplLazy(initializer: () -> PermissionService<*>) : PermissionServiceProvider {
    override val instance: PermissionService<*> by lazy(initializer)
}
