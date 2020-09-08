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
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.extension.Extension
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.description.PluginKind.*

/**
 * 插件类型.
 *
 * 插件类型将影响加载顺序: [LOADER] -> [HIGH_PRIORITY_EXTENSIONS] -> [NORMAL].
 *
 * 依赖解决过程与插件类型有很大关联. 在一个较早的阶段, 只会解决在此阶段加载的插件. 意味着 [LOADER] 不允许依赖一个 [NORMAL] 类型的插件.
 */
@Serializable(with = PluginKind.AsStringSerializer::class)
public enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 也可以同时提供其他 [Extension] 应最早被加载 */
    LOADER,

    /**
     * 表示此插件提供一些高优先级的 [Extension], 应在加载其他 [NORMAL] 类型插件前加载
     *
     * 高优先级的 [Extension] 通常是覆盖 Console 内置的部分服务的扩展. 如 [PermissionServiceProvider].
     *
     * 一些普通的 [Extension], 如 [BotConfigurationAlterer], 也可以使用 [NORMAL] 类型插件注册.
     */
    HIGH_PRIORITY_EXTENSIONS,

    /** 表示此插件为一个通常的插件, 按照正常的依赖关系加载. */
    NORMAL;

    public object AsStringSerializer : KSerializer<PluginKind> by String.serializer().map(
        serializer = { it.name },
        deserializer = { str ->
            values().firstOrNull {
                it.name.equals(str, ignoreCase = true)
            } ?: NORMAL
        }
    )
}