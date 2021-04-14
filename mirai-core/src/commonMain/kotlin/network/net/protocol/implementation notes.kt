/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ClassName")

package net.mamoe.mirai.internal.network.net.protocol

/*
 * 垃圾分类
 */

private object `handle packet1` {
//    if (packet != null && (bot.logger.isEnabled || logger.isEnabled)) {
//        when {
//            packet is ParseErrorPacket -> {
//                packet.direction.getLogger(bot).error(packet.error)
//            }
//            packet is Packet.NoLog -> {
//                // nothing to do
//            }
//            packet is MessageEvent -> packet.logMessageReceived()
//            packet is Event && packet !is Packet.NoEventLog -> bot.logger.verbose {
//                "Event: $packet".replaceMagicCodes()
//            }
//            else -> logger.verbose { "Recv: $packet".replaceMagicCodes() }
//        }
//    }
}

private object `handle packet2` {
//    if (packet is Event) {
//        if ((packet as? BroadcastControllable)?.shouldBroadcast != false) {
//            if (packet is BotEvent) {
//                withContext(bot.coroutineContext[CoroutineExceptionHandler] ?: CoroutineExceptionHandler { _, t ->
//                    bot.logger.warning(
//                        """
//                            Event processing: An exception occurred but no CoroutineExceptionHandler found in coroutineContext of bot
//                        """.trimIndent(), t
//                    )
//                }) {
//                    packet.broadcast()
//                }
//            } else {
//                packet.broadcast()
//            }
//        }
//
//        if (packet is CancellableEvent && packet.isCancelled) return
//    }
}

private object `stat heartbeat` {
//    private suspend fun doStatHeartbeat(): Throwable? {
//        return retryCatching(2) {
//            StatSvc.SimpleGet(bot.client)
//                .sendAndExpect<StatSvc.SimpleGet.Response>(
//                    timeoutMillis = bot.configuration.heartbeatTimeoutMillis,
//                    retry = 2
//                )
//            return null
//        }.exceptionOrNull()
//    }

}

private object syncMessageSvc {
//    private suspend fun syncMessageSvc() {
//        logger.info { "Syncing friend message history..." }
//        withTimeoutOrNull(30000) {
//            launch(CoroutineName("Syncing friend message history")) { nextEvent<MessageSvcPbGetMsg.GetMsgSuccess> { it.bot == this@QQAndroidBotNetworkHandler.bot } }
//            MessageSvcPbGetMsg(bot.client, MsgSvc.SyncFlag.START, null).sendAndExpect<Packet>()
//
//        } ?: error("timeout syncing friend message history.")
//        logger.info { "Syncing friend message history: Success." }
//    }
}

private object `config push syncer` {
//    @Suppress("FunctionName", "UNUSED_VARIABLE")
//    private fun BotNetworkHandler.ConfigPushSyncer(): suspend CoroutineScope.() -> Unit = launch@{
//        logger.info { "Awaiting ConfigPushSvc.PushReq." }
//        when (val resp: ConfigPushSvc.PushReq.PushReqResponse? = nextEventOrNull(20_000)) {
//            null -> {
//                val hasSession = bot.bdhSyncer.hasSession
//                kotlin.runCatching { bot.bdhSyncer.bdhSession.completeExceptionally(CancellationException("Timeout waiting for ConfigPushSvc.PushReq")) }
//                if (!hasSession) {
//                    logger.warning { "Missing ConfigPushSvc.PushReq. Switching server..." }
//                    bot.launch { BotOfflineEvent.RequireReconnect(bot).broadcast() }
//                } else {
//                    logger.warning { "Missing ConfigPushSvc.PushReq. Using the latest response. File uploading may be affected." }
//                }
//            }
//            is ConfigPushSvc.PushReq.PushReqResponse.ConfigPush -> {
//                logger.info { "ConfigPushSvc.PushReq: Config updated." }
//            }
//            is ConfigPushSvc.PushReq.PushReqResponse.ServerListPush -> {
//                logger.info { "ConfigPushSvc.PushReq: Server updated." }
//                // handled in ConfigPushSvc
//                return@launch
//            }
//        }
//    }
}

private object `network init` {
//    suspend fun init(): Unit = coroutineScope {
//        check(bot.isActive) { "bot is dead therefore network can't init." }
//        check(this@QQAndroidBotNetworkHandler.isActive) { "network is dead therefore can't init." }
//
//        contactUpdater.closeAllContacts(CancellationException("re-init"))
//
//        if (!pendingEnabled) {
//            pendingIncomingPackets = ConcurrentLinkedQueue()
//            _pendingEnabled.value = true
//        }
//
//        val registerResp = registerClientOnline()
//
//        this@QQAndroidBotNetworkHandler.launch(
//            CoroutineName("Awaiting ConfigPushSvc.PushReq"),
//            block = ConfigPushSyncer()
//        )
//
//        launch {
//            syncMessageSvc()
//        }
//
//        launch {
//            bot.otherClientsLock.withLock {
//                updateOtherClientsList()
//            }
//        }
//
//        contactUpdater.loadAll(registerResp.origin)
//
//        bot.firstLoginSucceed = true
//        postInitActions()
//    }

}

private object `update other client list` {
//
//    private suspend fun updateOtherClientsList() {
//        val list = Mirai.getOnlineOtherClientsList(bot)
//        bot.otherClients.delegate.clear()
//        bot.otherClients.delegate.addAll(list.map { bot.createOtherClient(it) })
//
//        if (bot.otherClients.isEmpty()) {
//            bot.logger.info { "No OtherClient online." }
//        } else {
//            bot.logger.info { "Online OtherClients: " + bot.otherClients.joinToString { "${it.deviceName}(${it.platform?.name ?: "unknown platform"})" } }
//        }
//    }
}

private object `skey refresh` {

//    suspend fun refreshKeys() {
//        WtLogin15(bot.client).sendAndExpect()
//    }

    /*
    val bot = (bot as QQAndroidBot)
            if (bot.firstLoginSucceed && bot.client.wLoginSigInfoInitialized) {
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.vKey.run {
                            //由过期时间最短的且不会被skey更换更新的vkey计算重新登录的时间
                            val delay = (expireTime - creationTime).seconds - 5.minutes
                            logger.info { "Scheduled refresh login session in ${delay.toHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            doFastLogin()
                            registerClientOnline()
                        }.onFailure {
                            logger.warning("Failed to refresh login session.", it)
                        }
                    }
                }
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.sKey.run {
                            val delay = (expireTime - creationTime).seconds - 5.minutes
                            logger.info { "Scheduled key refresh in ${delay.toHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            refreshKeys()
                        }.onFailure {
                            logger.error("Failed to refresh key.", it)
                        }
                    }
                }
            }
     */
}