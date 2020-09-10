/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.internal.data.kClassQualifiedNameOrTip
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.contracts.contract
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@ConsoleExperimentalAPI
public interface ExtensionPoint<T : Extension> {
    public val type: KClass<T>

    public fun registerExtension(plugin: Plugin, extension: T)
    public fun getExtensions(): Set<ExtensionRegistry<T>>

    public companion object {
        @JvmStatic
        @JvmSynthetic
        @ConsoleExperimentalAPI
        public inline fun <reified T : Extension> ExtensionPoint<*>.isFor(exactType: Boolean = false): Boolean {
            return if (exactType) {
                T::class == type
            } else T::class.isSubclassOf(type)
        }
    }
}

@ConsoleExperimentalAPI
public interface SingletonExtensionPoint<T : SingletonExtension<*>> : ExtensionPoint<T> {
    public companion object {
        @JvmStatic
        @ConsoleExperimentalAPI
        public fun <T : SingletonExtension<*>> SingletonExtensionPoint<T>.findSingleton(): T? {
            return SingletonExtensionSelector.selectSingleton(type, this.getExtensions())
        }
    }
}

/**
 * 表示一个扩展点
 */
@ConsoleExperimentalAPI
public open class AbstractExtensionPoint<T : Extension>(
    @ConsoleExperimentalAPI
    public override val type: KClass<T>
) : ExtensionPoint<T> {
    init {
        @Suppress("LeakingThis")
        allExtensionPoints.add(this)
    }

    private val instances: MutableSet<ExtensionRegistry<T>> = CopyOnWriteArraySet()

    @ConsoleExperimentalAPI
    public override fun registerExtension(plugin: Plugin, extension: T) {
        // require(plugin.isEnabled) { "Plugin $plugin must be enabled before registering an extension." }
        requireNotNull(extension::class.qualifiedName) { "Extension must not be an anonymous object" }
        instances.add(ExtensionRegistry(plugin, extension))
    }

    public override fun getExtensions(): Set<ExtensionRegistry<T>> = Collections.unmodifiableSet(instances)

    internal companion object {
        @ConsoleExperimentalAPI
        internal val allExtensionPoints: MutableList<AbstractExtensionPoint<*>> = mutableListOf()
    }
}


/**
 * 在调用一个 extension 时遇到的异常.
 *
 * @see PluginLoader.load
 * @see PluginLoader.enable
 * @see PluginLoader.disable
 * @see PluginLoader.description
 */
@ConsoleExperimentalAPI
public open class ExtensionException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

internal inline fun <T : Extension> AbstractExtensionPoint<out T>.withExtensions(block: T.() -> Unit) {
    return withExtensions { _ -> block() }
}

@LowPriorityInOverloadResolution
internal inline fun <T : Extension> AbstractExtensionPoint<out T>.withExtensions(block: T.(plugin: Plugin) -> Unit) {
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

internal inline fun <T : Extension, E> AbstractExtensionPoint<out T>.foldExtensions(
    initial: E,
    block: (acc: E, extension: T) -> E
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

internal fun <T : Extension> AbstractExtensionPoint<out T>.throwExtensionException(
    extension: T,
    plugin: Plugin,
    throwable: Throwable
) {
    throw ExtensionException(
        "Exception while executing extension ${extension.kClassQualifiedNameOrTip} provided by plugin '${plugin.name}', registered for ${this.type.qualifiedName}",
        throwable
    )
}

internal inline fun <T : Extension> AbstractExtensionPoint<T>.useExtensions(block: (extension: T) -> Unit): Unit =
    withExtensions(block)

@LowPriorityInOverloadResolution
internal inline fun <T : Extension> AbstractExtensionPoint<T>.useExtensions(block: (extension: T, plugin: Plugin) -> Unit): Unit =
    withExtensions(block)