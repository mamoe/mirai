/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.closeAndJoin
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

/*
 * 泛型 N 不需要向外(接口)暴露.
 */
@OptIn(MiraiExperimentalAPI::class)
@MiraiInternalAPI
abstract class BotImpl<N : BotNetworkHandler> constructor(
    context: Context,
    account: BotAccount,
    val configuration: BotConfiguration
) : Bot(), CoroutineScope {
    private val botJob = SupervisorJob(configuration.parentCoroutineContext[Job])
    final override val coroutineContext: CoroutineContext =
        configuration.parentCoroutineContext + botJob + (configuration.parentCoroutineContext[CoroutineExceptionHandler]
            ?: CoroutineExceptionHandler { _, e ->
                logger.error(
                    "An exception was thrown under a coroutine of Bot",
                    e
                )
            })
    override val context: Context by context.unsafeWeakRef()

    @OptIn(LowLevelAPI::class)
    @Suppress("CanBePrimaryConstructorProperty", "OverridingDeprecatedMember") // for logger
    final override val account: BotAccount = account

    @OptIn(RawAccountIdUse::class)
    override val uin: Long
        get() = this.account.id
    final override val logger: MiraiLogger by lazy { configuration.botLoggerSupplier(this) }

    init {
        instances.addLast(this.weakRef())
    }

    companion object {
        @PublishedApi
        internal val instances: LockFreeLinkedList<WeakRef<Bot>> = LockFreeLinkedList()

        inline fun forEachInstance(block: (Bot) -> Unit) = instances.forEach {
            it.get()?.let(block)
        }

        fun getInstance(qq: Long): Bot {
            instances.forEach {
                it.get()?.let { bot ->
                    if (bot.uin == qq) {
                        return bot
                    }
                }
            }
            throw NoSuchElementException()
        }
    }

    // region network

    final override val network: N get() = _network

    @Suppress("PropertyName")
    internal lateinit var _network: N

    @Suppress("unused")
    private val offlineListener: Listener<BotOfflineEvent> = this.subscribeAlways { event ->
        when (event) {
            is BotOfflineEvent.Dropped -> {
                if (!_network.isActive) {
                    return@subscribeAlways
                }
                bot.logger.info("Connection dropped by server or lost, retrying login")

                tryNTimesOrException(configuration.reconnectionRetryTimes) { tryCount ->
                    if (tryCount != 0) {
                        delay(configuration.reconnectPeriodMillis)
                    }
                    network.relogin(event.cause)
                    logger.info("Reconnected successfully")
                    BotReloginEvent(bot, event.cause).broadcast()
                    return@subscribeAlways
                }?.let {
                    logger.info("Cannot reconnect")
                    throw it
                }
            }
            is BotOfflineEvent.Active -> {
                val msg = if (event.cause == null) {
                    ""
                } else {
                    " with exception: " + event.cause.message
                }
                bot.logger.info { "Bot is closed manually$msg" }
                closeAndJoin(CancellationException(event.toString()))
            }
            is BotOfflineEvent.Force -> {
                bot.logger.info { "Connection occupied by another android device: ${event.message}" }
                closeAndJoin(ForceOfflineException(event.toString()))
            }
        }
    }

    final override suspend fun login() {
        logger.info("Logging in...")
        reinitializeNetworkHandler(null)
        logger.info("Login successful")
    }

    private suspend fun reinitializeNetworkHandler(
        cause: Throwable?
    ) {
        suspend fun doRelogin() {
            while (true) {
                _network = createNetworkHandler(this.coroutineContext)
                try {
                    _network.relogin()
                    return
                } catch (e: LoginFailedException) {
                    throw e
                } catch (e: Exception) {
                    network.logger.error(e)
                    _network.closeAndJoin(e)
                }
                logger.warning("Login failed. Retrying in 3s...")
                delay(3000)
            }
        }

        suspend fun doInit() {
            tryNTimesOrException(2) {
                if (it != 0) {
                    delay(3000)
                    logger.warning("Init failed. Retrying in 3s...")
                }
                _network.init()
            }?.let {
                network.logger.error(it)
                logger.error("cannot init. some features may be affected")
            }
        }

        // logger.info("Initializing BotNetworkHandler")

        if (::_network.isInitialized) {
            BotReloginEvent(this, cause).broadcast()
            doRelogin()
            return
        }

        doRelogin()
        doInit()
    }

    protected abstract fun createNetworkHandler(coroutineContext: CoroutineContext): N

    // endregion


    init {
        coroutineContext[Job]!!.invokeOnCompletion { throwable ->
            network.close(throwable)
            offlineListener.cancel(CancellationException("bot cancelled", throwable))

            groups.delegate.clear() // job is cancelled, so child jobs are to be cancelled
            friends.delegate.clear()
            instances.removeIf { it.get()?.uin == this.uin }
        }
    }

    @OptIn(MiraiInternalAPI::class)
    override fun close(cause: Throwable?) {
        if (!this.botJob.isActive) {
            // already cancelled
            return
        }
        if (cause == null) {
            this.botJob.cancel()
        } else {
            this.botJob.cancel(CancellationException("bot cancelled", cause))
        }
    }
}
