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

import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.EventPriority.MONITOR
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.BotNetworkHandler
import net.mamoe.mirai.internal.network.DefaultServerList
import net.mamoe.mirai.internal.network.closeAndJoin
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

internal abstract class AbstractBot<N : BotNetworkHandler> constructor(
    final override val configuration: BotConfiguration,
    final override val id: Long,
) : Bot, CoroutineScope {
    // FASTEST INIT

    val supervisor = SupervisorJob(configuration.parentCoroutineContext[Job])

    final override val logger: MiraiLogger by lazy { configuration.botLoggerSupplier(this) }

    final override val coroutineContext: CoroutineContext = // for id
        configuration.parentCoroutineContext
            .plus(supervisor)
            .plus(configuration.parentCoroutineContext[CoroutineExceptionHandler]
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

    // region network

    val network: N get() = _network

    @Suppress("PropertyName")
    internal lateinit var _network: N

    internal var _isConnecting: Boolean = false

    override val isOnline: Boolean get() = _network.areYouOk()
    final override val eventChannel: EventChannel<BotEvent> =
        GlobalEventChannel.filterIsInstance<BotEvent>().filter { it.bot === this@AbstractBot }

    val otherClientsLock = Mutex() // lock sync
    override val otherClients: ContactList<OtherClient> = ContactList()

    /**
     * Close server connection, resend login packet, BUT DOESN'T [BotNetworkHandler.init]
     */
    @ThisApiMustBeUsedInWithConnectionLockBlock
    @Throws(LoginFailedException::class) // only
    protected abstract suspend fun relogin(cause: Throwable?)

    @OptIn(ExperimentalTime::class)
    @Suppress("unused")
    private val offlineListener: Listener<BotOfflineEvent> =
        this@AbstractBot.eventChannel.parentJob(supervisor).subscribeAlways(
            priority = MONITOR,
            concurrency = ConcurrencyKind.LOCKED
        ) { event ->
            if (
                !event.bot.isActive // bot closed
                || !::_network.isInitialized // bot 还未登录就被 close
                || _isConnecting // bot 还在登入
            ) {
                // Close network to avoid endless reconnection while network is ok
                // https://github.com/mamoe/mirai/issues/894
                kotlin.runCatching { network.close(event.castOrNull<BotOfflineEvent.CauseAware>()?.cause) }
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
                    network.cancel(CancellationException("Bot offline manually $msg", cause))
                }
                is BotOfflineEvent.Force -> {
                    bot.logger.info { "Connection occupied by another android device: ${event.message}" }
                    if (event.reconnect) {
                        bot.logger.info { "Reconnecting..." }
                        // delay(3000)
                    } else {
                        network.cancel(ForceOfflineException("Connection occupied by another android device: ${event.message}"))
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
                if (!_network.isActive) {
                    // normally closed
                    return@subscribeAlways
                }
                bot.logger.info { "Connection lost, retrying login" }

                bot.asQQAndroidBot().client.run {
                    if (serverList.isEmpty()) {
                        serverList.addAll(DefaultServerList)
                    } else serverList.removeAt(0)
                }

                val success: Boolean
                val time = measureTime { success = Reconnect().reconnect(event) }

                if (success) {
                    logger.info { "Reconnected successfully in ${time.toHumanReadableString()}" }
                }
            }
        }

    private inner class Reconnect {
        suspend fun reconnect(event: BotOfflineEvent): Boolean {
            while (true) {
                retryCatchingExceptions<Unit>(
                    configuration.reconnectionRetryTimes,
                    except = LoginFailedException::class
                ) { tryCount, _ ->
                    if (tryCount != 0) {
                        delay(configuration.reconnectPeriodMillis)
                    }


                    // Close network to avoid endless reconnection while network is ok
                    // https://github.com/mamoe/mirai/issues/894
                    kotlin.runCatching { network.close(event.castOrNull<BotOfflineEvent.CauseAware>()?.cause) }

                    login()
                    _network.postInitActions()
//                    network.withConnectionLock {
//                        /**
//                         * [AbstractBot.relogin] only, no [BotNetworkHandler.init]
//                         */
//                        @OptIn(ThisApiMustBeUsedInWithConnectionLockBlock::class)
//                        relogin((event as? BotOfflineEvent.Dropped)?.cause)
//                    }
                    launch {
                        BotReloginEvent(bot, (event as? BotOfflineEvent.CauseAware)?.cause).broadcast()
                    }
                    return true
                }.getOrElse { exception ->
                    if (exception is LoginFailedException && !exception.killBot) {
                        logger.info { "Cannot reconnect." }
                        logger.warning(exception)
                        logger.info { "Retrying in 3s..." }
                        delay(3000)
                        return@getOrElse
                    }
                    logger.info { "Cannot reconnect due to fatal error." }
                    bot.cancel(CancellationException("Cannot reconnect due to fatal error.", exception))
                    return false
                }
            }
        }
    }

    /**
     * 仅用在 [login]
     */
    private inner class Login {

        private suspend fun doRelogin() {
            while (true) {
                _network = createNetworkHandler(coroutineContext)
                try {
                    _isConnecting = true
                    @OptIn(ThisApiMustBeUsedInWithConnectionLockBlock::class)
                    relogin(null)
                    return
                } catch (e: Exception) {
                    if (e is LoginFailedException) {
                        if (e.killBot) throw e
                    } else {
                        network.logger.error(e)
                    }
                    logger.warning { "Login failed. Retrying in 3s... (rootCause=${e.rootCause})" }
                    _network.closeAndJoin(e)
                    delay(3000)
                    continue
                } finally {
                    _isConnecting = false
                }
                // unreachable here
            }
        }

        private suspend fun doInit() {
            retryCatchingExceptions(5) { count, lastException ->
                if (count != 0) {
                    if (!isActive) {
                        logger.error("Cannot init due to fatal error")
                        throw lastException ?: error("<No lastException>")
                    }
                    logger.warning { "Init failed. Retrying in 3s... (rootCause=${lastException?.rootCause})" }
                    delay(3000)
                }

                _network.init()
            }.getOrElse {
                logger.error { "Cannot init. some features may be affected" }
                throw it // abort
            }
        }

        @ThisApiMustBeUsedInWithConnectionLockBlock
        private suspend fun reinitializeNetworkHandler(cause: Throwable?) {

            // logger.info("Initializing BotNetworkHandler")

            if (::_network.isInitialized) {
                _network.cancel(CancellationException("manual re-login", cause = cause))

                BotReloginEvent(this@AbstractBot, cause).broadcast()
                doRelogin()
                return
            }

            doRelogin()
            doInit()
        }

        suspend fun doLogin() {
            logger.info { "Logging in..." }
            if (::_network.isInitialized) {
                network.withConnectionLock {
                    @OptIn(ThisApiMustBeUsedInWithConnectionLockBlock::class)
                    reinitializeNetworkHandler(null)
                }
            } else {
                @OptIn(ThisApiMustBeUsedInWithConnectionLockBlock::class)
                reinitializeNetworkHandler(null)
            }
            logger.info { "Login successful" }
        }
    }


    /**
     * **Exposed public API**
     * [AbstractBot.relogin] && [BotNetworkHandler.init]
     */
    final override suspend fun login() {
        if (!isActive) error("Bot is already closed and cannot relogin. Please create a new Bot instance then do login.")
        Login().doLogin()
    }

    protected abstract fun createNetworkHandler(coroutineContext: CoroutineContext): N

    // endregion


    init {
        coroutineContext[Job]!!.invokeOnCompletion { throwable ->
            logger.info { "Bot cancelled" + throwable?.message?.let { ": $it" }.orEmpty() }

            kotlin.runCatching {
                network.close(throwable)
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


    override fun close(cause: Throwable?) {
        if (!this.isActive) {
            // already cancelled
            return
        }

        if (this.network.areYouOk()) {
            GlobalScope.launch {
                runCatching { BotOfflineEvent.Active(this@AbstractBot, cause).broadcast() }.exceptionOrNull()
                    ?.let { logger.error(it) }
            }
        }

        this.network.close(cause)

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

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
internal annotation class ThisApiMustBeUsedInWithConnectionLockBlock