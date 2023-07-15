/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.plugin.loader.PluginLoader

/**
 * 代表一个 未完成加载/延迟加载 的插件
 *
 * 此实例仅用于插件加载系统, 当 [PluginManager] 加载插件时会自动调用 [resolve] 解析真正的插件实例
 *
 * @see PluginLoader.listPlugins
 * @see PluginManager
 *
 * @since 2.16.0
 */
public interface NotYetLoadedPlugin<T : Plugin> : Plugin {
    public fun resolve(): T
}