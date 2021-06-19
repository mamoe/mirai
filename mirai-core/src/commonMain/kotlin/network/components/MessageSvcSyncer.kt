/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.*
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.addNameHierarchically
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.info
import kotlin.coroutines.CoroutineContext

internal interface MessageSvcSyncer {
    fun startSync()
    suspend fun joinSync()

    companion object : ComponentKey<MessageSvcSyncer>
}

internal class MessageSvcSyncerImpl(
    private val bot: QQAndroidBot,
    private val parentContext: CoroutineContext,
    private val logger: MiraiLogger,
) : MessageSvcSyncer {

    @Volatile
    private var scope: CoroutineScope? = null

    @Volatile
    private var job: Job? = null

    private fun initScope() {
        scope = parentContext.addNameHierarchically("MessageSvcSyncerImpl").childScope()
    }

    @Synchronized
    override fun startSync() {
        scope?.cancel()
        initScope()
        job = scope!!.launch { syncMessageSvc() }
    }

    private suspend fun syncMessageSvc() {
        logger.info { "Syncing friend message history..." }
        withTimeoutOrNull(30000) {
            launch(CoroutineName("Syncing friend message history")) {
                nextEvent<MessageSvcPbGetMsg.GetMsgSuccess> {
                    it.bot == this@MessageSvcSyncerImpl.bot
                }
            }
            MessageSvcPbGetMsg(bot.client, MsgSvc.SyncFlag.START, null).sendAndExpect(bot)
        } ?: error("timeout syncing friend message history.")
        logger.info { "Syncing friend message history: Success." }
    }

    @Synchronized
    override suspend fun joinSync() {
        job?.join()
    }
}