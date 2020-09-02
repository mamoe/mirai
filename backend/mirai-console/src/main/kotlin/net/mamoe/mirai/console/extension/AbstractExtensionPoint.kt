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

import net.mamoe.mirai.console.internal.data.kClassQualifiedNameOrTip
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.contracts.contract
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass

@ConsoleExperimentalAPI
public open class AbstractExtensionPoint<T : Any>(
    @ConsoleExperimentalAPI
    public val type: KClass<T>
) {

    @ConsoleExperimentalAPI
    public data class ExtensionRegistry<T>(
        public val plugin: Plugin,
        public val extension: T
    )

    private val instances: MutableSet<ExtensionRegistry<T>> = CopyOnWriteArraySet()

    @Synchronized
    @ConsoleExperimentalAPI
    public fun registerExtension(plugin: Plugin, extension: T) {
        require(plugin.isEnabled) { "Plugin $plugin must be enabled before registering an extension." }
        instances.add(ExtensionRegistry(plugin, extension))
    }

    @Synchronized
    internal fun getExtensions(): Set<ExtensionRegistry<T>> = instances
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

internal inline fun <T : Any> AbstractExtensionPoint<T>.withExtensions(block: T.() -> Unit) {
    return withExtensions { _ -> block() }
}

@LowPriorityInOverloadResolution
internal inline fun <T : Any> AbstractExtensionPoint<T>.withExtensions(block: T.(plugin: Plugin) -> Unit) {
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

internal inline fun <T : Any, E> AbstractExtensionPoint<T>.foldExtensions(
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

internal fun <T : Any> AbstractExtensionPoint<T>.throwExtensionException(
    extension: T,
    plugin: Plugin,
    throwable: Throwable
) {
    throw ExtensionException(
        "Exception while executing extension ${extension.kClassQualifiedNameOrTip} from ${plugin.name}, registered for ${this.type.qualifiedName}",
        throwable
    )
}

internal inline fun <T : Any> AbstractExtensionPoint<T>.useExtensions(block: (extension: T) -> Unit): Unit =
    withExtensions(block)

@LowPriorityInOverloadResolution
internal inline fun <T : Any> AbstractExtensionPoint<T>.useExtensions(block: (extension: T, plugin: Plugin) -> Unit): Unit =
    withExtensions(block)