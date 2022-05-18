/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

private val lock = reentrantLock()

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
internal class SynchronizedStdoutLogger(override val identity: String?) : net.mamoe.mirai.internal.utils.StdoutLogger(
    identity
) {

    override val output: (String) -> Unit = { str ->
        lock.withLock { println(str) }
    }
}