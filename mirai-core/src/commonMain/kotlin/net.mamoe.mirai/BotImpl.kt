/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.ForceOfflineException
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.closeAndJoin
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext

/*
 * 泛型 N 不需要向外(接口)暴露.
 */
@UseExperimental(MiraiExperimentalAPI::class)
@MiraiInternalAPI
abstract class BotImpl<N : BotNetworkHandler> constructor(
    context: Context,
    account: BotAccount,
    val configuration: BotConfiguration
) : Bot(), CoroutineScope {
    private val botJob = SupervisorJob(configuration.parentCoroutineContext[Job])
    override val coroutineContext: CoroutineContext =
        configuration.parentCoroutineContext + botJob + (configuration.parentCoroutineContext[CoroutineExceptionHandler]
            ?: CoroutineExceptionHandler { _, e -> logger.error("An exception was thrown under a coroutine of Bot", e) })
    override val context: Context by context.unsafeWeakRef()

    @Suppress("CanBePrimaryConstructorProperty") // for logger
    final override val account: BotAccount = account
    @UseExperimental(RawAccountIdUse::class)
    override val uin: Long
        get() = account.id
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

        fun instanceWhose(qq: Long): Bot {
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

    /**
     * 可阻止事件广播
     */
    abstract fun onEvent(event: BotEvent): Boolean

    // region network

    final override val network: N get() = _network

    @Suppress("PropertyName")
    internal lateinit var _network: N

    @Suppress("unused")
    private val offlineListener: Listener<BotOfflineEvent> = this.subscribeAlways { event ->
        when (event) {
            is BotOfflineEvent.Dropped -> {
                bot.logger.info("Connection dropped or lost by server, retrying login")

                var lastFailedException: Throwable? = null
                repeat(configuration.reconnectionRetryTimes) {
                    try {
                        network.relogin()
                        logger.info("Reconnected successfully")
                        return@subscribeAlways
                    } catch (e: Throwable) {
                        lastFailedException = e
                        delay(configuration.reconnectPeriodMillis)
                    }
                }
                if (lastFailedException != null) {
                    throw lastFailedException!!
                }
            }
            is BotOfflineEvent.Active -> {
                val msg = if (event.cause == null) {
                    ""
                } else {
                    " with exception: " + event.cause.message
                }
                bot.logger.info("Bot is closed manually$msg")
                close(CancellationException(event.toString()))
            }
            is BotOfflineEvent.Force -> {
                bot.logger.info("Connection occupied by another android device: ${event.message}")
                close(ForceOfflineException(event.toString()))
            }
        }
    }

    final override suspend fun login() = reinitializeNetworkHandler(null)

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
            repeat(2) {
                try {
                    _network.init()
                    return
                } catch (e: Exception) {
                    e.logStacktrace()
                }
                logger.warning("Init failed. Retrying in 3s...")
                delay(3000)
            }
            logger.error("cannot init. some features may be affected")
        }

        logger.info("Initializing BotNetworkHandler")

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

    @UseExperimental(MiraiInternalAPI::class)
    override fun close(cause: Throwable?) {
        kotlin.runCatching {
            if (cause == null) {
                network.close()
                this.botJob.complete()
                offlineListener.complete()
            } else {
                network.close(cause)
                this.botJob.completeExceptionally(cause)
                offlineListener.completeExceptionally(cause)
            }
        }
        groups.delegate.clear()
        qqs.delegate.clear()
        instances.removeIf { it.get()?.uin == this.uin }
    }
}
