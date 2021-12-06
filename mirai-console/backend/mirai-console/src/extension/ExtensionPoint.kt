/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.reflect.KClass


/**
 * 由 [Extension] 的伴生对象实现.
 *
 * @see AbstractExtensionPoint
 */
public interface ExtensionPoint<T : Extension> {
    /**
     * 扩展实例 [T] 的类型
     */
    public val extensionType: KClass<T>
}

public abstract class AbstractExtensionPoint<T : Extension>(
    public override val extensionType: KClass<T>,
) : ExtensionPoint<T>


/**
 * 表示一个 [SingletonExtension] 的 [ExtensionPoint]
 */
public interface SingletonExtensionPoint<T : SingletonExtension<*>> : ExtensionPoint<T>

/**
 * 表示一个 [InstanceExtension] 的 [ExtensionPoint]
 */
public interface InstanceExtensionPoint<T : InstanceExtension<*>> : ExtensionPoint<T>

/**
 * 表示一个 [FunctionExtension] 的 [ExtensionPoint]
 */
public interface FunctionExtensionPoint<T : FunctionExtension> : ExtensionPoint<T>


public abstract class AbstractInstanceExtensionPoint<E : InstanceExtension<T>, T>
@ConsoleExperimentalApi constructor(
    extensionType: KClass<E>,
    /**
     * 内建的实现列表.
     */
    @ConsoleExperimentalApi
    public vararg val builtinImplementations: E,
) : AbstractExtensionPoint<E>(extensionType)

public abstract class AbstractSingletonExtensionPoint<E : SingletonExtension<T>, T>
@ConsoleExperimentalApi constructor(
    extensionType: KClass<E>,
    /**
     * 内建的实现.
     */
    @ConsoleExperimentalApi
    public val builtinImplementation: T,
) : AbstractExtensionPoint<E>(extensionType), SingletonExtensionPoint<E> {

    /**
     * 由 [SingletonExtensionSelector] 选择后的实例.
     */
    @ConsoleExperimentalApi
    public open val selectedInstance: T by lazy {
        GlobalComponentStorage.run {
            this@AbstractSingletonExtensionPoint.findSingletonInstance(
                extensionType,
                builtinImplementation
            )
        }
    }
}