/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("HummerMessageKt")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/*
因为文件改名为做的兼容
 */

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("flash")
@SinceMirai("0.33.0")
inline fun Image.flash2(): FlashImage = FlashImage(this)

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("flash")
@JvmSynthetic
@SinceMirai("0.33.0")
inline fun GroupImage.flash2(): GroupFlashImage = FlashImage(this) as GroupFlashImage

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("flash")
@JvmSynthetic
@SinceMirai("0.33.0")
inline fun FriendImage.flash2(): FriendFlashImage = FlashImage(this) as FriendFlashImage
