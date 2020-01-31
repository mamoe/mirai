@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext

/*
 * 泛型 N 不需要向外(接口)暴露.
 */
@UseExperimental(MiraiExperimentalAPI::class)
@MiraiInternalAPI
abstract class BotImpl<N : BotNetworkHandler> constructor(
    account: BotAccount,
    val configuration: BotConfiguration
) : Bot(), CoroutineScope {
    private val botJob = SupervisorJob(configuration.parentCoroutineContext[Job])
    override val coroutineContext: CoroutineContext =
        configuration.parentCoroutineContext + botJob + (configuration.parentCoroutineContext[CoroutineExceptionHandler]
            ?: CoroutineExceptionHandler { _, e -> e.logStacktrace("An exception was thrown under a coroutine of Bot") })

    @Suppress("CanBePrimaryConstructorProperty") // for logger
    final override val account: BotAccount = account
    @UseExperimental(RawAccountIdUse::class)
    override val uin: Long
        get() = account.id
    final override val logger: MiraiLogger by lazy { configuration.logger ?: DefaultLogger("Bot($uin)").also { configuration.logger = it } }

    init {
        @Suppress("LeakingThis")
        instances.addLast(this)
    }

    companion object {
        @PublishedApi
        internal val instances: LockFreeLinkedList<Bot> = LockFreeLinkedList()

        inline fun forEachInstance(block: (Bot) -> Unit) = instances.forEach(block)

        fun instanceWhose(qq: Long): Bot {
            instances.forEach {
                @Suppress("PropertyName")
                if (it.uin == qq) {
                    return it
                }
            }
            throw NoSuchElementException()
        }
    }

    final override fun toString(): String = "Bot(${uin})"

    // region network

    final override val network: N get() = _network

    @Suppress("PropertyName")
    internal lateinit var _network: N

    final override suspend fun login() = reinitializeNetworkHandler(null)

    // shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
    fun tryReinitializeNetworkHandler(
        cause: Throwable?
    ): Job = launch {
        var lastFailedException: Throwable? = null
        repeat(configuration.reconnectionRetryTimes) {
            try {
                reinitializeNetworkHandler(cause)
                logger.info("Reconnected successfully")
                return@launch
            } catch (e: Throwable) {
                lastFailedException = e
                delay(configuration.reconnectPeriodMillis)
            }
        }
        if (lastFailedException != null) {
            throw lastFailedException!!
        }
    }

    private suspend fun reinitializeNetworkHandler(
        cause: Throwable?
    ) {
        logger.info("BotAccount: $uin")
        logger.info("Initializing BotNetworkHandler")
        try {
            if (::_network.isInitialized) {
                BotOfflineEvent(this).broadcast()
                _network.close(cause)
            }
        } catch (e: Exception) {
            logger.error("Cannot close network handler", e)
        }

        loginLoop@ while (true) {
            _network = createNetworkHandler(this.coroutineContext)
            try {
                _network.login()
                break@loginLoop
            } catch (e: Exception) {
                e.logStacktrace()
                _network.close(e)
            }
            logger.warning("Login failed. Retrying in 3s...")
            delay(3000)
        }

        while (true) {
            try {
                return _network.init()
            } catch (e: Exception) {
                e.logStacktrace()
                _network.close(e)
            }
            logger.warning("Init failed. Retrying in 3s...")
            delay(3000)
        }
    }

    protected abstract fun createNetworkHandler(coroutineContext: CoroutineContext): N

    // endregion

    @UseExperimental(MiraiInternalAPI::class)
    override fun dispose(throwable: Throwable?) {
        if (throwable == null) {
            network.close()
            this.botJob.complete()
            groups.delegate.clear()
            qqs.delegate.clear()
        } else {
            network.close(throwable)
            this.botJob.completeExceptionally(throwable)
            groups.delegate.clear()
            qqs.delegate.clear()
        }
    }
}
