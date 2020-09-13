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
import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.Extension
import net.mamoe.mirai.console.extension.FunctionExtension
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.internal.extension.BuiltInSingletonExtensionSelector
import net.mamoe.mirai.console.internal.extension.ExtensionRegistry
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.utils.info
import kotlin.reflect.KClass

/**
 * 用于同时拥有多个 [SingletonExtension] 时选择一个实例.
 *
 * 如有多个 [SingletonExtensionSelector] 注册, 将会停止服务器.
 */
public interface SingletonExtensionSelector : FunctionExtension {
    public data class Registry<T : Extension>(
        val plugin: Plugin,
        val extension: T,
    )

    /**
     * @return null 表示使用 builtin
     */
    public fun <T : Extension> selectSingleton(
        extensionType: KClass<T>,
        candidates: Collection<Registry<T>>,
    ): T?

    public companion object ExtensionPoint :
        AbstractExtensionPoint<SingletonExtensionSelector>(SingletonExtensionSelector::class) {

        private var instanceField: SingletonExtensionSelector? = null

        internal val instance: SingletonExtensionSelector get() = instanceField ?: error("")

        internal fun init() {
            check(instanceField == null) { "Internal error: reinitialize SingletonExtensionSelector" }
            val instances = GlobalComponentStorage.run { SingletonExtensionSelector.getExtensions() }
            instanceField = when {
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
            candidates: Collection<ExtensionRegistry<T>>,
        ): T? =
            instance.selectSingleton(extensionType, candidates.map { Registry(it.plugin, it.extension) })


        internal fun <T : Extension> SingletonExtensionSelector.selectSingleton(
            extensionType: KClass<T>,
            candidates: Collection<ExtensionRegistry<T>>,
        ): T? = selectSingleton(extensionType, candidates.map { Registry(it.plugin, it.extension) })
    }
}