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
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.plugin.PluginLoader

/**
 * 插件类型
 */
@Serializable(with = PluginKind.AsStringSerializer::class)
public enum class PluginKind {
    /** 表示此插件提供一个 [PluginLoader], 应在加载其他 [NORMAL] 类型插件前加载 */
    LOADER,

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