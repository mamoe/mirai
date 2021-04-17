/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.components

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.component.ComponentStorage
import net.mamoe.mirai.internal.network.handler.state.StateChangedObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info

internal interface BotInitProcessor {
    suspend fun init()

    companion object : ComponentKey<BotInitProcessor>
}

internal fun BotInitProcessor.asObserver(targetState: State = State.LOADING): StateObserver {
    return BotInitProcessorAsStateObserverAdapter(this, targetState)
}

private class BotInitProcessorAsStateObserverAdapter(
    private val processor: BotInitProcessor,
    targetState: State
) : StateChangedObserver(targetState) {
    override fun stateChanged0(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        new.launch(CoroutineName("BotInitProcessor.init")) {
            try {
                processor.init()
            } catch (e: Throwable) {
                throw IllegalStateException("Exception in BotInitProcessor.init", e)
            }
        }
    }

    override fun toString(): String {
        return "BotInitProcessorAsStateObserverAdapter"
    }
}


internal class BotInitProcessorImpl(
    private val bot: QQAndroidBot,
    private val context: ComponentStorage,
    private val logger: MiraiLogger,
) : BotInitProcessor {

    private val initialized = atomic(false)

    override tailrec suspend fun init() {
        if (initialized.value) return
        if (!initialized.compareAndSet(expect = false, update = true)) return init()

        check(bot.isActive) { "bot is dead therefore network can't init." }
        context[ContactUpdater].closeAllContacts(CancellationException("re-init"))

        val registerResp = registerClientOnline()

        bot.launch(CoroutineName("Awaiting ConfigPushSvc.PushReq")) {
            context[ConfigPushSyncer].awaitSync()
        } // TODO: 2021/4/17 should we launch here?

        // do them parallel.
        supervisorScope {
//            launch { syncMessageSvc() }
            launch { context[OtherClientUpdater].update() }
            launch { context[ContactUpdater].loadAll(registerResp.origin) }
        }

        bot.firstLoginSucceed = true
    }

    private suspend fun registerClientOnline(): StatSvc.Register.Response {
        return StatSvc.Register.online(context[SsoProcessor].client).sendAndExpect(bot)
    }

    private suspend fun syncMessageSvc() {
        logger.info { "Syncing friend message history..." }
        withTimeoutOrNull(30000) {
            launch(CoroutineName("Syncing friend message history")) {
                nextEvent<MessageSvcPbGetMsg.GetMsgSuccess> {
                    it.bot == this@BotInitProcessorImpl.bot
                }
            }
            MessageSvcPbGetMsg(bot.client, MsgSvc.SyncFlag.START, null).sendAndExpect()
        } ?: error("timeout syncing friend message history.")
        logger.info { "Syncing friend message history: Success." }
    }

    private suspend inline fun <T : Packet> OutgoingPacket.sendAndExpect() = this.sendAndExpect<T>(bot.network)
    private suspend inline fun <T : Packet> OutgoingPacketWithRespType<T>.sendAndExpect() =
        this.sendAndExpect(bot.network)
}

