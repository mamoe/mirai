/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.plugin.JarPluginLoaderImpl
import net.mamoe.mirai.console.plugin.FilePluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 内建的 Jar (JVM) 插件加载器
 */
@ConsoleExperimentalApi("classname might change")
public interface JarPluginLoader : CoroutineScope, FilePluginLoader<JvmPlugin, JvmPluginDescription> {
    /**
     * [JvmPlugin.reloadPluginData] 默认使用的实例
     */
    @ConsoleExperimentalApi
    public val dataStorage: PluginDataStorage

    /**
     * [JvmPlugin.reloadPluginData] 默认使用的实例
     */
    @ConsoleExperimentalApi
    public val configStorage: PluginDataStorage

    public companion object INSTANCE : JarPluginLoader by JarPluginLoaderImpl {
        @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
        override val JvmPlugin.description: JvmPluginDescription
            get() = JarPluginLoaderImpl.run { description }
    }
}