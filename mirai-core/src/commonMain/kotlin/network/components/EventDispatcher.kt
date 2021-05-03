/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.addNameHierarchically
import net.mamoe.mirai.utils.childScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal interface EventDispatcher {
    suspend fun broadcast(event: Event)
    fun broadcastAsync(event: Event, additionalContext: CoroutineContext = EmptyCoroutineContext)

    /**
     * Join all jobs. Joins also jobs launched during this call.
     */
    suspend fun joinBroadcast()

    companion object : ComponentKey<EventDispatcher>
}


internal class EventDispatcherImpl(
    private val lifecycleContext: CoroutineContext,
    private val logger: MiraiLogger,
) : EventDispatcher,
    CoroutineScope by lifecycleContext
        .addNameHierarchically("EventDispatcher")
        .childScope() {

    override suspend fun broadcast(event: Event) {
        try {
            event.broadcast()
        } catch (e: Exception) {
            if (logger.isEnabled) {
                val msg = optimizeEventToString(event)
                logger.error(IllegalStateException("Exception while broadcasting event '$msg'", e))
            }
        }
    }

    override fun broadcastAsync(event: Event, additionalContext: CoroutineContext) {
        launch(additionalContext) { broadcast(event) }
    }

    private fun optimizeEventToString(event: Event): String {
        val qualified = event::class.java.canonicalName ?: return event.toString()
        return qualified.substringAfter("net.mamoe.mirai.event.events.", "").ifEmpty { event.toString() }
    }

    override suspend fun joinBroadcast() {
        for (child in coroutineContext.job.children) {
            child.join()
        }
    }
}