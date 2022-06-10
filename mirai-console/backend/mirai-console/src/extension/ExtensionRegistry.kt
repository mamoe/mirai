/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 一个已经注册的 [Extension]. 可通过 [ComponentStorage.getExtensions] 获得.
 *
 * @since 2.11
 */
@NotStableForInheritance
public interface ExtensionRegistry<E : Extension> {
    /**
     * 提供该 [ExtensionRegistry] 的插件. 若为 `null` 则表示由 Mirai Console 内置或者由前端实现.
     */
    public val plugin: Plugin?

    /**
     * [Extension] 实例.
     */
    public val extension: E
}

internal inline val <T> ExtensionRegistry<out InstanceExtension<T>>.instance get() = extension.instance

internal class ExtensionRegistryImpl<E : Extension> internal constructor(
    /**
     * 提供该 [ExtensionRegistry] 的插件. 若为 `null` 则表示由 Mirai Console 内置或者由前端实现.
     */
    override val plugin: Plugin?,
    /**
     * [Extension] 实例.
     */
    extensionInitializer: () -> E,
) : ExtensionRegistry<E> {
    override val extension: E by lazy(extensionInitializer)
}
