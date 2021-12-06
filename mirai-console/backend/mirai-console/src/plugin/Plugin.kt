/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.getPluginDescription
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.SemVersion
import kotlin.DeprecationLevel.ERROR

/**
 * 表示一个 mirai-console 插件.
 *
 * @see PluginManager.enablePlugin 启用一个插件
 * @see PluginManager.disablePlugin 禁用一个插件
 * @see PluginManager.description 获取一个插件的 [描述][PluginDescription]
 *
 * @see PluginDescription 插件描述， 需由 [PluginLoader] 帮助提供（[PluginLoader.getPluginDescription]）
 * @see JvmPlugin Java, Kotlin 或其他 JVM 平台插件
 * @see PluginFileExtensions 支持文件系统存储的扩展
 *
 * @see PluginLoader 插件加载器
 */
public interface Plugin : CommandOwner {
    /**
     * 当插件已启用时返回 `true`, 否则表示插件未启用.
     *
     * @see PluginManager.enablePlugin 启用一个插件
     * @see PluginManager.disablePlugin 禁用一个插件
     */
    public val isEnabled: Boolean

    /**
     * 所属插件加载器实例, 此加载器必须能加载这个 [Plugin].
     */
    public val loader: PluginLoader<*, *>
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Moved to companion for a better Java API. ",
    ReplaceWith("this.description", "net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description"),
    level = ERROR
)
public inline val Plugin.description: PluginDescription
    get() = getPluginDescription(this) // resolved to net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.getDescription

/**
 * 获取 [PluginDescription.name]
 */
public inline val Plugin.name: String get() = getPluginDescription(this).name

/**
 * 获取 [PluginDescription.id]
 */
public inline val Plugin.id: String get() = getPluginDescription(this).id

/**
 * 获取 [PluginDescription.version]
 */
public inline val Plugin.version: SemVersion get() = getPluginDescription(this).version

/**
 * 获取 [PluginDescription.info]
 */
public inline val Plugin.info: String get() = getPluginDescription(this).info

/**
 * 获取 [PluginDescription.author]
 */
public inline val Plugin.author: String get() = getPluginDescription(this).author

/**
 * 获取 [PluginDescription.dependencies]
 */
public inline val Plugin.dependencies: Set<PluginDependency> get() = getPluginDescription(this).dependencies
