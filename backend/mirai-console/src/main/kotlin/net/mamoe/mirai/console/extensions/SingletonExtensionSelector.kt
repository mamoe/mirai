/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.*
import net.mamoe.mirai.console.internal.extensions.BuiltInSingletonExtensionSelector
import net.mamoe.mirai.console.plugin.description.PluginKind
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.utils.info
import kotlin.reflect.KClass

/**
 * 用于同时拥有多个 [SingletonExtension] 时选择一个实例.
 *
 * 如有多个 [SingletonExtensionSelector] 注册, 将会停止服务器.
 *
 * 此扩展可由 [PluginKind.LOADER] 和 [PluginKind.HIGH_PRIORITY_EXTENSIONS] 插件提供
 */
public interface SingletonExtensionSelector : FunctionExtension {

    public fun <T : Extension> selectSingleton(
        extensionType: KClass<T>,
        candidates: Collection<ExtensionRegistry<T>>
    ): T?

    public companion object ExtensionPoint :
        AbstractExtensionPoint<SingletonExtensionSelector>(SingletonExtensionSelector::class) {
        internal val instance: SingletonExtensionSelector by lazy {
            val instances = SingletonExtensionSelector.getExtensions()
            when {
                instances.isEmpty() -> BuiltInSingletonExtensionSelector
                instances.size == 1 -> {
                    instances.single().also { (plugin, ext) ->
                        MiraiConsole.mainLogger.info { "Loaded SingletonExtensionSelector: $ext from ${plugin.name}" }
                    }.extension
                }
                else -> {
                    error("Found too many SingletonExtensionSelectors: ${instances.joinToString { (p, i) -> "'$i' from '${p.name}'" }}. Check your plugins and ensure there is only one external SingletonExtensionSelectors")
                }
            }
        }

        internal fun <T : Extension> selectSingleton(
            extensionType: KClass<T>,
            candidates: Collection<ExtensionRegistry<T>>
        ): T? =
            instance.selectSingleton(extensionType, candidates)
    }
}