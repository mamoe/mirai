/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

private var isAddSuppressedSupported: Boolean = true

internal actual fun Throwable.addSuppressedMirai(e: Throwable) {
    if (this == e) {
        return
    }
    if (!isAddSuppressedSupported) {
        return
    }
    try {
        this.addSuppressed(e)
    } catch (e: Exception) {
        isAddSuppressedSupported = false
    }
}