/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.plugin.BuiltInJvmPluginLoaderImpl
import net.mamoe.mirai.console.plugin.loader.FilePluginLoader

/**
 * JVM 插件加载器
 */
public interface JvmPluginLoader : CoroutineScope, FilePluginLoader<JvmPlugin, JvmPluginDescription> {
    /**
     * ".jar"
     */
    public override val fileSuffix: String

    /**
     * [AbstractJvmPlugin.reloadPluginData] 默认使用的实例
     */
    public val dataStorage: PluginDataStorage

    /**
     * [AbstractJvmPlugin.reloadPluginData] 默认使用的实例
     */
    public val configStorage: PluginDataStorage

    public companion object BuiltIn : JvmPluginLoader by BuiltInJvmPluginLoaderImpl {
        @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
        override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription =
            BuiltInJvmPluginLoaderImpl.run { plugin.description }
    }
}