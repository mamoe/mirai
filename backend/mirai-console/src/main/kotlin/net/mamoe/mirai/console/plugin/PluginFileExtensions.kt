/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import java.io.File
import java.nio.file.Path


/**
 * 支持文件系统存储的扩展.
 *
 * @suppress 此接口只应由 [JvmPlugin] 继承
 *
 * @see JvmPlugin
 */
public interface PluginFileExtensions {
    /**
     * 数据目录路径
     * @see PluginData
     */
    public val dataFolderPath: Path

    /**
     * 数据目录. `dataFolderPath.toFile()`
     * @see PluginData
     */
    public val dataFolder: File


    /**
     * 从数据目录获取一个文件.
     * @see dataFolderPath
     */
    @JvmDefault
    public fun resolveDataFile(relativePath: String): File = dataFolderPath.resolve(relativePath).toFile()

    /**
     * 从数据目录获取一个文件.
     * @see dataFolderPath
     */
    @JvmDefault
    public fun resolveDataPath(relativePath: String): Path = dataFolderPath.resolve(relativePath)

    /**
     * 从数据目录获取一个文件.
     * @see dataFolderPath
     */
    @JvmDefault
    public fun resolveDataFile(relativePath: Path): File = dataFolderPath.resolve(relativePath).toFile()

    /**
     * 从数据目录获取一个文件路径.
     * @see dataFolderPath
     */
    @JvmDefault
    public fun resolveDataPath(relativePath: Path): Path = dataFolderPath.resolve(relativePath)


    /**
     * 插件配置保存路径
     * @see PluginConfig
     */
    public val configFolderPath: Path

    /**
     * 插件配置保存路径
     * @see PluginConfig
     */
    public val configFolder: File


    /**
     * 从配置目录获取一个文件.
     * @see configFolderPath
     */
    @JvmDefault
    public fun resolveConfigFile(relativePath: String): File = configFolderPath.resolve(relativePath).toFile()

    /**
     * 从配置目录获取一个文件.
     * @see configFolderPath
     */
    @JvmDefault
    public fun resolveConfigPath(relativePath: String): Path = configFolderPath.resolve(relativePath)

    /**
     * 从配置目录获取一个文件.
     * @see configFolderPath
     */
    @JvmDefault
    public fun resolveConfigFile(relativePath: Path): File = configFolderPath.resolve(relativePath).toFile()

    /**
     * 从配置目录获取一个文件路径.
     * @see configFolderPath
     */
    @JvmDefault
    public fun resolveConfigPath(relativePath: Path): Path = configFolderPath.resolve(relativePath)
}