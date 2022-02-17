/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.*
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.extension.SingletonExtensionSelectorImpl
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.info
import kotlin.reflect.KClass

/**
 * 用于同时拥有多个 [SingletonExtension] 时选择一个实例.
 *
 * 如有多个 [SingletonExtensionSelector] 注册, 将会停止服务器.
 */
@Deprecated(
    "Order of extensions is now determined by its priority property since 2.11. SingletonExtensionSelector is not needed anymore. ",
    level = DeprecationLevel.WARNING
)
@DeprecatedSinceMirai(warningSince = "2.11")
public interface SingletonExtensionSelector : FunctionExtension {
    /**
     * 表示一个插件注册的 [Extension]
     */
    @Deprecated(
        "Order of extensions is now determined by its priority property since 2.11. SingletonExtensionSelector is not needed anymore. ",
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.11")
    public data class Registry<T : Extension>(
        val plugin: Plugin?,
        val extension: T,
    )

    /**
     * @return `null` 表示使用 Console 内置的 [SingletonExtensionSelector]
     */
    public fun <T : Extension> selectSingleton(
        extensionType: KClass<T>,
        candidates: Collection<Registry<T>>,
    ): T?

    @Deprecated(
        "Order of extensions is now determined by its priority property since 2.11. SingletonExtensionSelector is not needed anymore. ",
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.11")
    public companion object ExtensionPoint :
        AbstractExtensionPoint<SingletonExtensionSelector>(SingletonExtensionSelector::class) {

        private var instanceField: SingletonExtensionSelector? = null

        internal val instance: SingletonExtensionSelector get() = instanceField ?: error("")

        internal fun init() {
            check(instanceField == null) { "Internal error: reinitialize SingletonExtensionSelector" }
            val instances = GlobalComponentStorage.getExtensions(ExtensionPoint).toList()
            instanceField = when {
                instances.isEmpty() -> SingletonExtensionSelectorImpl
                instances.size == 1 -> {
                    instances.single().also { registry ->
                        MiraiConsole.mainLogger.info { "Loaded SingletonExtensionSelector: ${registry.extension} from ${registry.plugin?.name ?: "<builtin>"}" }
                    }.extension
                }
                else -> {
                    val hint = instances.joinToString { reg ->
                        "'${reg.extension}' from '${reg.plugin?.name ?: "<builtin>"}'"
                    }
                    error(
                        "Found too many SingletonExtensionSelectors: $hint. " +
                                "Check your plugins and ensure there is only one external SingletonExtensionSelectors"
                    )
                }
            }
        }

        internal fun <T : Extension> selectSingleton(
            extensionType: KClass<T>,
            candidates: Collection<ExtensionRegistry<T>>,
        ): T? =
            instance.selectSingleton(extensionType, candidates.map { Registry(it.plugin, it.extension) })


        internal fun <T : Extension> SingletonExtensionSelector.selectSingleton(
            extensionType: KClass<T>,
            candidates: Collection<ExtensionRegistry<T>>,
        ): T? = selectSingleton(extensionType, candidates.map { Registry(it.plugin, it.extension) })
    }
}