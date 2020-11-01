/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Utils")
@file:JvmMultifileClass

package net.mamoe.mirai.internal.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

internal inline class MacOrAndroidIdChangeFlag(val value: Long = 0) {
    fun macChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x1)

    fun androidIdChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x2)

    fun guidChanged(): MacOrAndroidIdChangeFlag =
        MacOrAndroidIdChangeFlag(this.value or 0x3)

    companion object {
        val NoChange: MacOrAndroidIdChangeFlag get() = MacOrAndroidIdChangeFlag()
    }
}