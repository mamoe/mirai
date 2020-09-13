/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.plugin

import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.loader.PluginLoader

/**
 * 表示一个 mirai-console 插件.
 *
 * @see PluginManager.enable 启用一个插件
 * @see PluginManager.disable 禁用一个插件
 * @see PluginManager.description 获取一个插件的 [描述][PluginDescription]
 *
 * @see PluginDescription 插件描述， 需由 [PluginLoader] 帮助提供（[PluginLoader.description]）
 * @see JvmPlugin Java, Kotlin 或其他 JVM 平台插件
 * @see PluginFileExtensions 支持文件系统存储的扩展
 *
 * @see PluginLoader 插件加载器
 */
public interface Plugin : CommandOwner {
    /**
     * 判断此插件是否已启用
     *
     * @see PluginManager.enable 启用一个插件
     * @see PluginManager.disable 禁用一个插件
     */
    public val isEnabled: Boolean

    /**
     * 所属插件加载器实例, 此加载器必须能加载这个 [Plugin].
     */
    public val loader: PluginLoader<*, *>
}

/**
 * 获取 [PluginDescription]
 */
public inline val Plugin.description: PluginDescription get() = this.safeLoader.getPluginDescription(this)

/**
 * 获取 [PluginDescription.name`]
 */
public inline val Plugin.name: String get() = this.description.name

/**
 * 获取 [PluginDescription.version]
 */
public inline val Plugin.version: Semver get() = this.description.version

/**
 * 获取 [PluginDescription.info]
 */
public inline val Plugin.info: String get() = this.description.info

/**
 * 获取 [PluginDescription.author]
 */
public inline val Plugin.author: String get() = this.description.author

/**
 * 获取 [PluginDescription.dependencies]
 */
public inline val Plugin.dependencies: Set<PluginDependency> get() = this.description.dependencies
