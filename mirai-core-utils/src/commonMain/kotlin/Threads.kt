/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlin.reflect.KProperty

public operator fun <T : Any?> ThreadLocal<T>.getValue(thisRef: Any?, property: KProperty<*>): T = this.get()
public operator fun <T : Any?> ThreadLocal<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
    this.set(value)