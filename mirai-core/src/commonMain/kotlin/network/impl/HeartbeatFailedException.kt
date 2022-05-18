/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl

import net.mamoe.mirai.internal.network.handler.selector.NetworkException

internal class HeartbeatFailedException(
    private val name: String, // kind of HB
    override val cause: Throwable,
    recoverable: Boolean = cause is NetworkException && cause.recoverable,
) : NetworkException(recoverable) {
    override val message: String = "Exception in $name job"
    override fun toString(): String = "HeartbeatFailedException: $name, recoverable=$recoverable, cause=$cause"
}