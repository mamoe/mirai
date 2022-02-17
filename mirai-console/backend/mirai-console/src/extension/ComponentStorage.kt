/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.util.stream.Stream

/**
 * 组件容器, 容纳 [Plugin] 注册的 [Extension].
 *
 * 插件可在 [JvmPlugin.onLoad] 时提供扩展. 前端可在 [MiraiConsoleImplementation.BackendAccess.globalComponentStorage] 获取全局组件容器.
 * 目前未允许获取全局组件容器. 如有需求请 [提交 issues](https://github.com/mamoe/mirai/issues/new/choose).
 *
 * 实现细节: 线程安全.
 *
 * @see Extension
 * @see JvmPlugin.onLoad
 */
public interface ComponentStorage {

    /**
     * 注册一个扩展
     */
    public fun <E : Extension> contribute(
        extensionPoint: ExtensionPoint<E>,
        plugin: Plugin,
        extensionInstance: E,
    ) // 2.11: E changed to T (only naming)

    /**
     * 注册一个扩展. [lazyInstance] 将会在 [getExtensions] 时才会计算.
     */
    public fun <E : Extension> contribute(
        extensionPoint: ExtensionPoint<E>,
        plugin: Plugin,
        lazyInstance: () -> E,
    ) // 2.11: E changed to T (only naming)

    /**
     * 获取优先级最高的 [ExtensionPoint] 扩展实例. 在未找到任何注册的实例时抛出 [NoSuchElementException].
     *
     * @since 2.11
     */
    @MiraiExperimentalApi
    public fun <E : Extension> getPreferredExtension(
        extensionPoint: ExtensionPoint<E>,
    ): ExtensionRegistry<E> {
        return getExtensions(extensionPoint).firstOrNull()
            ?: throw NoSuchElementException("No extension registered for $extensionPoint")
    }

    /**
     * 获取注册的 [ExtensionPoint] 扩展实例列表. 返回的 [Sequence] 以 [Extension.priority] 倒序排序.
     *
     * @since 2.11
     */
    public fun <E : Extension> getExtensions(
        extensionPoint: ExtensionPoint<E>,
    ): Sequence<ExtensionRegistry<E>>

    /**
     * 获取注册的 [ExtensionPoint] 扩展实例列表. 返回的 [Stream] 以 [Extension.priority] 倒序排序.
     *
     * @since 2.11
     */
    public fun <E : Extension> getExtensionsStream(
        extensionPoint: ExtensionPoint<E>,
    ): Stream<ExtensionRegistry<E>>
}

/**
 * 仅前端实现可用
 */
@ConsoleFrontEndImplementation
public interface ComponentStorageInternal : ComponentStorage {

    /**
     * 注册一个由 Mirai Console 实现的扩展 (因此没有相关 [Plugin]). [lazyInstance] 将会在 [getExtensions] 时才会计算.
     */
    public fun <E : Extension> contributeConsole(
        extensionPoint: ExtensionPoint<E>,
        lazyInstance: () -> E,
    )

    /**
     * 注册一个由 Mirai Console 实现的扩展 (因此没有相关 [Plugin]).
     */
    public fun <E : Extension> contributeConsole(
        extensionPoint: ExtensionPoint<E>,
        instance: E,
    ) {
        @Suppress("USELESS_CAST") // bug
        contributeConsole(extensionPoint, { instance } as () -> E)
    }
}
