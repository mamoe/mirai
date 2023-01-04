/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extensions.*
import net.mamoe.mirai.console.internal.extension.AbstractConcurrentComponentStorage
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.utils.DeprecatedSinceMirai
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
     * 注册一个扩展. [E] 必须拥有伴生对象为 [ExtensionPoint].
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

    /** 注册一个 [net.mamoe.mirai.console.extensions.SingletonExtensionSelector] */
    @Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION_ERROR", "DEPRECATION")
    @Deprecated(
        "Order of extensions is now determined by its priority property since 2.11. SingletonExtensionSelector is not needed anymore. ",
        level = DeprecationLevel.HIDDEN
    )
    @DeprecatedSinceMirai(warningSince = "2.11", errorSince = "2.13", hiddenSince = "2.14")
    public fun contributeSingletonExtensionSelector(lazyInstance: () -> net.mamoe.mirai.console.extensions.SingletonExtensionSelector): Unit =
        contribute(net.mamoe.mirai.console.extensions.SingletonExtensionSelector.ExtensionPoint, plugin, lazyInstance)

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
    @JvmName("contributePermissionServiceProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributePermissionService(lazyProvider: () -> PermissionServiceProvider): Unit =
        contribute(PermissionServiceProvider, plugin, lazyProvider)

    /////////////////////////////////////

    /** 注册一个 [PluginLoaderProvider] */
    @JvmName("contributePluginLoaderProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributePluginLoader(lazyProvider: () -> PluginLoaderProvider): Unit =
        contribute(PluginLoaderProvider, plugin, lazyProvider) // lazy for safety

    /////////////////////////////////////


    /** 注册一个 [CommandCallParserProvider] */
    @ExperimentalCommandDescriptors
    @JvmName("contributeCommandCallParserProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributeCommandCallParser(provider: CommandCallParserProvider): Unit =
        contribute(CommandCallParserProvider, plugin, provider)

    /////////////////////////////////////


    /** 注册一个 [CommandCallResolverProvider] */
    @ExperimentalCommandDescriptors
    @JvmName("contributeCommandCallResolverProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributeCommandCallParser(provider: CommandCallResolverProvider): Unit =
        contribute(CommandCallResolverProvider, plugin, provider)

    /////////////////////////////////////

    /** 注册一个 [CommandCallInterceptorProvider] */
    @ExperimentalCommandDescriptors
    @JvmName("contributeCommandCallInterceptorProvider")
    @OverloadResolutionByLambdaReturnType
    public fun contributeCommandCallParser(provider: CommandCallInterceptorProvider): Unit =
        contribute(CommandCallInterceptorProvider, plugin, provider)
}