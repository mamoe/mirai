/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.util

import net.mamoe.mirai.console.internal.data.cast
import net.mamoe.mirai.console.internal.data.createInstanceOrNull
import java.io.InputStream
import kotlin.reflect.KClass

@Suppress("unused")
internal class ServiceList<T>(
    internal val classLoader: ClassLoader,
    internal val delegate: List<String>
)

internal object ServiceHelper {
    inline fun <reified T : Any> ClassLoader.findServices(): ServiceList<T> = findServices(T::class)

    fun <T : Any> ClassLoader.findServices(vararg serviceTypes: KClass<out T>): ServiceList<T> =
        serviceTypes.flatMap { serviceType ->
            getResourceAsStream("META-INF/services/" + serviceType.qualifiedName!!)
                ?.use(InputStream::readBytes)
                ?.let(::String)?.lines()?.filter(String::isNotBlank).orEmpty()
        }.let { ServiceList(this, it) }

    fun <T : Any> ServiceList<T>.loadAllServices(): List<T> {
        return delegate.mapNotNull { classLoader.loadService<T>(it) }
    }

    fun <T : Any> ClassLoader.loadService(
        classname: String
    ): T? {
        return kotlin.runCatching {
            val clazz =
                Class.forName(classname, true, this).cast<Class<out T>>()
            @Suppress("UNCHECKED_CAST")
            clazz.kotlin.objectInstance
                ?: clazz.kotlin.createInstanceOrNull()
                ?: clazz.constructors.firstOrNull { it.parameterCount == 0 }?.newInstance() as T?
                ?: error("Cannot find a no-arg constructor")
        }.getOrElse {
            throw ServiceLoadException("Could not load service ${classname}.", it)

            /*
            logger.error(
                { "Could not load PluginLoader ${pluginQualifiedName}." },
                PluginLoadException("Could not load PluginLoader ${pluginQualifiedName}.", it)
            )*/
        }
    }
}

@Suppress("unused", "RedundantVisibilityModifier")
internal open class ServiceLoadException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}