/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("MessageKt")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmName


/*
因为文件改名为做的兼容
 */

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("isPlain")
inline fun Message.isPlain2(): Boolean = this is PlainText

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("isNotPlain")
inline fun Message.isNotPlain2(): Boolean = this !is PlainText

@PlannedRemoval("1.0.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@JvmName("repeat")
// inline: for future removal
inline fun Message.repeat2(count: Int): MessageChain {
    if (this is ConstrainSingle<*>) {
        // fast-path
        return this.asMessageChain()
    }
    return buildMessageChain(count) {
        add(this@repeat2)
    }
}
