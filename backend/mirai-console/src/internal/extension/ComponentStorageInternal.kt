/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.extension

import net.mamoe.mirai.console.extension.*
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector.ExtensionPoint.selectSingleton
import net.mamoe.mirai.console.internal.data.kClassQualifiedNameOrTip
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.name
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * The [ComponentStorage] containing all components provided by Mirai Console internals and installed plugins.
 */
internal object GlobalComponentStorage : AbstractConcurrentComponentStorage()
internal interface ExtensionRegistry<out E : Extension> {
    val plugin: Plugin?
    val extension: E

    operator fun component1(): Plugin? {
        return this.plugin
    }

    operator fun component2(): E {
        return this.extension
    }
}

internal class LazyExtensionRegistry<out E : Extension>(
    override val plugin: Plugin?,
    initializer: () -> E,
) : ExtensionRegistry<E> {
    override val extension: E by lazy { initializer() }
}

internal data class DataExtensionRegistry<out E : Extension>(
    override val plugin: Plugin?,
    override val extension: E,
) : ExtensionRegistry<E>

internal abstract class AbstractConcurrentComponentStorage : ComponentStorage {
    private val instances: MutableMap<ExtensionPoint<*>, MutableSet<ExtensionRegistry<*>>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Extension> ExtensionPoint<out T>.getExtensions(): Set<ExtensionRegistry<T>> {
        val userDefined = instances.getOrPut(this, ::CopyOnWriteArraySet) as Set<ExtensionRegistry<T>>

        val builtins = if (this is AbstractInstanceExtensionPoint<*, *>) {
            this.builtinImplementations.mapTo(HashSet()) {
                DataExtensionRegistry(
                    null,
                    it
                )
            } as Set<ExtensionRegistry<T>>
        } else null

        return builtins?.plus(userDefined) ?: userDefined
    }

    // unused for now
    internal fun removeExtensionsRegisteredByPlugin(plugin: Plugin) {
        instances.forEach { (_, u) ->
            u.removeAll { it.plugin == plugin }
        }
    }

    internal fun mergeWith(another: AbstractConcurrentComponentStorage) {
        for ((ep, list) in another.instances) {
            for (extensionRegistry in list) {
                @Suppress("UNCHECKED_CAST")
                ep as ExtensionPoint<Extension>
                this.contribute(ep, extensionRegistry.plugin, lazyInstance = { extensionRegistry.extension })
            }
        }
    }

    internal inline fun <T : Extension> ExtensionPoint<out T>.withExtensions(block: T.() -> Unit) {
        return withExtensions { _ -> block() }
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    internal inline fun <T : Extension> ExtensionPoint<out T>.withExtensions(block: T.(plugin: Plugin?) -> Unit) {
        contract {
            callsInPlace(block)
        }
        for ((plugin, extension) in this.getExtensions()) {
            kotlin.runCatching {
                block.invoke(extension, plugin)
            }.getOrElse { throwable ->
                throwExtensionException(extension, plugin, throwable)
            }
        }
    }

    internal inline fun <reified E : SingletonExtension<*>> ExtensionPoint<out E>.findSingleton(builtin: E): E =
        findSingleton(E::class, builtin)

    internal fun <E : SingletonExtension<*>> ExtensionPoint<out E>.findSingleton(type: KClass<E>, builtin: E): E {
        val candidates = this.getExtensions()
        return when (candidates.size) {
            0 -> builtin
            1 -> candidates.single().extension
            else -> SingletonExtensionSelector.instance.selectSingleton(type, candidates) ?: builtin
        }
    }

    internal inline fun <reified E : SingletonExtension<T>, T> ExtensionPoint<out E>.findSingletonInstance(builtin: T): T =
        findSingletonInstance(E::class, builtin)

    internal fun <E : SingletonExtension<T>, T> ExtensionPoint<out E>.findSingletonInstance(
        type: KClass<E>,
        builtin: T,
    ): T {
        val candidates = this.getExtensions()
        return when (candidates.size) {
            0 -> builtin
            1 -> candidates.single().extension.instance
            else -> SingletonExtensionSelector.instance.selectSingleton(type, candidates)?.instance ?: builtin
        }
    }

    internal inline fun <T : Extension, E> ExtensionPoint<out T>.foldExtensions(
        initial: E,
        block: (acc: E, extension: T) -> E,
    ): E {
        contract {
            callsInPlace(block)
        }
        var e: E = initial
        for ((plugin, extension) in this.getExtensions()) {
            kotlin.runCatching {
                e = block.invoke(e, extension)
            }.getOrElse { throwable ->
                throwExtensionException(extension, plugin, throwable)
            }
        }
        return e
    }

    internal fun <T : Extension> ExtensionPoint<out T>.throwExtensionException(
        extension: T,
        plugin: Plugin?,
        throwable: Throwable,
    ) {
        throw ExtensionException(
            "Exception while executing extension '${extension.kClassQualifiedNameOrTip}' provided by plugin '${plugin?.name ?: "<builtin>"}', registered for '${this.extensionType.qualifiedName}'",
            throwable
        )
    }

    internal inline fun <T : Extension> ExtensionPoint<T>.useExtensions(block: (extension: T) -> Unit): Unit =
        withExtensions(block)

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    @kotlin.internal.LowPriorityInOverloadResolution
    internal inline fun <T : Extension> ExtensionPoint<T>.useExtensions(block: (extension: T, plugin: Plugin?) -> Unit): Unit =
        withExtensions(block)

    override fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin,
        extensionInstance: T,
    ) {
        instances.getOrPut(extensionPoint, ::CopyOnWriteArraySet).add(DataExtensionRegistry(plugin, extensionInstance))
    }

    @JvmName("contribute1")
    fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin?,
        extensionInstance: T,
    ) {
        instances.getOrPut(extensionPoint, ::CopyOnWriteArraySet).add(DataExtensionRegistry(plugin, extensionInstance))
    }

    override fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin,
        lazyInstance: () -> T,
    ) {
        instances.getOrPut(extensionPoint, ::CopyOnWriteArraySet).add(LazyExtensionRegistry(plugin, lazyInstance))
    }

    @JvmName("contribute1")
    fun <T : Extension> contribute(
        extensionPoint: ExtensionPoint<T>,
        plugin: Plugin?,
        lazyInstance: () -> T,
    ) {
        instances.getOrPut(extensionPoint, ::CopyOnWriteArraySet).add(LazyExtensionRegistry(plugin, lazyInstance))
    }
}