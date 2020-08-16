/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import java.io.File

/**
 * 插件加载器.
 *
 * 插件加载器只实现寻找插件列表, 加载插件, 启用插件, 关闭插件这四个功能.
 *
 * 有关插件的依赖和已加载的插件列表由 [PluginManager] 维护.
 *
 * @see JarPluginLoader Jar 插件加载器
 */
public interface PluginLoader<P : Plugin, D : PluginDescription> {
    /**
     * 扫描并返回可以被加载的插件的 [描述][PluginDescription] 列表. 此函数只会被调用一次
     */
    public fun listPlugins(): List<D>

    /**
     * 获取此插件的描述
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如无法读取插件信息等).
     */
    @get:JvmName("getPluginDescription")
    @get:Throws(PluginLoadException::class)
    public val P.description: D // Java signature: `public D getDescription(P)`

    /**
     * 加载一个插件 (实例), 但不 [启用][enable] 它. 返回加载成功的主类实例
     *
     * @throws PluginLoadException 在加载插件遇到意料之中的错误时抛出 (如找不到主类等).
     */
    @Throws(PluginLoadException::class)
    public fun load(description: D): P

    public fun enable(plugin: P)
    public fun disable(plugin: P)
}

@JvmSynthetic
public inline fun <D : PluginDescription, P : Plugin> PluginLoader<in P, out D>.getDescription(plugin: P): D =
    plugin.description

public open class PluginLoadException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

/**
 * '/plugins' 目录中的插件的加载器. 每个加载器需绑定一个后缀.
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
 * [FilePluginLoader] 的默认基础实现
 */
public abstract class AbstractFilePluginLoader<P : Plugin, D : PluginDescription>(
    public override val fileSuffix: String
) : FilePluginLoader<P, D> {
    private fun pluginsFilesSequence(): Sequence<File> =
        PluginManager.pluginsDir.walk().filter { it.isFile && it.name.endsWith(fileSuffix, ignoreCase = true) }

    /**
     * 读取扫描到的后缀与 [fileSuffix] 相同的文件中的 [PluginDescription]
     */
    protected abstract fun Sequence<File>.mapToDescription(): List<D>

    public final override fun listPlugins(): List<D> = pluginsFilesSequence().mapToDescription()
}


// Not yet decided to make public API
internal class DeferredPluginLoader<P : Plugin, D : PluginDescription>(
    initializer: () -> PluginLoader<P, D>
) : PluginLoader<P, D> {
    private val instance by lazy(initializer)

    override fun listPlugins(): List<D> = instance.listPlugins()
    override val P.description: D get() = instance.run { description }
    override fun load(description: D): P = instance.load(description)
    override fun enable(plugin: P) = instance.enable(plugin)
    override fun disable(plugin: P) = instance.disable(plugin)
}
