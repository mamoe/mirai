/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.errors.*

public expect interface Closeable {
    @Throws(IOException::class)
    public fun close()
}

public expect fun Closeable.asKtorCloseable(): io.ktor.utils.io.core.Closeable
public expect fun io.ktor.utils.io.core.Closeable.asMiraiCloseable(): io.ktor.utils.io.core.Closeable