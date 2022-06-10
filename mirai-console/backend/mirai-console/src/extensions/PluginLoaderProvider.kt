/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.Extension
import net.mamoe.mirai.console.extension.InstanceExtension
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.utils.DeprecatedSinceMirai

/**
 * 提供扩展 [PluginLoader]
 *
 * @see PluginComponentStorage.contributePluginLoader
 *
 *
 * @see Extension
 * @see PluginLoader
 *
 * @see PluginLoaderProviderImplLazy
 */
public interface PluginLoaderProvider : InstanceExtension<PluginLoader<*, *>> {
    public companion object ExtensionPoint : AbstractExtensionPoint<PluginLoaderProvider>(PluginLoaderProvider::class)
}

@Deprecated("Please implement your own PluginLoaderProvider.")
@DeprecatedSinceMirai(warningSince = "2.11") // for hidden.
public class PluginLoaderProviderImpl(override val instance: PluginLoader<*, *>) : PluginLoaderProvider

@Deprecated("Please implement your own PluginLoaderProvider.")
@DeprecatedSinceMirai(warningSince = "2.11") // for hidden.
public class PluginLoaderProviderImplLazy(initializer: () -> PluginLoader<*, *>) : PluginLoaderProvider {
    override val instance: PluginLoader<*, *> by lazy(initializer)
}