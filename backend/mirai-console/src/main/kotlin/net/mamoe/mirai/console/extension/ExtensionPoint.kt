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

import kotlin.reflect.KClass

/**
 * 由 [Extension] 的 `companion` 实现.
 */
public interface ExtensionPoint<T : Extension> {
    public val extensionType: KClass<T>
}

public open class AbstractExtensionPoint<T : Extension>(
    public override val extensionType: KClass<T>,
) : ExtensionPoint<T>

/**
 * 表示一个 [SingletonExtension] 的 [ExtensionPoint]
 */
public interface SingletonExtensionPoint<T : SingletonExtension<*>> : ExtensionPoint<T>