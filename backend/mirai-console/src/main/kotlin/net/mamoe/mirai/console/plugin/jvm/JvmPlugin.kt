/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "EXPOSED_SUPER_CLASS",
    "NOTHING_TO_INLINE",
    "INAPPLICABLE_JVM_NAME"
)

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.PermissionIdNamespace
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginFileExtensions
import net.mamoe.mirai.console.plugin.ResourceContainer
import net.mamoe.mirai.utils.MiraiLogger


/**
 * Java, Kotlin 或其他 JVM 平台插件
 *
 * @see AbstractJvmPlugin 默认实现
 *
 * @see JavaPlugin Java 插件
 * @see KotlinPlugin Kotlin 插件
 *
 * @see JvmPlugin 支持文件系统扩展
 * @see ResourceContainer 支持资源获取 (如 Jar 中的资源文件)
 */
public interface JvmPlugin : Plugin, CoroutineScope,
    PluginFileExtensions, ResourceContainer, PermissionIdNamespace {

    /** 日志 */
    public val logger: MiraiLogger

    /** 插件描述 */
    public val description: JvmPluginDescription

    /** 所属插件加载器实例 */
    // `final` in AbstractJvmPlugin
    public override val loader: JvmPluginLoader get() = JvmPluginLoader

    /**
     * 在插件被加载时调用. 只会被调用一次.
     *
     * 在 [onLoad] 时可注册扩展 [PluginComponentStorage.contribute]
     *
     * @see PluginComponentStorage 查看更多信息
     *
     * @receiver 组件容器
     */
    public fun PluginComponentStorage.onLoad() {}

    /**
     * 在插件被启用时调用, 可能会被调用多次
     */
    public fun onEnable() {}

    /**
     * 在插件被关闭时调用, 可能会被调用多次
     */
    public fun onDisable() {}

    public companion object {
        @JvmSynthetic
        public inline fun JvmPlugin.onLoad(storage: PluginComponentStorage): Unit = storage.onLoad()
    }
}