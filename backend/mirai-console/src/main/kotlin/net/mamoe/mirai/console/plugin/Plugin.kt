/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.io.File

/**
 * 表示一个 mirai-console 插件.
 *
 * @see PluginDescription 插件描述
 * @see JvmPlugin Java, Kotlin 或其他 JVM 平台插件
 * @see PluginFileExtensions 支持文件系统存储的扩展
 *
 * @see PluginLoader 插件加载器
 */
interface Plugin {
    /**
     * 所属插件加载器实例, 此加载器必须能加载这个 [Plugin].
     */
    val loader: PluginLoader<*, *>
}

@Suppress("UNCHECKED_CAST")
inline val <P : Plugin> P.safeLoader: PluginLoader<P, PluginDescription>
    get() = this.loader as PluginLoader<P, PluginDescription>

/**
 * 支持文件系统存储的扩展.
 *
 * @see JvmPlugin
 */
@MiraiExperimentalAPI("classname is subject to change")
interface PluginFileExtensions {
    /**
     * 数据目录
     */
    val dataFolder: File

    /**
     * 从数据目录获取一个文件, 若不存在则创建文件.
     */
    @JvmDefault
    fun file(relativePath: String): File = File(dataFolder, relativePath).apply { createNewFile() }
}