/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import java.io.File

/**
 * 插件管理器.
 */
public interface PluginManager {
    /**
     * `$rootDir/plugins`
     */
    public val pluginsDir: File

    /**
     * `$rootDir/data`
     */
    public val pluginsDataFolder: File

    /**
     * 已加载的插件列表
     */
    public val plugins: List<Plugin>

    /**
     * 内建的插件加载器列表. 由 [MiraiConsole] 初始化.
     *
     * @return 不可变的 list.
     */
    public val builtInLoaders: List<PluginLoader<*, *>>

    /**
     * 由插件创建的 [PluginLoader]
     */
    public val pluginLoaders: List<PluginLoader<*, *>>

    public fun registerPluginLoader(loader: PluginLoader<*, *>): Boolean

    public fun unregisterPluginLoader(loader: PluginLoader<*, *>): Boolean

    /**
     * 获取插件的 [描述][PluginDescription], 通过 [PluginLoader.getDescription]
     */
    public val Plugin.description: PluginDescription

    public companion object INSTANCE : PluginManager by PluginManagerImpl {
        override val Plugin.description: PluginDescription get() = PluginManagerImpl.run { description }
    }
}

@JvmSynthetic
public inline fun PluginLoader<*, *>.register(): Boolean = PluginManager.registerPluginLoader(this)

@JvmSynthetic
public inline fun PluginLoader<*, *>.unregister(): Boolean = PluginManager.unregisterPluginLoader(this)

public class PluginMissingDependencyException : PluginResolutionException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class PluginResolutionException : Exception {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}