/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.description

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.extension.Extension
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.plugin.description.PluginLoadPriority.*

/**
 * 插件类型.
 *
 * 插件类型将影响加载顺序: [BEFORE_EXTENSIONS] -> [ON_EXTENSIONS] -> [AFTER_EXTENSIONS].
 *
 * 依赖解决过程与插件类型有很大关联. 在一个较早的阶段, 只会解决在此阶段加载的插件. 意味着 [BEFORE_EXTENSIONS] 不允许依赖一个 [AFTER_EXTENSIONS] 类型的插件.
 */
public enum class PluginLoadPriority {
    /**
     * 表示此插件最早被加载. 在 Console 启动时的第一初始化阶段就会加载这些插件.
     *
     * 一般只有提供 [PluginLoaderProvider] 或 [SingletonExtensionSelector] 的插件才需要在此阶段加载.
     */
    BEFORE_EXTENSIONS,

    /**
     * 表示此插件提供一些高优先级的 [Extension], 应在加载其他 [AFTER_EXTENSIONS] 类型插件前加载
     *
     * 高优先级的 [Extension] 通常是覆盖 Console 内置的部分服务的扩展. 如 [PermissionServiceProvider].
     *
     * 一些普通的 [Extension], 如 [BotConfigurationAlterer], 也可以使用 [AFTER_EXTENSIONS] 类型插件注册.
     */
    ON_EXTENSIONS,

    /**
     * 表示此插件为一个通常的插件, 在扩展处理完毕后加载.
     */
    AFTER_EXTENSIONS;

    public object AsStringSerializer : KSerializer<PluginLoadPriority> by String.serializer().map(
        serializer = { it.name },
        deserializer = { str ->
            values().firstOrNull {
                it.name.equals(str, ignoreCase = true)
            } ?: AFTER_EXTENSIONS
        }
    )
}