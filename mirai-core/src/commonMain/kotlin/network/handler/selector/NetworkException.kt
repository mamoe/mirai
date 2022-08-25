/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.utils.unwrap

/**
 * 已知的网络异常.
 *
 * 在 [NetworkHandlerSupport.BaseStateImpl] 中抛出此异常,
 * 或用此异常 [close NetworkHandler][NetworkHandler.close], 可建议 [Selector][SelectorNetworkHandler] 是否执行重连.
 */
internal open class NetworkException : Exception {
    /**
     * If true, the selector may recover the network handler by some means.
     */
    val recoverable: Boolean

    constructor(recoverable: Boolean) : super() {
        this.recoverable = recoverable
    }

    constructor(recoverable: Boolean, cause: Throwable?) : super(cause) {
        this.recoverable = recoverable
    }

    constructor(message: String, recoverable: Boolean) : super(message) {
        this.recoverable = recoverable
    }

    constructor(message: String, cause: Throwable?, recoverable: Boolean) : super(message, cause) {
        this.recoverable = recoverable
    }

    /**
     * Returns the exception to be thrown for public API.
     */
    open fun unwrapForPublicApi(): Throwable {
        return this.unwrap<NetworkException>()
    }
}
