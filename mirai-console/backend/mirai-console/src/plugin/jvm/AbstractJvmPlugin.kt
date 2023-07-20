/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.internal.plugin.JvmPluginClassLoaderN
import net.mamoe.mirai.console.internal.plugin.JvmPluginInternal
import net.mamoe.mirai.console.internal.plugin.loadPluginDescriptionFromClassLoader
import net.mamoe.mirai.console.internal.util.PluginServiceHelper
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.minutesToMillis
import net.mamoe.mirai.utils.secondsToMillis
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 * [JavaPlugin] 和 [KotlinPlugin] 的父类. 所有 [JvmPlugin] 都应该拥有此类作为直接或间接父类.
 *
 * @see JavaPlugin
 * @see KotlinPlugin
 */
@OptIn(ConsoleExperimentalApi::class)
public abstract class AbstractJvmPlugin : JvmPluginInternal, JvmPlugin, AutoSavePluginDataHolder {
    @JvmOverloads
    public constructor(
        description: JvmPluginDescription,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    ) : super(parentCoroutineContext) {
        this.description = description
    }

    @JvmOverloads
    public constructor(parentCoroutineContext: CoroutineContext = EmptyCoroutineContext) : super(parentCoroutineContext) {
        this.description = javaClass.loadPluginDescriptionFromClassLoader()
    }


    final override val description: JvmPluginDescription

    @ConsoleExperimentalApi
    public final override val dataHolderName: String
        get() = this.description.id

    public final override val loader: JvmPluginLoader get() = super<JvmPluginInternal>.loader

    public final override fun permissionId(name: String): PermissionId =
        PermissionService.INSTANCE.allocatePermissionIdForPlugin(this, name)

    /**
     * 重载 [PluginData]
     *
     * @see reloadPluginData
     */
    @JvmName("reloadPluginData")
    public fun <T : PluginData> T.reload(): Unit = loader.dataStorage.load(this@AbstractJvmPlugin, this)

    /**
     * 重载 [PluginConfig]
     *
     * @see reloadPluginConfig
     */
    @JvmName("reloadPluginConfig")
    public fun <T : PluginConfig> T.reload(): Unit = loader.configStorage.load(this@AbstractJvmPlugin, this)

    /**
     * 立即保存 [PluginData]
     *
     * @see reloadPluginData
     * @since 2.9
     */
    @JvmName("savePluginData")
    public fun <T : PluginData> T.save(): Unit = loader.dataStorage.store(this@AbstractJvmPlugin, this)

    /**
     * 立即保存 [PluginConfig]
     *
     * @see reloadPluginConfig
     * @since 2.9
     */
    @JvmName("savePluginConfig")
    public fun <T : PluginConfig> T.save(): Unit = loader.configStorage.store(this@AbstractJvmPlugin, this)

    @ConsoleExperimentalApi
    public override val autoSaveIntervalMillis: LongRange = 30.secondsToMillis..10.minutesToMillis

    /**
     * 获取 [JvmPluginClasspath]
     *
     * 注: 仅插件通过 console 内置插件加载器加载时可用
     *
     * @since 2.12
     */
    protected val jvmPluginClasspath: JvmPluginClasspath by lazy {
        val classLoader = this@AbstractJvmPlugin.javaClass.classLoader
        if (classLoader is JvmPluginClassLoaderN) {
            return@lazy classLoader.openaccess
        }
        error("jvmPluginClasspath not available for $classLoader")
    }

    /**
     * 获取 指定类的 SPI Service
     *
     * 为了兼容 Kotlin object 单例类，此方法没有直接使用 java 原生的 API,
     * 而是 手动读取 `META-INF/services/` 的内容, 并尝试构造或获取实例
     *
     * 注: 仅包括当前插件 JAR 的 Service
     */
    @JvmSynthetic
    protected fun <T : Any> services(kClass: KClass<out T>): Lazy<List<T>> = lazy {
        val classLoader = try {
            jvmPluginClasspath.pluginClassLoader
        } catch (_: IllegalStateException) {
            this::class.java.classLoader
        }
        with(PluginServiceHelper) {
            classLoader
                .findServices(kClass)
                .loadAllServices()
        }
    }

    /**
     * 获取 指定类的 SPI Service
     *
     * 为了兼容 Kotlin object 单例类，此方法没有直接使用 java 原生的 API,
     * 而是 手动读取 `META-INF/services/` 的内容, 并尝试构造或获取实例
     *
     * 注: 仅包括当前插件 JAR 的 Service
     */
    protected fun <T : Any> services(clazz: Class<out T>): Lazy<List<T>> = services(kClass = clazz.kotlin)

    /**
     * 获取 指定类的 SPI Service
     *
     * 为了兼容 Kotlin object 单例类，此方法没有直接使用 java 原生的 API,
     * 而是 手动读取 `META-INF/services/` 的内容, 并尝试构造或获取实例
     *
     * 注: 仅包括当前插件 JAR 的 Service
     */
    protected inline fun <reified T : Any> services(): Lazy<List<T>> = services(kClass = T::class)
}

/**
 * 重载一个 [PluginData]
 *
 * @see AbstractJvmPlugin.reload
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.reloadPluginData(instance: PluginData): Unit = this.run { instance.reload() }

/**
 * 重载一个 [PluginConfig]
 *
 * @see AbstractJvmPlugin.reload
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.reloadPluginConfig(instance: PluginConfig): Unit = this.run { instance.reload() }

/**
 * 立即保存 [PluginData]
 *
 * @see AbstractJvmPlugin.save
 * @since 2.9
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.savePluginData(instance: PluginData): Unit = this.run { instance.save() }

/**
 * 立即保存 [PluginConfig]
 *
 * @see AbstractJvmPlugin.save
 * @since 2.9
 */
@JvmSynthetic
public inline fun AbstractJvmPlugin.savePluginConfig(instance: PluginConfig): Unit = this.run { instance.save() }
