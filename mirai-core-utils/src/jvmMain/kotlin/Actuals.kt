/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import java.util.*


public actual fun ByteArray.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}

public actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

public actual inline fun <reified E> Throwable.unwrap(addSuppressed: Boolean): Throwable {
    if (this !is E) return this
    return this.findCause { it !is E }
        ?.also { if (addSuppressed) it.addSuppressed(this) }
        ?: this
}