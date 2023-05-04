/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import kotlinx.coroutines.CoroutineScope
import me.him188.kotlin.dynamic.delegation.dynamicDelegation
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.plugin.BuiltInJvmPluginLoaderImpl
import net.mamoe.mirai.console.plugin.loader.FilePluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * JVM 插件加载器
 */
@NotStableForInheritance
public interface JvmPluginLoader : CoroutineScope, FilePluginLoader<JvmPlugin, JvmPluginDescription> {
    /**
     * ".jar"
     */
    public override val fileSuffix: String

    /**
     * [AbstractJvmPlugin.reloadPluginData] 默认使用的实例
     */
    @ConsoleExperimentalApi
    public val dataStorage: PluginDataStorage

    /**
     * [AbstractJvmPlugin.reloadPluginData] 默认使用的实例
     */
    @ConsoleExperimentalApi
    public val configStorage: PluginDataStorage

    /**
     * @since 2.10
     */
    @MiraiInternalApi
    public val classLoaders: List<ClassLoader>

    @MiraiInternalApi
    public fun findLoadedClass(name: String): Class<*>?

    public companion object BuiltIn :
        JvmPluginLoader by (dynamicDelegation {
            @OptIn(ConsoleFrontEndImplementation::class)
            MiraiConsoleImplementation.getInstance().jvmPluginLoader
        }) {

        override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription =
            BuiltInJvmPluginLoaderImpl.run { plugin.description }
    }
}