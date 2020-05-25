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
import net.mamoe.mirai.utils.MiraiLogger


/**
 * Java 或 Kotlin Jar 插件
 *
 * @see JavaPlugin Java 插件
 * @see KotlinPlugin Kotlin 插件
 */
interface JvmPlugin : Plugin, CoroutineScope {
    /** 日志 */
    val logger: MiraiLogger

    /** 插件描述 */
    val description: JvmPluginDescription

    /** 所属插件加载器实例 */
    override val loader: JarPluginLoader get() = JarPluginLoader

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


