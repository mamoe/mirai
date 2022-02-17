/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import kotlin.reflect.KClass


/**
 * 表示一个扩展接入点(扩展类型). 在 Kotlin 由 [Extension] 的伴生对象实现, 在 Java 可通过静态字段提供.
 *
 * 在[注册扩展][ComponentStorage.contribute]时需要提供其 [ExtensionPoint], [ExtensionPoint] 也可以用于获取所有注册的扩展 ([ComponentStorage.getExtensions]).
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
@Deprecated("Please use InstanceExtensionPoint instead.", replaceWith = ReplaceWith("InstanceExtensionPoint"))
@DeprecatedSinceMirai(warningSince = "2.11")
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
/**
 * @since 2.10
 */
@ConsoleExperimentalApi
public constructor(
    extensionType: KClass<E>
) : AbstractExtensionPoint<E>(extensionType) {

    /**
     * @since 2.10
     */
    @Deprecated(
        "Default(builtin) implementations are not allowed any more. " +
                "For plugin authors, provide them with lower priority when plugin being loaded(through the ComponentScope). " +
                "For frontend implementers, provide them by `BackendAccess.globalComponentScope.contribute`. ",
        replaceWith = ReplaceWith("AbstractInstanceExtensionPoint(extensionType)"),
        level = DeprecationLevel.ERROR,
    )
    @DeprecatedSinceMirai(errorSince = "2.11") // for removal
    @ConsoleExperimentalApi
    public constructor(
        extensionType: KClass<E>,
        /**
         * 内建的实现列表.
         */
        builtinImplementations: () -> E,
    ) : this(extensionType)

    /**
     * @since 2.0
     */
    @Deprecated(
        "Default(builtin) implementations are not allowed any more. " +
                "For plugin authors, provide them with lower priority when plugin being loaded(through the ComponentScope). " +
                "For frontend implementers, provide them by `BackendAccess.globalComponentScope.contribute`. ",
        replaceWith = ReplaceWith("AbstractInstanceExtensionPoint(extensionType)"),
        level = DeprecationLevel.ERROR
    )
    @DeprecatedSinceMirai(errorSince = "2.11") // for removal
    @ConsoleExperimentalApi // was experimental since 2.0
    public constructor(extensionType: KClass<E>, vararg builtinImplementations: E) : this(
        extensionType,
    )
}

@Deprecated(
    "Please use AbstractInstanceExtensionPoint instead.",
    replaceWith = ReplaceWith(
        "AbstractInstanceExtension",
        "net.mamoe.mirai.console.extension.AbstractInstanceExtensionPoint"
    )
)
@DeprecatedSinceMirai(warningSince = "2.11")
@Suppress("DEPRECATION")
public abstract class AbstractSingletonExtensionPoint<E : SingletonExtension<T>, T>
/**
 * @since 2.10
 */
@ConsoleExperimentalApi
constructor(
    extensionType: KClass<E>,
    /**
     * 内建的实现.
     */
    @ConsoleExperimentalApi
    public val builtinImplementation: () -> T,
) : AbstractExtensionPoint<E>(extensionType), SingletonExtensionPoint<E> {

    /**
     * @since 2.0
     */
    @Suppress("USELESS_CAST") // compiler bug
    @ConsoleExperimentalApi
    public constructor(extensionType: KClass<E>, builtinImplementation: T) : this(
        extensionType,
        { builtinImplementation } as () -> T
    )

    /**
     * 由 [SingletonExtensionSelector] 选择后的实例.
     */
    @ConsoleExperimentalApi
    public open val selectedInstance: T
        get() = throw UnsupportedOperationException("SingletonExtension has been deprecated.")
}