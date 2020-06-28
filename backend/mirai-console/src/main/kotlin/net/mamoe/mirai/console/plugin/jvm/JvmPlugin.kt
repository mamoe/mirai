/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginFileExtensions
import net.mamoe.mirai.console.setting.AutoSaveSettingHolder
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.utils.ResourceContainer
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.reflect.KClass


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
interface JvmPlugin : Plugin, CoroutineScope, PluginFileExtensions, ResourceContainer, AutoSaveSettingHolder {
    /** 日志 */
    val logger: MiraiLogger

    /** 插件描述 */
    val description: JvmPluginDescription

    /** 所属插件加载器实例 */
    override val loader: JarPluginLoader get() = JarPluginLoader

    /**
     * 获取一个 [Setting] 实例
     */
    fun <T : Setting> getSetting(clazz: Class<T>): T


    @JvmDefault
    fun onLoad() {
    }

    @JvmDefault
    fun onEnable() {
    }

    @JvmDefault
    fun onDisable() {
    }
}

fun <T : Setting> JvmPlugin.getSetting(clazz: KClass<T>) = this.getSetting(clazz.java)
inline fun <reified T : Setting> JvmPlugin.getSetting() = this.getSetting(T::class)