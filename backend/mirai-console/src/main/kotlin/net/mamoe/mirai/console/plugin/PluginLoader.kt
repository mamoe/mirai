/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import java.io.File
import java.util.*

/**
 * 插件加载器.
 *
 * 插件加载器只实现寻找插件列表, 加载插件, 启用插件, 关闭插件这四个功能.
 *
 * 有关插件的依赖和已加载的插件列表由 [PluginManager] 维护.
 *
 * ## 内建加载器
 * - [JarPluginLoader] Jar 插件加载器
 *
 * ## 扩展加载器
 * 插件被允许扩展一个加载器.
 * Console 使用 [ServiceLoader] 加载 [PluginLoader] 的实例.
 * 插件也可通过 [PluginManager.register] 手动注册, 然而这是不推荐的.
 *
 * ### 实现扩展加载器
 * 直接实现接口 [PluginLoader] 或 [FilePluginLoader], 并添加 [ServiceLoader] 相关资源文件即可.
 *
 * @see JarPluginLoader Jar 插件加载器
 * @see PluginManager.register 注册一个扩展的插件加载器
 */
public interface PluginLoader<P : Plugin, D : PluginDescription> {
    /**
     * 扫描并返回可以被加载的插件的列表.
     *
     * 这些插件都应处于还未被加载的状态.
     *
     * 在 console 启动时, [PluginManager] 会获取所有 [PluginDescription], 分析依赖关系, 确认插件加载顺序.
     *
     * **实现细节:** 此函数*只应该*在 console 启动时被调用一次. 但取决于前端实现不同, 或由于被一些插件需要, 此函数也可能会被多次调用.
     */
    public fun listPlugins(): List<P>

    /**
     * 获取此插件的描述.
     *
     * **实现细节**: 此函数只允许抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     *
     * 若在 console 启动并加载所有插件的过程中, 本函数抛出异常, 则会放弃此插件的加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如无法读取插件信息等).
     *
     * @see PluginDescription 插件描述
     * @see getDescription 无 receiver, 接受参数的版本.
     */
    @get:JvmName("getPluginDescription")
    @get:Throws(PluginLoadException::class)
    public val P.description: D // Java signature: `public D getDescription(P)`

    /**
     * 加载一个插件 (实例), 但不 [启用][enable] 它. 返回加载成功的主类实例
     *
     * **实现细节**: 此函数只允许抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     * 当异常发生时, 插件将会直接被放弃加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     */
    @Throws(PluginLoadException::class)
    public fun load(plugin: P)

    /**
     * 启用这个插件.
     *
     * **实现细节**: 此函数可抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
     * 当异常发生时, 插件将会直接被放弃加载, 并影响依赖它的其他插件.
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     *
     * @see PluginManager.enable
     */
    @Throws(IllegalStateException::class, PluginLoadException::class)
    public fun enable(plugin: P)

    /**
     * 禁用这个插件.
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

/**
 * 获取此插件的描述.
 *
 * **实现细节**: 此函数只允许抛出 [PluginLoadException] 作为正常失败原因, 其他任意异常都属于意外错误.
 *
 * 若在 console 启动并加载所有插件的过程中, 本函数抛出异常, 则会放弃此插件的加载, 并影响依赖它的其他插件.
 *
 * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如无法读取插件信息等).
 *
 * @see PluginDescription 插件描述
 * @see PluginLoader.description
 */
@Suppress("UNCHECKED_CAST")
@JvmSynthetic
public inline fun <D : PluginDescription, P : Plugin> PluginLoader<in P, out D>.getDescription(plugin: P): D =
    plugin.description

/**
 * 在加载插件过程中遇到的意料之中的问题.
 *
 * @see PluginLoader.load
 * @see PluginLoader.enable
 * @see PluginLoader.disable
 * @see PluginLoader.description
 */
public open class PluginLoadException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

/**
 * ['/plugins'][PluginManager.pluginsPath] 目录中的插件的加载器. 每个加载器需绑定一个后缀.
 *
 * @see AbstractFilePluginLoader 默认基础实现
 * @see JarPluginLoader 内建的 Jar (JVM) 插件加载器.
 */
public interface FilePluginLoader<P : Plugin, D : PluginDescription> : PluginLoader<P, D> {
    /**
     * 所支持的插件文件后缀, 含 '.'. 如 [JarPluginLoader] 为 ".jar"
     */
    public val fileSuffix: String
}

/**
 * [FilePluginLoader] 的默认基础实现.
 *
 * @see FilePluginLoader
 */
public abstract class AbstractFilePluginLoader<P : Plugin, D : PluginDescription>(
    /**
     * 所支持的插件文件后缀, 含 '.'. 如 [JarPluginLoader] 为 ".jar"
     */
    public override val fileSuffix: String
) : FilePluginLoader<P, D> {
    private fun pluginsFilesSequence(): Sequence<File> =
        PluginManager.pluginsFolder.listFiles().orEmpty().asSequence()
            .filter { it.isFile && it.name.endsWith(fileSuffix, ignoreCase = true) }

    /**
     * 读取扫描到的后缀与 [fileSuffix] 相同的文件中的插件实例, 但不 [加载][PluginLoader.load]
     */
    protected abstract fun Sequence<File>.extractPlugins(): List<P>

    public final override fun listPlugins(): List<P> = pluginsFilesSequence().extractPlugins()
}


// Not yet decided to make public API
internal class DeferredPluginLoader<P : Plugin, D : PluginDescription>(
    initializer: () -> PluginLoader<P, D>
) : PluginLoader<P, D> {
    private val instance by lazy(initializer)

    override fun listPlugins(): List<P> = instance.run { listPlugins() }
    override val P.description: D get() = instance.run { description }
    override fun load(plugin: P) = instance.load(plugin)
    override fun enable(plugin: P) = instance.enable(plugin)
    override fun disable(plugin: P) = instance.disable(plugin)
}
