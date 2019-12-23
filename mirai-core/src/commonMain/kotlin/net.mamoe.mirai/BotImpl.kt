@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext

/*
 * 泛型 N 不需要向外(接口)暴露.
 */
@MiraiInternalAPI
abstract class BotImpl<N : BotNetworkHandler> constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : Bot(), CoroutineScope {
    private val supervisorJob = SupervisorJob(context[Job])
    override val coroutineContext: CoroutineContext =
        context + supervisorJob + CoroutineExceptionHandler { _, e -> e.logStacktrace("An exception was thrown under a coroutine of Bot") }

    @Suppress("CanBePrimaryConstructorProperty") // for logger
    override val account: BotAccount = account
    override val logger: MiraiLogger = logger ?: DefaultLogger("Bot(" + account.id + ")")

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
                if (it.qqAccount == qq) {
                    return it
                }
            }
            throw NoSuchElementException()
        }
    }

    override fun toString(): String = "Bot(${account.id})"

    // region network

    final override val network: N get() = _network

    private lateinit var _network: N

    override suspend fun login(configuration: BotConfiguration) =
        reinitializeNetworkHandler(configuration, null)

    // shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
    fun tryReinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable?
    ): Job = launch {
        repeat(configuration.reconnectionRetryTimes) {
            try {
                reinitializeNetworkHandler(configuration, cause)
                logger.info("Reconnected successfully")
                return@launch
            } catch (e: LoginFailedException) {
                delay(configuration.reconnectPeriodMillis)
            }
        }
    }

    private suspend fun reinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable?
    ) {
        logger.info("BotAccount: $qqAccount")
        logger.info("Initializing BotNetworkHandler")
        try {
            if (::_network.isInitialized) {
                _network.close(cause)
            }
        } catch (e: Exception) {
            logger.error("Cannot close network handler", e)
        }
        _network = createNetworkHandler(this.coroutineContext + configuration)

        _network.login()
    }

    protected abstract fun createNetworkHandler(coroutineContext: CoroutineContext): N

    // endregion

    @UseExperimental(MiraiInternalAPI::class)
    override fun close(throwable: Throwable?) {
        if (throwable == null) {
            network.close()
            this.supervisorJob.complete()
            groups.delegate.clear()
            qqs.delegate.clear()
        } else {
            network.close(throwable)
            this.supervisorJob.completeExceptionally(throwable)
            groups.delegate.clear()
            qqs.delegate.clear()
        }
    }
}
