/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.Services.qualifiedNameOrFail
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
public actual fun <T : Any> loadServiceOrNull(
    clazz: KClass<out T>,
    fallbackImplementation: String?
): T? =
    Services.firstImplementationOrNull(qualifiedNameOrFail(clazz)) as T?

public actual fun <T : Any> loadService(
    clazz: KClass<out T>,
    fallbackImplementation: String?
): T = loadServiceOrNull(clazz, fallbackImplementation)
    ?: error("Could not load service '${clazz.qualifiedName ?: clazz}'. Current services: ${Services.print()}")

public actual fun <T : Any> loadServices(clazz: KClass<out T>): Sequence<T> =
    Services.implementations(qualifiedNameOrFail(clazz))?.asSequence()?.map { it.value }.orEmpty().castUp()