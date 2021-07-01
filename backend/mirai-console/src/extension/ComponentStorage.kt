/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad

/**
 * 组件容器, 容纳 [Plugin] 注册的 [Extension].
 *
 * @see Extension
 * @see JvmPlugin.onLoad
 */
public interface ComponentStorage {
    public fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin,
        extensionInstance: T,
    )

    public fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin,
        lazyInstance: () -> T,
    )
}

