/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.message

internal class SendMessageStateController(
    initialState: SendMessageState = SendMessageState.UNINITIALIZED,
) {
    var state: SendMessageState = initialState
        private set

    val stateAvailability: MutableMap<SendMessageState, Boolean> = mutableMapOf()

    fun nextState() {
        state = when (state) {
            SendMessageState.UNINITIALIZED -> SendMessageState.ORIGIN
            SendMessageState.ORIGIN -> SendMessageState.LONG
            SendMessageState.LONG -> SendMessageState.FRAGMENTED
            SendMessageState.FRAGMENTED -> throw IllegalStateException("Failed to send message: all strategies tried out.")
        }
        if (stateAvailability[state] == false) {
            nextState()
        }
    }
}

internal enum class SendMessageState {
    UNINITIALIZED,
    ORIGIN,
    LONG,
    FRAGMENTED
}

internal val SendMessageState.isFragmented: Boolean get() = this == SendMessageState.FRAGMENTED
internal val SendMessageState.isLong: Boolean get() = this == SendMessageState.LONG
