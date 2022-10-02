/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package network.framework.components

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.network.components.EventDispatcherImpl
import net.mamoe.mirai.internal.network.handler.NetworkHandler.Companion.runUnwrapCancellationException
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TestOnly
import kotlin.coroutines.CoroutineContext

internal open class TestEventDispatcherImpl(
    lifecycleContext: CoroutineContext,
    logger: MiraiLogger,
) : EventDispatcherImpl(lifecycleContext, logger) {
    override suspend fun broadcast(event: Event) {
        runUnwrapCancellationException {
            if (isActive) {
                // This requires the scope to be active, while the original one doesn't.

                // so that [joinBroadcast] works.
                launch(
                    start = CoroutineStart.UNDISPATCHED
                ) {
                    super.broadcast(event)
                }.join()
            } else {
                // Scope closed, typically when broadcasting `BotOfflineEvent` by StateObserver from `bot.close`
                super.broadcast(event)
            }
        }
    }

    @OptIn(TestOnly::class)
    override suspend fun joinBroadcast() {
        for (child in coroutineContext.job.children) {
            child.join()
        }
    }
}