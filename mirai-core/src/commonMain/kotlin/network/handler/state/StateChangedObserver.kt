/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport

@Suppress("FunctionName")
internal fun StateChangedObserver(
    to: State,
    action: (new: NetworkHandlerSupport.BaseStateImpl) -> Unit
): StateObserver {
    return object : StateChangedObserver(to) {
        override fun stateChanged0(
            networkHandler: NetworkHandlerSupport,
            previous: NetworkHandlerSupport.BaseStateImpl,
            new: NetworkHandlerSupport.BaseStateImpl
        ) {
            action(new)
        }
    }
}

@Suppress("FunctionName")
internal fun StateChangedObserver(
    from: State,
    to: State,
    action: (new: NetworkHandlerSupport.BaseStateImpl) -> Unit
): StateObserver {
    return object : StateObserver {
        override fun stateChanged(
            networkHandler: NetworkHandlerSupport,
            previous: NetworkHandlerSupport.BaseStateImpl,
            new: NetworkHandlerSupport.BaseStateImpl
        ) {
            if (previous.correspondingState == from && new.correspondingState == to) {
                action(new)
            }
        }
    }
}

internal abstract class StateChangedObserver(
    val state: State,
) : StateObserver {
    protected abstract fun stateChanged0(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    )

    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        if (previous.correspondingState != state && new.correspondingState == state) {
            stateChanged0(networkHandler, previous, new)
        }
    }
}