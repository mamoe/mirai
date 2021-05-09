/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.RequiresOptIn.Level.ERROR
import kotlin.annotation.AnnotationTarget.*


@RequiresOptIn("This can only be used in tests.", level = ERROR)
@Target(CLASS, FUNCTION, PROPERTY)
@Retention(AnnotationRetention.BINARY)
public annotation class TestOnly