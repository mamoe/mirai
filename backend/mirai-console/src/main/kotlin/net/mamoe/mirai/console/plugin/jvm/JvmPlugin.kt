/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginFileExtensions
import net.mamoe.mirai.console.setting.AutoSaveSettingHolder
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.util.ResourceContainer
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.reflect.KClass


/**
 * Java, Kotlin 或其他 JVM 平台插件
 *
 * ### ResourceContainer
 * 实现为 [ClassLoader.getResourceAsStream]
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
    PluginFileExtensions, ResourceContainer, AutoSaveSettingHolder {
    /** 日志 */
    public val logger: MiraiLogger

    /** 插件描述 */
    public val description: JvmPluginDescription

    /** 所属插件加载器实例 */
    @JvmDefault
    public override val loader: JarPluginLoader
        get() = JarPluginLoader

    /**
     * 从 [JarPluginLoader.settingStorage] 获取一个 [Setting] 实例
     */
    @JvmDefault
    public fun <T : Setting> loadSetting(clazz: Class<T>): T = loader.settingStorage.load(this, clazz)

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

@JvmSynthetic
public inline fun <T : Setting> JvmPlugin.loadSetting(clazz: KClass<T>): T = this.loadSetting(clazz.java)

@JvmSynthetic
public inline fun <reified T : Setting> JvmPlugin.loadSetting(): T = this.loadSetting(T::class)