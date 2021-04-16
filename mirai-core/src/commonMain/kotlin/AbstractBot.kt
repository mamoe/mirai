/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "DEPRECATION_ERROR",
    "OverridingDeprecatedMember",
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER"
)

package net.mamoe.mirai.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.EventPriority.MONITOR
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.network.DefaultServerList
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.ServerList
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

internal abstract class AbstractBot constructor(
    final override val configuration: BotConfiguration,
    final override val id: Long,
) : Bot, CoroutineScope {
    ///////////////////////////////////////////////////////////////////////////
    // lifecycle
    ///////////////////////////////////////////////////////////////////////////

    // FASTEST INIT
    private val supervisor = SupervisorJob(configuration.parentCoroutineContext[Job])

    final override val logger: MiraiLogger by lazy { configuration.botLoggerSupplier(this) }

    final override val coroutineContext: CoroutineContext = // for id
        configuration.parentCoroutineContext
            .plus(supervisor)
            .plus(
                configuration.parentCoroutineContext[CoroutineExceptionHandler]
                    ?: CoroutineExceptionHandler { _, e ->
                        logger.error("An exception was thrown under a coroutine of Bot", e)
                    }
            )
            .plus(CoroutineName("Mirai Bot"))

    init {
        @Suppress("LeakingThis")
        Bot._instances[this.id] = this
        supervisorJob.invokeOnCompletion {
            Bot._instances.remove(id)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // overrides
    ///////////////////////////////////////////////////////////////////////////

    final override val isOnline: Boolean get() = _network.state == NetworkHandler.State.OK
    final override val eventChannel: EventChannel<BotEvent> =
        GlobalEventChannel.filterIsInstance<BotEvent>().filter { it.bot === this@AbstractBot }

    override val otherClients: ContactList<OtherClient> = ContactList()

    ///////////////////////////////////////////////////////////////////////////
    // sync (// TODO: 2021/4/14 extract sync logic
    ///////////////////////////////////////////////////////////////////////////


    val otherClientsLock = Mutex() // lock sync

    // TODO: 2021/4/14 extract offlineListener

    @OptIn(ExperimentalTime::class)
    @Suppress("unused")
    private val offlineListener: Listener<BotOfflineEvent> =
        this@AbstractBot.eventChannel.parentJob(supervisor).subscribeAlways(
            priority = MONITOR,
            concurrency = ConcurrencyKind.LOCKED
        ) { event ->
            val bot = bot.asQQAndroidBot()
            if (
                !event.bot.isActive // bot closed
                || !::_network.isInitialized // bot 还未登录就被 close
            // || _isConnecting // bot 还在登入 // TODO: 2021/4/14 处理还在登入?
            ) {
                // Close network to avoid endless reconnection while network is ok
                // https://github.com/mamoe/mirai/issues/894
                kotlin.runCatching { network.close() }
                return@subscribeAlways
            }
            /*
            if (network.areYouOk() && event !is BotOfflineEvent.Force && event !is BotOfflineEvent.MsfOffline) {
                // network 运行正常
                return@subscribeAlways
            }*/
            when (event) {
                is BotOfflineEvent.Active -> {
                    val cause = event.cause
                    val msg = if (cause == null) "" else " with exception: $cause"
                    bot.logger.info("Bot is closed manually $msg", cause)
                    network.close()
                }
                is BotOfflineEvent.Force -> {
                    bot.logger.info { "Connection occupied by another android device: ${event.message}" }
                    bot.asQQAndroidBot().accountSecretsFile.delete()
                    bot.client = bot.initClient()
                    if (event.reconnect) {
                        bot.logger.info { "Reconnecting..." }
                        // delay(3000)
                    } else {
                        network.close()
                    }
                }
                is BotOfflineEvent.MsfOffline,
                is BotOfflineEvent.Dropped,
                is BotOfflineEvent.RequireReconnect,
                is BotOfflineEvent.PacketFactoryErrorCode
                -> {
                    // nothing to do
                }
            }

            if (event.reconnect) {
                if (_network.state != NetworkHandler.State.OK) {
                    // normally closed
                    return@subscribeAlways
                }

                val causeMessage = event.castOrNull<BotOfflineEvent.CauseAware>()?.cause?.toString() ?: event.toString()
                bot.logger.info { "Connection lost, retrying login ($causeMessage)" }

                bot.asQQAndroidBot().client.run {
                    if (serverList.isEmpty()) {
                        bot.bdhSyncer.loadServerListFromCache()
                        if (serverList.isEmpty()) {
                            serverList.addAll(DefaultServerList)
                        } else Unit
                    } else serverList.removeAt(0)
                }

                bot.launch {
                    val success: Boolean
                    val time = measureTime {
                        success = TODO("relogin")
                    }

                    if (success) {
                        logger.info { "Reconnected successfully in ${time.toHumanReadableString()}" }
                    }
                }
            }
        }

    ///////////////////////////////////////////////////////////////////////////
    // network
    ///////////////////////////////////////////////////////////////////////////

    internal val serverList: MutableList<Pair<String, Int>> = mutableListOf() // TODO: 2021/4/16 remove old
    internal val serverListNew = ServerList() // TODO: 2021/4/16 load server list from cache (add a provider)

    // TODO: 2021/4/14 handle serverList

    val network: NetworkHandler get() = _network

    @Suppress("PropertyName")
    internal lateinit var _network: NetworkHandler


    /**
     * **Exposed public API**
     * [AbstractBot.relogin] && [BotNetworkHandler.init]
     */
    final override suspend fun login() {
        if (!isActive) error("Bot is already closed and cannot relogin. Please create a new Bot instance then do login.")
        network
    }

    protected abstract fun createNetworkHandler(coroutineContext: CoroutineContext): NetworkHandler

    // endregion


    init {
        coroutineContext[Job]!!.invokeOnCompletion { throwable ->
            logger.info { "Bot cancelled" + throwable?.message?.let { ": $it" }.orEmpty() }

            kotlin.runCatching {
                network.close()
            }
            offlineListener.cancel(CancellationException("Bot cancelled", throwable))

            // help GC release instances
            groups.forEach {
                it.members.delegate.clear()
            }
            groups.delegate.clear() // job is cancelled, so child jobs are to be cancelled
            friends.delegate.clear()
        }
    }

    protected abstract suspend fun sendLogout()

    override fun close(cause: Throwable?) {
        if (!this.isActive) {
            // already cancelled
            return
        }

        this.network.close()

        if (supervisorJob.isActive) {
            if (cause == null) {
                supervisorJob.cancel()
            } else {
                supervisorJob.cancel(CancellationException("Bot closed", cause))
            }
        }
    }

    final override fun toString(): String = "Bot($id)"
}

private val Throwable.rootCause: Throwable
    get() {
        var depth = 0
        var rootCause: Throwable? = this
        while (rootCause?.cause != null) {
            rootCause = rootCause.cause
            if (depth++ == 20) break
        }
        return rootCause ?: this
    }