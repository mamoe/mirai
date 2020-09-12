/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.*
import net.mamoe.mirai.console.internal.extension.AbstractConcurrentComponentStorage
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import kotlin.reflect.full.companionObjectInstance

/**
 * 添加一些扩展给 [Plugin] 的 [ComponentStorage].
 *
 * 所有扩展都会以 'lazy' 形式注册, 由 Console 在不同的启动阶段分别初始化各类扩展.
 */
@Suppress("EXPOSED_SUPER_CLASS", "unused", "MemberVisibilityCanBePrivate")
public class PluginComponentStorage(
    @JvmField
    internal val plugin: Plugin,
) : AbstractConcurrentComponentStorage() {
    /**
     * 注册一个扩展
     */
    public fun <E : Extension> contribute(
        extensionPoint: ExtensionPoint<E>,
        lazyInstance: () -> E,
    ): Unit = contribute(extensionPoint, plugin, lazyInstance)

    /**
     * 注册一个扩展
     */
    public inline fun <reified E : Extension> contribute(
        noinline lazyInstance: () -> E,
    ) {
        @Suppress("UNCHECKED_CAST")
        (contribute(
            (E::class.companionObjectInstance as? ExtensionPoint<E>
                ?: error("Companion object of ${E::class.qualifiedName} is not an ExtensionPoint")),
            lazyInstance
        ))
    }

    ///////////////////////////////////////////////////////////////////////////
    // FunctionExtension
    ///////////////////////////////////////////////////////////////////////////

    /** 注册一个 [SingletonExtensionSelector] */
    public fun contributeSingletonExtensionSelector(lazyInstance: () -> SingletonExtensionSelector): Unit =
        contribute(SingletonExtensionSelector, plugin, lazyInstance)

    /** 注册一个 [BotConfigurationAlterer] */
    public fun contributeBotConfigurationAlterer(instance: BotConfigurationAlterer): Unit =
        contribute(BotConfigurationAlterer, plugin, lazyInstance = { instance })

    /** 注册一个 [PostStartupExtension] */
    public fun contributePostStartupExtension(instance: PostStartupExtension): Unit =
        contribute(PostStartupExtension, plugin, lazyInstance = { instance })

    /** 注册一个 [PostStartupExtension] */
    public fun runAfterStartup(block: () -> Unit): Unit = contributePostStartupExtension(block)

    ///////////////////////////////////////////////////////////////////////////
    // InstanceExtensions & SingletonExtensions
    ///////////////////////////////////////////////////////////////////////////

    /** 注册一个 [PermissionServiceProvider] */
    @OverloadResolutionByLambdaReturnType
    public fun contributePermissionService(
        lazyInstance: () -> PermissionService<*>,
    ): Unit = contribute(PermissionServiceProvider, plugin, LazyPermissionServiceProviderImpl(lazyInstance))

    /** 注册一个 [PermissionServiceProvider] */
    @JvmName("contributePermissionServiceProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributePermissionService(
        lazyProvider: () -> PermissionServiceProvider,
    ): Unit = contribute(PermissionServiceProvider, plugin, lazyProvider)

    /////////////////////////////////////

    /** 注册一个 [PluginLoaderProvider] */
    @OverloadResolutionByLambdaReturnType
    public fun contributePluginLoader(lazyInstance: () -> PluginLoader<*, *>): Unit =
        contribute(PluginLoaderProvider, plugin, LazyPluginLoaderProviderImpl(lazyInstance))

    /** 注册一个 [PluginLoaderProvider] */
    @JvmName("contributePluginLoaderProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributePluginLoader(lazyProvider: () -> PluginLoaderProvider): Unit =
        contribute(PluginLoaderProvider, plugin, lazyProvider)
}