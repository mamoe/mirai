/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin.loader

import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader

/**
 * 插件加载器.
 *
 * 插件加载器只实现寻找插件列表, 加载插件, 启用插件, 关闭插件这四个功能.
 *
 * 一个插件要在何时被加载，依赖如何处理，[PluginLoader] 都无需关心.
 *
 * 有关插件的依赖和已加载的插件列表由 [PluginManager] 维护.
 *
 * ## 内建加载器
 * - [JvmPluginLoader] Jar 插件加载器
 *
 * ## 扩展加载器
 * 插件被允许扩展一个加载器.
 *
 * ### 实现扩展加载器
 * 直接实现接口 [PluginLoader] 或 [FilePluginLoader], 并注册 [PluginLoaderProvider]
 *
 * @see JvmPluginLoader Jar 插件加载器
 */
public interface PluginLoader<P : Plugin, D : PluginDescription> {
    /**
     * 扫描并返回可以被加载的插件的列表.
     *
     * 这些插件都应处于还未被加载的状态.
     *
     * 在 Console 启动时, [PluginManager] 会获取所有 [PluginDescription], 分析依赖关系, 确认插件加载顺序.
     *
     * **实现细节:** 此函数*只应该*在 Console 启动时被调用一次. 但取决于前端实现不同, 或由于被一些插件需要, 此函数也可能会被多次调用.
     */
    public fun listPlugins(): List<P>

    /**
     * 获取此插件的描述.
     *
     * **实现细节**: 此函数只允许抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     *
     * 若在 Console 启动并加载所有插件的过程中, 本函数抛出异常, 则会放弃此插件的加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如无法读取插件信息等).
     *
     * @see PluginDescription 插件描述
     * @see getPluginDescription 无 receiver, 接受参数的版本.
     */
    @Throws(PluginLoadException::class)
    public fun getPluginDescription(plugin: P): D // Java signature: `public D getDescription(P)`

    /**
     * 主动加载一个插件 (实例), 但不 [启用][enable] 它. 返回加载成功的主类实例
     *
     * **实现注意**: Console 不会把一个已经启用了的插件再次调用 [load] 或 [enable], 但不排除意外情况. 实现本函数时应在这种情况时立即抛出异常 [IllegalStateException].
     *
     * **实现细节**: 此函数只允许抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     * 当异常发生时, 插件将会直接被放弃加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     * @throws IllegalStateException 在插件已经被加载时抛出. 这属于意料之外的情况.
     */
    @Throws(PluginLoadException::class)
    public fun load(plugin: P)

    /**
     * 主动启用这个插件.
     *
     * **实现注意**: Console 不会把一个已经启用了的插件再次调用 [load] 或 [enable], 但不排除意外情况. 实现本函数时应在这种情况时立即抛出异常 [IllegalStateException].
     *
     * **实现细节**: 此函数可抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     * 当异常发生时, 插件将会直接被放弃加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     * @throws IllegalStateException 在插件已经被加载时抛出. 这属于意料之外的情况.
     *
     * @see PluginManager.enable
     */
    @Throws(IllegalStateException::class, PluginLoadException::class)
    public fun enable(plugin: P)

    /**
     * 主动禁用这个插件.
     *
     * **实现细节**: 此函数可抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     * 当异常发生时, 插件将会直接被放弃加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     *
     * @see PluginManager.disable
     */
    @Throws(IllegalStateException::class, PluginLoadException::class)
    public fun disable(plugin: P)
}