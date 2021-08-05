/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.reflect.KClass

public expect fun <T : Any> loadServiceOrNull(clazz: KClass<out T>, fallbackImplementation: String? = null): T?
public expect fun <T : Any> loadService(clazz: KClass<out T>, fallbackImplementation: String? = null): T

public inline fun <reified T : Any> loadService(fallbackImplementation: String? = null): T =
    loadService(T::class, fallbackImplementation)

public inline fun <reified T : Any> loadService(noinline fallbackImplementation: () -> T): T =
    loadServiceOrNull(T::class) ?: fallbackImplementation()