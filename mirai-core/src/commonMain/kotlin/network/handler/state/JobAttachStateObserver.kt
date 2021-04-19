/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The [StateObserver] that attaches a job to the [CoroutineScope] of the state.
 */
internal class JobAttachStateObserver(
    private val name: String,
    targetState: NetworkHandler.State,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val job: suspend CoroutineScope.(state: NetworkHandlerSupport.BaseStateImpl) -> Unit,
) : StateChangedObserver(targetState) {
    override fun stateChanged0(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        new.launch(CoroutineName(name) + coroutineContext) {
            try {
                job(new)
            } catch (e: Throwable) {
                throw IllegalStateException("Exception in attached Job '$name'", e)
            }
        }
    }

    override fun toString(): String {
        return "JobAttachStateObserver(name=$name)"
    }
}