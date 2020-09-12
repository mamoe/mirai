/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector.ExtensionPoint.selectSingleton
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 表示一个扩展.
 */
@ConsoleExperimentalApi
public interface Extension

/**
 * 增加一些函数 (方法)的扩展
 */
@ConsoleExperimentalApi
public interface FunctionExtension : Extension

/**
 * 为某单例服务注册的 [Extension].
 *
 * 若同时有多个实例可用, 将会使用 [SingletonExtensionSelector.selectSingleton] 选择
 *
 * @see PermissionServiceProvider
 */
@ConsoleExperimentalApi
public interface SingletonExtension<T> : Extension {
    public val instance: T
}

/**
 * 为一些实例注册的 [Extension].
 *
 * @see PluginLoaderProvider
 */
@ConsoleExperimentalApi
public interface InstanceExtension<T> : Extension {
    public val instance: T
}
