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

import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@ConsoleExperimentalApi
public interface ExtensionPoint<T : Extension> {
    public val type: KClass<T>

    public companion object {
        @JvmStatic
        @JvmSynthetic
        @ConsoleExperimentalApi
        public inline fun <reified T : Extension> ExtensionPoint<*>.isFor(exactType: Boolean = false): Boolean {
            return if (exactType) {
                T::class == type
            } else T::class.isSubclassOf(type)
        }
    }
}

@ConsoleExperimentalApi
public interface SingletonExtensionPoint<T : SingletonExtension<*>> : ExtensionPoint<T>

/**
 * 表示一个扩展点
 */
@ConsoleExperimentalApi
public open class AbstractExtensionPoint<T : Extension>(
    @ConsoleExperimentalApi
    public override val type: KClass<T>,
) : ExtensionPoint<T>


/**
 * 在调用一个 extension 时遇到的异常.
 *
 * @see PluginLoader.load
 * @see PluginLoader.enable
 * @see PluginLoader.disable
 * @see PluginLoader.description
 */
@ConsoleExperimentalApi
public open class ExtensionException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}