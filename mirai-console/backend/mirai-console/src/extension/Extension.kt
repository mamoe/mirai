/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector.ExtensionPoint.selectSingleton
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad

/**
 * 表示一个扩展.
 *
 * ### 获取扩展
 * Console 不允许插件获取自己或其他插件注册的扩展
 *
 * ### 注册扩展
 * 插件仅能在 [JvmPlugin.onLoad] 阶段注册扩展
 *
 * ```kotlin
 * object MyPlugin : KotlinPlugin( /* ... */ ) {
 *     fun PluginComponentStorage.onLoad() {
 *         contributePermissionService { /* ... */ }
 *         contributePluginLoader { /* ... */ }
 *         contribute(ExtensionPoint) { /* ... */ }
 *     }
 * }
 * ```
 *
 * @see ComponentStorage
 */
public interface Extension

/**
 * 增加一些函数 (方法)的扩展
 */
public interface FunctionExtension : Extension

/**
 * 为某单例服务注册的 [Extension].
 *
 * 若同时有多个实例可用, 将会使用 [SingletonExtensionSelector.selectSingleton] 选择
 *
 * @see PermissionServiceProvider
 */
public interface SingletonExtension<T> : Extension {
    public val instance: T
}

/**
 * 为一些实例注册的 [Extension].
 *
 * @see PluginLoaderProvider
 */
public interface InstanceExtension<T> : Extension {
    public val instance: T
}
