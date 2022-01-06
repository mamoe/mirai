/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.network.handler.NetworkHandler
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun <R> NetworkHandler.useNetworkHandler(action: NetworkHandler.(handler: NetworkHandler) -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    val r = kotlin.runCatching { action(this) }
    r.fold(
        onSuccess = { close(null) },
        onFailure = { close(it) }
    )
    return r.getOrThrow()
}