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
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginFileExtensions
import net.mamoe.mirai.console.plugin.ResourceContainer
import net.mamoe.mirai.console.plugin.getDescription
import net.mamoe.mirai.utils.MiraiLogger


/**
 * Java, Kotlin 或其他 JVM 平台插件
 *
 * 有关 [JvmPlugin] 相关实现方法，请参考
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
    PluginFileExtensions, ResourceContainer, AutoSavePluginDataHolder {

    /** 日志 */
    public val logger: MiraiLogger

    /** 插件描述 */
    public val description: JvmPluginDescription get() = loader.getDescription(this)

    /** 所属插件加载器实例 */
    @JvmDefault
    public override val loader: JarPluginLoader
        get() = JarPluginLoader

    /**
     * 重载 [PluginData]
     *
     * @see reloadPluginData
     */
    @JvmDefault
    @JvmName("reloadPluginData")
    public fun <T : PluginData> T.reload(): Unit = loader.dataStorage.load(this@JvmPlugin, this)

    /**
     * 重载 [PluginConfig]
     *
     * @see reloadPluginConfig
     */
    @JvmDefault
    @JvmName("reloadPluginConfig")
    public fun <T : PluginConfig> T.reload(): Unit = loader.configStorage.load(this@JvmPlugin, this)

    /**
     * 在插件被加载时调用. 只会被调用一次.
     */
    @JvmDefault
    public fun onLoad() {
    }

    /**
     * 在插件被启用时调用, 可能会被调用多次
     */
    @JvmDefault
    public fun onEnable() {
    }

    /**
     * 在插件被关闭时调用, 可能会被调用多次
     */
    @JvmDefault
    public fun onDisable() {
    }
}

/**
 * 重载一个 [PluginData]
 *
 * @see JvmPlugin.reload
 */
@JvmSynthetic
public inline fun JvmPlugin.reloadPluginData(instance: PluginData): Unit = this.run { instance.reload() }

/**
 * 重载一个 [PluginConfig]
 *
 * @see JvmPlugin.reload
 */
@JvmSynthetic
public inline fun JvmPlugin.reloadPluginConfig(instance: PluginConfig): Unit = this.run { instance.reload() }