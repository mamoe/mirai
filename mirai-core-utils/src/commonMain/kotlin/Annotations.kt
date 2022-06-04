/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.RequiresOptIn.Level.ERROR
import kotlin.annotation.AnnotationTarget.*


@RequiresOptIn("This can only be used in tests.", level = ERROR)
@Target(CLASS, FUNCTION, PROPERTY, CLASS, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
public annotation class TestOnly

/**
 * 标注 API 弃用记录, 用于在将来提升弃用等级.
 *
 * 注意, 在使用时必须使用 named arguments, 如:
 * ```
 * @DeprecatedSinceMirai(warningSince = "2.9")
 * ```
 *
 * @since 2.9.0-RC
 */ // https://github.com/mamoe/mirai/issues/1669
@Target(
    CLASS,
    PROPERTY,
    CONSTRUCTOR,
    FUNCTION,
    TYPEALIAS
)
public annotation class DeprecatedSinceMirai(
    val warningSince: String = "",
    val errorSince: String = "",
    val hiddenSince: String = "",
    val internalSince: String = "",
)