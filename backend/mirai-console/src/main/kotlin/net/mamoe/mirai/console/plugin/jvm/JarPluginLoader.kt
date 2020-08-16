/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.internal.plugin.JarPluginLoaderImpl
import net.mamoe.mirai.console.plugin.FilePluginLoader
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

/**
 * 内建的 Jar (JVM) 插件加载器
 */
public interface JarPluginLoader : CoroutineScope, FilePluginLoader<JvmPlugin, JvmPluginDescription> {
    /**
     * [JvmPlugin.loadSetting] 默认使用的实例
     */
    @ConsoleExperimentalAPI
    public val settingStorage: SettingStorage

    public companion object INSTANCE : JarPluginLoader by JarPluginLoaderImpl {
        @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
        override val JvmPlugin.description: JvmPluginDescription
            get() = JarPluginLoaderImpl.run { description }
    }
}