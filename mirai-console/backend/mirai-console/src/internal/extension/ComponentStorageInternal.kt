/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.extension

import net.mamoe.mirai.console.extension.*
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.data.kClassQualifiedNameOrTip
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.name
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream
import kotlin.contracts.contract

internal class GlobalComponentStorageImpl : AbstractConcurrentComponentStorage()

// source compatibility for <2.11
internal val GlobalComponentStorage get() = MiraiConsoleImplementationBridge.globalComponentStorage

/**
 * thread-safe.
 */
internal abstract class AbstractConcurrentComponentStorage : ComponentStorage, ComponentStorageInternal {
    ///////////////////////////////////////////////////////////////////////////
    // registry implementation
    ///////////////////////////////////////////////////////////////////////////

    /**
     * For each [ExtensionPoint]. thread-safe.
     */
    internal class Registries<T : Extension> {
        @Volatile
        private var data: MutableCollection<ExtensionRegistry<T>> = ArrayList()
        private val lock = Any()

        fun register(registry: ExtensionRegistry<T>) {
            synchronized(lock) {
                val list = PriorityQueue(
                    data.size + 1,
                    Comparator.comparing<ExtensionRegistry<T>, Int> { it.extension.priority }.reversed()
                )
                list.addAll(data)
                list.add(registry)
                data = list
            }
        }

        /**
         * @return thread-safe Sequence
         */
        fun asSequence(): Sequence<ExtensionRegistry<T>> = data.asSequence()

        /**
         * @return thread-safe Sequence
         */
        fun asStream(): Stream<ExtensionRegistry<T>> = data.stream()
    }

    private val registries: MutableMap<ExtensionPoint<*>, Registries<*>> = ConcurrentHashMap()

    private fun <T : Extension> getRegistries(ep: ExtensionPoint<T>): Registries<T> {
        @Suppress("UNCHECKED_CAST")
        return registries.getOrPut(ep) { Registries<T>() } as Registries<T>
    }

    private fun <T : Extension> registerExtension(ep: ExtensionPoint<T>, registry: ExtensionRegistry<T>) {
        getRegistries(ep).register(registry)
    }


    ///////////////////////////////////////////////////////////////////////////
    // public API implementation
    ///////////////////////////////////////////////////////////////////////////


    internal fun mergeWith(another: AbstractConcurrentComponentStorage) {
        another.registries.forEach { (ep, registries) ->
            for (extensionRegistry in registries.asSequence()) {
                @Suppress("UNCHECKED_CAST")
                ep as ExtensionPoint<Extension>
                registerExtension(ep, ExtensionRegistryImpl(extensionRegistry.plugin) { extensionRegistry.extension })
            }
        }
    }

    internal inline fun <T : Extension> useEachExtensions(
        extensionPoint: ExtensionPoint<T>,
        block: ExtensionRegistry<T>.(instance: T) -> Unit
    ) {
        contract { callsInPlace(block) }
        getExtensions(extensionPoint).forEach { registry ->
            val plugin = registry.plugin
            val extension = registry.extension
            kotlin.runCatching {
                block.invoke(registry, registry.extension)
            }.getOrElse { throwable ->
                extensionPoint.throwExtensionException(extension, plugin, throwable)
            }
        }
    }

    internal inline fun <T : Extension, E> foldExtensions(
        extensionPoint: ExtensionPoint<T>,
        initial: E,
        block: (acc: E, extension: T) -> E,
    ): E {
        contract { callsInPlace(block) }
        var e: E = initial
        getExtensions(extensionPoint).forEach { registry ->
            val plugin = registry.plugin
            val extension = registry.extension
            kotlin.runCatching {
                e = block.invoke(e, extension)
            }.getOrElse { throwable ->
                extensionPoint.throwExtensionException(extension, plugin, throwable)
            }
        }
        return e
    }

    internal fun <T : Extension> ExtensionPoint<out T>.throwExtensionException(
        extension: T,
        plugin: Plugin?,
        throwable: Throwable,
    ): Nothing {
        throw ExtensionException(
            "Exception while executing extension '${extension.kClassQualifiedNameOrTip}' provided by plugin '${plugin?.name ?: "<builtin>"}', registered for '${this.extensionType.qualifiedName}'",
            throwable
        )
    }

    override fun <E : Extension> contribute(
        extensionPoint: ExtensionPoint<E>,
        plugin: Plugin,
        extensionInstance: E,
    ) {
        registerExtension(extensionPoint, ExtensionRegistryImpl(plugin) { extensionInstance })
    }

    override fun <E : Extension> contributeConsole(extensionPoint: ExtensionPoint<E>, lazyInstance: () -> E) {
        registerExtension(extensionPoint, ExtensionRegistryImpl(null, lazyInstance))
    }

    override fun <E : Extension> contribute(
        extensionPoint: ExtensionPoint<E>,
        plugin: Plugin,
        lazyInstance: () -> E,
    ) {
        registerExtension(extensionPoint, ExtensionRegistryImpl(plugin, lazyInstance))
    }

    override fun <E : Extension> getExtensions(extensionPoint: ExtensionPoint<E>): Sequence<ExtensionRegistry<E>> {
        return getRegistries(extensionPoint).asSequence()
    }

    override fun <E : Extension> getExtensionsStream(extensionPoint: ExtensionPoint<E>): Stream<ExtensionRegistry<E>> {
        return getRegistries(extensionPoint).asStream()
    }
}